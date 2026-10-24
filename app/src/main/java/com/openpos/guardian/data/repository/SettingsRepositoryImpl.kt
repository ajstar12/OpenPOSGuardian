package com.openpos.guardian.data.repository

import com.openpos.guardian.data.local.datastore.PreferencesDataStore
import com.openpos.guardian.domain.model.AuditEventType
import com.openpos.guardian.domain.model.AuditStatus
import com.openpos.guardian.domain.model.ThemeMode
import com.openpos.guardian.domain.model.UserSettings
import com.openpos.guardian.domain.repository.AuditRepository
import com.openpos.guardian.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val auditRepository: AuditRepository
) : SettingsRepository {

    override fun getUserSettings(): Flow<UserSettings> = preferencesDataStore.userSettingsFlow

    override suspend fun setThemeMode(mode: ThemeMode) {
        preferencesDataStore.setThemeMode(mode)
        auditRepository.recordEvent(
            eventType = AuditEventType.CONFIG_CHANGE,
            action = "THEME_CHANGED",
            status = AuditStatus.INFO,
            details = "Terminal theme appearance set to: ${mode.name}",
            actor = "USER_ADMIN"
        )
    }

    override suspend fun setGridColumns(columns: Int) {
        preferencesDataStore.setGridColumns(columns)
        auditRepository.recordEvent(
            eventType = AuditEventType.CONFIG_CHANGE,
            action = "GRID_COLUMNS_CHANGED",
            status = AuditStatus.INFO,
            details = "Launcher grid columns set to: $columns",
            actor = "USER_ADMIN"
        )
    }

    override suspend fun setAutoLockTask(enabled: Boolean) {
        preferencesDataStore.setAutoLockTask(enabled)
        auditRepository.recordEvent(
            eventType = AuditEventType.CONFIG_CHANGE,
            action = "AUTO_LOCK_TASK_TOGGLED",
            status = AuditStatus.INFO,
            details = "Auto lock-task on launch set to: $enabled",
            actor = "USER_ADMIN"
        )
    }

    override suspend fun setAdminPin(pin: String) {
        preferencesDataStore.setAdminPin(pin)
        auditRepository.recordEvent(
            eventType = AuditEventType.SECURITY_ALERT,
            action = "ADMIN_PIN_UPDATED",
            status = AuditStatus.WARNING,
            details = "Admin access PIN successfully modified.",
            actor = "USER_ADMIN"
        )
    }

    override suspend fun updateLastSync(timestamp: Long) {
        preferencesDataStore.setLastSyncTimestamp(timestamp)
    }
}
