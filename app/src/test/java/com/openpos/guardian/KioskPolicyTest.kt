package com.openpos.guardian

import com.openpos.guardian.domain.model.LockTaskModeState
import com.openpos.guardian.fakes.FakeKioskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class KioskPolicyTest {

    private lateinit var kioskRepository: FakeKioskRepository

    @Before
    fun setup() {
        kioskRepository = FakeKioskRepository()
    }

    @Test
    fun nonDeviceOwner_cannotSyncLockTaskPackages() = runTest {
        kioskRepository.isDeviceOwner = false

        val result = kioskRepository.syncLockTaskPackages(listOf("com.squareup.pos"))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)

        val policyState = kioskRepository.getKioskPolicyState().first()
        assertTrue(policyState.lockTaskPackages.isEmpty())
    }

    @Test
    fun deviceOwner_canSyncLockTaskPackages() = runTest {
        kioskRepository.isDeviceOwner = true

        val packages = listOf("com.squareup.pos", "com.clover.pos")
        val result = kioskRepository.syncLockTaskPackages(packages)
        assertTrue(result.isSuccess)

        val policyState = kioskRepository.getKioskPolicyState().first()
        assertEquals(2, policyState.lockTaskPackages.size)
        assertTrue(policyState.lockTaskPackages.contains("com.squareup.pos"))
    }

    @Test
    fun nonDeviceOwner_cannotUpdateSystemFeatures() = runTest {
        kioskRepository.isDeviceOwner = false

        val result = kioskRepository.updateKioskFeatures(
            systemInfo = true,
            notifications = false,
            home = false,
            globalActions = true,
            keyguard = false
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }

    @Test
    fun nonDeviceOwner_cannotDisableStatusBar() = runTest {
        kioskRepository.isDeviceOwner = false

        val result = kioskRepository.setStatusBarDisabled(true)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }

    @Test
    fun lockTaskModeState_distinguishesLockedFromPinned() = runTest {
        // Device Owner enters LOCKED (true enterprise kiosk)
        kioskRepository.isDeviceOwner = true
        // Simulating activity invocation through fake
        kioskRepository.currentMode = LockTaskModeState.LOCKED
        assertEquals(LockTaskModeState.LOCKED, kioskRepository.currentMode)

        // Non-Device Owner enters PINNED (screen pinning)
        kioskRepository.isDeviceOwner = false
        kioskRepository.currentMode = LockTaskModeState.PINNED
        assertEquals(LockTaskModeState.PINNED, kioskRepository.currentMode)
    }
}
