package com.mughalarts.gownordermanager.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mughalarts.gownordermanager.ui.screens.HomeScreen
import com.mughalarts.gownordermanager.ui.screens.NewOrderScreen
import com.mughalarts.gownordermanager.ui.screens.OrdersScreen
import com.mughalarts.gownordermanager.ui.screens.SettingsScreen

/** One function used by the bottom bar AND by every in-screen button, so they always agree. */
fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun GownApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = { navController.navigateToTab(screen.route) },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onNewOrder = { navController.navigateToTab(Screen.NewOrder.route) })
            }
            composable(Screen.Orders.route) {
                OrdersScreen()
            }
            composable(Screen.NewOrder.route) {
                NewOrderScreen(onOrderSaved = { navController.navigateToTab(Screen.Orders.route) })
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
