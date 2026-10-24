package com.openpos.guardian.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AuditLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun auditLogDao(): AuditLogDao
}
