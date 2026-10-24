package com.openpos.guardian.device.kiosk

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import com.openpos.guardian.device.admin.OpenPOSDeviceAdminReceiver
import com.openpos.guardian.domain.model.KioskPolicyState
import com.openpos.guardian.domain.model.LockTaskModeState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enterprise Kiosk Manager.
 *
 * Coordinates Android Enterprise dedicated-device lockdown policies, lock-task mode
 * execution, and status bar / keyguard restrictions via [DevicePolicyManager] and [ActivityManager].
 */
@Singleton
class KioskManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val devicePolicyManager: DevicePolicyManager,
    private val activityManager: ActivityManager
) {
    private val adminComponent: ComponentName by lazy {
        OpenPOSDeviceAdminReceiver.getComponentName(context)
    }

    val isDeviceOwner: Boolean
        get() = runCatching {
            devicePolicyManager.isDeviceOwnerApp(context.packageName)
        }.getOrDefault(false)

    val isProfileOwner: Boolean
        get() = runCatching {
            devicePolicyManager.isProfileOwnerApp(context.packageName)
        }.getOrDefault(false)

    val isAdminActive: Boolean
        get() = runCatching {
            devicePolicyManager.isAdminActive(adminComponent)
        }.getOrDefault(false)

    val currentLockTaskMode: LockTaskModeState
        get() = when (activityManager.lockTaskModeState) {
            ActivityManager.LOCK_TASK_MODE_LOCKED -> LockTaskModeState.LOCKED
            ActivityManager.LOCK_TASK_MODE_PINNED -> LockTaskModeState.PINNED
            else -> LockTaskModeState.NONE
        }

    fun getKioskPolicyState(): KioskPolicyState {
        val lockTaskPkgs = runCatching {
            if (isDeviceOwner) {
                devicePolicyManager.getLockTaskPackages(adminComponent).toList()
            } else {
                emptyList()
            }
        }.getOrDefault(emptyList())

        return KioskPolicyState(
            isDeviceOwner = isDeviceOwner,
            isProfileOwner = isProfileOwner,
            isAdminActive = isAdminActive,
            lockTaskModeState = currentLockTaskMode,
            lockTaskPackages = lockTaskPkgs,
            isStatusBarDisabled = false, // DevicePolicyManager doesn't provide a direct getter
            isKeyguardDisabled = false,
            systemInfoFeature = true,
            notificationsFeature = false,
            homeFeature = false,
            globalActionsFeature = true,
            keyguardFeature = false
        )
    }

    /**
     * Whitelists packages that are permitted to run in Lock-Task mode without user prompt.
     * Requires Device Owner privileges.
     */
    fun setLockTaskPackages(packages: List<String>): Result<Unit> {
        return runCatching {
            if (!isDeviceOwner) {
                throw SecurityException("Device Owner privilege required to configure lock-task packages.")
            }
            // Ensure this application itself is always permitted so the kiosk can run
            val combinedPackages = (packages + context.packageName).distinct().toTypedArray()
            devicePolicyManager.setLockTaskPackages(adminComponent, combinedPackages)
            Log.i(TAG, "Configured lock-task packages: ${combinedPackages.joinToString()}")
        }
    }

    /**
     * Configures allowed system UI features while in Lock-Task mode (API 28+).
     */
    fun setLockTaskFeatures(
        systemInfo: Boolean,
        notifications: Boolean,
        home: Boolean,
        globalActions: Boolean,
        keyguard: Boolean
    ): Result<Unit> {
        return runCatching {
            if (!isDeviceOwner) {
                throw SecurityException("Device Owner privilege required to configure lock-task features.")
            }
            var flags = DevicePolicyManager.LOCK_TASK_FEATURE_NONE
            if (systemInfo) flags = flags or DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO
            if (notifications) flags = flags or DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS
            if (home) flags = flags or DevicePolicyManager.LOCK_TASK_FEATURE_HOME
            if (globalActions) flags = flags or DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS
            if (keyguard) flags = flags or DevicePolicyManager.LOCK_TASK_FEATURE_KEYGUARD

            devicePolicyManager.setLockTaskFeatures(adminComponent, flags)
            Log.i(TAG, "Configured lock-task features: flags=$flags")
        }
    }

    /**
     * Disables or enables the system status bar.
     * Prevents pull-down of quick settings and notifications in dedicated terminal setups.
     */
    fun setStatusBarDisabled(disabled: Boolean): Result<Unit> {
        return runCatching {
            if (!isDeviceOwner) {
                throw SecurityException("Device Owner privilege required to disable status bar.")
            }
            val success = devicePolicyManager.setStatusBarDisabled(adminComponent, disabled)
            if (!success) {
                throw IllegalStateException("DevicePolicyManager rejected status bar policy change.")
            }
            Log.i(TAG, "Status bar disabled state set to: $disabled")
        }
    }

    /**
     * Disables the Android keyguard (lock screen).
     */
    fun setKeyguardDisabled(disabled: Boolean): Result<Unit> {
        return runCatching {
            if (!isDeviceOwner) {
                throw SecurityException("Device Owner privilege required to disable keyguard.")
            }
            val success = devicePolicyManager.setKeyguardDisabled(adminComponent, disabled)
            if (!success) {
                throw IllegalStateException("DevicePolicyManager rejected keyguard policy change.")
            }
            Log.i(TAG, "Keyguard disabled state set to: $disabled")
        }
    }

    /**
     * Initiates Lock-Task mode for the host Activity.
     *
     * If the app is Device Owner and in lockTaskPackages, it enters LOCK_TASK_MODE_LOCKED (true kiosk).
     * If not, Android will initiate Screen Pinning (LOCK_TASK_MODE_PINNED), prompting the user.
     */
    fun startLockTask(activity: Activity): Result<Unit> {
        return runCatching {
            if (activityManager.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE) {
                Log.d(TAG, "Activity already in lock-task mode: ${activityManager.lockTaskModeState}")
                return@runCatching
            }
            activity.startLockTask()
            Log.i(TAG, "activity.startLockTask() invoked.")
        }
    }

    /**
     * Terminates Lock-Task mode and restores normal Android navigation.
     */
    fun stopLockTask(activity: Activity): Result<Unit> {
        return runCatching {
            if (activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
                Log.d(TAG, "Activity is not in lock-task mode.")
                return@runCatching
            }
            activity.stopLockTask()
            Log.i(TAG, "activity.stopLockTask() invoked.")
        }
    }

    companion object {
        private const val TAG = "KioskManager"
    }
}
