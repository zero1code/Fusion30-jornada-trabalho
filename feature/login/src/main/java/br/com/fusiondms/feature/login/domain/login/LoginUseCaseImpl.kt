package br.com.fusiondms.feature.login.domain.login

import br.com.fusiondms.core.model.login.Login
import br.com.fusiondms.core.network.repository.login.LoginRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCaseImpl @Inject constructor(
    private val loginRepository: LoginRepository
) : LoginUsecase {
    override suspend fun fazerLogin(credenciais: Login) : Flow<Int> {
        return loginRepository.fazerLogin(credenciais)
    }
}