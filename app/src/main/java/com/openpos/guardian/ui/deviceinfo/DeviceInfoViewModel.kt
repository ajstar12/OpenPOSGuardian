package com.openpos.guardian.ui.deviceinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openpos.guardian.domain.model.DeviceInfo
import com.openpos.guardian.domain.repository.DeviceInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceInfoUiState(
    val deviceInfo: DeviceInfo? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class DeviceInfoViewModel @Inject constructor(
    private val deviceInfoRepository: DeviceInfoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceInfoUiState())
    val uiState: StateFlow<DeviceInfoUiState> = _uiState.asStateFlow()

    init {
        loadDeviceInfo()
    }

    private fun loadDeviceInfo() {
        viewModelScope.launch {
            deviceInfoRepository.getDeviceInfoFlow().collect { info ->
                _uiState.update { it.copy(deviceInfo = info, isLoading = false) }
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
