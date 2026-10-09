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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mughalarts.gownordermanager.ui.screens.HomeScreen
import com.mughalarts.gownordermanager.ui.screens.NewOrderScreen
import com.mughalarts.gownordermanager.ui.screens.OrderDetailsScreen
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

    // While viewing or editing an order, the Orders tab stays highlighted.
    val selectedRoute = when {
        currentRoute == null -> null
        currentRoute.startsWith("order_details") -> Screen.Orders.route
        currentRoute.startsWith("edit_order") -> Screen.Orders.route
        else -> currentRoute
    }

    val orderIdArgument = listOf(
        navArgument(Routes.ORDER_ID) { type = NavType.LongType }
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = selectedRoute == screen.route,
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
                HomeScreen(
                    onNewOrder = { navController.navigateToTab(Screen.NewOrder.route) },
                    onOrderClick = { id -> navController.navigate(Routes.orderDetails(id)) }
                )
            }
            composable(Screen.Orders.route) {
                OrdersScreen(
                    onOrderClick = { id -> navController.navigate(Routes.orderDetails(id)) }
                )
            }
            composable(Screen.NewOrder.route) {
                NewOrderScreen(
                    orderId = null,
                    onSaved = { id ->
                        // Go to the Orders list, then open the new order's Agreement Slip.
                        navController.navigateToTab(Screen.Orders.route)
                        navController.navigate(Routes.orderDetails(id))
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(Routes.ORDER_DETAILS, arguments = orderIdArgument) { entry ->
                val orderId = entry.arguments?.getLong(Routes.ORDER_ID) ?: 0L
                OrderDetailsScreen(
                    orderId = orderId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.editOrder(orderId)) }
                )
            }
            composable(Routes.EDIT_ORDER, arguments = orderIdArgument) { entry ->
                val orderId = entry.arguments?.getLong(Routes.ORDER_ID) ?: 0L
                NewOrderScreen(
                    orderId = orderId,
                    onSaved = { navController.popBackStack() }
                )
            }
        }
    }
}
