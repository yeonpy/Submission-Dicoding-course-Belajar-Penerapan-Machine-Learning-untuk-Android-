package com.dicoding.asclepius.view

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    companion object {
        const val EXTRA_LABEL = "EXTRA_LABEL"
        const val EXTRA_SCORE = "EXTRA_SCORE"
        const val EXTRA_INFERENCE_TIME = "EXTRA_INFERENCE_TIME"
        const val EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initalize binding
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: display image result, prediction, dan confidence score.
        // Get data from the intent using the keys from the companion object
        val name = intent.getStringExtra(EXTRA_LABEL)
        val score = intent.getFloatExtra(EXTRA_SCORE, 0.0f)
        val inferenceTime = intent.getLongExtra(EXTRA_INFERENCE_TIME, 0L)
        val imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI)
        val imageUri = imageUriString?.let { Uri.parse(it) }

        // Display the image, prediction name, score, and inference time
        displayResult(imageUri, name, score, inferenceTime)
    }

    // Function to set the data to the UI elements
    @SuppressLint("SetTextI18n")
    private fun displayResult(imageUri: Uri?, name: String?, score: Float, inferenceTime: Long) {
        imageUri?.let { binding.resultImage.setImageURI(it) }
        binding.resultText.text = name ?: "Unknown"
        binding.resultScoreTextView.text = "Confidence: ${(score * 100).toInt()}%"
        binding.inferenceTimeTextView.text = "Inference time: ${inferenceTime}ms"
    }


}