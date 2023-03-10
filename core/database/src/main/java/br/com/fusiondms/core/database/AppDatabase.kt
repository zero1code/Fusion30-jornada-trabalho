package br.com.fusiondms.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.fusiondms.core.database.dao.ColaboradorDao
import br.com.fusiondms.core.database.dao.LoginDao
import br.com.fusiondms.core.database.model.jornadatrabalho.ColaboradorEntity
import br.com.fusiondms.core.database.model.jornadatrabalho.RegistroPontoEntity

@Database(
    entities = [ColaboradorEntity::class, RegistroPontoEntity::class],
    version = 2,
    autoMigrations = [

    ]
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun getColaboradorDao() : ColaboradorDao

    abstract fun getLoginDao() : LoginDao
}