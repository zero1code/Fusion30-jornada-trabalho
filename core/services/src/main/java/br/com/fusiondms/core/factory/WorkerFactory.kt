package br.com.fusiondms.core.factory

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import br.com.fusiondms.core.database.dao.ColaboradorDao
import br.com.fusiondms.core.services.envioregistroponto.EnviarRegistroPontoWork
import javax.inject.Inject

class WorkerFactory @Inject constructor(
    private val enviarRegistroPontoWorkFactory: EnviarRegistroPontoWork.Factory
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            EnviarRegistroPontoWork::class.java.name ->
                enviarRegistroPontoWorkFactory.create(appContext, workerParameters)
            else -> null
        }
    }
}