package com.openpos.guardian.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.openpos.guardian.domain.model.ThemeMode
import com.openpos.guardian.domain.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "openpos_guardian_prefs")

@Singleton
class PreferencesDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    object Keys {
        val APPROVED_PACKAGES = stringSetPreferencesKey("approved_launcher_packages")
        val GRID_COLUMNS = intPreferencesKey("launcher_grid_columns")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AUTO_LOCK_TASK = booleanPreferencesKey("auto_lock_task_on_launch")
        val ADMIN_PIN = stringPreferencesKey("admin_pin_hash")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")

        // Kiosk feature preferences
        val FEATURE_SYSTEM_INFO = booleanPreferencesKey("kiosk_feat_system_info")
        val FEATURE_NOTIFICATIONS = booleanPreferencesKey("kiosk_feat_notifications")
        val FEATURE_HOME = booleanPreferencesKey("kiosk_feat_home")
        val FEATURE_GLOBAL_ACTIONS = booleanPreferencesKey("kiosk_feat_global_actions")
        val FEATURE_KEYGUARD = booleanPreferencesKey("kiosk_feat_keyguard")
    }

    val approvedPackagesFlow: Flow<Set<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[Keys.APPROVED_PACKAGES] ?: emptySet()
        }

    val userSettingsFlow: Flow<UserSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val themeStr = preferences[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val theme = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.SYSTEM)
            UserSettings(
                launcherGridColumns = preferences[Keys.GRID_COLUMNS] ?: 3,
                themeMode = theme,
                autoLockTaskOnLaunch = preferences[Keys.AUTO_LOCK_TASK] ?: false,
                adminPin = preferences[Keys.ADMIN_PIN] ?: "1234",
                lastSyncTimestamp = preferences[Keys.LAST_SYNC_TIMESTAMP] ?: 0L
            )
        }

    suspend fun addApprovedPackage(packageName: String) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.APPROVED_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            current.add(packageName)
            preferences[Keys.APPROVED_PACKAGES] = current
        }
    }

    suspend fun removeApprovedPackage(packageName: String) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.APPROVED_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            current.remove(packageName)
            preferences[Keys.APPROVED_PACKAGES] = current
        }
    }

    suspend fun setApprovedPackages(packages: Set<String>) {
        dataStore.edit { preferences ->
            preferences[Keys.APPROVED_PACKAGES] = packages
        }
    }

    suspend fun setGridColumns(columns: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.GRID_COLUMNS] = columns.coerceIn(2, 6)
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setAutoLockTask(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.AUTO_LOCK_TASK] = enabled
        }
    }

    suspend fun setAdminPin(pin: String) {
        dataStore.edit { preferences ->
            preferences[Keys.ADMIN_PIN] = pin
        }
    }

    suspend fun setLastSyncTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_SYNC_TIMESTAMP] = timestamp
        }
    }
}
