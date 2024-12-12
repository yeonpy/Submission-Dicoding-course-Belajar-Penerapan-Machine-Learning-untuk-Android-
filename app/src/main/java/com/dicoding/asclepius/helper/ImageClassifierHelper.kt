package com.dicoding.asclepius.helper

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import com.dicoding.asclepius.R
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier


@Suppress("DEPRECATION")
class ImageClassifierHelper(
    var threshold: Float = 0.1f,
    var maxResults: Int = 3,
    val modelName: String = "mobilenet_v1.tflite",
    val context: Context,
    val classifierListener: ClassifierListener?
) {
    //interface
    interface ClassifierListener {
        fun onErrorImage(error: String)
        fun onResultsImage(
            results: List<Classifications>?,
            inferenceTime: Long
        )
    }

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }

    //nullables types
    private var imageResultClassifier: ImageClassifier? = null
    //initialize
    init {
        setupImageResultStaticClassifier()
    }

    //setup image result classifier
    private fun setupImageResultStaticClassifier() {
        // TODO: ready for Image Classifier to process.
        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)
        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(4)
        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        //try catch
        try {
            imageResultClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                modelName,
                optionsBuilder.build()
            )
        } catch (e: IllegalStateException) {
            classifierListener?.onErrorImage(context.getString(R.string.image_failed_to_load))
            Log.e(TAG, e.message.toString())
        }
    }

    fun classifyResultImage(imageUri: Uri) {
        // TODO: classify the imageUri dari from static image.
        if (imageResultClassifier == null) {
            setupImageResultStaticClassifier()
        }

        try {
            //conversion static image to bitmap
            val bitmapImage = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)

            // Process the image (resize, cast to appropriate type)
            val imageResult = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(CastOp(DataType.UINT8))
                .build()

            //run inference
            val tensorImage = imageResult.process(TensorImage.fromBitmap(bitmapImage))
            var inferenceTime = SystemClock.uptimeMillis()
            val resultsImg = imageResultClassifier?.classify(tensorImage)
            inferenceTime = SystemClock.uptimeMillis() - inferenceTime

            // Return the results to the listener
            classifierListener?.onResultsImage(resultsImg, inferenceTime)

        } catch (e: Exception) {
            // Handle exceptions (e.g., URI conversion failure)
            classifierListener?.onErrorImage(e.message ?: "Unknown error")
            Log.e(TAG, "Error during classification: ${e.message}")
        }
    }

}