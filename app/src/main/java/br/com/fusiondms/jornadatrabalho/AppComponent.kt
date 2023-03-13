package br.com.fusiondms.jornadatrabalho

import android.app.Application
import dagger.Component
import javax.inject.Singleton

// Para o workFactory e workmanager aceitarem dependencias via injeção
@Singleton
@Component
interface AppComponent {
    fun injectTo(application: Application)
}