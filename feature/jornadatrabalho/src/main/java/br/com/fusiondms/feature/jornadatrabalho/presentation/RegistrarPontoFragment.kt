package br.com.fusiondms.feature.jornadatrabalho.presentation

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import br.com.fusiondms.core.common.converterDataParatextoLegivel
import br.com.fusiondms.feature.facedetection.ModoDeteccao
import br.com.fusiondms.feature.facedetection.presentation.FaceDetectorActivity
import br.com.fusiondms.feature.jornadatrabalho.databinding.FragmentRegistrarPontoBinding
import br.com.fusiondms.feature.jornadatrabalho.presentation.adapter.JornadaColaboradoresAdapter
import br.com.fusiondms.feature.jornadatrabalho.presentation.viewmodel.JornadaTrabalhoViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrarPontoFragment : Fragment() {
    private var _binding: FragmentRegistrarPontoBinding? = null
    private val binding get() = _binding!!

    private val jornadaViewModel: JornadaTrabalhoViewModel by activityViewModels()

    private val adapter by lazy { JornadaColaboradoresAdapter() }
    private val faceDetectorLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val faceEquals = data?.extras?.getBoolean(FaceDetectorActivity.FACE_DETECTOR_RESULT) ?: false
            if (faceEquals) {
                findNavController().navigate(br.com.fusiondms.core.navigation.R.id.action_registrarPontoFragment_to_confirmarRegistroPontoFragment)
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
//            rvColaboradores.adapter = adapter
            jornadaViewModel.getRegistroPonto()
            tvData.text = converterDataParatextoLegivel(jornadaViewModel.horaAtual.value)
        }

        bindObservers()
        bindListeners()
    }

    private fun bindObservers() {
        lifecycleScope.launchWhenStarted {
            jornadaViewModel.listaReccibo.collect { result ->
                when (result) {
                    is JornadaTrabalhoViewModel.JornadaStatus.SuccessListaRegistro -> adapter.submitList(result.listaRegistroPonto)
                    else -> Unit
                }

            }
        }
    }

    private fun bindListeners() {
        adapter.onColaboradorClickListener = { colaborador ->
            jornadaViewModel.setColaboradorSelecionado(colaborador)
            findNavController().navigate(br.com.fusiondms.core.navigation.R.id.action_registrarPontoFragment_to_confirmarRegistroPontoFragment)
        }

        binding.btnRegistrarPonto.setOnClickListener {
            val intent = Intent(requireActivity(), FaceDetectorActivity::class.java).apply {
                putExtra(FaceDetectorActivity.FACE_DETECTOR_TIPO, ModoDeteccao.COMPARAR_FACE.value)
            }
            faceDetectorLauncher.launch(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}