package com.openpos.guardian

import android.app.Application
import android.util.Log
import com.openpos.guardian.domain.model.AuditEventType
import com.openpos.guardian.domain.model.AuditStatus
import com.openpos.guardian.domain.repository.AuditRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class OpenPOSApp : Application() {

    @Inject
    lateinit var auditRepository: AuditRepository

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "OpenPOS Guardian application starting...")
        applicationScope.launch {
            auditRepository.recordEvent(
                eventType = AuditEventType.APP_LIFECYCLE,
                action = "APPLICATION_START",
                status = AuditStatus.INFO,
                details = "OpenPOS Guardian launched (PID=${android.os.Process.myPid()}).",
                actor = "SYSTEM"
            )
        }
    }

    companion object {
        private const val TAG = "OpenPOSApp"
    }
}
