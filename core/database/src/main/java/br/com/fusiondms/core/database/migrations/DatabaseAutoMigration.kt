package br.com.fusiondms.core.database.migrations

import androidx.room.RoomDatabase

//Usar automigration quando uma tabela/coluna foi renomeada ou excluida
abstract class DatabaseAutoMigration: RoomDatabase() {

//    @RenameColumn(tableName = "tb_colaboradores", fromColumnName = "funcao", toColumnName = "cargo")
//    class Migration2To3: AutoMigrationSpec
}