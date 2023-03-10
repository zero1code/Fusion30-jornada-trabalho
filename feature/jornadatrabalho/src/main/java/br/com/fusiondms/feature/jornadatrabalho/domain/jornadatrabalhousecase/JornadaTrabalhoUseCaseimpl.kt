package br.com.fusiondms.feature.jornadatrabalho.domain.jornadatrabalhousecase

import br.com.fusiondms.core.database.repository.jornadatrabalho.JornadaTrabalhoRepository
import br.com.fusiondms.core.model.jornadatrabalho.Colaborador
import br.com.fusiondms.core.model.jornadatrabalho.ReciboRegistroPonto
import br.com.fusiondms.core.model.jornadatrabalho.RegistroPonto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class JornadaTrabalhoUseCaseimpl @Inject constructor(
    private val jornadaTrabalhoRepository: JornadaTrabalhoRepository

) : JornadaTrabalhoUseCase {
    override suspend fun getColaborador(matricula: Long): Flow<Colaborador> {
        return jornadaTrabalhoRepository.getColaborador(matricula)
    }

    override suspend fun inserirRegistroPonto(registroPonto: RegistroPonto): Flow<Long> {
        return jornadaTrabalhoRepository.inserirRegistroPonto(registroPonto)
    }

    override suspend fun getAllRecibo(): Flow<List<ReciboRegistroPonto>> {
        return jornadaTrabalhoRepository.getAllRecibo()
    }

    override suspend fun getAllRegistroPontoDia(matricula: Long): Flow<List<RegistroPonto>> {
        return jornadaTrabalhoRepository.getAllRegistroPontoDia(matricula)
    }
}