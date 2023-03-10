package br.com.fusiondms.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.fusiondms.core.database.model.jornadatrabalho.ColaboradorEntity
import br.com.fusiondms.core.database.model.jornadatrabalho.ReciboRegistroPontoEntity
import br.com.fusiondms.core.database.model.jornadatrabalho.RegistroPontoEntity

@Dao
interface ColaboradorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirRegistroPonto(registroPonto: RegistroPontoEntity) : Long

    @Query("SELECT * FROM tb_colaborador WHERE matricula = :matricula")
    suspend fun getColaborador(matricula: Long) : ColaboradorEntity?

    @Query("SELECT * FROM tb_registro_ponto WHERE matricula = :matricula AND DATE(dataRegistro, 'unixepoch') = DATE('now')")
    suspend fun getAllRegistroPonto(matricula: Long) : List<RegistroPontoEntity>

    @Query("SELECT tb1.*, tb2.nome FROM tb_registro_ponto AS tb1 JOIN tb_colaborador AS tb2 ON tb2.matricula = tb1.matricula ORDER BY tb1.dataRegistro DESC")
    suspend fun getAllRecibo() : List<ReciboRegistroPontoEntity>
}