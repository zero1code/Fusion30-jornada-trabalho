package br.com.fusiondms.feature.facedetection.tflite

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Trace
import android.util.Pair
import br.com.fusiondms.feature.facedetection.tflite.SimilarityClassifier.*
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

class TFLiteObjectDetectionAPIModel(): SimilarityClassifier {

    private var isModelQuantized = true
    private var inputSize = 0
    private val labels = Vector<String>()
    private lateinit var intValues: IntArray
    private lateinit var outputLocations: Array<Array<FloatArray>>
    private lateinit var outputClasses: Array<FloatArray>
    private lateinit var outputScores: Array<FloatArray>
    private lateinit var numDetections: FloatArray
    private lateinit var embeedings: Array<FloatArray>
    private lateinit var imgData: ByteBuffer
    private lateinit var tfLite: Interpreter
    private val registered: HashMap<String, Recognition> = HashMap<String, Recognition>()

    override fun register(name: String, recognition: Recognition) {
        registered[name] = recognition
    }

    @Throws(IOException::class)
    private fun loadModelFile(assets: AssetManager, modelFilename: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelFilename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun create(
        assetManager: AssetManager,
        modelFilename: String,
        labelFilename: String,
        inputSize: Int,
        isQuantized: Boolean
    ): SimilarityClassifier {
        val d = TFLiteObjectDetectionAPIModel()
        val actualFilename =
            labelFilename.split("file:///android_asset/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
        val labelsInput = assetManager.open(actualFilename)
        val br = BufferedReader(InputStreamReader(labelsInput))
        while (br.readLine() != null) {
            val line = br.readLine()
            d.labels.add(line)
        }
        br.close()
        d.inputSize = inputSize
        try {
            d.tfLite = Interpreter(
                loadModelFile(
                    assetManager,
                    modelFilename
                )
            )
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        d.isModelQuantized = isQuantized
        // Pre-allocate buffers.
        val numBytesPerChannel: Int = if (isQuantized) {
            1 // Quantized
        } else {
            4 // Floating point
        }
        d.imgData =
            ByteBuffer.allocateDirect(1 * d.inputSize * d.inputSize * 3 * numBytesPerChannel)
        d.imgData.order(ByteOrder.nativeOrder())
        d.intValues = IntArray(d.inputSize * d.inputSize)
        d.outputLocations = Array(1) { Array(NUM_DETECTIONS) { FloatArray(4) } }
        d.outputClasses = Array(1) { FloatArray(NUM_DETECTIONS) }
        d.outputScores = Array(1) { FloatArray(NUM_DETECTIONS) }
        d.numDetections = FloatArray(1)
        return d
    }

    private fun findNearest(emb: FloatArray): Pair<String, Float>? {
        var ret: Pair<String, Float>? = null
        for ((name, recognition) in registered) {
            val knownEmb = (recognition.extra as Array<FloatArray>)[0]

            var distance = 0f
            for (i in emb.indices) {
                val diff = emb[i] - knownEmb[i]
                distance += diff * diff
            }
            distance = Math.sqrt(distance.toDouble()).toFloat()

            if (ret == null || distance < ret.second) {
                ret = Pair(name, distance)
            }
        }
        return ret
    }

    override fun recognizeImage(
        bitmap: Bitmap
    ): List<Recognition> {
        // Log this method so that it can be analyzed with systrace.

        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage")

        Trace.beginSection("preprocessBitmap")
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        imgData.rewind()
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val pixelValue = intValues[i * inputSize + j]
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((pixelValue shr 16 and 0xFF).toByte())
                    imgData.put((pixelValue shr 8 and 0xFF).toByte())
                    imgData.put((pixelValue and 0xFF).toByte())
                } else { // Float model
                    imgData.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                }
            }
        }
        Trace.endSection() // preprocessBitmap

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed")

        val inputArray = arrayOf<Any>(imgData)

        Trace.endSection()

        // Here outputMap is changed to fit the Face Mask detector

        // Here outputMap is changed to fit the Face Mask detector
        val outputMap: MutableMap<Int, Any> = HashMap()

        embeedings = Array(1) { FloatArray(OUTPUT_SIZE) }
        outputMap[0] = embeedings

        // Run the inference call.
        Trace.beginSection("run")
        //tfLite.runForMultipleInputsOutputs(inputArray, outputMapBack);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap)
        Trace.endSection()

        var distance = Float.MAX_VALUE
        val id = "0"
        var label: String? = "?"

        if (registered.size > 0) {
            val nearest = findNearest(embeedings[0])
            if (nearest != null) {
                val name = nearest.first
                label = name
                distance = nearest.second
            }
        }

        val numDetectionsOutput = 1
        val recognitions = ArrayList<Recognition>(numDetectionsOutput)
        val rec = Recognition(
            id,
            label!!,
            distance,
            RectF()
        )

        recognitions.add(rec)
        rec.setExtra(embeedings)

        Trace.endSection()
        return recognitions
    }

    companion object {
        const val OUTPUT_SIZE = 192
        const val IMAGE_MEAN = 128.0f
        const val IMAGE_STD = 128.0f
        const val NUM_DETECTIONS = 1
    }
}