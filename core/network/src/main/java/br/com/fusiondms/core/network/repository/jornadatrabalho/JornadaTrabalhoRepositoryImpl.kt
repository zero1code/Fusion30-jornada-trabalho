package br.com.fusiondms.core.network.repository.jornadatrabalho

import android.content.Context
import br.com.fusiondms.core.database.AppDatabase
import br.com.fusiondms.core.database.dao.ColaboradorDao
import br.com.fusiondms.core.model.exceptions.ErrorApiEnvioRegistroPonto
import br.com.fusiondms.core.model.exceptions.ErrorUpdateStatusRegistroPonto
import br.com.fusiondms.core.model.jornadatrabalho.RegistroPonto
import br.com.fusiondms.core.network.api.FusionApi
import br.com.fusiondms.core.network.model.RegistroPontoDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class JornadaTrabalhoRepositoryImpl @Inject constructor(
    private val fusionApi: FusionApi,
    private val colaboradorDao: ColaboradorDao,
    private val context: Context,
    private val dispatcher: CoroutineDispatcher
) : JornadaTrabalhoRepository {
    override fun enviarRegistroPonto(lista: List<RegistroPonto>): Flow<Int> {
        return flow {
            try {
                RegistroPontoDto().modelListToDto(lista).let { dtoList ->
                    fusionApi.enviarRegistroPonto(dtoList).run {
                        if (isSuccessful) {
                            body()?.let { registroPontoIds ->
                                val result = colaboradorDao.updateStatusRegistroPonto(registroPontoIds)
                                if (result <= 0) {
                                    throw ErrorUpdateStatusRegistroPonto("Não foi possível atualizar o status dos registros de ponto.")
                                }
                            }
                            emit(code())
                        } else {
                            throw ErrorApiEnvioRegistroPonto("Erro ao enviar dados dos registros de ponto.")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.flowOn(dispatcher)
    }
}