package com.openpos.app.data.repository

import com.openpos.app.data.AppDatabase
import com.openpos.app.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.random.Random

/**
 * Single access point for all local data. No network calls anywhere
 * in this class on purpose — everything reads/writes the local Room DB.
 */
class PosRepository(private val db: AppDatabase) {

    // ---- Categories ----
    fun observeCategories(): Flow<List<Category>> = db.categoryDao().observeAll()
    suspend fun allCategories() = db.categoryDao().getAll()
    suspend fun saveCategory(category: Category) =
        if (category.id == 0L) db.categoryDao().insert(category) else {
            db.categoryDao().update(category); category.id
        }
    suspend fun deleteCategory(category: Category) = db.categoryDao().delete(category)

    // ---- Products ----
    fun observeActiveProducts(): Flow<List<Product>> = db.productDao().observeActive()
    fun observeAllProducts(): Flow<List<Product>> = db.productDao().observeAll()
    fun observeLowStock(): Flow<List<Product>> = db.productDao().observeLowStock()
    suspend fun allProducts() = db.productDao().getAll()
    suspend fun getProduct(id: Long) = db.productDao().getById(id)
    suspend fun getProductByBarcode(barcode: String) = db.productDao().getByBarcode(barcode)

    suspend fun saveProduct(product: Product): Long =
        if (product.id == 0L) db.productDao().insert(product) else {
            db.productDao().update(product); product.id
        }

    suspend fun deleteProduct(product: Product) = db.productDao().delete(product)

    suspend fun restock(product: Product, addQty: Double, note: String?) {
        db.productDao().adjustStock(product.id, addQty)
        db.stockMovementDao().insert(
            StockMovement(
                productId = product.id,
                productName = product.name,
                changeQty = addQty,
                reason = "Restock",
                note = note,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun adjustStock(product: Product, newQty: Double, note: String?) {
        val delta = newQty - product.stockQty
        db.productDao().adjustStock(product.id, delta)
        db.stockMovementDao().insert(
            StockMovement(
                productId = product.id,
                productName = product.name,
                changeQty = delta,
                reason = "Adjustment",
                note = note,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    fun observeRecentStockMovements(): Flow<List<StockMovement>> = db.stockMovementDao().observeRecent()

    // ---- Sales (checkout) ----
    data class CartLine(
        val product: Product?,
        val name: String,
        val quantity: Double,
        val unitPrice: Double,
        val unitCost: Double,
        val taxPercent: Double,
        val lineDiscount: Double
    ) {
        val lineSubtotal get() = quantity * unitPrice
        val lineTax get() = (lineSubtotal - lineDiscount) * (taxPercent / 100.0)
        val lineTotal get() = lineSubtotal - lineDiscount + lineTax
    }

    suspend fun completeSale(
        lines: List<CartLine>,
        discountAmount: Double,
        amountPaid: Double,
        paymentMethod: String,
        customerName: String?,
        note: String?
    ): Long {
        val subTotal = lines.sumOf { it.lineSubtotal }
        val taxAmount = lines.sumOf { it.lineTax }
        val total = (subTotal - discountAmount + taxAmount).coerceAtLeast(0.0)
        val invoiceNo = generateInvoiceNo()

        val sale = Sale(
            invoiceNo = invoiceNo,
            createdAt = System.currentTimeMillis(),
            subTotal = subTotal,
            discountAmount = discountAmount,
            taxAmount = taxAmount,
            total = total,
            amountPaid = amountPaid,
            changeDue = (amountPaid - total).coerceAtLeast(0.0),
            paymentMethod = paymentMethod,
            customerName = customerName,
            note = note
        )
        val saleId = db.saleDao().insert(sale)

        for (line in lines) {
            db.saleItemDao().insert(
                SaleItem(
                    saleId = saleId,
                    productId = line.product?.id,
                    productName = line.name,
                    quantity = line.quantity,
                    unitPrice = line.unitPrice,
                    unitCost = line.unitCost,
                    taxPercent = line.taxPercent,
                    lineDiscount = line.lineDiscount
                )
            )
            val product = line.product
            if (product != null && product.trackStock) {
                db.productDao().adjustStock(product.id, -line.quantity)
                db.stockMovementDao().insert(
                    StockMovement(
                        productId = product.id,
                        productName = product.name,
                        changeQty = -line.quantity,
                        reason = "Sale",
                        note = "Invoice $invoiceNo",
                        createdAt = sale.createdAt
                    )
                )
            }
        }
        return saleId
    }

    private suspend fun generateInvoiceNo(): String {
        val count = db.saleDao().count() + 1
        val suffix = Random.nextInt(100, 999)
        return String.format(Locale.US, "INV-%06d-%d", count, suffix)
    }

    fun observeSales(): Flow<List<Sale>> = db.saleDao().observeAll()
    suspend fun getSale(id: Long) = db.saleDao().getById(id)
    suspend fun getSaleItems(saleId: Long) = db.saleItemDao().getForSale(saleId)
    suspend fun salesBetween(from: Long, to: Long) = db.saleDao().getBetween(from, to)
    suspend fun allSales() = db.saleDao().getAll()
    suspend fun allSaleItems() = db.saleItemDao().getAll()

    // ---- Expenses ----
    fun observeExpenses(): Flow<List<Expense>> = db.expenseDao().observeAll()
    suspend fun allExpenses() = db.expenseDao().getAll()
    suspend fun expensesBetween(from: Long, to: Long) = db.expenseDao().getBetween(from, to)
    suspend fun saveExpense(expense: Expense) =
        if (expense.id == 0L) db.expenseDao().insert(expense) else {
            db.expenseDao().update(expense); expense.id
        }
    suspend fun deleteExpense(expense: Expense) = db.expenseDao().delete(expense)

    fun observeExpenseCategories(): Flow<List<ExpenseCategory>> = db.expenseCategoryDao().observeAll()
    suspend fun allExpenseCategories() = db.expenseCategoryDao().getAll()
    suspend fun addExpenseCategory(name: String) = db.expenseCategoryDao().insert(ExpenseCategory(name = name))
    suspend fun deleteExpenseCategory(category: ExpenseCategory) = db.expenseCategoryDao().delete(category)

    // ---- Backup / restore (raw access for BackupManager) ----
    suspend fun wipeAndRestore(
        categories: List<Category>,
        products: List<Product>,
        sales: List<Sale>,
        saleItems: List<SaleItem>,
        expenses: List<Expense>,
        expenseCategories: List<ExpenseCategory>,
        stockMovements: List<StockMovement>
    ) = withContext(Dispatchers.IO) {
        // clearAllTables() is a blocking call, so this whole restore runs
        // off the main thread.
        db.clearAllTables()
        // Inserted with their original (nonzero) ids preserved, so that
        // products.categoryId / saleItems.saleId foreign keys still line up.
        db.categoryDao().insertAll(categories)
        db.productDao().insertAll(products)
        db.saleDao().insertAll(sales)
        db.saleItemDao().insertAll(saleItems)
        db.expenseDao().insertAll(expenses)
        db.expenseCategoryDao().insertAll(expenseCategories)
        db.stockMovementDao().insertAll(stockMovements)
        Unit
    }

    fun rawDb(): AppDatabase = db
}
