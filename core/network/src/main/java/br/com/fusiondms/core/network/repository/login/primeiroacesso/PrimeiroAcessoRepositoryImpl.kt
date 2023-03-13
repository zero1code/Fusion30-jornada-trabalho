package br.com.fusiondms.core.network.repository.login.primeiroacesso

import android.content.Context
import br.com.fusiondms.core.model.exceptions.ErrorApiCadastroColaborador
import br.com.fusiondms.core.model.exceptions.ErrorCadastroColaborador
import br.com.fusiondms.core.model.jornadatrabalho.Colaborador
import br.com.fusiondms.core.network.api.FusionApi
import br.com.fusiondms.core.network.model.ColaboradorDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class PrimeiroAcessoRepositoryImpl @Inject constructor(
    private val fusionApi: FusionApi,
    private val dispatcher: CoroutineDispatcher
) : PrimeiroAcessoRepository {

    override fun cadastrarColaborador(colaborador: Colaborador): Flow<Int> {
        return flow {
            try {
                val dto = ColaboradorDto().mapModelToDto(colaborador)
                val response = fusionApi.cadastrarColaborador(dto)
                if (response.isSuccessful) {
                    emit(response.code())
                } else {
                    throw ErrorApiCadastroColaborador(response.message(), response.code())
                }
            } catch (e: Exception) {
                throw ErrorCadastroColaborador(e.message)
            }
        }.flowOn(dispatcher)
    }
}