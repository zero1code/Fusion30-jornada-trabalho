package br.com.fusiondms.feature.facedetection.mlkit.face

import android.graphics.*
import androidx.annotation.ColorInt
import br.com.fusiondms.feature.facedetection.camerax.GraphicOverlay
import br.com.fusiondms.feature.facedetection.presentation.utils.FaceDetectStatus
import com.google.mlkit.vision.face.Face

class FaceContourGraphic(
    overlay: GraphicOverlay,
    private val face: Face,
    private val imageRect: Rect,
) : GraphicOverlay.Graphic(overlay) {

    var faceDetectStatus: FaceDetectStatus? = null

    private val facePositionPaint: Paint
    private val idPaint: Paint
    private val boxPaint: Paint

    init {
        val selectedColor = Color.WHITE

        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor

        idPaint = Paint()
        idPaint.color = selectedColor
        idPaint.textSize = ID_TEXT_SIZE

        boxPaint = Paint()
        boxPaint.color = selectedColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH
    }

    private fun Canvas.drawFace(facePosition: Int, @ColorInt selectedColor: Int) {
        val contour = face.getContour(facePosition)
        val path = Path()
        contour?.points?.forEachIndexed { index, pointF ->
            if (index == 0) {
                path.moveTo(
                    translateX(pointF.x),
                    translateY(pointF.y)
                )
            }
            path.lineTo(
                translateX(pointF.x),
                translateY(pointF.y)
            )
        }
        val paint = Paint().apply {
            color = selectedColor
            style = Paint.Style.STROKE
            strokeWidth = BOX_STROKE_WIDTH
        }
        drawPath(path, paint)
    }

    override fun draw(canvas: Canvas?) {

        val centerScreenWidth = getDisplayMetrics("width") / 2f
        val centerWidthLimit = (centerScreenWidth / 5)

        val centerScreenHeight = getDisplayMetrics("height") / 2f
        val centerHeightLimit = (centerScreenHeight / 5)

        val rect = calculateRect(
            imageRect.height().toFloat(),
            imageRect.width().toFloat(),
            face.boundingBox
        )

        val rectCenterX = rect.centerX()
        val rectCenterY = rect.centerY()

        val isRectCenteredHorizontal = when (rectCenterX) {
            in (centerScreenWidth - centerWidthLimit)..(centerScreenWidth + centerWidthLimit) -> true
            else -> false
        }
        val isRectCenteredVertical = when (rectCenterY) {
            in (centerScreenHeight - centerHeightLimit)..(centerScreenHeight + centerHeightLimit) -> true
            else -> false
        }
        val isRectCentered = isRectCenteredHorizontal && isRectCenteredVertical

        if (isRectCentered) {
            faceDetectStatus?.onFaceLocated(rect)
//            boxPaint.color = Color.RED
//            canvas?.drawRect(rect, boxPaint)
        } else {
            faceDetectStatus?.onFaceNotLocated()
        }

//        canvas?.drawRect(rect, boxPaint)



//        val contours = face.allContours
//
//        contours.forEach {
//            it.points.forEach { point ->
//                val px = translateX(point.x)
//                val py = translateY(point.y)
//                canvas?.drawCircle(px, py, FACE_POSITION_RADIUS, facePositionPaint)
//            }
//        }

        // face
//        canvas?.drawFace(FaceContour.FACE, Color.BLUE)
//
//        // left eye
//        canvas?.drawFace(FaceContour.LEFT_EYEBROW_TOP, Color.RED)
//        canvas?.drawFace(FaceContour.LEFT_EYE, Color.BLACK)
//        canvas?.drawFace(FaceContour.LEFT_EYEBROW_BOTTOM, Color.CYAN)
//
//        // right eye
//        canvas?.drawFace(FaceContour.RIGHT_EYE, Color.DKGRAY)
//        canvas?.drawFace(FaceContour.RIGHT_EYEBROW_BOTTOM, Color.GRAY)
//        canvas?.drawFace(FaceContour.RIGHT_EYEBROW_TOP, Color.GREEN)
//
//        // nose
//        canvas?.drawFace(FaceContour.NOSE_BOTTOM, Color.LTGRAY)
//        canvas?.drawFace(FaceContour.NOSE_BRIDGE, Color.MAGENTA)
//
//        // rip
//        canvas?.drawFace(FaceContour.LOWER_LIP_BOTTOM, Color.WHITE)
//        canvas?.drawFace(FaceContour.LOWER_LIP_TOP, Color.YELLOW)
//        canvas?.drawFace(FaceContour.UPPER_LIP_BOTTOM, Color.GREEN)
//        canvas?.drawFace(FaceContour.UPPER_LIP_TOP, Color.CYAN)
    }

    companion object {
        private const val FACE_POSITION_RADIUS = 4.0f
        private const val ID_TEXT_SIZE = 30.0f
        private const val BOX_STROKE_WIDTH = 5.0f
    }

}