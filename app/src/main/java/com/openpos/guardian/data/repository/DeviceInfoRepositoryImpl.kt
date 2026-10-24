package com.openpos.guardian.data.repository

import com.openpos.guardian.device.info.DeviceInfoProvider
import com.openpos.guardian.domain.model.DeviceInfo
import com.openpos.guardian.domain.repository.DeviceInfoRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoRepositoryImpl @Inject constructor(
    private val deviceInfoProvider: DeviceInfoProvider
) : DeviceInfoRepository {

    override fun getDeviceInfoFlow(): Flow<DeviceInfo> = flow {
        while (currentCoroutineContext().isActive) {
            emit(deviceInfoProvider.getDeviceInfo())
            delay(5000L) // Poll every 5s for battery/storage updates
        }
    }

    override suspend fun refreshDeviceInfo(): DeviceInfo {
        return deviceInfoProvider.getDeviceInfo()
    }
}
