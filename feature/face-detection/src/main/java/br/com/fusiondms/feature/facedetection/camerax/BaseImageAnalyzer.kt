package br.com.fusiondms.feature.facedetection.camerax

import android.annotation.SuppressLint
import android.graphics.*
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import java.io.IOException

abstract class BaseImageAnalyzer<T> : ImageAnalysis.Analyzer {

    abstract val graphicOverlay: GraphicOverlay

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        mediaImage?.let {
            try {
                detectInImage(InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees))
                    .addOnSuccessListener { results ->
                        onSuccess(
                            results,
                            graphicOverlay,
                            it.cropRect
                        )
                    }
                    .addOnFailureListener {
                        graphicOverlay.clear()
                        graphicOverlay.postInvalidate()
                        onFailure(it)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }catch (e: IOException) {
                Log.e("ImageDebug", "Error creating InputImage: IOException", e)
            } catch (e: IllegalArgumentException) {
                Log.e("ImageDebug", "Error creating InputImage: IllegalArgumentException", e)
            } catch (e: SecurityException) {
                Log.e("ImageDebug", "Error creating InputImage: SecurityException", e)
            }
        }
    }

    abstract fun stop()

    protected abstract fun detectInImage(image: InputImage): Task<T>

    protected abstract fun onSuccess(
        results: T,
        graphicOverlay: GraphicOverlay,
        rect: Rect
    )

    protected abstract fun onFailure(e: Exception)

}