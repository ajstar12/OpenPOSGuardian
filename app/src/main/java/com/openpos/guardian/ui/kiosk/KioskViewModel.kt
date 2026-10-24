package com.openpos.guardian.ui.kiosk

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openpos.guardian.domain.model.KioskPolicyState
import com.openpos.guardian.domain.model.LockTaskModeState
import com.openpos.guardian.domain.repository.KioskRepository
import com.openpos.guardian.domain.repository.LauncherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KioskUiState(
    val kioskPolicyState: KioskPolicyState? = null,
    val approvedLauncherAppsCount: Int = 0,
    val isExecuting: Boolean = false,
    val feedbackMessage: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class KioskViewModel @Inject constructor(
    private val kioskRepository: KioskRepository,
    private val launcherRepository: LauncherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KioskUiState())
    val uiState: StateFlow<KioskUiState> = _uiState.asStateFlow()

    init {
        observeKioskState()
    }

    private fun observeKioskState() {
        viewModelScope.launch {
            combine(
                kioskRepository.getKioskPolicyState(),
                launcherRepository.getApprovedApps()
            ) { policyState, approvedApps ->
                _uiState.value.copy(
                    kioskPolicyState = policyState,
                    approvedLauncherAppsCount = approvedApps.size
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun startKioskMode(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true) }
            val result = kioskRepository.enterLockTaskMode(activity)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isExecuting = false,
                        feedbackMessage = "Kiosk mode requested successfully.",
                        isError = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isExecuting = false,
                        feedbackMessage = "Failed to enter kiosk mode: ${result.exceptionOrNull()?.localizedMessage}",
                        isError = true
                    )
                }
            }
        }
    }

    fun stopKioskMode(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true) }
            val result = kioskRepository.exitLockTaskMode(activity)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isExecuting = false,
                        feedbackMessage = "Exited kiosk mode successfully.",
                        isError = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isExecuting = false,
                        feedbackMessage = "Failed to exit kiosk mode: ${result.exceptionOrNull()?.localizedMessage}",
                        isError = true
                    )
                }
            }
        }
    }

    fun syncAllowlistToLockTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true) }
            val approved = launcherRepository.getApprovedApps().first()
            val packages = approved.map { it.packageName }
            val result = kioskRepository.syncLockTaskPackages(packages)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isExecuting = false,
                        feedbackMessage = "Synced ${packages.size} approved packages to Device Policy lock-task list.",
                        isError = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isExecuting = false,
                        feedbackMessage = "Policy sync failed: ${result.exceptionOrNull()?.localizedMessage}. Device Owner is required.",
                        isError = true
                    )
                }
            }
        }
    }

    fun updateFeature(
        systemInfo: Boolean,
        notifications: Boolean,
        home: Boolean,
        globalActions: Boolean,
        keyguard: Boolean
    ) {
        viewModelScope.launch {
            val result = kioskRepository.updateKioskFeatures(
                systemInfo = systemInfo,
                notifications = notifications,
                home = home,
                globalActions = globalActions,
                keyguard = keyguard
            )
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(feedbackMessage = "Kiosk features updated successfully.", isError = false)
                }
            } else {
                _uiState.update {
                    it.copy(feedbackMessage = "Feature update failed: ${result.exceptionOrNull()?.localizedMessage}", isError = true)
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
