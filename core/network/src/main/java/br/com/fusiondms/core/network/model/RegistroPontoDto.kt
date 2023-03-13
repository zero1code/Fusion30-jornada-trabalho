package br.com.fusiondms.core.network.model

import br.com.fusiondms.core.model.jornadatrabalho.RegistroPonto
import br.com.fusiondms.core.network.mapper.DtoMapper
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class RegistroPontoDto(
    val id: Int = 0,
    val matricula: Long = 0,
    @SerializedName("data_registro")
    val dataRegistro: Long = 0,
    @SerializedName("registro_efetuado")
    val registroEfetuado: Boolean = false,
    val latitude: String = "",
    val longitude: String = ""
) : DtoMapper<RegistroPonto, RegistroPontoDto> {
    override fun mapDtoToModel(dto: RegistroPontoDto): RegistroPonto {
        val json = Gson().toJson(dto)
        return Gson().fromJson(json, RegistroPonto::class.java)
    }

    override fun mapModelToDto(model: RegistroPonto): RegistroPontoDto {
        val json = Gson().toJson(model)
        return Gson().fromJson(json, RegistroPontoDto::class.java)
    }

    fun modelListToDto(modelList: List<RegistroPonto>): List<RegistroPontoDto> {
        return modelList.map {
            mapModelToDto(it)
        }
    }
}
