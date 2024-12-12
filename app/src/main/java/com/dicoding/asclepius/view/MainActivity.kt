package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.yalantis.ucrop.UCrop
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File

class MainActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var modelMain: ViewModelMain
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private var imageNotCrop: Uri? = null

    // Register ActivityResultLauncher for picking an image from gallery
    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        //check if the uri is null
        if (uri == null) {
            "No Media Selected".showToast()
        }

        //set the uri to the current image uri
        uri?.let {
            //set the uri to the current image uri with live data
            modelMain.setPreviewImage(it)
            startCrop(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initialize binding, view model, and image classifier helper
        modelMain = ViewModelProvider(this)[ViewModelMain::class.java]
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialize image classifier helper
        imageClassifierHelper = ImageClassifierHelper(
            threshold = 0.5f,
            maxResults = 1,
            modelName = "cancer_classification.tflite",
            context = this,
            classifierListener = this
        )

        //button start gallery and analyze
        binding.galleryButton.setOnClickListener {
            startGallery()
        }
        binding.analyzeButton.setOnClickListener {
            analyzeImage()
        }

        //observe the preview image live data
        modelMain.previewImage.observe(this) { uri ->
            uri?.let {
                showImage(it)
            }
        }
    }

    private fun startGallery() {
        // TODO: get image from Gallery use PhotoPicker.
        launcherGallery.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun showImage(uri: Uri) {
        // TODO: show image from Gallery.
        binding.previewImageView.setImageURI(uri)
    }

    private fun analyzeImage() {
        // TODO: analyze image yang if exist.
        //indicator visible
        binding.progressIndicator.visibility = android.view.View.VISIBLE

        val imageUri = modelMain.previewImage.value
        if (imageUri == null) {
            "No Image Selected".showToast()

            //indicator gone
            binding.progressIndicator.visibility = android.view.View.GONE
        } else {
            //call helper
            imageClassifierHelper.classifyResultImage(imageUri)
        }

    }

    private fun moveToResult(label: String, score: Float, inferenceTime: Long) {
        // Check if there's a preview image
        val hasPreviewImage = modelMain.previewImage.value != null

        Log.d("MainActivity", "analyzeImage called with imageUri: $hasPreviewImage")

        // Create the intent for ResultActivity
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_LABEL, label)
            putExtra(ResultActivity.EXTRA_SCORE, score)
            putExtra(ResultActivity.EXTRA_INFERENCE_TIME, inferenceTime)

            // Add the image Uri to the extras if it's not null
            modelMain.previewImage.value?.let {
                putExtra(ResultActivity.EXTRA_IMAGE_URI, it.toString())
            }
        }

        // Only proceed to ResultActivity if preview image is present
        if (hasPreviewImage) {
            startActivity(intent)
        }
    }


    private fun String.showToast() {
        Toast.makeText(this@MainActivity, this, Toast.LENGTH_SHORT).show()
    }

    //onErrorImage & onResultsImage for ImageClassifierHelper
    override fun onErrorImage(error: String) {
        runOnUiThread {
            //indicator gone
            binding.progressIndicator.visibility = android.view.View.GONE
            // Show an error message or handle the error as needed
            "Error: $error".showToast()
        }
    }

    override fun onResultsImage(
        results: List<Classifications>?,
        inferenceTime: Long
    ) {
        runOnUiThread {
            //indicator gone
            binding.progressIndicator.visibility = android.view.View.GONE
            // Display the classification results or perform any other actions
            results?.let { classifications ->
                if (classifications.isNotEmpty() && classifications[0].categories.isNotEmpty()) {
                    val analyze = classifications[0].categories[0]
                    moveToResult(analyze.label, analyze.score, inferenceTime)
                } else {
                    "No Match Results. Not Classify Image.".showToast()
                }
            } ?: run {
                "No Results".showToast()
            }
        }
    }


    //crop image
    private fun startCrop(uri: Uri) {
        //set image not crop
        imageNotCrop = uri

        // TODO: crop image using UCrop.
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))

        UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(800, 800) // Set max size for the cropped image
            .start(this) // start UCrop
    }

    //handle result crop
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            // use image crop
            resultUri?.let {
                //set the uri to the current image uri with live data
                modelMain.setPreviewImage(it)
                // image preview
                showImage(it)
            }
        } else if (resultCode == RESULT_CANCELED) {
            //use image gallery
            modelMain.setPreviewImage(imageNotCrop)
            // image preview
            showImage(imageNotCrop!!)
            //toast cancel
            "Cancel".showToast()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            // handle crop error
            cropError?.printStackTrace()
            "Crop Error".showToast()
        }
    }


}