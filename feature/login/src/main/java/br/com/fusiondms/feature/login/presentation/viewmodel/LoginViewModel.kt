package br.com.fusiondms.feature.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fusiondms.core.datastore.repository.DataStoreChaves
import br.com.fusiondms.core.datastore.repository.DataStoreRepository
import br.com.fusiondms.core.model.login.Login
import br.com.fusiondms.feature.login.domain.login.LoginUsecase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUsecase: LoginUsecase,
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {

    sealed class Status() {
        object Nothing: Status()
        class Loading(val isLoading: Boolean) : Status()
        object Success : Status()
        class Error(val message: String?) : Status()
    }

    private var _login = MutableStateFlow<Status>(Status.Nothing)
    val login: StateFlow<Status> get() = _login

    fun fazerLogin(credenciais: Login) =
        viewModelScope.launch {
            loginUsecase.fazerLogin(credenciais)
                .onStart {
                    _login.emit(Status.Loading(true))
                }
                .onCompletion {
                    _login.emit(Status.Loading(false))
                }
                .catch { error ->
                    _login.emit(Status.Error(error.message))
                }
                .collect {
                    _login.emit(Status.Success)
                }
        }

    fun getIdEmpresa(): String? = runBlocking {
        dataStoreRepository.getString(DataStoreChaves.ID_EMPRESA)
    }

    fun salvarMatricula(matricula: String) = runBlocking {
        dataStoreRepository.putString(DataStoreChaves.MATRICULA_MOTORISTA, matricula)
    }

}