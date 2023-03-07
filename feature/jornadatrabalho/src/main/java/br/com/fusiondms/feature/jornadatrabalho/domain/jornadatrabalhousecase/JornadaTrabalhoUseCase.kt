package br.com.fusiondms.feature.jornadatrabalho.domain.jornadatrabalhousecase

import br.com.fusiondms.core.model.jornadatrabalho.Colaborador
import br.com.fusiondms.core.model.jornadatrabalho.ReciboRegistroPonto
import br.com.fusiondms.core.model.jornadatrabalho.RegistroPonto
import kotlinx.coroutines.flow.Flow

interface JornadaTrabalhoUseCase {
    suspend fun getColaborador(matricula: Long): Flow<Colaborador>
    suspend fun inserirRegistroPonto(registroPonto: RegistroPonto): Flow<Long>
    suspend fun getAllRecibo(): Flow<List<ReciboRegistroPonto>>
    suspend fun getAllRegistroPontoDia(matricula: Long): Flow<List<RegistroPonto>>
}