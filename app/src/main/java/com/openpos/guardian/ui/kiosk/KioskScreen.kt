package com.openpos.guardian.ui.kiosk

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openpos.guardian.R
import com.openpos.guardian.domain.model.AuditStatus
import com.openpos.guardian.domain.model.LockTaskModeState
import com.openpos.guardian.ui.components.ConfirmationDialog
import com.openpos.guardian.ui.components.EnterpriseHeader
import com.openpos.guardian.ui.components.StatusBadge

@Composable
fun KioskScreen(
    viewModel: KioskViewModel,
    onNavigateToSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    var showExitDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    val state = uiState.kioskPolicyState

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                EnterpriseHeader(
                    title = stringResource(R.string.kiosk_status_title),
                    subtitle = "Dedicated Device & Lock-Task Policy Engine",
                    icon = Icons.Default.Lock
                )
            }

            // Device Owner Notice Banner if not active
            if (state?.isDeviceOwner == false) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Device Owner Privileges Required",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Without Device Owner provisioning, Android restricts kiosk mode to ordinary Screen Pinning (which prompts users and permits manual exit via gesture). Whitelisting lock-task packages requires ADB provisioning.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onNavigateToSetup,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Open Setup Guide")
                            }
                        }
                    }
                }
            }

            // Current Lock-Task Mode Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current State",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val mode = state?.lockTaskModeState ?: LockTaskModeState.NONE
                            val (badgeText, badgeStatus) = when (mode) {
                                LockTaskModeState.LOCKED -> "LOCKED (Enterprise Kiosk)" to AuditStatus.SUCCESS
                                LockTaskModeState.PINNED -> "PINNED (Screen Pinning)" to AuditStatus.WARNING
                                LockTaskModeState.NONE -> "NORMAL (Unpinned)" to AuditStatus.INFO
                            }
                            StatusBadge(text = badgeText, status = badgeStatus)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val mode = state?.lockTaskModeState ?: LockTaskModeState.NONE
                        if (mode == LockTaskModeState.NONE) {
                            Text(
                                text = stringResource(R.string.start_kiosk_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { activity?.let { viewModel.startKioskMode(it) } },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isExecuting
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.enter_lock_task))
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.stop_kiosk_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showExitDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                enabled = !uiState.isExecuting
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.exit_lock_task))
                            }
                        }
                    }
                }
            }

            // Sync Allowlist to Lock-Task Policy
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Lock-Task Package Whitelist",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Registers approved applications with DevicePolicyManager. Only whitelisted packages can run seamlessly in Lock-Task mode without user interruption or escape dialogs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.syncAllowlistToLockTask() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isExecuting
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.sync_allowlist_to_locktask))
                        }
                    }
                }
            }

            // Active LockTask Packages
            item {
                Text(
                    text = "Provisioned Lock-Task Packages (${state?.lockTaskPackages?.size ?: 0})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state?.lockTaskPackages.isNullOrEmpty()) {
                item {
                    Text(
                        text = "No packages currently provisioned in DevicePolicyManager lock-task list.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(state?.lockTaskPackages ?: emptyList()) { pkg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = pkg,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    ConfirmationDialog(
        show = showExitDialog,
        title = "Exit Dedicated Kiosk Mode?",
        message = "Terminating Lock-Task mode will return the terminal to ordinary Android operation and restore system navigation buttons.",
        confirmText = "Exit Kiosk",
        dismissText = "Keep Locked",
        onConfirm = { activity?.let { viewModel.stopKioskMode(it) } },
        onDismiss = { showExitDialog = false }
    )
}
