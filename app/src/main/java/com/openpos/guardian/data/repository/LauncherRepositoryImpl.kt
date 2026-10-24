package com.openpos.guardian.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.openpos.guardian.data.local.datastore.PreferencesDataStore
import com.openpos.guardian.device.kiosk.KioskManager
import com.openpos.guardian.domain.model.AuditEventType
import com.openpos.guardian.domain.model.AuditStatus
import com.openpos.guardian.domain.model.LauncherApp
import com.openpos.guardian.domain.repository.AuditRepository
import com.openpos.guardian.domain.repository.LauncherRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LauncherRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferencesDataStore: PreferencesDataStore,
    private val kioskManager: KioskManager,
    private val auditRepository: AuditRepository
) : LauncherRepository {

    private val packageManager: PackageManager = context.packageManager

    override fun getApprovedApps(): Flow<List<LauncherApp>> {
        return preferencesDataStore.approvedPackagesFlow.map { approvedSet ->
            withContext(Dispatchers.IO) {
                val installed = queryInstalledLauncherApps()
                val kioskState = kioskManager.getKioskPolicyState()
                val lockTaskPkgs = kioskState.lockTaskPackages.toSet()

                installed.filter { approvedSet.contains(it.packageName) }
                    .map { app ->
                        app.copy(
                            isApproved = true,
                            isLockTaskPermitted = lockTaskPkgs.contains(app.packageName)
                        )
                    }
            }
        }
    }

    override suspend fun getInstalledLauncherApps(): List<LauncherApp> = withContext(Dispatchers.IO) {
        val approvedSet = preferencesDataStore.approvedPackagesFlow.first()
        val kioskState = kioskManager.getKioskPolicyState()
        val lockTaskPkgs = kioskState.lockTaskPackages.toSet()

        queryInstalledLauncherApps().map { app ->
            app.copy(
                isApproved = approvedSet.contains(app.packageName),
                isLockTaskPermitted = lockTaskPkgs.contains(app.packageName)
            )
        }
    }

    override suspend fun addAppToAllowlist(packageName: String): Result<Unit> {
        return runCatching {
            // Validate package existence
            packageManager.getPackageInfo(packageName, 0)
            preferencesDataStore.addApprovedPackage(packageName)
            auditRepository.recordEvent(
                eventType = AuditEventType.ALLOWLIST_MODIFIED,
                action = "APP_ADDED_TO_ALLOWLIST",
                status = AuditStatus.SUCCESS,
                details = "Approved package added to POS launcher allowlist: $packageName",
                actor = "USER_ADMIN"
            )
        }
    }

    override suspend fun removeAppFromAllowlist(packageName: String): Result<Unit> {
        return runCatching {
            preferencesDataStore.removeApprovedPackage(packageName)
            auditRepository.recordEvent(
                eventType = AuditEventType.ALLOWLIST_MODIFIED,
                action = "APP_REMOVED_FROM_ALLOWLIST",
                status = AuditStatus.SUCCESS,
                details = "Package removed from POS launcher allowlist: $packageName",
                actor = "USER_ADMIN"
            )
        }
    }

    override suspend fun isAppApproved(packageName: String): Boolean {
        val approvedSet = preferencesDataStore.approvedPackagesFlow.first()
        return approvedSet.contains(packageName)
    }

    private fun queryInstalledLauncherApps(): List<LauncherApp> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_ALL
        )

        val ownPackage = context.packageName

        return resolveInfoList
            .filter { it.activityInfo.packageName != ownPackage }
            .map { info ->
                val pkgName = info.activityInfo.packageName
                val actName = info.activityInfo.name
                val label = info.loadLabel(packageManager).toString()
                LauncherApp(
                    packageName = pkgName,
                    activityName = actName,
                    label = label,
                    isApproved = false,
                    isLockTaskPermitted = false
                )
            }
            .sortedBy { it.label.lowercase() }
    }
}
