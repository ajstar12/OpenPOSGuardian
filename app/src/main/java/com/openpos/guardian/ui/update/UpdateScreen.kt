package com.openpos.guardian.ui.update

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openpos.guardian.R
import com.openpos.guardian.ui.components.EnterpriseHeader
import com.openpos.guardian.ui.components.InfoRow

@Composable
fun UpdateScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pInfo = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }.getOrNull()

    val versionName = pInfo?.versionName ?: "1.0.0"
    val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        pInfo?.longVersionCode ?: 1L
    } else {
        @Suppress("DEPRECATION")
        pInfo?.versionCode?.toLong() ?: 1L
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            EnterpriseHeader(
                title = stringResource(R.string.update_title),
                subtitle = "Version Verification & Enterprise Distribution Strategy",
                icon = Icons.Default.SystemUpdate
            )
        }

        // Current App Version Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Installed Binary Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoRow(label = "Application ID", value = context.packageName)
                    InfoRow(label = "Version Name", value = versionName)
                    InfoRow(label = "Version Code", value = "$versionCode")
                    InfoRow(label = "Target Android SDK", value = "API ${context.applicationInfo.targetSdkVersion}")
                    InfoRow(label = "Min Android SDK", value = "API ${context.applicationInfo.minSdkVersion}")
                }
            }
        }

        // Enterprise Distribution Architecture
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.distribution_guide_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "In an enterprise POS terminal environment, applications are deployed and updated through strictly governed channels:\n\n" +
                                "1. Enterprise MDM / EMM Solutions:\n" +
                                "   Mobile Device Management systems (e.g. Google Workspace, VMware Workspace ONE, Microsoft Intune) use Android Enterprise APIs to silently push and stage APK updates without operator intervention.\n\n" +
                                "2. ADB Sideload for Dedicated Staging:\n" +
                                "   During lab certification or technician maintenance, APKs are updated via:\n" +
                                "   adb install -r -d app-release.apk\n\n" +
                                "3. Supported PackageInstaller Sessions:\n" +
                                "   Device Owner applications can use Android's PackageInstaller API with a staged Session to install trusted updates, but ordinary apps cannot silently bypass Android installation security checks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Security Notice on Auto-Updates
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Platform Security Model Integrity",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "OpenPOS Guardian strictly adheres to Android platform security standards: it does not download arbitrary remote binaries or bypass APK signature verification. All updates require identical signing keys (CERT signature match) to preserve user and device configuration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
