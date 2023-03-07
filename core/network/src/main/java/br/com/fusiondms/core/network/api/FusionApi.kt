package br.com.fusiondms.core.network.api

import br.com.fusiondms.core.network.model.ColaboradorDto
import br.com.fusiondms.core.network.model.LoginDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FusionApi {
    @POST("cadastrar_colaborador")
    suspend fun cadastrarColaborador(@Body colaboradorDto: ColaboradorDto): Response<ColaboradorDto>

    @POST("login")
    suspend fun fazerLogin(@Body credenciaisDto: LoginDto) : Response<ColaboradorDto>

}