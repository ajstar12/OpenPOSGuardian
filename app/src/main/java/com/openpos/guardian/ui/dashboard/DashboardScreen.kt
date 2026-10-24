package com.openpos.guardian.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openpos.guardian.R
import com.openpos.guardian.domain.model.AuditStatus
import com.openpos.guardian.domain.model.LockTaskModeState
import com.openpos.guardian.navigation.Screen
import com.openpos.guardian.ui.components.EnterpriseHeader
import com.openpos.guardian.ui.components.StatCard
import com.openpos.guardian.ui.components.StatusBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            EnterpriseHeader(
                title = stringResource(R.string.dashboard_title),
                subtitle = stringResource(R.string.dashboard_subtitle),
                icon = Icons.Default.Security
            )
        }

        if (uiState.isLoading && uiState.deviceInfo == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            val dev = uiState.deviceInfo
            val kiosk = uiState.kioskState

            // Kiosk & Device Owner Status Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isOwner = kiosk?.isDeviceOwner ?: false
                    StatCard(
                        title = "Device Owner",
                        value = if (isOwner) "ACTIVE" else "INACTIVE",
                        icon = if (isOwner) Icons.Default.VerifiedUser else Icons.Default.GppMaybe,
                        statusText = if (isOwner) "Provisioned" else "Unprovisioned",
                        status = if (isOwner) AuditStatus.SUCCESS else AuditStatus.WARNING,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.SetupGuide.route) }
                    )

                    val lockMode = kiosk?.lockTaskModeState ?: LockTaskModeState.NONE
                    val (modeText, modeStatus) = when (lockMode) {
                        LockTaskModeState.LOCKED -> "LOCKED" to AuditStatus.SUCCESS
                        LockTaskModeState.PINNED -> "PINNED" to AuditStatus.WARNING
                        LockTaskModeState.NONE -> "NORMAL" to AuditStatus.INFO
                    }
                    StatCard(
                        title = "Lock-Task State",
                        value = modeText,
                        icon = Icons.Default.Lock,
                        statusText = modeText,
                        status = modeStatus,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.Kiosk.route) }
                    )
                }
            }

            // Hardware & App Metrics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Approved POS Apps",
                        value = "${uiState.approvedAppsCount}",
                        icon = Icons.Default.Apps,
                        statusText = "Launcher Items",
                        status = AuditStatus.INFO,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.Allowlist.route) }
                    )

                    val batteryText = if ((dev?.batteryPct ?: -1) >= 0) "${dev?.batteryPct}%" else "N/A"
                    val batteryStatus = if (dev?.isCharging == true) "Charging (${dev.chargePlug})" else "Battery"
                    StatCard(
                        title = "Power & Battery",
                        value = batteryText,
                        icon = if (dev?.isCharging == true) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                        statusText = batteryStatus,
                        status = if (dev?.isCharging == true) AuditStatus.SUCCESS else AuditStatus.INFO,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.DeviceInfo.route) }
                    )
                }
            }

            // Storage & OS Metrics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val freeGb = if ((dev?.freeStorageBytes ?: -1L) >= 0) {
                        String.format(Locale.US, "%.1f GB", dev!!.freeStorageBytes / (1024.0 * 1024.0 * 1024.0))
                    } else "N/A"
                    StatCard(
                        title = "Free Storage",
                        value = freeGb,
                        icon = Icons.Default.SdCard,
                        statusText = "Internal Memory",
                        status = AuditStatus.INFO,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.DeviceInfo.route) }
                    )

                    StatCard(
                        title = "Platform OS",
                        value = "Android ${dev?.androidRelease ?: ""}",
                        icon = Icons.Default.Android,
                        statusText = "API ${dev?.apiLevel ?: ""}",
                        status = AuditStatus.INFO,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.DeviceInfo.route) }
                    )
                }
            }

            // Quick Navigation Hub
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Terminal Quick Actions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onNavigate(Screen.Launcher.route) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("POS Launcher")
                            }

                            FilledTonalButton(
                                onClick = { onNavigate(Screen.Kiosk.route) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Kiosk Controls")
                            }
                        }
                    }
                }
            }

            // Recent Audit Events
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.recent_audit_events),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { onNavigate(Screen.Audit.route) }) {
                        Text(stringResource(R.string.view_all_logs))
                    }
                }
            }

            if (uiState.recentEvents.isEmpty()) {
                item {
                    Text(
                        text = "No recent audit events recorded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(uiState.recentEvents) { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(Screen.Audit.route) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.action,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = event.details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                StatusBadge(text = event.status.name, status = event.status)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = timeFormat.format(Date(event.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
