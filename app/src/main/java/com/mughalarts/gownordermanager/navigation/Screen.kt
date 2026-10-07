package com.mughalarts.gownordermanager.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object Orders : Screen("orders", "Orders", Icons.AutoMirrored.Filled.List)
    data object NewOrder : Screen("new_order", "New Order", Icons.Filled.Add)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Orders,
    Screen.NewOrder,
    Screen.Settings
)
