package br.com.fusiondms.jornadatrabalho

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import br.com.fusiondms.core.factory.WorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App: Application() {
    private lateinit var appComponent: AppComponent

    @Inject
    lateinit var workerFactory: WorkerFactory

    override fun onCreate() {
        appComponent = DaggerAppComponent.create()
        appComponent.injectTo(this)
        super.onCreate()

        val workManagerConfig = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
        WorkManager.initialize(this, workManagerConfig)
    }
}