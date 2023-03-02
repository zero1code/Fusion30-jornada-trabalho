package br.com.fusiondms.feature.login.presentation

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import br.com.fusiondms.core.common.*
import br.com.fusiondms.core.common.bottomdialog.Dialog
import br.com.fusiondms.core.common.snackbar.TipoMensagem
import br.com.fusiondms.core.common.snackbar.showMessage
import br.com.fusiondms.feature.facedetection.ModoDeteccao
import br.com.fusiondms.feature.facedetection.presentation.FaceDetectorActivity
import br.com.fusiondms.feature.facedetection.presentation.utils.getSavedFacePhoto
import br.com.fusiondms.feature.login.R
import br.com.fusiondms.feature.login.databinding.FragmentPrimeiroAcessoBinding
import br.com.fusiondms.feature.login.presentation.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

@AndroidEntryPoint
class PrimeiroAcessoFragment : Fragment() {

    private var _binding: FragmentPrimeiroAcessoBinding? = null
    private val binding get() = _binding!!

    private var radioButtonSelected = false

    private val loginViewModel: LoginViewModel by viewModels()
    private val faceDetectorLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val faceEquals = data?.extras?.getBoolean(FaceDetectorActivity.FACE_DETECTOR_RESULT) ?: false
            if (faceEquals) {
                loginViewModel.showNext(binding.vfFormulario.displayedChild + 1)
                bindConfirmacaoDados()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrimeiroAcessoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindObservers()
        bindListeners()
    }

    private fun bindObservers() {
        lifecycleScope.launchWhenStarted {
            loginViewModel.viewFlipperPosition.collect { position ->
                if (position != 0) {

                    binding.linearProgressIndicator.progressAnimation(100 / binding.vfFormulario.childCount)
                    binding.vfFormulario.displayedChild = position
                }
            }
        }
    }

    private fun bindListeners() {
        binding.apply {
            btnNext.setOnClickListener {
                if (checarCampo(vfFormulario.displayedChild)) {
                    loginViewModel.showNext(vfFormulario.displayedChild + 1)
                    when(vfFormulario.displayedChild) {
                        2 -> requireContext().hideKeyboard(it)
                        3 -> btnNext.hideFabAnimation(0F)
                    }
                }
            }

            rgFuncao.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId != -1 && !radioButtonSelected) {
                    radioButtonSelected = true
                }
            }

            btnTirarFoto.setOnClickListener {
                val intent = Intent(requireActivity(), FaceDetectorActivity::class.java).apply {
                    putExtra(FaceDetectorActivity.FACE_DETECTOR_TIPO, ModoDeteccao.CADASTRAR_FACE.value)
                }
                faceDetectorLauncher.launch(intent)
            }

            btnCadastrar.setOnClickListener {
                confirmarFormulario()
            }
        }
    }

    private fun checarCampo(displayedChild: Int) : Boolean {
        when (displayedChild) {
            0 -> {
                if (binding.etEmpresaId.text.isEmpty()) {
                    binding.etEmpresaId.error = getString(br.com.fusiondms.core.common.R.string.label_digite_id_empresa)
                    return false
                }
            }
            1 -> {
                if (binding.etMatricula.text.isEmpty()) {
                    binding.etMatricula.error = getString(br.com.fusiondms.core.common.R.string.label_digite_matricula)
                    return false
                }
            }
        }
        return true
    }

    private fun bindConfirmacaoDados() {
        binding.apply {
            btnNext.hideFabAnimation(0F)
            btnCadastrar.moveInAnimation()
            btnNext.showMessage(getString(br.com.fusiondms.core.common.R.string.label_imagem_registrada), TipoMensagem.SUCCESS, true)

            ivFaceCadastrada.setImageBitmap(getFaceSaved())
            tvEmpresaId.text = Html.fromHtml("${getString(br.com.fusiondms.core.common.R.string.label_id_empresa)}: <b>${etEmpresaId.text}</b>", 0)
            tvMatricula.text = Html.fromHtml("${getString(br.com.fusiondms.core.common.R.string.label_matricula)}: <b>${etMatricula.text}</b>", 0)
            val funcao = if (rgFuncao.checkedRadioButtonId == R.id.rb_motorista)
                getString(br.com.fusiondms.core.common.R.string.label_motorista) else getString(br.com.fusiondms.core.common.R.string.label_ajudante)
            tvFuncao.text = Html.fromHtml("${getString(br.com.fusiondms.core.common.R.string.label_funcao)}: <b>$funcao</b>", 0)
        }
    }

    private fun getFaceSaved(): Bitmap? {
        return try {
            val savedFaceFile = getSavedFacePhoto(requireContext())
            BitmapFactory.decodeFile(savedFaceFile.absolutePath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    private fun confirmarFormulario() {
        val funcao = if (binding.rgFuncao.checkedRadioButtonId == R.id.rb_motorista)
            getString(br.com.fusiondms.core.common.R.string.label_motorista) else getString(br.com.fusiondms.core.common.R.string.label_ajudante)
        val empresaId = binding.etEmpresaId.text.toString()
        val matricula = binding.etMatricula.text.toString()

        val mensagem = "${getString(br.com.fusiondms.core.common.R.string.label_id_empresa)}: <b>$empresaId</b><br>" +
                "${getString(br.com.fusiondms.core.common.R.string.label_matricula)}: <b>$matricula</b><br>" +
                "${getString(br.com.fusiondms.core.common.R.string.label_funcao)}: <b>$funcao</b>"

        Dialog(
            getString(br.com.fusiondms.core.common.R.string.label_confirmar_dados),
            mensagem,
            getString(br.com.fusiondms.core.common.R.string.label_confirmar),
            getString(br.com.fusiondms.core.common.R.string.label_cancelar),
            acaoPositiva = {
                binding.linearProgressIndicator.progressAnimation(100 / binding.vfFormulario.childCount)
                cadastrarColaborador()
            },
            acaoNegativa = {
                findNavController().navigateUp()
            }
        ).show(requireActivity().supportFragmentManager, Dialog.TAG)
    }

    private fun cadastrarColaborador() {
        binding.btnCadastrar.showMessage(getString(br.com.fusiondms.core.common.R.string.label_cadastro_realizado), TipoMensagem.SUCCESS, true)
        findNavController().navigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}