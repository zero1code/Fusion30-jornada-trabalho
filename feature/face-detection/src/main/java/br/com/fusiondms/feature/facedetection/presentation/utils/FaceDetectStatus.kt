package br.com.fusiondms.feature.facedetection.presentation.utils

import android.graphics.Rect
import android.graphics.RectF
import com.google.mlkit.vision.face.Face

interface FaceDetectStatus {
    fun onFaceLocated(image: RectF?)
    fun onFaceNotLocated()
}