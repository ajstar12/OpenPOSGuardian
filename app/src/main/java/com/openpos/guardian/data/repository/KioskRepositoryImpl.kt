package com.openpos.guardian.data.repository

import android.app.Activity
import com.openpos.guardian.device.kiosk.KioskManager
import com.openpos.guardian.domain.model.AuditEventType
import com.openpos.guardian.domain.model.AuditStatus
import com.openpos.guardian.domain.model.KioskPolicyState
import com.openpos.guardian.domain.repository.AuditRepository
import com.openpos.guardian.domain.repository.KioskRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KioskRepositoryImpl @Inject constructor(
    private val kioskManager: KioskManager,
    private val auditRepository: AuditRepository
) : KioskRepository {

    override fun getKioskPolicyState(): Flow<KioskPolicyState> = flow {
        while (currentCoroutineContext().isActive) {
            emit(kioskManager.getKioskPolicyState())
            delay(2000L) // Poll state periodically to detect external lock task changes
        }
    }

    override suspend fun enterLockTaskMode(activity: Activity): Result<Unit> {
        val result = kioskManager.startLockTask(activity)
        if (result.isSuccess) {
            auditRepository.recordEvent(
                eventType = AuditEventType.LOCK_TASK_STATE,
                action = "ENTER_LOCK_TASK_REQUESTED",
                status = AuditStatus.SUCCESS,
                details = "Requested Lock-Task mode for activity: ${activity.componentName.className}",
                actor = "USER_ADMIN"
            )
        } else {
            auditRepository.recordEvent(
                eventType = AuditEventType.LOCK_TASK_STATE,
                action = "ENTER_LOCK_TASK_FAILED",
                status = AuditStatus.FAILURE,
                details = "Failed to start Lock-Task mode: ${result.exceptionOrNull()?.message}",
                actor = "USER_ADMIN"
            )
        }
        return result
    }

    override suspend fun exitLockTaskMode(activity: Activity): Result<Unit> {
        val result = kioskManager.stopLockTask(activity)
        if (result.isSuccess) {
            auditRepository.recordEvent(
                eventType = AuditEventType.LOCK_TASK_STATE,
                action = "EXIT_LOCK_TASK_REQUESTED",
                status = AuditStatus.SUCCESS,
                details = "Requested exit from Lock-Task mode.",
                actor = "USER_ADMIN"
            )
        } else {
            auditRepository.recordEvent(
                eventType = AuditEventType.LOCK_TASK_STATE,
                action = "EXIT_LOCK_TASK_FAILED",
                status = AuditStatus.FAILURE,
                details = "Failed to exit Lock-Task mode: ${result.exceptionOrNull()?.message}",
                actor = "USER_ADMIN"
            )
        }
        return result
    }

    override suspend fun syncLockTaskPackages(packages: List<String>): Result<Unit> {
        val result = kioskManager.setLockTaskPackages(packages)
        if (result.isSuccess) {
            auditRepository.recordEvent(
                eventType = AuditEventType.POLICY_ENFORCEMENT,
                action = "SET_LOCK_TASK_PACKAGES",
                status = AuditStatus.SUCCESS,
                details = "Successfully synced ${packages.size} packages to DevicePolicyManager lock-task policy.",
                actor = "USER_ADMIN"
            )
        } else {
            auditRepository.recordEvent(
                eventType = AuditEventType.POLICY_ENFORCEMENT,
                action = "SET_LOCK_TASK_PACKAGES_FAILED",
                status = AuditStatus.FAILURE,
                details = "Failed to sync lock-task packages: ${result.exceptionOrNull()?.message}",
                actor = "USER_ADMIN"
            )
        }
        return result
    }

    override suspend fun updateKioskFeatures(
        systemInfo: Boolean,
        notifications: Boolean,
        home: Boolean,
        globalActions: Boolean,
        keyguard: Boolean
    ): Result<Unit> {
        val result = kioskManager.setLockTaskFeatures(
            systemInfo = systemInfo,
            notifications = notifications,
            home = home,
            globalActions = globalActions,
            keyguard = keyguard
        )
        if (result.isSuccess) {
            auditRepository.recordEvent(
                eventType = AuditEventType.POLICY_ENFORCEMENT,
                action = "UPDATE_KIOSK_FEATURES",
                status = AuditStatus.SUCCESS,
                details = "Updated lock-task features (sysInfo=$systemInfo, notif=$notifications, home=$home, globalActions=$globalActions, keyguard=$keyguard)",
                actor = "USER_ADMIN"
            )
        } else {
            auditRepository.recordEvent(
                eventType = AuditEventType.POLICY_ENFORCEMENT,
                action = "UPDATE_KIOSK_FEATURES_FAILED",
                status = AuditStatus.FAILURE,
                details = "Failed to update lock-task features: ${result.exceptionOrNull()?.message}",
                actor = "USER_ADMIN"
            )
        }
        return result
    }

    override suspend fun setStatusBarDisabled(disabled: Boolean): Result<Unit> {
        val result = kioskManager.setStatusBarDisabled(disabled)
        if (result.isSuccess) {
            auditRepository.recordEvent(
                eventType = AuditEventType.POLICY_ENFORCEMENT,
                action = "SET_STATUS_BAR_DISABLED",
                status = AuditStatus.SUCCESS,
                details = "Status bar disabled state updated to: $disabled",
                actor = "USER_ADMIN"
            )
        }
        return result
    }

    override suspend fun setKeyguardDisabled(disabled: Boolean): Result<Unit> {
        val result = kioskManager.setKeyguardDisabled(disabled)
        if (result.isSuccess) {
            auditRepository.recordEvent(
                eventType = AuditEventType.POLICY_ENFORCEMENT,
                action = "SET_KEYGUARD_DISABLED",
                status = AuditStatus.SUCCESS,
                details = "Keyguard disabled state updated to: $disabled",
                actor = "USER_ADMIN"
            )
        }
        return result
    }
}
