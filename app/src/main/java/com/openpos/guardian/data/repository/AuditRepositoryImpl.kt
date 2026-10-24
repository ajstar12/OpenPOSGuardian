package com.openpos.guardian.data.repository

import com.openpos.guardian.data.local.db.AuditLogDao
import com.openpos.guardian.data.local.db.AuditLogEntity
import com.openpos.guardian.domain.model.AuditEvent
import com.openpos.guardian.domain.model.AuditEventType
import com.openpos.guardian.domain.model.AuditStatus
import com.openpos.guardian.domain.repository.AuditRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditRepositoryImpl @Inject constructor(
    private val auditLogDao: AuditLogDao
) : AuditRepository {

    override fun getAllEvents(): Flow<List<AuditEvent>> {
        return auditLogDao.getAllFlow().map { list -> list.map { it.toDomain() } }
    }

    override fun getFilteredEvents(
        types: Set<AuditEventType>?,
        statuses: Set<AuditStatus>?
    ): Flow<List<AuditEvent>> {
        val hasTypes = if (!types.isNullOrEmpty()) 1 else 0
        val typeNames = types?.map { it.name } ?: emptyList()
        val hasStatuses = if (!statuses.isNullOrEmpty()) 1 else 0
        val statusNames = statuses?.map { it.name } ?: emptyList()

        return auditLogDao.getFilteredFlow(
            filterTypes = hasTypes,
            types = typeNames,
            filterStatuses = hasStatuses,
            statuses = statusNames
        ).map { list -> list.map { it.toDomain() } }
    }

    override fun getRecentEvents(limit: Int): Flow<List<AuditEvent>> {
        return auditLogDao.getRecentFlow(limit).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun recordEvent(
        eventType: AuditEventType,
        action: String,
        status: AuditStatus,
        details: String,
        actor: String
    ): Long {
        val entity = AuditLogEntity(
            timestamp = System.currentTimeMillis(),
            eventType = eventType.name,
            action = action,
            status = status.name,
            details = details,
            actor = actor
        )
        return auditLogDao.insert(entity)
    }

    override suspend fun clearAuditLogs(): Int {
        val deletedCount = auditLogDao.clearAll()
        recordEvent(
            eventType = AuditEventType.ADMIN_AUTH,
            action = "AUDIT_LOGS_PURGED",
            status = AuditStatus.WARNING,
            details = "All local audit logs ($deletedCount records) were cleared by administrator.",
            actor = "ADMIN_USER"
        )
        return deletedCount
    }

    override suspend fun exportLogsAsJson(): String {
        val logs = auditLogDao.getAllList()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)

        val root = JSONObject().apply {
            put("app", "OpenPOS Guardian")
            put("exportTimestamp", System.currentTimeMillis())
            put("exportIsoDate", dateFormat.format(Date()))
            put("totalRecords", logs.size)

            val array = JSONArray()
            for (log in logs) {
                val item = JSONObject().apply {
                    put("id", log.id)
                    put("timestamp", log.timestamp)
                    put("isoDate", dateFormat.format(Date(log.timestamp)))
                    put("eventType", log.eventType)
                    put("action", log.action)
                    put("status", log.status)
                    put("details", log.details)
                    put("actor", log.actor)
                }
                array.put(item)
            }
            put("records", array)
        }

        return root.toString(2)
    }
}
