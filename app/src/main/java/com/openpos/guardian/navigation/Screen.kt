package com.openpos.guardian.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )

    object Launcher : Screen(
        route = "launcher",
        title = "POS Launcher",
        selectedIcon = Icons.Filled.Apps,
        unselectedIcon = Icons.Outlined.Apps
    )

    object Kiosk : Screen(
        route = "kiosk",
        title = "Kiosk Control",
        selectedIcon = Icons.Filled.Lock,
        unselectedIcon = Icons.Outlined.Lock
    )

    object Allowlist : Screen(
        route = "allowlist",
        title = "Allowlist",
        selectedIcon = Icons.Filled.Checklist,
        unselectedIcon = Icons.Outlined.Checklist
    )

    object DeviceInfo : Screen(
        route = "device_info",
        title = "Device Info",
        selectedIcon = Icons.Filled.Info,
        unselectedIcon = Icons.Outlined.Info
    )

    object Audit : Screen(
        route = "audit",
        title = "Audit Logs",
        selectedIcon = Icons.Filled.Security,
        unselectedIcon = Icons.Outlined.Security
    )

    object SetupGuide : Screen(
        route = "setup_guide",
        title = "Setup Guide",
        selectedIcon = Icons.Filled.Build,
        unselectedIcon = Icons.Outlined.Build
    )

    object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    object Update : Screen(
        route = "update",
        title = "App Info",
        selectedIcon = Icons.Filled.SystemUpdate,
        unselectedIcon = Icons.Outlined.SystemUpdate
    )

    companion object {
        val primaryNavItems = listOf(
            Dashboard,
            Launcher,
            Kiosk,
            Allowlist,
            Audit
        )

        val secondaryNavItems = listOf(
            DeviceInfo,
            SetupGuide,
            Settings,
            Update
        )
    }
}
