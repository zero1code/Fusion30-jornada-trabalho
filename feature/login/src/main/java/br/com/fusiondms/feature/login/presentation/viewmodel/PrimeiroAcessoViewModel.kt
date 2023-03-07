package br.com.fusiondms.feature.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fusiondms.core.datastore.repository.DataStoreChaves
import br.com.fusiondms.core.datastore.repository.DataStoreRepository
import br.com.fusiondms.core.model.jornadatrabalho.Colaborador
import br.com.fusiondms.feature.login.domain.primeiroacessousecase.PrimeiroAcessoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class PrimeiroAcessoViewModel @Inject constructor(
    private val primeiroAcessoUseCase: PrimeiroAcessoUseCase,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    sealed class Status() {
        object Nothing: Status()
        class Loading(val isLoading: Boolean) : Status()
        object Success : Status()
        class Error(val message: String?) : Status()
    }

    private var _cadastroStatus = MutableStateFlow<Status>(Status.Nothing)
    val cadastroStatus: StateFlow<Status> get() = _cadastroStatus

    private var _viewFlipperPosition = MutableStateFlow(0)
    val viewFlipperPosition: StateFlow<Int> get() = _viewFlipperPosition

    fun showNext(position: Int) = viewModelScope.launch {
        _viewFlipperPosition.emit(position)
    }

    fun cadastrarColaborador(colaborador: Colaborador) =
        viewModelScope.launch {
            primeiroAcessoUseCase.cadastrarColaborador(colaborador)
                .onStart {
                    _cadastroStatus.emit(Status.Loading(true))
                }
                .onCompletion {
                    _cadastroStatus.emit(Status.Loading(false))
                }
                .catch { error ->
                    _cadastroStatus.emit(Status.Error(error.message))
                }
                .collect {
                    _cadastroStatus.emit(Status.Success)
                }
        }

    fun salvarIdEmpresa(idEmpresa: String) = runBlocking {
        dataStoreRepository.putString(DataStoreChaves.ID_EMPRESA, idEmpresa)
    }
}