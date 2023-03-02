package br.com.fusiondms.feature.facedetection.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.fusiondms.core.common.R
import br.com.fusiondms.core.common.adicionarNotch
import br.com.fusiondms.core.common.desativarTelaLigada
import br.com.fusiondms.core.common.manterTelaLigada
import br.com.fusiondms.core.common.permissiondiaolog.PermissionRequestDialog
import br.com.fusiondms.core.common.removerNotch
import br.com.fusiondms.feature.facedetection.ModoDeteccao
import br.com.fusiondms.feature.facedetection.camerax.CameraManager
import br.com.fusiondms.feature.facedetection.databinding.ActivityFaceDetectorBinding
import br.com.fusiondms.feature.facedetection.presentation.utils.*
import br.com.fusiondms.feature.facedetection.tflite.Recognition
import br.com.fusiondms.feature.facedetection.tflite.SimilarityClassifier
import br.com.fusiondms.feature.facedetection.tflite.TFLiteObjectDetectionAPIModel
import java.io.IOException
import java.lang.Integer.min
import java.util.*
import kotlin.properties.Delegates

class FaceDetectorActivity : AppCompatActivity(), FaceDetectStatus {
    private var _binding: ActivityFaceDetectorBinding? = null
    private val binding get() = _binding!!
    private var tipoServico by Delegates.notNull<Int>()
    private var permissionRequestDialog: PermissionRequestDialog? = null
    private val resultIntent = Intent()
    private var continuarComparando = true

    private lateinit var cameraManager: CameraManager
    private lateinit var face: RectF
    private var framePicture: Bitmap? = null
    private lateinit var detector: SimilarityClassifier

    private val cameraPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permission ->
        when {
            permission.getOrDefault(Manifest.permission.CAMERA, false) -> {
                //Camera aceita
            }
            permission.getOrDefault(Manifest.permission.WRITE_EXTERNAL_STORAGE, false) -> {

            }
            permission.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false) -> {

            }
            else -> {
                permissionRequestDialog()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFaceDetectorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tipoServico = intent.extras?.getInt(FACE_DETECTOR_TIPO) ?: 0
        removerNotch()
        manterTelaLigada()

        bindCameraManager()
        bindListeners()
        tipoDeteccao()
    }


    private fun tipoDeteccao() {
        when(tipoServico) {
            ModoDeteccao.COMPARAR_FACE.value -> {
                bindDetector()
                getFotoRostoSalva()
            }
            ModoDeteccao.CADASTRAR_FACE.value -> {
                deleteFacePhoto(this)
            }
            ModoDeteccao.RECADASTRAR_FACE.value -> {

            }
        }
    }

    private fun bindListeners() {
        binding.btnTirarFoto.setOnClickListener {
            tirarFoto()
        }
    }

    private fun bindCameraManager() {
        cameraManager = CameraManager(
            this,
            binding.previewViewFinder,
            this,
            binding.graphicOverlayFinder,
            this
        )
    }

    private fun tirarFoto() {
        cameraManager.imageCapture.takePicture(
            cameraManager.cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
                @SuppressLint("UnsafeExperimentalUsageError", "RestrictedApi")
                override fun onCaptureSuccess(image: ImageProxy) {
                    image.image?.let {
                        imageToBitMapESalvarFoto(it)
                    }
                    super.onCaptureSuccess(image)
                }
            }
        )
    }

    private fun ajustarImagem(bitmap: Bitmap?): Bitmap? {
        return bitmap?.rotateFlipImage(
            cameraManager.rotation,
            cameraManager.isFrontMode()
        )?.scaleImage(
            binding.previewViewFinder,
            cameraManager.isHorizontalMode()
        )
    }

    private fun imageToBitMapESalvarFoto(image: Image) {
        var imagemSalva = false
        ajustarImagem(image.imageToBitmap())?.let { bitmap ->
            cortarBitmap(bitmap).let {
                binding.graphicOverlayFinder.processCanvas.drawBitmap(
                    it,
                    0f,
                    it.getBaseYByView(
                        binding.graphicOverlayFinder,
                        cameraManager.isHorizontalMode()
                    ),
                    Paint().apply {
                        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
                    }
                )
                imagemSalva = binding.graphicOverlayFinder.processBitmap.saveToGallery(this)
            }
        }

        if (imagemSalva) {
            resultIntent.putExtra(FACE_DETECTOR_RESULT, true)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun cortarBitmap(bitmap: Bitmap): Bitmap {
        val imageWidth = bitmap.width
        val imageHeight = bitmap.height
        val imageSize = min(imageWidth, imageHeight)

        val x = (imageWidth - imageSize) / 2
        val y = (imageHeight - imageSize) / 2

        return Bitmap.createBitmap(bitmap, x, y, imageSize, imageSize)
    }

    private fun bindDetector() {
        try {
            detector = TFLiteObjectDetectionAPIModel().create(
                this.assets,
                TF_OD_API_MODEL_FILE,
                TF_OD_API_LABELS_FILE,
                TF_OD_API_INPUT_SIZE,
                TF_OD_API_IS_QUANTIZED
            )
        }catch (ioe: IOException) {
            ioe.printStackTrace()
        }
    }

    private fun getFotoRostoSalva() {
        val savedFaceFile = getSavedFacePhoto(this)
        val savedFaceBitmap = BitmapFactory.decodeFile(savedFaceFile.absolutePath)
        val scaledBitmap = Bitmap.createScaledBitmap(
            savedFaceBitmap,
            TF_OD_API_INPUT_SIZE,
            TF_OD_API_INPUT_SIZE,
            false
        )

        val rectF = RectF(0F, 0F, savedFaceBitmap.width.toFloat(), scaledBitmap.height.toFloat())
        val result = Recognition(
            "0",
            "Colaborador",
            -1f,
            rectF
        )
        val resultAux = detector.recognizeImage(scaledBitmap)
        if (resultAux.isNotEmpty()) {
            binding.imageSaved.setImageBitmap(scaledBitmap)
            val savedFace = resultAux[0]
            result.extra = savedFace.extra
            detector.register("Colaborador", result)
        }
    }

    private fun initFaceComparacao() {
        getFramePicture()
        ajustarImagem(framePicture)?.let {
                cortarBitmap(it).let { result ->
                    Bitmap.createScaledBitmap(
                        result,
                        TF_OD_API_INPUT_SIZE,
                        TF_OD_API_INPUT_SIZE,
                        false
                    )
                }
            }?.let { bitmap ->
                binding.imageCaptured.setImageBitmap(bitmap)

                val resultAux = detector.recognizeImage(bitmap)
                if (resultAux.isNotEmpty()) {
                    val result = resultAux[0]

                    val confianca = result.distance
                    if (confianca < 1.0f && continuarComparando) {
                        if (result.id == "0") {
                            facesIguais()
                        } else {
                            binding.facePosition.setStrokeColor(R.color.brand_red)
                        }
                    }
                }
            }
    }

    private fun getFramePicture(){
        cameraManager.imageCapture.takePicture(
            cameraManager.cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
                @SuppressLint("UnsafeExperimentalUsageError", "RestrictedApi")
                override fun onCaptureSuccess(image: ImageProxy) {
                    image.image?.let {
                        framePicture = it.imageToBitmap()
                    }
                    super.onCaptureSuccess(image)
                }
            }
        )
    }

    private fun facesIguais() {
        continuarComparando = false
        binding.facePosition.setStrokeColor(R.color.brand_green_success)
        resultIntent.putExtra(FACE_DETECTOR_RESULT, true)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun solicitarPermissao() {
        cameraPermissionResult.launch(REQUIRED_PERMISSIONS)
    }

    private fun actionConfiguracoes() {
        val uri = Uri.fromParts("package", packageName, null)
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            ).apply {
                data = uri
            })
    }

    private fun permissionRequestDialog() {
        if (permissionRequestDialog == null) {
            permissionRequestDialog = PermissionRequestDialog(
                R.drawable.ic_camera,
                getString(R.string.label_permissao_camera),
                getString(R.string.label_permissao_camera_mensagem),
                getString(R.string.label_aceitar_permissao),
                getString(R.string.label_ir_para_configuracoes),
                acaoAceitarPermissao = { solicitarPermissao() },
                acaoConfiguracoes = { actionConfiguracoes() }
            )
            permissionRequestDialog?.show(
                supportFragmentManager,
                PermissionRequestDialog.TAG
            )
        }
    }

    private fun verificarPermissao() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                permissionRequestDialog?.dismiss()
                cameraManager.startCamera()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                //Permissao negada mais de uma vez ir para as configurações
                permissionRequestDialog()
            }
            else -> {
                permissionRequestDialog()
            }
        }
    }

    override fun onFaceLocated(image: RectF?) {
        image?.let { face = it }
        binding.facePosition.setStrokeColor(R.color.brand_celtic_blue)
        if (tipoServico == 1) {
            binding.btnTirarFoto.visibility = View.VISIBLE
        } else {
            initFaceComparacao()
        }
    }

    override fun onFaceNotLocated() {
        binding.facePosition.setStrokeColor(R.color.white)
        binding.btnTirarFoto.visibility = View.INVISIBLE
    }

    override fun onResume() {
        super.onResume()
        verificarPermissao()
    }

    override fun onDestroy() {
        super.onDestroy()
        adicionarNotch()
        desativarTelaLigada()
        _binding = null
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        const val TF_OD_API_MODEL_FILE = "mobile_face_net.tflite"
        const val TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt"
        const val TF_OD_API_INPUT_SIZE = 112
        const val TF_OD_API_IS_QUANTIZED = false
        const val FACE_DETECTOR_TIPO = "tipoServico"
        const val FACE_DETECTOR_RESULT = "faceDetectorResult"
    }
}