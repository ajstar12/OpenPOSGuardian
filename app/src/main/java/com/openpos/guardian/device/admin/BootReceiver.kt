package com.openpos.guardian.device.admin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.openpos.guardian.MainActivity
import com.openpos.guardian.data.local.datastore.PreferencesDataStore
import com.openpos.guardian.domain.model.AuditEventType
import com.openpos.guardian.domain.model.AuditStatus
import com.openpos.guardian.domain.repository.AuditRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferencesDataStore: PreferencesDataStore

    @Inject
    lateinit var auditRepository: AuditRepository

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(TAG, "Device boot completed received")
            scope.launch {
                auditRepository.recordEvent(
                    eventType = AuditEventType.APP_LIFECYCLE,
                    action = "DEVICE_BOOT_COMPLETED",
                    status = AuditStatus.INFO,
                    details = "Device system reboot detected.",
                    actor = "ANDROID_OS"
                )

                val settings = preferencesDataStore.userSettingsFlow.first()
                if (settings.autoLockTaskOnLaunch) {
                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(MainActivity.EXTRA_AUTO_START_KIOSK, true)
                    }
                    context.startActivity(launchIntent)
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
