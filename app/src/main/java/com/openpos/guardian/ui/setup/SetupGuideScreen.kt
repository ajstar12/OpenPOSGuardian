package com.openpos.guardian.ui.setup

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openpos.guardian.R
import com.openpos.guardian.ui.components.EnterpriseHeader

@Composable
fun SetupGuideScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val adbCommand = "adb shell dpm set-device-owner com.openpos.guardian/.device.admin.OpenPOSDeviceAdminReceiver"

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
                    title = stringResource(R.string.setup_title),
                    subtitle = stringResource(R.string.setup_subtitle),
                    icon = Icons.Default.Build
                )
            }

            // Architecture Overview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "What is Device Owner Mode?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "In Android Enterprise, Device Owner is the highest management privilege tier designed for dedicated corporate appliances, POS terminals, and self-service kiosks. Unlike standard applications, a Device Owner app can enforce true Lock-Task kiosk mode without escape gestures, configure lock-task allowed packages, suppress system status bars, and enforce security policies.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Crucial Security Notice
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Prerequisites & Zero-Account Requirement",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• The device MUST NOT have any Google accounts or work accounts configured.\n• Best practice: Use a factory-reset dedicated test device or an Android Virtual Device (AVD).\n• Enable 'Developer Options' and 'USB Debugging' via ADB.\n• Ordinary applications CANNOT silently elevate to Device Owner.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Step-by-Step Instructions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Step-by-Step ADB Provisioning",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        StepItem(
                            stepNumber = "1",
                            title = "Connect Device via USB",
                            description = "Connect your test device to your workstation and ensure ADB detects it: run 'adb devices'."
                        )
                        StepItem(
                            stepNumber = "2",
                            title = "Install OpenPOS Guardian APK",
                            description = "Build and install this debug build onto the device: run './gradlew installDebug'."
                        )
                        StepItem(
                            stepNumber = "3",
                            title = "Execute Device Owner Provisioning Command",
                            description = "Run the following shell command to register the DeviceAdminReceiver as Device Owner:"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SelectionContainer {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = adbCommand,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("ADB Command", adbCommand)
                                clipboard.setPrimaryClip(clip)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.copy_command))
                        }
                    }
                }
            }

            // Troubleshooting
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Common Provisioning Errors & Fixes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        TroubleshootItem(
                            error = "IllegalStateException: Not allowed to set the device owner because there are already some accounts",
                            resolution = "Remove all Google, email, and social accounts in Settings > Passwords & Accounts, or factory reset the device."
                        )
                        TroubleshootItem(
                            error = "IllegalStateException: User setup already complete",
                            resolution = "On Android 7.0+, if users or accounts are configured, you must remove all accounts first or provision right after factory reset."
                        )
                        TroubleshootItem(
                            error = "SecurityException: Package does not have administrative privileges",
                            resolution = "Ensure the APK is installed and the exact ComponentName matches 'com.openpos.guardian/.device.admin.OpenPOSDeviceAdminReceiver'."
                        )
                    }
                }
            }

            // De-provisioning instructions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "De-provisioning & Removing Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To remove Device Owner privileges after testing:\n" +
                                    "1. In a debug build, call DevicePolicyManager.clearDeviceOwnerApp() from code or execute:\n" +
                                    "   adb shell dpm remove-active-admin com.openpos.guardian/.device.admin.OpenPOSDeviceAdminReceiver\n" +
                                    "2. Alternatively, perform a standard factory reset from Settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepItem(stepNumber: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TroubleshootItem(error: String, resolution: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = "❌ $error", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = "💡 Fix: $resolution", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
