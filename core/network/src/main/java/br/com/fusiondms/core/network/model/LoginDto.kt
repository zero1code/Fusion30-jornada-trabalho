package br.com.fusiondms.core.network.model

import br.com.fusiondms.core.databasejornadatrabalho.model.jornadatrabalho.ColaboradorEntity
import br.com.fusiondms.core.model.login.Login
import br.com.fusiondms.core.network.mapper.DtoMapper
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class LoginDto(
    @SerializedName("id_empresa")
    val idEmpresa: Long = 0,
    val matricula: Long = 0,
    val senha: String = "",
) : DtoMapper<Login, LoginDto> {

    override fun mapDtoToModel(dto: LoginDto): Login {
        val json = Gson().toJson(dto)
        return Gson().fromJson(json, Login::class.java)
    }

    override fun mapModelToDto(model: Login): LoginDto {
        val json = Gson().toJson(model)
        return Gson().fromJson(json, LoginDto::class.java)
    }

}
