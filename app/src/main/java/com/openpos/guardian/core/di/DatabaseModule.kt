package com.openpos.guardian.core.di

import android.content.Context
import androidx.room.Room
import com.openpos.guardian.data.local.db.AppDatabase
import com.openpos.guardian.data.local.db.AuditLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "openpos_guardian.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideAuditLogDao(database: AppDatabase): AuditLogDao {
        return database.auditLogDao()
    }
}
