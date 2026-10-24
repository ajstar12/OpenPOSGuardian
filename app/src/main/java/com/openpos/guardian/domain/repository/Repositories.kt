package com.openpos.guardian.domain.repository

import android.app.Activity
import com.openpos.guardian.domain.model.AuditEvent
import com.openpos.guardian.domain.model.AuditEventType
import com.openpos.guardian.domain.model.AuditStatus
import com.openpos.guardian.domain.model.DeviceInfo
import com.openpos.guardian.domain.model.KioskPolicyState
import com.openpos.guardian.domain.model.LauncherApp
import com.openpos.guardian.domain.model.ThemeMode
import com.openpos.guardian.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface AuditRepository {
    fun getAllEvents(): Flow<List<AuditEvent>>
    fun getFilteredEvents(types: Set<AuditEventType>?, statuses: Set<AuditStatus>?): Flow<List<AuditEvent>>
    fun getRecentEvents(limit: Int): Flow<List<AuditEvent>>
    suspend fun recordEvent(
        eventType: AuditEventType,
        action: String,
        status: AuditStatus,
        details: String,
        actor: String = "SYSTEM"
    ): Long
    suspend fun clearAuditLogs(): Int
    suspend fun exportLogsAsJson(): String
}

interface DeviceInfoRepository {
    fun getDeviceInfoFlow(): Flow<DeviceInfo>
    suspend fun refreshDeviceInfo(): DeviceInfo
}

interface KioskRepository {
    fun getKioskPolicyState(): Flow<KioskPolicyState>
    suspend fun enterLockTaskMode(activity: Activity): Result<Unit>
    suspend fun exitLockTaskMode(activity: Activity): Result<Unit>
    suspend fun syncLockTaskPackages(packages: List<String>): Result<Unit>
    suspend fun updateKioskFeatures(
        systemInfo: Boolean,
        notifications: Boolean,
        home: Boolean,
        globalActions: Boolean,
        keyguard: Boolean
    ): Result<Unit>
    suspend fun setStatusBarDisabled(disabled: Boolean): Result<Unit>
    suspend fun setKeyguardDisabled(disabled: Boolean): Result<Unit>
}

interface LauncherRepository {
    fun getApprovedApps(): Flow<List<LauncherApp>>
    suspend fun getInstalledLauncherApps(): List<LauncherApp>
    suspend fun addAppToAllowlist(packageName: String): Result<Unit>
    suspend fun removeAppFromAllowlist(packageName: String): Result<Unit>
    suspend fun isAppApproved(packageName: String): Boolean
}

interface SettingsRepository {
    fun getUserSettings(): Flow<UserSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setGridColumns(columns: Int)
    suspend fun setAutoLockTask(enabled: Boolean)
    suspend fun setAdminPin(pin: String)
    suspend fun updateLastSync(timestamp: Long)
}
