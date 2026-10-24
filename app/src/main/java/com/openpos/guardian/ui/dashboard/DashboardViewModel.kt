package com.openpos.guardian.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openpos.guardian.domain.model.AuditEvent
import com.openpos.guardian.domain.model.DeviceInfo
import com.openpos.guardian.domain.model.KioskPolicyState
import com.openpos.guardian.domain.repository.AuditRepository
import com.openpos.guardian.domain.repository.DeviceInfoRepository
import com.openpos.guardian.domain.repository.KioskRepository
import com.openpos.guardian.domain.repository.LauncherRepository
import com.openpos.guardian.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val deviceInfo: DeviceInfo? = null,
    val kioskState: KioskPolicyState? = null,
    val approvedAppsCount: Int = 0,
    val recentEvents: List<AuditEvent> = emptyList(),
    val lastSyncTime: Long = 0L,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val deviceInfoRepository: DeviceInfoRepository,
    private val kioskRepository: KioskRepository,
    private val launcherRepository: LauncherRepository,
    private val auditRepository: AuditRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            combine(
                deviceInfoRepository.getDeviceInfoFlow(),
                kioskRepository.getKioskPolicyState(),
                launcherRepository.getApprovedApps(),
                auditRepository.getRecentEvents(5),
                settingsRepository.getUserSettings()
            ) { deviceInfo, kioskState, approvedApps, recentEvents, settings ->
                DashboardUiState(
                    deviceInfo = deviceInfo,
                    kioskState = kioskState,
                    approvedAppsCount = approvedApps.size,
                    recentEvents = recentEvents,
                    lastSyncTime = settings.lastSyncTimestamp,
                    isLoading = false
                )
            }.catch {
                _uiState.update { it.copy(isLoading = false) }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val refreshed = deviceInfoRepository.refreshDeviceInfo()
            _uiState.update { it.copy(deviceInfo = refreshed, isLoading = false) }
        }
    }
}
