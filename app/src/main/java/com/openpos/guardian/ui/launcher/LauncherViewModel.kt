package com.openpos.guardian.ui.launcher

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openpos.guardian.domain.model.AuditEventType
import com.openpos.guardian.domain.model.AuditStatus
import com.openpos.guardian.domain.model.LauncherApp
import com.openpos.guardian.domain.model.UserSettings
import com.openpos.guardian.domain.repository.AuditRepository
import com.openpos.guardian.domain.repository.KioskRepository
import com.openpos.guardian.domain.repository.LauncherRepository
import com.openpos.guardian.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LauncherUiState(
    val approvedApps: List<LauncherApp> = emptyList(),
    val filteredApps: List<LauncherApp> = emptyList(),
    val searchQuery: String = "",
    val gridColumns: Int = 3,
    val adminPin: String = "1234",
    val isKioskActive: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LauncherViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val launcherRepository: LauncherRepository,
    private val settingsRepository: SettingsRepository,
    private val kioskRepository: KioskRepository,
    private val auditRepository: AuditRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    init {
        observeLauncherData()
    }

    private fun observeLauncherData() {
        viewModelScope.launch {
            combine(
                launcherRepository.getApprovedApps(),
                settingsRepository.getUserSettings(),
                kioskRepository.getKioskPolicyState()
            ) { apps, settings, kiosk ->
                val filtered = if (_uiState.value.searchQuery.isBlank()) {
                    apps
                } else {
                    apps.filter {
                        it.label.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                                it.packageName.contains(_uiState.value.searchQuery, ignoreCase = true)
                    }
                }
                _uiState.value.copy(
                    approvedApps = apps,
                    filteredApps = filtered,
                    gridColumns = settings.launcherGridColumns,
                    adminPin = settings.adminPin,
                    isKioskActive = kiosk.lockTaskModeState != com.openpos.guardian.domain.model.LockTaskModeState.NONE
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val filtered = if (query.isBlank()) {
                current.approvedApps
            } else {
                current.approvedApps.filter {
                    it.label.contains(query, ignoreCase = true) ||
                            it.packageName.contains(query, ignoreCase = true)
                }
            }
            current.copy(searchQuery = query, filteredApps = filtered)
        }
    }

    fun launchApp(app: LauncherApp) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching {
                context.startActivity(launchIntent)
            }.onSuccess {
                viewModelScope.launch {
                    auditRepository.recordEvent(
                        eventType = AuditEventType.APP_LIFECYCLE,
                        action = "APP_LAUNCHED_FROM_KIOSK",
                        status = AuditStatus.INFO,
                        details = "Launched application: ${app.label} (${app.packageName})",
                        actor = "KIOSK_OPERATOR"
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = "Failed to launch ${app.label}: ${err.message}") }
            }
        } else {
            _uiState.update { it.copy(errorMessage = "Cannot find launchable intent for ${app.label}") }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
