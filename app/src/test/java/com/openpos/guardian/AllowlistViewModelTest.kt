package com.openpos.guardian

import com.openpos.guardian.domain.model.LauncherApp
import com.openpos.guardian.fakes.FakeKioskRepository
import com.openpos.guardian.fakes.FakeLauncherRepository
import com.openpos.guardian.ui.allowlist.AllowlistViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class AllowlistViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var launcherRepository: FakeLauncherRepository
    private lateinit var kioskRepository: FakeKioskRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        launcherRepository = FakeLauncherRepository()
        kioskRepository = FakeKioskRepository()

        launcherRepository.setInstalledApps(
            listOf(
                LauncherApp("com.squareup.pos", "SquareActivity", "Square POS", isApproved = false),
                LauncherApp("com.clover.pos", "CloverActivity", "Clover POS", isApproved = false),
                LauncherApp("com.toast.pos", "ToastActivity", "Toast POS", isApproved = false)
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addApp_addsAppAndUpdatesState() = runTest {
        val viewModel = AllowlistViewModel(launcherRepository, kioskRepository)
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.approvedApps.size)

        viewModel.addApp("com.squareup.pos")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.approvedApps.size)
        assertEquals("com.squareup.pos", viewModel.uiState.value.approvedApps[0].packageName)
        assertFalse(viewModel.uiState.value.isError)
    }

    @Test
    fun removeApp_removesAppAndUpdatesState() = runTest {
        val viewModel = AllowlistViewModel(launcherRepository, kioskRepository)
        advanceUntilIdle()

        viewModel.addApp("com.squareup.pos")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.approvedApps.size)

        viewModel.removeApp("com.squareup.pos")
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.approvedApps.size)
    }

    @Test
    fun searchQuery_filtersApprovedApps() = runTest {
        val viewModel = AllowlistViewModel(launcherRepository, kioskRepository)
        advanceUntilIdle()

        viewModel.addApp("com.squareup.pos")
        viewModel.addApp("com.toast.pos")
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.approvedApps.size)

        viewModel.onSearchQueryChanged("Toast")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.filteredApprovedApps.size)
        assertEquals("com.toast.pos", viewModel.uiState.value.filteredApprovedApps[0].packageName)

        // Clear query
        viewModel.onSearchQueryChanged("")
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.filteredApprovedApps.size)
    }

    @Test
    fun addAppDialog_togglesStateCorrectly() = runTest {
        val viewModel = AllowlistViewModel(launcherRepository, kioskRepository)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAddDialog)

        viewModel.openAddAppDialog()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showAddDialog)
        assertEquals(3, viewModel.uiState.value.installedApps.size)

        viewModel.dismissAddAppDialog()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showAddDialog)
    }
}
