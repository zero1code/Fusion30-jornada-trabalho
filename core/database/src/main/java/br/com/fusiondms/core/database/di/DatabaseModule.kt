package br.com.fusiondms.core.database.di

import android.content.Context
import androidx.room.Room
import br.com.fusiondms.core.database.AppDatabase
import br.com.fusiondms.core.database.migrations.DatabaseMirgation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    fun provideColaboradorDao(appDatabase: AppDatabase) = appDatabase.getColaboradorDao()

    @Provides
    fun provideLoginDao(appDatabase: AppDatabase) = appDatabase.getLoginDao()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "db.jornada_trabalho"
        ).addMigrations(
            DatabaseMirgation.MIGRATION_1_TO_2
        ).build()
    }
}