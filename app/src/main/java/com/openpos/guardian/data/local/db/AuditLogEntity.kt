package com.openpos.guardian.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.openpos.guardian.domain.model.AuditEvent
import com.openpos.guardian.domain.model.AuditEventType
import com.openpos.guardian.domain.model.AuditStatus

@Entity(
    tableName = "audit_logs",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["eventType"]),
        Index(value = ["status"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val eventType: String,
    val action: String,
    val status: String,
    val details: String,
    val actor: String
) {
    fun toDomain(): AuditEvent = AuditEvent(
        id = id,
        timestamp = timestamp,
        eventType = runCatching { AuditEventType.valueOf(eventType) }.getOrDefault(AuditEventType.APP_LIFECYCLE),
        action = action,
        status = runCatching { AuditStatus.valueOf(status) }.getOrDefault(AuditStatus.INFO),
        details = details,
        actor = actor
    )

    companion object {
        fun fromDomain(event: AuditEvent): AuditLogEntity = AuditLogEntity(
            id = event.id,
            timestamp = event.timestamp,
            eventType = event.eventType.name,
            action = event.action,
            status = event.status.name,
            details = event.details,
            actor = event.actor
        )
    }
}
