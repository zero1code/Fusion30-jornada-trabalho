package br.com.fusiondms.core.network.repository.login.primeiroacesso

import br.com.fusiondms.core.model.jornadatrabalho.Colaborador
import kotlinx.coroutines.flow.Flow

interface PrimeiroAcessoRepository {

    fun cadastrarColaborador(colaborador: Colaborador) : Flow<Int>
}