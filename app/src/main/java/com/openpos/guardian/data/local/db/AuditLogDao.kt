package com.openpos.guardian.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AuditLogEntity): Long

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentFlow(limit: Int): Flow<List<AuditLogEntity>>

    @Query("""
        SELECT * FROM audit_logs 
        WHERE (:filterTypes = 0 OR eventType IN (:types))
          AND (:filterStatuses = 0 OR status IN (:statuses))
        ORDER BY timestamp DESC
    """)
    fun getFilteredFlow(
        filterTypes: Int,
        types: List<String>,
        filterStatuses: Int,
        statuses: List<String>
    ): Flow<List<AuditLogEntity>>

    @Query("DELETE FROM audit_logs")
    suspend fun clearAll(): Int

    @Query("SELECT COUNT(*) FROM audit_logs")
    suspend fun getCount(): Int

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    suspend fun getAllList(): List<AuditLogEntity>
}
