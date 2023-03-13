package br.com.fusiondms.core.network.repository.jornadatrabalho

import br.com.fusiondms.core.model.jornadatrabalho.RegistroPonto
import kotlinx.coroutines.flow.Flow


interface JornadaTrabalhoRepository {

    fun enviarRegistroPonto(lista: List<RegistroPonto>): Flow<Int>
}