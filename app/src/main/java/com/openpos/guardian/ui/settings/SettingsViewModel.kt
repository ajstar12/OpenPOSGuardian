package com.openpos.guardian.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openpos.guardian.domain.model.ThemeMode
import com.openpos.guardian.domain.model.UserSettings
import com.openpos.guardian.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userSettings: UserSettings = UserSettings(),
    val feedbackMessage: String? = null,
    val showPinChangeDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getUserSettings().collect { settings ->
                _uiState.update { it.copy(userSettings = settings) }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setGridColumns(columns: Int) {
        viewModelScope.launch {
            settingsRepository.setGridColumns(columns)
        }
    }

    fun setAutoLockTask(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoLockTask(enabled)
        }
    }

    fun updatePin(newPin: String) {
        viewModelScope.launch {
            settingsRepository.setAdminPin(newPin)
            _uiState.update { it.copy(showPinChangeDialog = false, feedbackMessage = "Admin PIN updated successfully.") }
        }
    }

    fun openPinDialog() {
        _uiState.update { it.copy(showPinChangeDialog = true) }
    }

    fun dismissPinDialog() {
        _uiState.update { it.copy(showPinChangeDialog = false) }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
