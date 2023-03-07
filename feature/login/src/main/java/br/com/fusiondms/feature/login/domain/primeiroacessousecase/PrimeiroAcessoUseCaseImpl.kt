package br.com.fusiondms.feature.login.domain.primeiroacessousecase

import br.com.fusiondms.core.model.jornadatrabalho.Colaborador
import br.com.fusiondms.core.network.repository.primeiroacesso.PrimeiroAcessoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PrimeiroAcessoUseCaseImpl @Inject constructor(
    private val primeiroAcessoRepository: PrimeiroAcessoRepository
) : PrimeiroAcessoUseCase {
    override suspend fun cadastrarColaborador(colaborador: Colaborador): Flow<Int> {
        return primeiroAcessoRepository.cadastrarColaborador(colaborador)
    }
}