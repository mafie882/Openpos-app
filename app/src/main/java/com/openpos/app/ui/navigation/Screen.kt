package com.openpos.app.ui.navigation

sealed class Screen(val route: String) {
    data object Cart : Screen("cart")
    data object Products : Screen("products")
    data object ProductEdit : Screen("product_edit/{productId}") {
        fun create(productId: Long) = "product_edit/$productId"
    }
    data object Categories : Screen("categories")
    data object Checkout : Screen("checkout")
    data object Receipt : Screen("receipt/{saleId}") {
        fun create(saleId: Long) = "receipt/$saleId"
    }
    data object SalesHistory : Screen("sales_history")
    data object HeldOrders : Screen("held_orders")
    data object Customers : Screen("customers")
    data object CustomerDetail : Screen("customer_detail/{customerId}") {
        fun create(customerId: Long) = "customer_detail/$customerId"
    }
    data object Inventory : Screen("inventory")
    data object Expenses : Screen("expenses")
    data object ExpenseEdit : Screen("expense_edit/{expenseId}") {
        fun create(expenseId: Long) = "expense_edit/$expenseId"
    }
    data object Reports : Screen("reports")
    data object Settings : Screen("settings")
}

data class BottomDestination(
    val screen: Screen,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
