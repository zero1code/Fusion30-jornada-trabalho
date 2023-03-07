package br.com.fusiondms.feature.jornadatrabalho.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fusiondms.core.datastore.repository.DataStoreChaves
import br.com.fusiondms.core.datastore.repository.DataStoreRepository
import br.com.fusiondms.core.model.jornadatrabalho.Colaborador
import br.com.fusiondms.core.model.jornadatrabalho.ReciboRegistroPonto
import br.com.fusiondms.core.model.jornadatrabalho.RegistroPonto
import br.com.fusiondms.feature.jornadatrabalho.domain.jornadatrabalhousecase.JornadaTrabalhoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class JornadaTrabalhoViewModel @Inject constructor(
    private val jornadaTrabalhoUseCase: JornadaTrabalhoUseCase,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    sealed class StatusRecibo() {
        object Nothing : StatusRecibo()
        class Loading(val isLoading: Boolean) : StatusRecibo()
        class Error(val message: String?) : StatusRecibo()
        class Success(val listaRecibo: List<ReciboRegistroPonto>) : StatusRecibo()
    }
    sealed class StatusRegistroPonto() {
        object Nothing : StatusRegistroPonto()
        class Loading(val isLoading: Boolean) : StatusRegistroPonto()
        class Error(val message: String?) : StatusRegistroPonto()
        class Success(val listaRegistroPonto: List<RegistroPonto>) : StatusRegistroPonto()
    }
    sealed class StatusColaborador() {
        object Nothing : StatusColaborador()
        class Success(val colaborador: Colaborador) : StatusColaborador()
        class Error(val message: String?) : StatusColaborador()
    }

    private var _horaAtual: MutableStateFlow<Long> = MutableStateFlow(Instant.now().epochSecond)
    val horaAtual: StateFlow<Long> get() = _horaAtual

    private var _registrosPonto: MutableStateFlow<StatusRegistroPonto> = MutableStateFlow(StatusRegistroPonto.Nothing)
    val registrosPonto: StateFlow<StatusRegistroPonto> get() = _registrosPonto

    private var _colaborador: MutableStateFlow<StatusColaborador> = MutableStateFlow(
        StatusColaborador.Nothing
    )
    val colaborador : StateFlow<StatusColaborador> get() = _colaborador

    private var _registrarPonto: MutableStateFlow<StatusRegistroPonto> = MutableStateFlow(
        StatusRegistroPonto.Nothing
    )
    val registrarPonto : StateFlow<StatusRegistroPonto> get() = _registrarPonto

    private var _listaRecibo: MutableStateFlow<StatusRecibo> = MutableStateFlow(StatusRecibo.Nothing)
    val listaRecibo: StateFlow<StatusRecibo> get() = _listaRecibo

    init {
        getColaborador()
    }
    private fun getColaborador() =
        viewModelScope.launch {
            val matricula = getMatricula()?.toLong() ?: 0L
            jornadaTrabalhoUseCase.getColaborador(matricula)
                .catch { error ->
                    _colaborador.emit(StatusColaborador.Error(error.message))
                }
                .collect { colaborador ->
                    _colaborador.emit(StatusColaborador.Success(colaborador))
                }
        }

    fun inserirRegistroPonto(registroPonto: RegistroPonto) =
        viewModelScope.launch {
            jornadaTrabalhoUseCase.inserirRegistroPonto(registroPonto)
                .onStart {
                    _registrarPonto.emit(StatusRegistroPonto.Loading(true))
                }
                .onCompletion {
                    _registrarPonto.emit(StatusRegistroPonto.Loading(false))
                }
                .catch { error ->
                    _registrarPonto.emit(StatusRegistroPonto.Error(error.message))
                }
                .collect {
                    _registrarPonto.emit(StatusRegistroPonto.Success(arrayListOf()))
                }
        }

    fun getAllRegistroPontoDia(matricula: Long) =
        viewModelScope.launch {
            jornadaTrabalhoUseCase.getAllRegistroPontoDia(matricula)
                .onStart {
                    _registrosPonto.emit(StatusRegistroPonto.Loading(true))
                }
                .onCompletion {
                    _registrosPonto.emit(StatusRegistroPonto.Loading(false))
                }
                .catch { error ->
                    _registrosPonto.emit(StatusRegistroPonto.Error(error.message))
                }
                .collect { lista ->
                    _registrosPonto.emit(StatusRegistroPonto.Success(lista))
                }
        }

    fun getListaRecibo() =
        viewModelScope.launch {
            jornadaTrabalhoUseCase.getAllRecibo()
                .onStart {
                }
                .onCompletion {
                }
                .catch {
                }
                .collect { lista ->
                    _listaRecibo.emit(StatusRecibo.Success(lista))
                }
        }

    fun atualizarData() =
        viewModelScope.launch {
        _horaAtual.emit(Instant.now().epochSecond)
//        _horaAtual.emit(Instant.now().toEpochMilli())
    }

    fun resetJornadaState() =
        viewModelScope.launch {
            _registrosPonto.emit(StatusRegistroPonto.Nothing)
        }

    private fun getMatricula(): String? = runBlocking {
        dataStoreRepository.getString(DataStoreChaves.MATRICULA_MOTORISTA)
    }
}