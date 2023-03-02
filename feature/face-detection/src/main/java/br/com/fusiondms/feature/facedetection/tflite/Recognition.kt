package br.com.fusiondms.feature.facedetection.tflite

import android.graphics.Bitmap
import android.graphics.RectF

class Recognition(
    val id: String,
    val title: String,
    val distance: Float,
    var location: RectF,
    var color: Int?,
    var extra: Any?,
    var crop: Bitmap?
) {
    constructor(
        id: String,
        title: String,
        distance: Float,
        location: RectF,
    ) : this(id, title, distance, location, null, null, null) {}

    fun setExtra(extra: Any?): Any? {
        val newExtra = extra
        this.extra = newExtra
        return newExtra
    }
}