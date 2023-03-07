package br.com.fusiondms.core.databasejornadatrabalho.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import br.com.fusiondms.core.databasejornadatrabalho.model.jornadatrabalho.ColaboradorEntity

@Dao
interface LoginDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirColaborador(colaborador: ColaboradorEntity) : Long
}