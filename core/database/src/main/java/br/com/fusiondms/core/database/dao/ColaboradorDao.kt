package br.com.fusiondms.core.database.dao

import androidx.room.*
import br.com.fusiondms.core.database.model.jornadatrabalho.ColaboradorEntity
import br.com.fusiondms.core.database.model.jornadatrabalho.ReciboRegistroPontoEntity
import br.com.fusiondms.core.database.model.jornadatrabalho.RegistroPontoEntity
import dagger.Provides

@Dao
interface ColaboradorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirRegistroPonto(registroPonto: RegistroPontoEntity) : Long

    @Query("SELECT * FROM tb_colaborador WHERE matricula = :matricula")
    suspend fun getColaborador(matricula: Long) : ColaboradorEntity?

    @Query("SELECT * FROM tb_registro_ponto WHERE matricula = :matricula AND DATE(dataRegistro, 'unixepoch') = DATE('now')")
    suspend fun getAllRegistroPonto(matricula: Long) : List<RegistroPontoEntity>

    @Query("SELECT * FROM tb_registro_ponto WHERE matricula = :matricula AND registroEfetuado = 0")
    suspend fun getAllRegistroPontoPendentes(matricula: Long) : List<RegistroPontoEntity>

    @Query("UPDATE tb_registro_ponto SET registroEfetuado = 1 WHERE id IN(:registroPontoIds)")
    suspend fun updateStatusRegistroPonto(registroPontoIds: List<Int>) : Int

    @Query("SELECT tb1.*, tb2.nome FROM tb_registro_ponto AS tb1 JOIN tb_colaborador AS tb2 ON tb2.matricula = tb1.matricula ORDER BY tb1.dataRegistro DESC")
    suspend fun getAllRecibo() : List<ReciboRegistroPontoEntity>
}