package com.openpos.guardian.device.info

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import com.openpos.guardian.device.admin.OpenPOSDeviceAdminReceiver
import com.openpos.guardian.domain.model.DeviceInfo
import com.openpos.guardian.domain.model.LockTaskModeState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val devicePolicyManager: DevicePolicyManager,
    private val activityManager: ActivityManager
) {
    fun getDeviceInfo(): DeviceInfo {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryLevel = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val batteryScale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (batteryLevel >= 0 && batteryScale > 0) {
            (batteryLevel * 100) / batteryScale
        } else {
            -1
        }

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug = when (batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> if (isCharging) "Connected" else "Discharging (Battery)"
        }

        val health = when (batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        // Internal Storage calculations
        val statFs = runCatching { StatFs(Environment.getDataDirectory().path) }.getOrNull()
        val totalStorageBytes = statFs?.totalBytes ?: -1L
        val freeStorageBytes = statFs?.availableBytes ?: -1L

        // Security patch
        val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH
        } else {
            "N/A"
        }

        // Enterprise & Kiosk status
        val packageName = context.packageName
        val adminComponent = OpenPOSDeviceAdminReceiver.getComponentName(context)

        val isDeviceOwner = runCatching {
            devicePolicyManager.isDeviceOwnerApp(packageName)
        }.getOrDefault(false)

        val isProfileOwner = runCatching {
            devicePolicyManager.isProfileOwnerApp(packageName)
        }.getOrDefault(false)

        val isAdminActive = runCatching {
            devicePolicyManager.isAdminActive(adminComponent)
        }.getOrDefault(false)

        val lockTaskModeState = when (activityManager.lockTaskModeState) {
            ActivityManager.LOCK_TASK_MODE_LOCKED -> LockTaskModeState.LOCKED
            ActivityManager.LOCK_TASK_MODE_PINNED -> LockTaskModeState.PINNED
            else -> LockTaskModeState.NONE
        }

        return DeviceInfo(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            brand = Build.BRAND,
            board = Build.BOARD,
            hardware = Build.HARDWARE,
            androidRelease = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            securityPatch = securityPatch,
            buildId = Build.DISPLAY,
            batteryPct = batteryPct,
            isCharging = isCharging,
            chargePlug = chargePlug,
            batteryHealth = health,
            totalStorageBytes = totalStorageBytes,
            freeStorageBytes = freeStorageBytes,
            uptimeMillis = SystemClock.elapsedRealtime(),
            isDeviceOwner = isDeviceOwner,
            isProfileOwner = isProfileOwner,
            isAdminActive = isAdminActive,
            lockTaskModeState = lockTaskModeState
        )
    }
}
