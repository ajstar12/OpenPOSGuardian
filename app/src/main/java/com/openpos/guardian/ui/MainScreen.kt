package com.openpos.guardian.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.openpos.guardian.R
import com.openpos.guardian.domain.model.ThemeMode
import com.openpos.guardian.navigation.Screen
import com.openpos.guardian.ui.allowlist.AllowlistScreen
import com.openpos.guardian.ui.allowlist.AllowlistViewModel
import com.openpos.guardian.ui.audit.AuditScreen
import com.openpos.guardian.ui.audit.AuditViewModel
import com.openpos.guardian.ui.dashboard.DashboardScreen
import com.openpos.guardian.ui.dashboard.DashboardViewModel
import com.openpos.guardian.ui.deviceinfo.DeviceInfoScreen
import com.openpos.guardian.ui.deviceinfo.DeviceInfoViewModel
import com.openpos.guardian.ui.kiosk.KioskScreen
import com.openpos.guardian.ui.kiosk.KioskViewModel
import com.openpos.guardian.ui.launcher.LauncherScreen
import com.openpos.guardian.ui.launcher.LauncherViewModel
import com.openpos.guardian.ui.settings.SettingsScreen
import com.openpos.guardian.ui.settings.SettingsViewModel
import com.openpos.guardian.ui.setup.SetupGuideScreen
import com.openpos.guardian.ui.update.UpdateScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentTheme: ThemeMode,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide chrome when inside POS Launcher dedicated mode
    val isDedicatedLauncherMode = currentRoute == Screen.Launcher.route

    var showDrawerOrMore by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (!isDedicatedLauncherMode) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentRoute) {
                                Screen.Dashboard.route -> stringResource(R.string.nav_dashboard)
                                Screen.Launcher.route -> stringResource(R.string.nav_launcher)
                                Screen.Kiosk.route -> stringResource(R.string.nav_kiosk)
                                Screen.Allowlist.route -> stringResource(R.string.nav_allowlist)
                                Screen.DeviceInfo.route -> stringResource(R.string.nav_device_info)
                                Screen.Audit.route -> stringResource(R.string.nav_audit)
                                Screen.SetupGuide.route -> stringResource(R.string.nav_setup)
                                Screen.Settings.route -> stringResource(R.string.nav_settings)
                                Screen.Update.route -> stringResource(R.string.nav_update)
                                else -> stringResource(R.string.app_name)
                            }
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.DeviceInfo.route) }) {
                            Icon(Icons.Default.Info, contentDescription = "Device Info")
                        }
                        IconButton(onClick = { navController.navigate(Screen.SetupGuide.route) }) {
                            Icon(Icons.Default.Build, contentDescription = "Setup Guide")
                        }
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = { navController.navigate(Screen.Update.route) }) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = "Update")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        bottomBar = {
            if (!isDedicatedLauncherMode) {
                NavigationBar {
                    Screen.primaryNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val vm: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = vm,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(Screen.Launcher.route) {
                val vm: LauncherViewModel = hiltViewModel()
                LauncherScreen(
                    viewModel = vm,
                    onNavigateToAdmin = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Kiosk.route) {
                val vm: KioskViewModel = hiltViewModel()
                KioskScreen(
                    viewModel = vm,
                    onNavigateToSetup = { navController.navigate(Screen.SetupGuide.route) }
                )
            }

            composable(Screen.Allowlist.route) {
                val vm: AllowlistViewModel = hiltViewModel()
                AllowlistScreen(viewModel = vm)
            }

            composable(Screen.DeviceInfo.route) {
                val vm: DeviceInfoViewModel = hiltViewModel()
                DeviceInfoScreen(viewModel = vm)
            }

            composable(Screen.Audit.route) {
                val vm: AuditViewModel = hiltViewModel()
                AuditScreen(viewModel = vm)
            }

            composable(Screen.SetupGuide.route) {
                SetupGuideScreen()
            }

            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = hiltViewModel()
                SettingsScreen(viewModel = vm)
            }

            composable(Screen.Update.route) {
                UpdateScreen()
            }
        }
    }
}
