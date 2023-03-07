package br.com.fusiondms.feature.jornadatrabalho.presentation

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import br.com.fusiondms.core.common.bottomdialog.Dialog
import br.com.fusiondms.core.common.converterDataParaHorasMinutos
import br.com.fusiondms.core.common.converterDataParatextoLegivel
import br.com.fusiondms.core.common.getColorFromAttr
import br.com.fusiondms.core.common.progressdialog.showProgressBar
import br.com.fusiondms.core.model.jornadatrabalho.RegistroPonto
import br.com.fusiondms.feature.facedetection.ModoDeteccao
import br.com.fusiondms.feature.facedetection.presentation.FaceDetectorActivity
import br.com.fusiondms.feature.jornadatrabalho.R
import br.com.fusiondms.feature.jornadatrabalho.databinding.FragmentRegistrarPontoBinding
import br.com.fusiondms.feature.jornadatrabalho.databinding.ItemJornadaHorarioRegistroBinding
import br.com.fusiondms.feature.jornadatrabalho.presentation.viewmodel.JornadaTrabalhoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegistrarPontoFragment : Fragment() {
    private var _binding: FragmentRegistrarPontoBinding? = null
    private val binding get() = _binding!!

    private val jornadaViewModel: JornadaTrabalhoViewModel by activityViewModels()

    private val faceDetectorLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val faceEquals = data?.extras?.getBoolean(FaceDetectorActivity.FACE_DETECTOR_RESULT) ?: false
            if (faceEquals) {
                findNavController().navigate(R.id.action_registrarPontoFragment_to_confirmarRegistroPontoFragment)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrarPontoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            tvData.text = converterDataParatextoLegivel(jornadaViewModel.horaAtual.value)
        }
        bindObservers()
        bindListeners()
    }

    private fun bindObservers() {
        lifecycleScope.launchWhenStarted {
            jornadaViewModel.colaborador.collect { result ->
                when (result) {
                    is JornadaTrabalhoViewModel.StatusColaborador.Success -> {
                        jornadaViewModel.getAllRegistroPontoDia(result.colaborador.matricula)
                    }
                    else -> Unit
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            jornadaViewModel.registrosPonto.collect { result ->
                when(result) {
                    is JornadaTrabalhoViewModel.StatusRegistroPonto.Error -> dialogErro(result.message)
                    is JornadaTrabalhoViewModel.StatusRegistroPonto.Success -> bindRegistroPonto(result.listaRegistroPonto)
                    else -> Unit
                }
            }
        }
    }

    private fun bindRegistroPonto(listaRegistroPonto: List<RegistroPonto>) {
        binding.apply {
            tvNenhumRegistro.visibility = if (listaRegistroPonto.isNotEmpty()) View.GONE else View.VISIBLE
            glRegistrosPonto.visibility = if (listaRegistroPonto.isNotEmpty()) View.VISIBLE else View.GONE

            if (glRegistrosPonto.isVisible) {
                listaRegistroPonto.mapIndexed { position, registro ->
                    val layoutInflater = LayoutInflater.from(requireContext())
                    val registroPontoBinding =
                        ItemJornadaHorarioRegistroBinding.inflate(layoutInflater)
                    if (position == 0) {
                        registroPontoBinding.root.setPadding(0, 4, 8, 4)
                    } else {
                        registroPontoBinding.root.setPadding(8, 4, 8, 4)
                    }

                    registroPontoBinding.tvHorarioBatida.text =
                        converterDataParaHorasMinutos(registro.dataRegistro)
                    if (position == 3) {
                        with(registroPontoBinding) {
                            tvHorarioBatida.setTextColor(requireContext().getColor(br.com.fusiondms.core.common.R.color.brand_color_on_surface_riple))
                            cvHorario.setCardBackgroundColor(requireContext().getColorFromAttr(com.google.android.material.R.attr.colorSurface))
                            cvHorario.strokeColor =
                                requireContext().getColor(br.com.fusiondms.core.common.R.color.brand_color_on_surface_riple)
                        }
                    }
                    binding.glRegistrosPonto.addView(registroPontoBinding.root)
                }
            }
        }
    }

    private fun bindListeners() {
        binding.btnRegistrarPonto.setOnClickListener {
            val intent = Intent(requireActivity(), FaceDetectorActivity::class.java).apply {
                putExtra(FaceDetectorActivity.FACE_DETECTOR_TIPO, ModoDeteccao.COMPARAR_FACE.value)
            }
            faceDetectorLauncher.launch(intent)
        }
    }

    private fun dialogErro(message: String?) {
        lifecycleScope.launch {
            Dialog(
                getString(br.com.fusiondms.core.common.R.string.label_login_falhou),
                message ?: "",
                getString(br.com.fusiondms.core.common.R.string.label_tentar_novamente),
                getString(br.com.fusiondms.core.common.R.string.label_cancelar),
                acaoPositiva = { },
                acaoNegativa = {}
            ).show(requireActivity().supportFragmentManager, Dialog.TAG)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}