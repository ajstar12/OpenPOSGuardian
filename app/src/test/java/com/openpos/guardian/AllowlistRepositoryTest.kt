package com.openpos.guardian

import com.openpos.guardian.domain.model.LauncherApp
import com.openpos.guardian.fakes.FakeLauncherRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AllowlistRepositoryTest {

    private lateinit var launcherRepository: FakeLauncherRepository

    @Before
    fun setup() {
        launcherRepository = FakeLauncherRepository()
        launcherRepository.setInstalledApps(
            listOf(
                LauncherApp("com.squareup.pos", "MainActivity", "Square POS", isApproved = false),
                LauncherApp("com.clover.pos", "MainActivity", "Clover POS", isApproved = false),
                LauncherApp("com.toast.pos", "MainActivity", "Toast POS", isApproved = false)
            )
        )
    }

    @Test
    fun initialApprovedApps_isEmpty() = runTest {
        val approved = launcherRepository.getApprovedApps().first()
        assertTrue(approved.isEmpty())
    }

    @Test
    fun addAppToAllowlist_succeedsForInstalledApp() = runTest {
        val result = launcherRepository.addAppToAllowlist("com.squareup.pos")
        assertTrue(result.isSuccess)

        val approved = launcherRepository.getApprovedApps().first()
        assertEquals(1, approved.size)
        assertEquals("com.squareup.pos", approved[0].packageName)
        assertTrue(approved[0].isApproved)
        assertTrue(launcherRepository.isAppApproved("com.squareup.pos"))
    }

    @Test
    fun addAppToAllowlist_failsForNonInstalledApp() = runTest {
        val result = launcherRepository.addAppToAllowlist("com.nonexistent.fakeapp")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)

        val approved = launcherRepository.getApprovedApps().first()
        assertTrue(approved.isEmpty())
    }

    @Test
    fun removeAppFromAllowlist_removesFromApprovedList() = runTest {
        launcherRepository.addAppToAllowlist("com.squareup.pos")
        launcherRepository.addAppToAllowlist("com.toast.pos")
        assertEquals(2, launcherRepository.getApprovedApps().first().size)

        val removeResult = launcherRepository.removeAppFromAllowlist("com.squareup.pos")
        assertTrue(removeResult.isSuccess)

        val remaining = launcherRepository.getApprovedApps().first()
        assertEquals(1, remaining.size)
        assertEquals("com.toast.pos", remaining[0].packageName)
        assertFalse(launcherRepository.isAppApproved("com.squareup.pos"))
    }
}
