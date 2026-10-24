package com.openpos.guardian

import com.openpos.guardian.domain.model.ThemeMode
import com.openpos.guardian.fakes.FakeSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SettingsRepositoryTest {

    private lateinit var settingsRepository: FakeSettingsRepository

    @Before
    fun setup() {
        settingsRepository = FakeSettingsRepository()
    }

    @Test
    fun defaultSettings_haveExpectedValues() = runTest {
        val settings = settingsRepository.getUserSettings().first()
        assertEquals(3, settings.launcherGridColumns)
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertFalse(settings.autoLockTaskOnLaunch)
        assertEquals("1234", settings.adminPin)
    }

    @Test
    fun setThemeMode_updatesThemeSetting() = runTest {
        settingsRepository.setThemeMode(ThemeMode.DARK)
        val updated = settingsRepository.getUserSettings().first()
        assertEquals(ThemeMode.DARK, updated.themeMode)
    }

    @Test
    fun setGridColumns_clampsWithinPermittedRange() = runTest {
        // Minimum allowed is 2
        settingsRepository.setGridColumns(1)
        assertEquals(2, settingsRepository.getUserSettings().first().launcherGridColumns)

        // Maximum allowed is 6
        settingsRepository.setGridColumns(10)
        assertEquals(6, settingsRepository.getUserSettings().first().launcherGridColumns)

        // Valid value in range
        settingsRepository.setGridColumns(4)
        assertEquals(4, settingsRepository.getUserSettings().first().launcherGridColumns)
    }

    @Test
    fun setAutoLockTask_togglesFlagCorrectly() = runTest {
        settingsRepository.setAutoLockTask(true)
        assertTrue(settingsRepository.getUserSettings().first().autoLockTaskOnLaunch)

        settingsRepository.setAutoLockTask(false)
        assertFalse(settingsRepository.getUserSettings().first().autoLockTaskOnLaunch)
    }

    @Test
    fun setAdminPin_updatesPinSecurely() = runTest {
        settingsRepository.setAdminPin("9876")
        assertEquals("9876", settingsRepository.getUserSettings().first().adminPin)
    }
}
