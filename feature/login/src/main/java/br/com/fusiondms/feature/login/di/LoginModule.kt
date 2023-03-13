package br.com.fusiondms.feature.login.di

import br.com.fusiondms.core.network.repository.login.LoginRepository
import br.com.fusiondms.core.network.repository.login.primeiroacesso.PrimeiroAcessoRepository
import br.com.fusiondms.feature.login.domain.login.LoginUseCaseImpl
import br.com.fusiondms.feature.login.domain.login.LoginUsecase
import br.com.fusiondms.feature.login.domain.primeiroacessousecase.PrimeiroAcessoUseCase
import br.com.fusiondms.feature.login.domain.primeiroacessousecase.PrimeiroAcessoUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoginModule {

    @Singleton
    @Provides
    fun providePrimeiroAcessoUseCase(
        primeiroAcessoRepository: PrimeiroAcessoRepository
    ) : PrimeiroAcessoUseCase = PrimeiroAcessoUseCaseImpl(primeiroAcessoRepository)

    @Singleton
    @Provides
    fun provideLoginUsecase(
        loginRepository: LoginRepository
    ) : LoginUsecase = LoginUseCaseImpl(loginRepository)
}