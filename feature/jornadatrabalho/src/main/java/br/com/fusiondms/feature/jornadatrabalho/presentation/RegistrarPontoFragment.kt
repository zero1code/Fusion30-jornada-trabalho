package br.com.fusiondms.feature.jornadatrabalho.presentation

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import br.com.fusiondms.core.common.bottomdialog.Dialog
import br.com.fusiondms.core.common.converterDataParaHorasMinutos
import br.com.fusiondms.core.common.converterDataParatextoLegivel
import br.com.fusiondms.core.common.getColorFromAttr
import br.com.fusiondms.core.common.permissiondiaolog.PermissionRequestDialog
import br.com.fusiondms.core.model.jornadatrabalho.RegistroPonto
import br.com.fusiondms.core.services.location.ForegroundLocationService
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
    private var permissionRequestDialog: PermissionRequestDialog? = null

    private val jornadaViewModel: JornadaTrabalhoViewModel by activityViewModels()

    private var foregroundLocationServiceBound = false
    // Fornece a atualizações de localização para while-in-use recurso.
    private var foregroundLocationService: ForegroundLocationService? = null

    // Monitora a conexão com o while-in-use service.
    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundLocationService.LocalBinder
            foregroundLocationService = binder.service
            foregroundLocationServiceBound = true
            startLocationService()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundLocationService = null
            foregroundLocationServiceBound = false
        }
    }

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

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permission ->
        when {
            permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                //Localização precisa aceita
                permissionRequestDialog?.dismiss()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    permissionRequestDialog = null
                    verificarPermissaoBackground()
                }
            }
            permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                //Localização aproximada acieta
            }
            else -> {
                //Permissão de localização negada
                permissionRequestDialog = null
                permissionRequestDialog()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val locationBackgroundPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permission ->
        when {
            permission.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
                //Localização background aceita
                permissionRequestDialog?.dismiss()
                foregroundLocationService?.subscribeToLocationUpdates()
            }
            else -> {
                //Localização background negada
                permissionBackgroundRequestDialog()
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

    private fun bindListeners() {
        binding.btnRegistrarPonto.setOnClickListener {
//            startLocationService()
            val intent = Intent(requireActivity(), FaceDetectorActivity::class.java).apply {
                putExtra(FaceDetectorActivity.FACE_DETECTOR_TIPO, ModoDeteccao.COMPARAR_FACE.value)
            }
            faceDetectorLauncher.launch(intent)
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

    private fun permissionRequestDialog() {
        if (permissionRequestDialog == null) {
            permissionRequestDialog = PermissionRequestDialog(
                br.com.fusiondms.core.common.R.drawable.ic_location,
                getString(br.com.fusiondms.core.common.R.string.label_permissao_localizacao),
                getString(br.com.fusiondms.core.common.R.string.label_permissao_localizacao_mensagem),
                getString(br.com.fusiondms.core.common.R.string.label_aceitar_permissao),
                getString(br.com.fusiondms.core.common.R.string.label_ir_para_configuracoes),
                acaoAceitarPermissao = { solicitarPermissao() },
                acaoConfiguracoes = { actionConfiguracoes() }
            )
            permissionRequestDialog?.show(
                requireActivity().supportFragmentManager,
                PermissionRequestDialog.TAG
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun permissionBackgroundRequestDialog() {
        if (permissionRequestDialog == null) {
            permissionRequestDialog = PermissionRequestDialog(
                br.com.fusiondms.core.common.R.drawable.ic_location_all_time,
                getString(br.com.fusiondms.core.common.R.string.label_permissao_localizacao_background),
                getString(br.com.fusiondms.core.common.R.string.label_permissao_localizacao_background_mensagem),
                getString(br.com.fusiondms.core.common.R.string.label_aceitar_permissao),
                getString(br.com.fusiondms.core.common.R.string.label_ir_para_configuracoes),
                acaoAceitarPermissao = { solicitarPermissaoBackground() },
                acaoConfiguracoes = { actionConfiguracoes() }
            )
            permissionRequestDialog?.show(
                requireActivity().supportFragmentManager,
                PermissionRequestDialog.TAG
            )
        }
    }

    private fun solicitarPermissao() {
        locationPermissionRequest.launch(LOCATION_PERMISSIONS)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun solicitarPermissaoBackground() {
        locationBackgroundPermissionRequest.launch(BACKGROUND_LOCATION_PERMISSION)
    }

    private fun verificarPermissao(): Boolean {
        return when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                permissionRequestDialog?.dismiss()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    permissionRequestDialog = null
                    verificarPermissaoBackground()
                } else {
                    true
                }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                //Permissao negada mais de uma vez ir para as configurações
                permissionRequestDialog()
                false
            }

            else -> {
                permissionRequestDialog()
                false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun verificarPermissaoBackground(): Boolean {
        return when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                permissionRequestDialog?.dismiss()
                true
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) -> {
                //Permissao negada mais de uma vez ir para as configurações
                permissionBackgroundRequestDialog()
                false
            }
            else -> {
                permissionBackgroundRequestDialog()
                false
            }
        }
    }

    private fun actionConfiguracoes() {
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        requireActivity().startActivity(
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            ).apply {
                data = uri
            })
    }

    private fun startLocationService() {
        if (verificarPermissao()) {
            foregroundLocationService?.subscribeToLocationUpdates()
                ?: Log.d("ForegroundLocationService", "Service Not Bound")
        } else {
            solicitarPermissao()
        }
    }

    override fun onResume() {
        super.onResume()
        verificarPermissao()
    }

    override fun onStart() {
        super.onStart()
        if (verificarPermissao()) {
            val serviceIntent = Intent(requireActivity(), ForegroundLocationService::class.java)
            requireActivity().bindService(
                serviceIntent,
                foregroundOnlyServiceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        if (foregroundLocationServiceBound) {
            requireActivity().unbindService(foregroundOnlyServiceConnection)
            foregroundLocationServiceBound = false
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        foregroundLocationService?.unsubscribeToLocationUpdates()
        _binding = null
    }

    companion object {
        val LOCATION_PERMISSIONS = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        )

        @RequiresApi(Build.VERSION_CODES.Q)
        val BACKGROUND_LOCATION_PERMISSION = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
}