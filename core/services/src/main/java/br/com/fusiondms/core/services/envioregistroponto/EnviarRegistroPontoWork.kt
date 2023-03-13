package br.com.fusiondms.core.services.envioregistroponto

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import br.com.fusiondms.core.database.dao.ColaboradorDao
import br.com.fusiondms.core.database.model.jornadatrabalho.RegistroPontoEntity
import br.com.fusiondms.core.network.repository.jornadatrabalho.JornadaTrabalhoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.runBlocking

class EnviarRegistroPontoWork @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val colaboradorDao: ColaboradorDao,
    private val jornadaTrabalhoRepository: JornadaTrabalhoRepository
): Worker(context, workerParameters) {
    private lateinit var result: Result
    override fun doWork(): Result {
        return runBlocking {
            try {
                val matricula = workerParameters.inputData.getLong("matricula_colaborador", 0)
                colaboradorDao.getAllRegistroPontoPendentes(matricula).let { entityList ->
                    if (entityList.isNotEmpty()) {
                        RegistroPontoEntity().entityListToModel(entityList)
                            .let { listaRegistroPendente ->
                                jornadaTrabalhoRepository.enviarRegistroPonto(listaRegistroPendente)
                                    .collect { resultCode ->
                                        result = if (resultCode == 200) Result.success()
                                        else Result.retry()
                                    }
                                result
                            }
                    } else {
                        Result.success()
                    }
                }
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(appContext: Context, params: WorkerParameters) : EnviarRegistroPontoWork
    }

    companion object {
        const val TAG = "EnviarRegistroPontoWork"
        const val WORK_NAME = "enviar_registros_ponto"
    }
}