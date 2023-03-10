package br.com.fusiondms.core.network.repository.login

import android.content.Context
import br.com.fusiondms.core.database.AppDatabase
import br.com.fusiondms.core.model.exceptions.ErrorApiLogin
import br.com.fusiondms.core.model.exceptions.ErrorLogin
import br.com.fusiondms.core.model.exceptions.ErrorSalvarColaboradorLocalLogin
import br.com.fusiondms.core.model.login.Login
import br.com.fusiondms.core.network.api.FusionApi
import br.com.fusiondms.core.network.model.ColaboradorDto
import br.com.fusiondms.core.network.model.LoginDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val fusionApi: FusionApi,
    private val appDatabase: AppDatabase,
    private val context: Context,
    private val dispatcher: CoroutineDispatcher
) : LoginRepository {
    override fun fazerLogin(credenciais: Login): Flow<Int> {
        return flow {
            try {
                val dto = LoginDto().mapModelToDto(credenciais)
                val response = fusionApi.fazerLogin(dto)
                if (response.isSuccessful) {
                    val result = response.body()?.let { colaborador ->
                        appDatabase.getLoginDao().inserirColaborador(ColaboradorDto().mapDtoToEntity(colaborador))
                    } ?: 0
                    if (result == dto.matricula) {
                        emit(response.code())
                    } else {
                        throw ErrorSalvarColaboradorLocalLogin("Erro ao salvar os dados no dispositivo. Tente novamente.")
                    }
                } else {
                    throw ErrorApiLogin(response.message(), response.code())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw ErrorLogin(e.message)
            }
        }.flowOn(dispatcher)
    }
}