package br.com.fusiondms.feature.facedetection.presentation.utils

import android.content.Context
import android.os.Environment
import java.io.File

val rootGaleryFolder =
    File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "Fusion${File.separator}"
    ).apply {
        if (!exists())
            mkdirs()
    }

fun makeGaleryTempFile(): File = File(rootGaleryFolder, "face_cadastrada.jpg")

fun getSavedFacePhoto(context: Context): File {
    val rootApplicationFolder = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), File.separator + "FUSION")
    val fileName = "/face_cadastrada.jpg"
    return File(rootApplicationFolder, fileName)
}

fun deleteFacePhoto(context: Context) {
    val rootApplicationFolder = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), File.separator + "FUSION")
    val fileName = "/face_cadastrada.jpg"
    File(rootApplicationFolder, fileName).run {
        if (exists()) {
            delete()
        }
    }
}

fun makeApplicationTempFile(context: Context) :File {
    val rootApplicationFolder = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        File.separator + "FUSION"
    ).apply {
        if (!exists())
            mkdirs()
    }
    return File(rootApplicationFolder, "face_cadastrada.jpg")
}