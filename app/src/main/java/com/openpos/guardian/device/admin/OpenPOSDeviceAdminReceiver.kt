package com.openpos.guardian.device.admin

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.openpos.guardian.domain.model.AuditEventType
import com.openpos.guardian.domain.model.AuditStatus
import com.openpos.guardian.domain.repository.AuditRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Enterprise Device Admin Receiver for OpenPOS Guardian.
 *
 * Handles Android Enterprise provisioning lifecycle events and lock-task state transitions.
 * When provisioned as Device Owner, this receiver is the identity component for all
 * DevicePolicyManager operations.
 */
@AndroidEntryPoint
class OpenPOSDeviceAdminReceiver : DeviceAdminReceiver() {

    @Inject
    lateinit var auditRepository: AuditRepository

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "Device Admin enabled")
        scope.launch {
            auditRepository.recordEvent(
                eventType = AuditEventType.POLICY_ENFORCEMENT,
                action = "DEVICE_ADMIN_ENABLED",
                status = AuditStatus.SUCCESS,
                details = "OpenPOS Guardian granted Device Administrator privileges.",
                actor = "ADMIN_PROVISIONING"
            )
        }
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.w(TAG, "Device Admin disabled")
        scope.launch {
            auditRepository.recordEvent(
                eventType = AuditEventType.POLICY_ENFORCEMENT,
                action = "DEVICE_ADMIN_DISABLED",
                status = AuditStatus.WARNING,
                details = "OpenPOS Guardian Device Administrator privileges revoked.",
                actor = "SYSTEM"
            )
        }
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        super.onLockTaskModeEntering(context, intent, pkg)
        Log.i(TAG, "Entered Lock-Task Mode: $pkg")
        scope.launch {
            auditRepository.recordEvent(
                eventType = AuditEventType.LOCK_TASK_STATE,
                action = "LOCK_TASK_ENTERED",
                status = AuditStatus.SUCCESS,
                details = "Terminal entered dedicated Lock-Task mode for package: $pkg",
                actor = "KIOSK_MANAGER"
            )
        }
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        super.onLockTaskModeExiting(context, intent)
        Log.i(TAG, "Exited Lock-Task Mode")
        scope.launch {
            auditRepository.recordEvent(
                eventType = AuditEventType.LOCK_TASK_STATE,
                action = "LOCK_TASK_EXITED",
                status = AuditStatus.INFO,
                details = "Terminal exited Lock-Task mode. Standard navigation restored.",
                actor = "KIOSK_MANAGER"
            )
        }
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        Log.i(TAG, "Profile provisioning complete")
        scope.launch {
            auditRepository.recordEvent(
                eventType = AuditEventType.POLICY_ENFORCEMENT,
                action = "PROVISIONING_COMPLETE",
                status = AuditStatus.SUCCESS,
                details = "Android Enterprise provisioning sequence completed successfully.",
                actor = "DEVICE_OWNER"
            )
        }
    }

    companion object {
        private const val TAG = "OpenPOSDeviceAdmin"

        /**
         * Returns the ComponentName for this DeviceAdminReceiver.
         */
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, OpenPOSDeviceAdminReceiver::class.java)
        }
    }
}
