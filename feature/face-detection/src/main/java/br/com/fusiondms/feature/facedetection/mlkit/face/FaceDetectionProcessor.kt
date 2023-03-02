package br.com.fusiondms.feature.facedetection.mlkit.face

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.media.Image
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import br.com.fusiondms.feature.facedetection.camerax.BaseImageAnalyzer
import br.com.fusiondms.feature.facedetection.camerax.GraphicOverlay
import br.com.fusiondms.feature.facedetection.presentation.utils.FaceDetectStatus
import java.io.IOException

class FaceDetectionProcessor(private val view: GraphicOverlay) :
    BaseImageAnalyzer<List<Face>>(), FaceDetectStatus {
    var faceDetectStatus: FaceDetectStatus? = null

    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(realTimeOpts)

    override val graphicOverlay: GraphicOverlay
        get() = view

    override fun detectInImage(image: InputImage): Task<List<Face>> {
        return detector.process(image)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }

    override fun onSuccess(
        results: List<Face>,
        graphicOverlay: GraphicOverlay,
        rect: Rect
    ) {
        graphicOverlay.clear()
        results.forEach {

            //Rosto identificado
            val faceGraphic = FaceContourGraphic(graphicOverlay, it, rect)
            faceGraphic.faceDetectStatus = this
            graphicOverlay.add(faceGraphic)
        }
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Face Detector failed.$e")
    }

    companion object {
        private const val TAG = "FaceDetectorProcessor"
    }

    override fun onFaceLocated(image: RectF?) {
        faceDetectStatus?.onFaceLocated(image)
    }

    override fun onFaceNotLocated() {
        faceDetectStatus?.onFaceNotLocated()
    }

}