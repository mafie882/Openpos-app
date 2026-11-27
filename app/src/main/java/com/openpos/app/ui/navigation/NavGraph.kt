package com.openpos.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.openpos.app.ui.cart.CartScreen
import com.openpos.app.ui.cart.CartViewModel
import com.openpos.app.ui.cart.CheckoutScreen
import com.openpos.app.ui.cart.HeldOrdersScreen
import com.openpos.app.ui.categories.CategoryScreen
import com.openpos.app.ui.common.openPosApp
import com.openpos.app.ui.expenses.ExpenseEditScreen
import com.openpos.app.ui.expenses.ExpenseListScreen
import com.openpos.app.ui.inventory.InventoryScreen
import com.openpos.app.ui.products.ProductEditScreen
import com.openpos.app.ui.products.ProductListScreen
import com.openpos.app.ui.reports.ReportsScreen
import com.openpos.app.ui.sales.ReceiptScreen
import com.openpos.app.ui.sales.SalesHistoryScreen
import com.openpos.app.ui.settings.SettingsScreen

private val bottomDestinations = listOf(
    BottomDestination(Screen.Products, "Sell", Icons.Default.Storefront),
    BottomDestination(Screen.Inventory, "Inventory", Icons.Default.Inventory2),
    BottomDestination(Screen.Expenses, "Expenses", Icons.Default.Receipt),
    BottomDestination(Screen.Reports, "Reports", Icons.Default.ListAlt),
    BottomDestination(Screen.Settings, "Settings", Icons.Default.Settings),
)

@Composable
fun OpenPosNavGraph() {
    val navController = rememberNavController()
    val app = openPosApp()
    val cartViewModel: CartViewModel = viewModel(factory = com.openpos.app.ui.common.SimpleViewModelFactory {
        CartViewModel(app.repository)
    })

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    val showBottomBar = bottomDestinations.any { it.screen.route == currentRoute?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomDestinations.forEach { dest ->
                        val selected = currentRoute?.hierarchy?.any { it.route == dest.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Products.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Products.route) {
                ProductListScreen(
                    cartViewModel = cartViewModel,
                    onOpenCart = { navController.navigate(Screen.Cart.route) },
                    onAddProduct = { navController.navigate(Screen.ProductEdit.create(0)) },
                    onEditProduct = { id -> navController.navigate(Screen.ProductEdit.create(id)) },
                    onOpenCategories = { navController.navigate(Screen.Categories.route) },
                    onOpenSalesHistory = { navController.navigate(Screen.SalesHistory.route) }
                )
            }
            composable(Screen.ProductEdit.route) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")?.toLongOrNull() ?: 0L
                ProductEditScreen(productId = productId, onDone = { navController.popBackStack() })
            }
            composable(Screen.Categories.route) {
                CategoryScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    cartViewModel = cartViewModel,
                    onBack = { navController.popBackStack() },
                    onCheckout = { navController.navigate(Screen.Checkout.route) },
                    onOpenHeldOrders = { navController.navigate(Screen.HeldOrders.route) }
                )
            }
            composable(Screen.HeldOrders.route) {
                HeldOrdersScreen(
                    cartViewModel = cartViewModel,
                    onBack = { navController.popBackStack() },
                    onResumed = { navController.popBackStack(Screen.Cart.route, inclusive = false) }
                )
            }
            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    cartViewModel = cartViewModel,
                    onBack = { navController.popBackStack() },
                    onComplete = { saleId ->
                        navController.navigate(Screen.Receipt.create(saleId)) {
                            popUpTo(Screen.Products.route)
                        }
                    }
                )
            }
            composable(Screen.Receipt.route) { backStackEntry ->
                val saleId = backStackEntry.arguments?.getString("saleId")?.toLongOrNull() ?: 0L
                ReceiptScreen(saleId = saleId, onDone = {
                    navController.popBackStack(Screen.Products.route, inclusive = false)
                })
            }
            composable(Screen.SalesHistory.route) {
                SalesHistoryScreen(onOpenReceipt = { id -> navController.navigate(Screen.Receipt.create(id)) })
            }
            composable(Screen.Inventory.route) {
                InventoryScreen()
            }
            composable(Screen.Expenses.route) {
                ExpenseListScreen(
                    onAdd = { navController.navigate(Screen.ExpenseEdit.create(0)) },
                    onEdit = { id -> navController.navigate(Screen.ExpenseEdit.create(id)) }
                )
            }
            composable(Screen.ExpenseEdit.route) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("expenseId")?.toLongOrNull() ?: 0L
                ExpenseEditScreen(expenseId = id, onDone = { navController.popBackStack() })
            }
            composable(Screen.Reports.route) {
                ReportsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
