package com.openpos.guardian

import com.openpos.guardian.domain.model.AuditEventType
import com.openpos.guardian.domain.model.AuditStatus
import com.openpos.guardian.domain.model.DeviceInfo
import com.openpos.guardian.domain.model.LockTaskModeState
import com.openpos.guardian.domain.repository.DeviceInfoRepository
import com.openpos.guardian.fakes.FakeAuditRepository
import com.openpos.guardian.fakes.FakeKioskRepository
import com.openpos.guardian.fakes.FakeLauncherRepository
import com.openpos.guardian.fakes.FakeSettingsRepository
import com.openpos.guardian.ui.dashboard.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var auditRepository: FakeAuditRepository
    private lateinit var kioskRepository: FakeKioskRepository
    private lateinit var launcherRepository: FakeLauncherRepository
    private lateinit var settingsRepository: FakeSettingsRepository

    private val fakeDeviceInfo = DeviceInfo(
        manufacturer = "Zebra",
        model = "TC52",
        brand = "Zebra",
        board = "universal",
        hardware = "qcom",
        androidRelease = "14",
        apiLevel = 34,
        securityPatch = "2026-09-01",
        buildId = "UP1A.231005.007",
        batteryPct = 95,
        isCharging = true,
        chargePlug = "AC Charger",
        batteryHealth = "Good",
        totalStorageBytes = 64_000_000_000L,
        freeStorageBytes = 42_000_000_000L,
        uptimeMillis = 3600_000L,
        isDeviceOwner = true,
        isProfileOwner = false,
        isAdminActive = true,
        lockTaskModeState = LockTaskModeState.LOCKED
    )

    private val fakeDeviceInfoRepo = object : DeviceInfoRepository {
        override fun getDeviceInfoFlow(): Flow<DeviceInfo> = flowOf(fakeDeviceInfo)
        override suspend fun refreshDeviceInfo(): DeviceInfo = fakeDeviceInfo
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        auditRepository = FakeAuditRepository()
        kioskRepository = FakeKioskRepository()
        launcherRepository = FakeLauncherRepository()
        settingsRepository = FakeSettingsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsDataSuccessfully() = runTest {
        auditRepository.recordEvent(
            eventType = AuditEventType.APP_LIFECYCLE,
            action = "TERMINAL_START",
            status = AuditStatus.SUCCESS,
            details = "POS started"
        )

        val viewModel = DashboardViewModel(
            deviceInfoRepository = fakeDeviceInfoRepo,
            kioskRepository = kioskRepository,
            launcherRepository = launcherRepository,
            auditRepository = auditRepository,
            settingsRepository = settingsRepository
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.deviceInfo)
        assertEquals("Zebra", state.deviceInfo?.manufacturer)
        assertEquals("TC52", state.deviceInfo?.model)
        assertEquals(1, state.recentEvents.size)
    }

    @Test
    fun refresh_updatesDeviceInfo() = runTest {
        val viewModel = DashboardViewModel(
            deviceInfoRepository = fakeDeviceInfoRepo,
            kioskRepository = kioskRepository,
            launcherRepository = launcherRepository,
            auditRepository = auditRepository,
            settingsRepository = settingsRepository
        )

        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Zebra", viewModel.uiState.value.deviceInfo?.manufacturer)
    }
}
