package com.openpos.guardian.fakes

import android.app.Activity
import com.openpos.guardian.domain.model.*
import com.openpos.guardian.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class FakeAuditRepository : AuditRepository {
    private val events = mutableListOf<AuditEvent>()
    private val eventsFlow = MutableStateFlow<List<AuditEvent>>(emptyList())
    private var nextId = 1L

    override fun getAllEvents(): Flow<List<AuditEvent>> = eventsFlow

    override fun getFilteredEvents(
        types: Set<AuditEventType>?,
        statuses: Set<AuditStatus>?
    ): Flow<List<AuditEvent>> {
        return eventsFlow.map { list ->
            list.filter { event ->
                val matchType = types.isNullOrEmpty() || types.contains(event.eventType)
                val matchStatus = statuses.isNullOrEmpty() || statuses.contains(event.status)
                matchType && matchStatus
            }
        }
    }

    override fun getRecentEvents(limit: Int): Flow<List<AuditEvent>> {
        return eventsFlow.map { it.take(limit) }
    }

    override suspend fun recordEvent(
        eventType: AuditEventType,
        action: String,
        status: AuditStatus,
        details: String,
        actor: String
    ): Long {
        val event = AuditEvent(
            id = nextId++,
            timestamp = System.currentTimeMillis(),
            eventType = eventType,
            action = action,
            status = status,
            details = details,
            actor = actor
        )
        events.add(0, event)
        eventsFlow.value = events.toList()
        return event.id
    }

    override suspend fun clearAuditLogs(): Int {
        val count = events.size
        events.clear()
        eventsFlow.value = emptyList()
        return count
    }

    override suspend fun exportLogsAsJson(): String {
        return """
            {
                "app": "OpenPOS Guardian",
                "totalRecords": ${events.size},
                "records": [
                    ${events.joinToString(",") { e ->
                        """{"id": ${e.id}, "action": "${e.action}", "status": "${e.status.name}", "details": "${e.details}"}"""
                    }}
                ]
            }
        """.trimIndent()
    }
}

class FakeSettingsRepository : SettingsRepository {
    private val settingsFlow = MutableStateFlow(UserSettings())

    override fun getUserSettings(): Flow<UserSettings> = settingsFlow

    override suspend fun setThemeMode(mode: ThemeMode) {
        settingsFlow.value = settingsFlow.value.copy(themeMode = mode)
    }

    override suspend fun setGridColumns(columns: Int) {
        settingsFlow.value = settingsFlow.value.copy(launcherGridColumns = columns.coerceIn(2, 6))
    }

    override suspend fun setAutoLockTask(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(autoLockTaskOnLaunch = enabled)
    }

    override suspend fun setAdminPin(pin: String) {
        settingsFlow.value = settingsFlow.value.copy(adminPin = pin)
    }

    override suspend fun updateLastSync(timestamp: Long) {
        settingsFlow.value = settingsFlow.value.copy(lastSyncTimestamp = timestamp)
    }
}

class FakeLauncherRepository : LauncherRepository {
    private val installedApps = mutableListOf<LauncherApp>()
    private val approvedPackages = mutableSetOf<String>()
    private val approvedAppsFlow = MutableStateFlow<List<LauncherApp>>(emptyList())

    fun setInstalledApps(apps: List<LauncherApp>) {
        installedApps.clear()
        installedApps.addAll(apps)
        refreshApprovedFlow()
    }

    override fun getApprovedApps(): Flow<List<LauncherApp>> = approvedAppsFlow

    override suspend fun getInstalledLauncherApps(): List<LauncherApp> {
        return installedApps.map { it.copy(isApproved = approvedPackages.contains(it.packageName)) }
    }

    override suspend fun addAppToAllowlist(packageName: String): Result<Unit> {
        if (installedApps.none { it.packageName == packageName }) {
            return Result.failure(IllegalArgumentException("Package not installed: $packageName"))
        }
        approvedPackages.add(packageName)
        refreshApprovedFlow()
        return Result.success(Unit)
    }

    override suspend fun removeAppFromAllowlist(packageName: String): Result<Unit> {
        approvedPackages.remove(packageName)
        refreshApprovedFlow()
        return Result.success(Unit)
    }

    override suspend fun isAppApproved(packageName: String): Boolean {
        return approvedPackages.contains(packageName)
    }

    private fun refreshApprovedFlow() {
        val approved = installedApps
            .filter { approvedPackages.contains(it.packageName) }
            .map { it.copy(isApproved = true) }
        approvedAppsFlow.value = approved
    }
}

class FakeKioskRepository : KioskRepository {
    var isDeviceOwner: Boolean = false
    var currentMode: LockTaskModeState = LockTaskModeState.NONE
    private val lockTaskPackages = mutableListOf<String>()

    private val policyFlow = MutableStateFlow(
        KioskPolicyState(
            isDeviceOwner = false,
            isProfileOwner = false,
            isAdminActive = false,
            lockTaskModeState = LockTaskModeState.NONE,
            lockTaskPackages = emptyList(),
            isStatusBarDisabled = false,
            isKeyguardDisabled = false,
            systemInfoFeature = true,
            notificationsFeature = false,
            homeFeature = false,
            globalActionsFeature = true,
            keyguardFeature = false
        )
    )

    override fun getKioskPolicyState(): Flow<KioskPolicyState> = policyFlow

    override suspend fun enterLockTaskMode(activity: Activity): Result<Unit> {
        currentMode = if (isDeviceOwner) LockTaskModeState.LOCKED else LockTaskModeState.PINNED
        updateFlow()
        return Result.success(Unit)
    }

    override suspend fun exitLockTaskMode(activity: Activity): Result<Unit> {
        currentMode = LockTaskModeState.NONE
        updateFlow()
        return Result.success(Unit)
    }

    override suspend fun syncLockTaskPackages(packages: List<String>): Result<Unit> {
        if (!isDeviceOwner) {
            return Result.failure(SecurityException("Device Owner privilege required"))
        }
        lockTaskPackages.clear()
        lockTaskPackages.addAll(packages)
        updateFlow()
        return Result.success(Unit)
    }

    override suspend fun updateKioskFeatures(
        systemInfo: Boolean,
        notifications: Boolean,
        home: Boolean,
        globalActions: Boolean,
        keyguard: Boolean
    ): Result<Unit> {
        if (!isDeviceOwner) {
            return Result.failure(SecurityException("Device Owner privilege required"))
        }
        return Result.success(Unit)
    }

    override suspend fun setStatusBarDisabled(disabled: Boolean): Result<Unit> {
        if (!isDeviceOwner) {
            return Result.failure(SecurityException("Device Owner privilege required"))
        }
        return Result.success(Unit)
    }

    override suspend fun setKeyguardDisabled(disabled: Boolean): Result<Unit> {
        if (!isDeviceOwner) {
            return Result.failure(SecurityException("Device Owner privilege required"))
        }
        return Result.success(Unit)
    }

    private fun updateFlow() {
        policyFlow.value = policyFlow.value.copy(
            isDeviceOwner = isDeviceOwner,
            lockTaskModeState = currentMode,
            lockTaskPackages = lockTaskPackages.toList()
        )
    }
}
