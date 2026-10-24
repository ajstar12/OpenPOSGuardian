package com.openpos.guardian.ui.audit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openpos.guardian.R
import com.openpos.guardian.domain.model.AuditEvent
import com.openpos.guardian.domain.model.AuditStatus
import com.openpos.guardian.ui.components.ConfirmationDialog
import com.openpos.guardian.ui.components.EmptyStateView
import com.openpos.guardian.ui.components.EnterpriseHeader
import com.openpos.guardian.ui.components.StatusBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditScreen(
    viewModel: AuditViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportLogsToUri(it) }
    }

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    val timeFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EnterpriseHeader(
                title = stringResource(R.string.audit_title),
                subtitle = stringResource(R.string.audit_subtitle),
                icon = Icons.Default.Security
            )

            // Search & Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::setSearchQuery,
                    placeholder = { Text("Filter logs…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                FilledTonalButton(
                    onClick = {
                        val filename = "openpos_audit_${System.currentTimeMillis()}.json"
                        exportLauncher.launch(filename)
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.export_logs))
                }

                IconButton(
                    onClick = { showClearDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear Logs",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedStatus == null,
                    onClick = { viewModel.setStatusFilter(null) },
                    label = { Text("All (${uiState.events.size})") }
                )
                FilterChip(
                    selected = uiState.selectedStatus == AuditStatus.SUCCESS,
                    onClick = { viewModel.setStatusFilter(if (uiState.selectedStatus == AuditStatus.SUCCESS) null else AuditStatus.SUCCESS) },
                    label = { Text("Success") }
                )
                FilterChip(
                    selected = uiState.selectedStatus == AuditStatus.FAILURE,
                    onClick = { viewModel.setStatusFilter(if (uiState.selectedStatus == AuditStatus.FAILURE) null else AuditStatus.FAILURE) },
                    label = { Text("Failure") }
                )
                FilterChip(
                    selected = uiState.selectedStatus == AuditStatus.WARNING,
                    onClick = { viewModel.setStatusFilter(if (uiState.selectedStatus == AuditStatus.WARNING) null else AuditStatus.WARNING) },
                    label = { Text("Warning") }
                )
                FilterChip(
                    selected = uiState.selectedStatus == AuditStatus.INFO,
                    onClick = { viewModel.setStatusFilter(if (uiState.selectedStatus == AuditStatus.INFO) null else AuditStatus.INFO) },
                    label = { Text("Info") }
                )
            }

            // Audit Records List
            if (uiState.filteredEvents.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.History,
                    title = "No Audit Records",
                    message = stringResource(R.string.no_audit_logs)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredEvents, key = { it.id }) { event ->
                        AuditLogItem(event = event, timeFormat = timeFormat)
                    }
                }
            }
        }
    }

    ConfirmationDialog(
        show = showClearDialog,
        title = stringResource(R.string.confirm_clear_logs_title),
        message = stringResource(R.string.confirm_clear_logs_message),
        confirmText = stringResource(R.string.clear_logs),
        dismissText = stringResource(R.string.cancel),
        onConfirm = viewModel::clearAllLogs,
        onDismiss = { showClearDialog = false }
    )
}

@Composable
fun AuditLogItem(
    event: AuditEvent,
    timeFormat: SimpleDateFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.action,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.eventType.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusBadge(text = event.status.name, status = event.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = event.details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Actor: ${event.actor}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timeFormat.format(Date(event.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
