package com.openpos.guardian.domain.model

enum class AuditEventType {
    APP_LIFECYCLE,
    CONFIG_CHANGE,
    ALLOWLIST_MODIFIED,
    LOCK_TASK_STATE,
    POLICY_ENFORCEMENT,
    SECURITY_ALERT,
    ADMIN_AUTH
}

enum class AuditStatus {
    SUCCESS,
    FAILURE,
    WARNING,
    INFO
}

data class AuditEvent(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: AuditEventType,
    val action: String,
    val status: AuditStatus,
    val details: String,
    val actor: String = "SYSTEM"
)

enum class LockTaskModeState {
    NONE,
    PINNED,
    LOCKED
}

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val brand: String,
    val board: String,
    val hardware: String,
    val androidRelease: String,
    val apiLevel: Int,
    val securityPatch: String,
    val buildId: String,
    val batteryPct: Int,
    val isCharging: Boolean,
    val chargePlug: String,
    val batteryHealth: String,
    val totalStorageBytes: Long,
    val freeStorageBytes: Long,
    val uptimeMillis: Long,
    val isDeviceOwner: Boolean,
    val isProfileOwner: Boolean,
    val isAdminActive: Boolean,
    val lockTaskModeState: LockTaskModeState
)

data class KioskPolicyState(
    val isDeviceOwner: Boolean,
    val isProfileOwner: Boolean,
    val isAdminActive: Boolean,
    val lockTaskModeState: LockTaskModeState,
    val lockTaskPackages: List<String>,
    val isStatusBarDisabled: Boolean,
    val isKeyguardDisabled: Boolean,
    val systemInfoFeature: Boolean,
    val notificationsFeature: Boolean,
    val homeFeature: Boolean,
    val globalActionsFeature: Boolean,
    val keyguardFeature: Boolean
)

data class LauncherApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val isApproved: Boolean,
    val isLockTaskPermitted: Boolean = false
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserSettings(
    val launcherGridColumns: Int = 3,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val autoLockTaskOnLaunch: Boolean = false,
    val adminPin: String = "1234",
    val lastSyncTimestamp: Long = 0L
)
