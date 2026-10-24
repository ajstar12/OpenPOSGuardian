package com.openpos.guardian.ui.deviceinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openpos.guardian.R
import com.openpos.guardian.domain.model.AuditStatus
import com.openpos.guardian.ui.components.EnterpriseHeader
import com.openpos.guardian.ui.components.InfoRow
import com.openpos.guardian.ui.components.StatusBadge
import java.util.Locale

@Composable
fun DeviceInfoScreen(
    viewModel: DeviceInfoViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val dev = uiState.deviceInfo

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            EnterpriseHeader(
                title = stringResource(R.string.device_info_title),
                subtitle = "Hardware Diagnostics & Enterprise Attributes",
                icon = Icons.Default.Info
            )
        }

        if (dev != null) {
            // Enterprise Management
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.section_enterprise),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.is_device_owner), style = MaterialTheme.typography.bodyMedium)
                            StatusBadge(
                                text = if (dev.isDeviceOwner) "ACTIVE (Owner)" else "INACTIVE",
                                status = if (dev.isDeviceOwner) AuditStatus.SUCCESS else AuditStatus.WARNING
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.is_profile_owner), style = MaterialTheme.typography.bodyMedium)
                            StatusBadge(
                                text = if (dev.isProfileOwner) "ACTIVE" else "INACTIVE",
                                status = if (dev.isProfileOwner) AuditStatus.SUCCESS else AuditStatus.INFO
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.is_admin_active), style = MaterialTheme.typography.bodyMedium)
                            StatusBadge(
                                text = if (dev.isAdminActive) "ACTIVE" else "INACTIVE",
                                status = if (dev.isAdminActive) AuditStatus.SUCCESS else AuditStatus.WARNING
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow(label = "Lock-Task State", value = dev.lockTaskModeState.name)
                    }
                }
            }

            // Platform & OS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.section_platform_os),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        InfoRow(label = stringResource(R.string.android_version), value = "Android ${dev.androidRelease}")
                        InfoRow(label = stringResource(R.string.api_level), value = "API ${dev.apiLevel}")
                        InfoRow(label = stringResource(R.string.security_patch), value = dev.securityPatch)
                        InfoRow(label = stringResource(R.string.build_fingerprint), value = dev.buildId)
                    }
                }
            }

            // Hardware Specs
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.section_device_identity),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        InfoRow(label = stringResource(R.string.manufacturer), value = dev.manufacturer)
                        InfoRow(label = stringResource(R.string.model), value = dev.model)
                        InfoRow(label = stringResource(R.string.device_brand), value = "${dev.brand} / ${dev.board}")
                        InfoRow(label = "Hardware Chipset", value = dev.hardware)

                        val hours = dev.uptimeMillis / (1000 * 60 * 60)
                        val minutes = (dev.uptimeMillis / (1000 * 60)) % 60
                        InfoRow(label = stringResource(R.string.uptime), value = "$hours h $minutes m")
                    }
                }
            }

            // Power & Battery
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.section_battery_power),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val battPct = if (dev.batteryPct >= 0) "${dev.batteryPct}%" else "Unavailable"
                        InfoRow(label = stringResource(R.string.battery_level), value = battPct)
                        InfoRow(label = stringResource(R.string.battery_status), value = dev.chargePlug)
                        InfoRow(label = stringResource(R.string.battery_health), value = dev.batteryHealth)
                    }
                }
            }

            // Storage Metrics
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.section_storage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val totalGb = if (dev.totalStorageBytes >= 0) {
                            String.format(Locale.US, "%.2f GB", dev.totalStorageBytes / (1024.0 * 1024.0 * 1024.0))
                        } else "Unavailable"
                        val freeGb = if (dev.freeStorageBytes >= 0) {
                            String.format(Locale.US, "%.2f GB", dev.freeStorageBytes / (1024.0 * 1024.0 * 1024.0))
                        } else "Unavailable"

                        InfoRow(label = stringResource(R.string.total_storage), value = totalGb)
                        InfoRow(label = stringResource(R.string.free_storage), value = freeGb)
                    }
                }
            }
        }
    }
}
