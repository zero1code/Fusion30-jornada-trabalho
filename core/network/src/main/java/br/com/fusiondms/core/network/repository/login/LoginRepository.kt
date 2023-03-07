package br.com.fusiondms.core.network.repository.login

import br.com.fusiondms.core.model.login.Login
import kotlinx.coroutines.flow.Flow

interface LoginRepository {
    fun fazerLogin(credenciais: Login): Flow<Int>
}