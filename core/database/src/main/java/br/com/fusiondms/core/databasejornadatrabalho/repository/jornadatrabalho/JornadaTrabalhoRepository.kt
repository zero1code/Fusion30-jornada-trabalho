package br.com.fusiondms.core.databasejornadatrabalho.repository.jornadatrabalho

import br.com.fusiondms.core.model.jornadatrabalho.Colaborador
import br.com.fusiondms.core.model.jornadatrabalho.ReciboRegistroPonto
import br.com.fusiondms.core.model.jornadatrabalho.RegistroPonto
import kotlinx.coroutines.flow.Flow

interface JornadaTrabalhoRepository {
    suspend fun inserirRegistroPonto(registroPonto: RegistroPonto): Flow<Long>
    suspend fun getAllRecibo(): Flow<List<ReciboRegistroPonto>>
    suspend fun getColaborador(matricula: Long): Flow<Colaborador>
    suspend fun getAllRegistroPontoDia(matricula: Long): Flow<List<RegistroPonto>>
}