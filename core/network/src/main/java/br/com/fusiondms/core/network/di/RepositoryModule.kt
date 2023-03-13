package br.com.fusiondms.core.network.di

import android.content.Context
import br.com.fusiondms.core.database.AppDatabase
import br.com.fusiondms.core.database.dao.ColaboradorDao
import br.com.fusiondms.core.network.api.FusionApi
import br.com.fusiondms.core.network.repository.jornadatrabalho.JornadaTrabalhoRepository
import br.com.fusiondms.core.network.repository.jornadatrabalho.JornadaTrabalhoRepositoryImpl
import br.com.fusiondms.core.network.repository.login.LoginRepository
import br.com.fusiondms.core.network.repository.login.LoginRepositoryImpl
import br.com.fusiondms.core.network.repository.login.primeiroacesso.PrimeiroAcessoRepository
import br.com.fusiondms.core.network.repository.login.primeiroacesso.PrimeiroAcessoRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideJornadaTrabalhoRepository(
        fusionApi: FusionApi,
        colaboradorDao: ColaboradorDao,
        @ApplicationContext context: Context
    ) : JornadaTrabalhoRepository = JornadaTrabalhoRepositoryImpl(fusionApi, colaboradorDao, context, Dispatchers.IO)

    @Singleton
    @Provides
    fun providePrimeiroAcessoRepository(
        fusionApi: FusionApi
    ) : PrimeiroAcessoRepository = PrimeiroAcessoRepositoryImpl(fusionApi, Dispatchers.IO)

    @Singleton
    @Provides
    fun provideLoginRepository(
        fusionApi: FusionApi,
        appDatabase: AppDatabase,
        @ApplicationContext context: Context
    ) : LoginRepository = LoginRepositoryImpl(fusionApi, appDatabase, context, Dispatchers.IO)
}