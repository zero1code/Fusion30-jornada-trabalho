package br.com.fusiondms.core.database.model.jornadatrabalho

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.com.fusiondms.core.database.mapper.EntityMapper
import br.com.fusiondms.core.model.jornadatrabalho.Colaborador
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tb_colaborador")
data class ColaboradorEntity(
    @SerializedName("id_empresa")
    var idEmpresa: Long = 0,
    @PrimaryKey()
    var matricula: Long = 0,
    var nome: String = "",
    var funcao: String = "",
    @SerializedName("image_base_64")
    var image64: String? = null,
    @SerializedName("image_path")
    var imagePath: String? = null
) : EntityMapper<Colaborador, ColaboradorEntity> {
    override fun mapEntityToModel(entity: ColaboradorEntity): Colaborador {
        return Colaborador(
            idEmpresa = entity.idEmpresa,
            matricula = entity.matricula,
            nome = entity.nome,
            funcao = entity.funcao,
            image64 = entity.image64,
            imagePath = entity.imagePath
        )
    }

    override fun mapModelToEntity(model: Colaborador): ColaboradorEntity {
        val json = Gson().toJson(model)
        return Gson().fromJson(json, ColaboradorEntity::class.java)
    }
}
