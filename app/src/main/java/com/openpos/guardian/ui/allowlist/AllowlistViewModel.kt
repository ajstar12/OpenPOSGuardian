package com.openpos.guardian.ui.allowlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openpos.guardian.domain.model.LauncherApp
import com.openpos.guardian.domain.repository.KioskRepository
import com.openpos.guardian.domain.repository.LauncherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllowlistUiState(
    val approvedApps: List<LauncherApp> = emptyList(),
    val installedApps: List<LauncherApp> = emptyList(),
    val filteredApprovedApps: List<LauncherApp> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val dialogSearchQuery: String = "",
    val feedbackMessage: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class AllowlistViewModel @Inject constructor(
    private val launcherRepository: LauncherRepository,
    private val kioskRepository: KioskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllowlistUiState())
    val uiState: StateFlow<AllowlistUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            launcherRepository.getApprovedApps().collect { approved ->
                _uiState.update { current ->
                    val filtered = if (current.searchQuery.isBlank()) {
                        approved
                    } else {
                        approved.filter {
                            it.label.contains(current.searchQuery, ignoreCase = true) ||
                                    it.packageName.contains(current.searchQuery, ignoreCase = true)
                        }
                    }
                    current.copy(
                        approvedApps = approved,
                        filteredApprovedApps = filtered,
                        isLoading = false
                    )
                }
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
            current.copy(searchQuery = query, filteredApprovedApps = filtered)
        }
    }

    fun openAddAppDialog() {
        viewModelScope.launch {
            val installed = launcherRepository.getInstalledLauncherApps()
            _uiState.update { it.copy(installedApps = installed, showAddDialog = true, dialogSearchQuery = "") }
        }
    }

    fun dismissAddAppDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun onDialogSearchChanged(query: String) {
        _uiState.update { it.copy(dialogSearchQuery = query) }
    }

    fun addApp(packageName: String) {
        viewModelScope.launch {
            val result = launcherRepository.addAppToAllowlist(packageName)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(feedbackMessage = "Added $packageName to approved launcher apps.", isError = false)
                }
            } else {
                _uiState.update {
                    it.copy(feedbackMessage = "Failed to add $packageName: ${result.exceptionOrNull()?.localizedMessage}", isError = true)
                }
            }
        }
    }

    fun removeApp(packageName: String) {
        viewModelScope.launch {
            val result = launcherRepository.removeAppFromAllowlist(packageName)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(feedbackMessage = "Removed $packageName from approved apps.", isError = false)
                }
            } else {
                _uiState.update {
                    it.copy(feedbackMessage = "Failed to remove $packageName", isError = true)
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
