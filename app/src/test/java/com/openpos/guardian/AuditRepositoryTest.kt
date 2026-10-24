package com.openpos.guardian

import com.openpos.guardian.domain.model.AuditEventType
import com.openpos.guardian.domain.model.AuditStatus
import com.openpos.guardian.fakes.FakeAuditRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AuditRepositoryTest {

    private lateinit var auditRepository: FakeAuditRepository

    @Before
    fun setup() {
        auditRepository = FakeAuditRepository()
    }

    @Test
    fun recordEvent_insertsAndRetrievesEventSuccessfully() = runTest {
        val eventId = auditRepository.recordEvent(
            eventType = AuditEventType.POLICY_ENFORCEMENT,
            action = "DEVICE_ADMIN_ENABLED",
            status = AuditStatus.SUCCESS,
            details = "Device admin granted",
            actor = "ADMIN_PROVISIONING"
        )

        assertTrue(eventId > 0)
        val events = auditRepository.getAllEvents().first()
        assertEquals(1, events.size)
        assertEquals("DEVICE_ADMIN_ENABLED", events[0].action)
        assertEquals(AuditStatus.SUCCESS, events[0].status)
        assertEquals(AuditEventType.POLICY_ENFORCEMENT, events[0].eventType)
    }

    @Test
    fun filterEvents_filtersByTypeAndStatusCorrectly() = runTest {
        auditRepository.recordEvent(
            eventType = AuditEventType.APP_LIFECYCLE,
            action = "APP_START",
            status = AuditStatus.INFO,
            details = "App started"
        )
        auditRepository.recordEvent(
            eventType = AuditEventType.LOCK_TASK_STATE,
            action = "LOCK_TASK_FAILED",
            status = AuditStatus.FAILURE,
            details = "Permission denied"
        )
        auditRepository.recordEvent(
            eventType = AuditEventType.LOCK_TASK_STATE,
            action = "LOCK_TASK_ENTERED",
            status = AuditStatus.SUCCESS,
            details = "Kiosk mode locked"
        )

        // Filter only FAILURE events
        val failures = auditRepository.getFilteredEvents(
            types = null,
            statuses = setOf(AuditStatus.FAILURE)
        ).first()
        assertEquals(1, failures.size)
        assertEquals("LOCK_TASK_FAILED", failures[0].action)

        // Filter by LOCK_TASK_STATE type
        val lockTaskEvents = auditRepository.getFilteredEvents(
            types = setOf(AuditEventType.LOCK_TASK_STATE),
            statuses = null
        ).first()
        assertEquals(2, lockTaskEvents.size)
    }

    @Test
    fun clearAuditLogs_removesAllEvents() = runTest {
        auditRepository.recordEvent(
            eventType = AuditEventType.CONFIG_CHANGE,
            action = "THEME_CHANGED",
            status = AuditStatus.INFO,
            details = "Dark mode"
        )
        auditRepository.recordEvent(
            eventType = AuditEventType.SECURITY_ALERT,
            action = "PIN_CHANGED",
            status = AuditStatus.WARNING,
            details = "PIN updated"
        )

        val beforeClear = auditRepository.getAllEvents().first()
        assertEquals(2, beforeClear.size)

        val deletedCount = auditRepository.clearAuditLogs()
        assertEquals(2, deletedCount)

        val afterClear = auditRepository.getAllEvents().first()
        assertTrue(afterClear.isEmpty())
    }

    @Test
    fun exportLogsAsJson_producesValidJsonStructure() = runTest {
        auditRepository.recordEvent(
            eventType = AuditEventType.ALLOWLIST_MODIFIED,
            action = "APP_ADDED",
            status = AuditStatus.SUCCESS,
            details = "Added com.example.pos"
        )

        val json = auditRepository.exportLogsAsJson()
        assertTrue(json.contains("OpenPOS Guardian"))
        assertTrue(json.contains("com.example.pos"))
        assertTrue(json.contains("APP_ADDED"))
    }
}
