package com.openpos.app.data

import android.content.Context
import android.net.Uri
import com.google.gson.GsonBuilder
import com.openpos.app.data.entity.*
import com.openpos.app.data.repository.PosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.io.InputStreamReader

/**
 * Exports / imports a single human-readable JSON file containing every
 * table in the database. This is the only "sync" this app has — a file
 * you choose to create and move yourself. Nothing is ever uploaded.
 */
class BackupManager(private val repository: PosRepository) {

    data class BackupFile(
        val formatVersion: Int = 2,
        val exportedAt: Long = System.currentTimeMillis(),
        val categories: List<Category>,
        val products: List<Product>,
        val sales: List<Sale>,
        val saleItems: List<SaleItem>,
        val expenses: List<Expense>,
        val expenseCategories: List<ExpenseCategory>,
        val stockMovements: List<StockMovement>,
        val customers: List<Customer> = emptyList(),
        val heldOrders: List<HeldOrder> = emptyList(),
        val refunds: List<Refund> = emptyList(),
        val refundItems: List<RefundItem> = emptyList(),
        val staff: List<StaffUser> = emptyList()
    )

    private val gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportTo(context: Context, uri: Uri) {
        val backup = BackupFile(
            categories = repository.allCategories(),
            products = repository.allProducts(),
            sales = repository.allSales(),
            saleItems = repository.allSaleItems(),
            expenses = repository.allExpenses(),
            expenseCategories = repository.allExpenseCategories(),
            stockMovements = repository.rawDb().stockMovementDao().getAll(),
            customers = repository.allCustomers(),
            heldOrders = repository.rawDb().heldOrderDao().getAll(),
            refunds = repository.rawDb().refundDao().getAll(),
            refundItems = repository.rawDb().refundItemDao().getAll(),
            staff = repository.allStaff()
        )
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                OutputStreamWriter(out).use { writer -> gson.toJson(backup, writer) }
            }
        }
    }

    suspend fun importFrom(context: Context, uri: Uri) {
        val backup: BackupFile = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                InputStreamReader(input).use { reader -> gson.fromJson(reader, BackupFile::class.java) }
            } ?: throw IllegalArgumentException("Could not read backup file")
        }

        repository.wipeAndRestore(
            categories = backup.categories,
            products = backup.products,
            sales = backup.sales,
            saleItems = backup.saleItems,
            expenses = backup.expenses,
            expenseCategories = backup.expenseCategories,
            stockMovements = backup.stockMovements,
            customers = backup.customers,
            heldOrders = backup.heldOrders,
            refunds = backup.refunds,
            refundItems = backup.refundItems,
            staff = backup.staff
        )
    }
}
