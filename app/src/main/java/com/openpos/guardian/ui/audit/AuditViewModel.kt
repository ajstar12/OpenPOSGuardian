package com.openpos.guardian.ui.audit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openpos.guardian.domain.model.AuditEvent
import com.openpos.guardian.domain.model.AuditEventType
import com.openpos.guardian.domain.model.AuditStatus
import com.openpos.guardian.domain.repository.AuditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import javax.inject.Inject

data class AuditUiState(
    val events: List<AuditEvent> = emptyList(),
    val filteredEvents: List<AuditEvent> = emptyList(),
    val selectedStatus: AuditStatus? = null,
    val selectedType: AuditEventType? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val feedbackMessage: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class AuditViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val auditRepository: AuditRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditUiState())
    val uiState: StateFlow<AuditUiState> = _uiState.asStateFlow()

    init {
        loadAuditLogs()
    }

    private fun loadAuditLogs() {
        viewModelScope.launch {
            auditRepository.getAllEvents().collect { allEvents ->
                _uiState.update { current ->
                    current.copy(
                        events = allEvents,
                        filteredEvents = applyFilters(allEvents, current.selectedStatus, current.selectedType, current.searchQuery),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setStatusFilter(status: AuditStatus?) {
        _uiState.update { current ->
            current.copy(
                selectedStatus = status,
                filteredEvents = applyFilters(current.events, status, current.selectedType, current.searchQuery)
            )
        }
    }

    fun setTypeFilter(type: AuditEventType?) {
        _uiState.update { current ->
            current.copy(
                selectedType = type,
                filteredEvents = applyFilters(current.events, current.selectedStatus, type, current.searchQuery)
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredEvents = applyFilters(current.events, current.selectedStatus, current.selectedType, query)
            )
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            val count = auditRepository.clearAuditLogs()
            _uiState.update {
                it.copy(feedbackMessage = "Cleared $count audit log records.", isError = false)
            }
        }
    }

    fun exportLogsToUri(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val json = auditRepository.exportLogsAsJson()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            writer.write(json)
                        }
                    }
                }
            }.onSuccess {
                _uiState.update {
                    it.copy(feedbackMessage = "Audit ledger successfully exported to file.", isError = false)
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(feedbackMessage = "Export failed: ${err.localizedMessage}", isError = true)
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    private fun applyFilters(
        events: List<AuditEvent>,
        status: AuditStatus?,
        type: AuditEventType?,
        query: String
    ): List<AuditEvent> {
        return events.filter { event ->
            val matchesStatus = status == null || event.status == status
            val matchesType = type == null || event.eventType == type
            val matchesQuery = query.isBlank() ||
                    event.action.contains(query, ignoreCase = true) ||
                    event.details.contains(query, ignoreCase = true) ||
                    event.actor.contains(query, ignoreCase = true)
            matchesStatus && matchesType && matchesQuery
        }
    }
}
