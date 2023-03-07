package br.com.fusiondms.feature.login.presentation

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import br.com.fusiondms.core.common.bottomdialog.Dialog
import br.com.fusiondms.core.common.hideKeyboard
import br.com.fusiondms.core.common.progressdialog.showProgressBar
import br.com.fusiondms.core.model.login.Login
import br.com.fusiondms.feature.login.R
import br.com.fusiondms.feature.login.databinding.FragmentLoginBinding
import br.com.fusiondms.feature.login.presentation.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindObservers()
        bindListeners()
    }

    private fun bindObservers() {
        lifecycleScope.launchWhenStarted {
            loginViewModel.login.collect { result ->
                when (result) {
                    is LoginViewModel.Status.Loading -> progessDialogStatus(result.isLoading)
                    is LoginViewModel.Status.Error -> dialogErro(result.message)
                    is LoginViewModel.Status.Success -> sucessoLogin()
                    else -> Unit
                }
            }
        }
    }

    private fun bindListeners() {
        binding.apply {
            btnLogin.setOnClickListener {
                fazerLogin()
            }
            btnEsqueciSenha.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_esqueciSenhaFragment)
            }
            btnPrimeiroAcesso.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_primeiroAcessoFragment)
            }

            etEmpresa.setOnClickListener {
                nestedScrollView.fullScroll(View.FOCUS_DOWN)
            }
            etMatricula.setOnClickListener {
                nestedScrollView.fullScroll(View.FOCUS_DOWN)
            }
            etSenha.setOnClickListener {
                nestedScrollView.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    private fun fazerLogin() {
        requireActivity().hideKeyboard()
        progressDialog = requireActivity().showProgressBar(getString(br.com.fusiondms.core.common.R.string.label_aguarde_momento))
        try {
            binding.apply {
                val credenciais = Login(
                    etEmpresa.text.toString().toLong(),
                    etMatricula.text.toString().toLong(),
                    etSenha.text.toString()
                )
                loginViewModel.fazerLogin(credenciais)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sucessoLogin() {
        loginViewModel.salvarMatricula(binding.etMatricula.text.toString())
        findNavController().navigate(R.id.action_loginFragment_to_jornadaTrabalhoActivity)
        activity?.finish()
    }

    private fun dialogErro(message: String?) {
        lifecycleScope.launch {
            Dialog(
                getString(br.com.fusiondms.core.common.R.string.label_login_falhou),
                message ?: "",
                getString(br.com.fusiondms.core.common.R.string.label_tentar_novamente),
                getString(br.com.fusiondms.core.common.R.string.label_cancelar),
                acaoPositiva = { fazerLogin() },
                acaoNegativa = {}
            ).show(requireActivity().supportFragmentManager, Dialog.TAG)
        }
    }

    private fun progessDialogStatus(isLoading: Boolean) {
        if (isLoading) {
            progressDialog.show()
        } else {
            if (progressDialog.isShowing) progressDialog.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            etEmpresa.setText(loginViewModel.getIdEmpresa() ?: "")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}