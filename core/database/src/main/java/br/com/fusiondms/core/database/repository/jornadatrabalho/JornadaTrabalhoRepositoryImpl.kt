package br.com.fusiondms.core.database.repository.jornadatrabalho

import android.content.Context
import br.com.fusiondms.core.database.dao.ColaboradorDao
import br.com.fusiondms.core.database.model.jornadatrabalho.ColaboradorEntity
import br.com.fusiondms.core.database.model.jornadatrabalho.ReciboRegistroPontoEntity
import br.com.fusiondms.core.database.model.jornadatrabalho.RegistroPontoEntity
import br.com.fusiondms.core.model.exceptions.ErrorGetColaborador
import br.com.fusiondms.core.model.exceptions.ErrorSalvarRegistroPonto
import br.com.fusiondms.core.model.jornadatrabalho.Colaborador
import br.com.fusiondms.core.model.jornadatrabalho.ReciboRegistroPonto
import br.com.fusiondms.core.model.jornadatrabalho.RegistroPonto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class JornadaTrabalhoRepositoryImpl @Inject constructor(
    private val colaboradorDao: ColaboradorDao,
    private val dispatcher: CoroutineDispatcher,
    private val context: Context
) : JornadaTrabalhoRepository {

    override suspend fun inserirRegistroPonto(registroPonto: RegistroPonto): Flow<Long> {
        return flow {
            try {
                delay(2000)
                val result = colaboradorDao.inserirRegistroPonto(RegistroPontoEntity().mapModelToEntity(registroPonto))
                if (result > 0) {
                    emit(result)
                } else {
                    throw ErrorSalvarRegistroPonto("Erro ao salvar registro de ponto.")
                }
                emit(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.flowOn(dispatcher)
    }

    override suspend fun getAllRecibo(): Flow<List<ReciboRegistroPonto>> {
        return flow {
            try {
                val lista = colaboradorDao.getAllRecibo()
                emit(ReciboRegistroPontoEntity().entityListToModel(lista))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.flowOn(dispatcher)
    }

    override suspend fun getColaborador(matricula: Long): Flow<Colaborador> {
        return flow {
            try {
                val colaborador = colaboradorDao.getColaborador(matricula) ?: ColaboradorEntity()
                if (colaborador.matricula != 0L) {
                    emit(ColaboradorEntity().mapEntityToModel(colaborador))
                } else {
                   throw ErrorGetColaborador("O Colaborador não foi encontrado.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.flowOn(dispatcher)
    }

    override suspend fun getAllRegistroPontoDia(matricula: Long): Flow<List<RegistroPonto>> {
        return flow {
            try {
                val lista = colaboradorDao.getAllRegistroPonto(matricula)
                emit(RegistroPontoEntity().entityListToModel(lista))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.flowOn(dispatcher)
    }
}