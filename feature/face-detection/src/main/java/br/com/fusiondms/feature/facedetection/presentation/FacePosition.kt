package br.com.fusiondms.feature.facedetection.presentation

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.content.res.TypedArray
import android.util.Log
import androidx.core.content.ContextCompat
import br.com.fusiondms.feature.facedetection.R

class FacePosition(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var selectedColor = ContextCompat.getColor(context, br.com.fusiondms.core.common.R.color.white)
    private val rect = RectF()

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FacePosition)
        selectedColor = typedArray.getColor(R.styleable.FacePosition_strokeColor, ContextCompat.getColor(context, br.com.fusiondms.core.common.R.color.white))
        typedArray.recycle()
    }

    private val paint = Paint().apply {
        color = selectedColor
        style = Paint.Style.STROKE
        strokeWidth = 15f
        pathEffect = DashPathEffect(floatArrayOf(50f, 50f), 0f)
        strokeCap = Paint.Cap.ROUND
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = selectedColor
        val screenWidth = context.resources.displayMetrics.widthPixels
        val screenHeight = context.resources.displayMetrics.heightPixels

        rect.set(0f, 0f, screenWidth / 1.5f, screenHeight / 2.5f)

        // calcular as coordenadas x e y da forma
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f
        val left = centerX - rect.width() / 2f
        val top = centerY - rect.height() / 2f
        val right = centerX + rect.width() / 2f
        val bottom = centerY + rect.height() / 2f

        rect.set(left, top, right, bottom)
        canvas?.drawOval(rect, paint)
    }

    fun setStrokeColor(color: Int) {
        selectedColor = ContextCompat.getColor(context, color)
        invalidate()
    }
}