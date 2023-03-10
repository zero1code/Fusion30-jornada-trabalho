package br.com.fusiondms.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
//Usar migration quando a mudança for muito complexa add tabelas por exemplo
object DatabaseMirgation {
    val MIGRATION_1_TO_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE 'tb_registro_ponto' ADD COLUMN 'latitude' TEXT NOT NULL DEFAULT '';")
            database.execSQL("ALTER TABLE 'tb_registro_ponto' ADD COLUMN 'longitude' TEXT NOT NULL DEFAULT '';")


        }
    }
}