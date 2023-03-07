package br.com.fusiondms.feature.login.domain.primeiroacessousecase

import br.com.fusiondms.core.model.jornadatrabalho.Colaborador
import kotlinx.coroutines.flow.Flow


interface PrimeiroAcessoUseCase {

    suspend fun cadastrarColaborador(colaborador: Colaborador): Flow<Int>
}