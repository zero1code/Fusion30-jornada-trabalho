package br.com.fusiondms.core.network.model

import br.com.fusiondms.core.databasejornadatrabalho.model.jornadatrabalho.ColaboradorEntity
import br.com.fusiondms.core.model.jornadatrabalho.Colaborador
import br.com.fusiondms.core.network.mapper.DtoMapper
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class ColaboradorDto(
    @SerializedName("id_empresa")
    val idEmpresa: Long = 0,
    val matricula: Long = 0,
    val nome: String = "",
    val funcao: String = "",
    @SerializedName("image_base_64")
    val image64: String? = null,
    @SerializedName("image_path")
    val imagePath: String? = null
) : DtoMapper<Colaborador, ColaboradorDto> {

    override fun mapDtoToModel(dto: ColaboradorDto): Colaborador {
        val json = Gson().toJson(dto)
        return Gson().fromJson(json, Colaborador::class.java)
    }

    override fun mapModelToDto(model: Colaborador): ColaboradorDto {
        val json = Gson().toJson(model)
        return Gson().fromJson(json, ColaboradorDto::class.java)
    }

    fun mapDtoToEntity(dto: ColaboradorDto): ColaboradorEntity {
        val json = Gson().toJson(dto)
        return Gson().fromJson(json, ColaboradorEntity::class.java)
    }

}