package br.com.fusiondms.feature.login.domain.login

import br.com.fusiondms.core.model.login.Login
import kotlinx.coroutines.flow.Flow

interface LoginUsecase {
    suspend fun fazerLogin(credenciais: Login): Flow<Int>
}