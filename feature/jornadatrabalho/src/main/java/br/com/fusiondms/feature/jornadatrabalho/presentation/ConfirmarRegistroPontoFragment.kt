package br.com.fusiondms.feature.jornadatrabalho.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import br.com.fusiondms.core.common.R
import br.com.fusiondms.core.common.bottomdialog.Dialog
import br.com.fusiondms.core.common.converterDataParaDiaMesAnoHoraMinuto
import br.com.fusiondms.core.common.converterDataParaHorasMinutos
import br.com.fusiondms.core.common.progressdialog.showProgressBar
import br.com.fusiondms.core.model.jornadatrabalho.Colaborador
import br.com.fusiondms.core.model.jornadatrabalho.RegistroPonto
import br.com.fusiondms.core.services.location.ForegroundLocationService
import br.com.fusiondms.feature.facedetection.presentation.utils.getSavedFacePhoto
import br.com.fusiondms.feature.jornadatrabalho.databinding.FragmentConfirmarRegistroPontoBinding
import br.com.fusiondms.feature.jornadatrabalho.presentation.viewmodel.JornadaTrabalhoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.util.*
import kotlin.concurrent.timerTask

@AndroidEntryPoint
class ConfirmarRegistroPontoFragment : Fragment() {
    private var _binding: FragmentConfirmarRegistroPontoBinding? = null
    private val  binding get() = _binding!!
    private lateinit var progressDialog: AlertDialog

    private val jornadaViewModel: JornadaTrabalhoViewModel by activityViewModels()
    private val timerAtualizaData = Timer()
    private val timerSucesso = Timer()
    private val timerRegistroPonto = Timer()
    private lateinit var registroPonto: RegistroPonto

    private lateinit var colaborador: Colaborador

    private var currentLatitude = ""
    private var currentLongitude = ""
    // Escuta o location broadcasts do ForegroundOnlyLocationService.
    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmarRegistroPontoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationBroadcastReceiver = LocationBroadcastReceiver()

        bindObservers()
        bindListeners()
    }

    private fun bindObservers() {
        lifecycleScope.launchWhenStarted {
            jornadaViewModel.horaAtual.collect() { data ->
                binding.tvHoraAtual.text = converterDataParaHorasMinutos(data)
            }
        }

        lifecycleScope.launchWhenStarted {
            jornadaViewModel.colaborador.collect { result ->
                when (result) {
                    is JornadaTrabalhoViewModel.StatusColaborador.Success -> {
                        colaborador = result.colaborador
                        binding.apply {
                            ivFace.setImageBitmap(getFaceSaved())
                            tvNome.text = result.colaborador.nome
                            tvFuncao.text = result.colaborador.funcao
                        }
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            jornadaViewModel.registrarPonto.collect { result ->
                when (result) {
                    is JornadaTrabalhoViewModel.StatusRegistroPonto.Loading -> progessDialogStatus(result.isLoading)
                    is JornadaTrabalhoViewModel.StatusRegistroPonto.Error -> dialogErro(result.message)
                    is JornadaTrabalhoViewModel.StatusRegistroPonto.Success -> sucessoRegistrarPonto()
                    else -> Unit
                }
            }
        }
    }

    private fun bindListeners() {
        timerAtualizaData.scheduleAtFixedRate(timerTask() {
            jornadaViewModel.atualizarData()
        }, 1 * 1000, 5 * 1000)

        binding.btnRegistrarPonto.setOnClickListener {
            progressDialog = requireActivity().showProgressBar(getString(br.com.fusiondms.core.common.R.string.label_aguarde_momento))
            progessDialogStatus(true)
            registrarPonto()
        }
    }

    private fun registrarPonto() {
        if (currentLatitude != "" && currentLongitude != "") {
            registroPonto = RegistroPonto(
                matricula = colaborador.matricula,
                dataRegistro = jornadaViewModel.horaAtual.value,
                registroEfetuado = false,
                latitude = currentLatitude,
                longitude = currentLongitude
            )
            jornadaViewModel.inserirRegistroPonto(registroPonto)
        } else {
            timerRegistroPonto.schedule(timerTask() {
                registrarPonto()
            }, 3 * 1000)
        }
    }

    private fun sucessoRegistrarPonto() {
        binding.apply {
            if (::registroPonto.isInitialized) {
                vfFormulario.displayedChild = 1
                tvNomeRecibo.text = colaborador.nome
                tvData.text = converterDataParaDiaMesAnoHoraMinuto(registroPonto.dataRegistro)
                timerSucesso.schedule(timerTask() {
                    requireActivity().runOnUiThread {
                        findNavController().navigateUp()
                    }
                }, 10 * 1000)
            }
        }
    }
    private fun dialogErro(message: String?) {
        lifecycleScope.launch {
            Dialog(
                getString(R.string.label_login_falhou),
                message ?: "",
                getString(R.string.label_tentar_novamente),
                getString(R.string.label_cancelar),
                acaoPositiva = { registrarPonto() },
                acaoNegativa = {}
            ).show(requireActivity().supportFragmentManager, Dialog.TAG)
        }
    }

    private fun progessDialogStatus(isLoading: Boolean) {
        if (::progressDialog.isInitialized) {
            if (isLoading) {
                progressDialog.show()
            } else {
                if (progressDialog.isShowing) progressDialog.dismiss()
            }
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

    private inner class LocationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.extras?.getParcelable(ForegroundLocationService.EXTRA_LOCATION, Location::class.java)
            } else {
                intent.getParcelableExtra(ForegroundLocationService.EXTRA_LOCATION)
            }

            location?.let {
                currentLatitude = it.latitude.toString()
                currentLongitude = it.longitude.toString()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val intentFilter = IntentFilter(ForegroundLocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        requireActivity().registerReceiver(locationBroadcastReceiver, intentFilter)
    }

    override fun onDestroy() {
        requireActivity().unregisterReceiver(locationBroadcastReceiver)
        super.onDestroy()
        timerAtualizaData.cancel()
        timerRegistroPonto.cancel()
        timerSucesso.cancel()
        jornadaViewModel.resetJornadaState()
        _binding = null
    }
}