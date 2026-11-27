package com.openpos.app.data.dao

import androidx.room.*
import com.openpos.app.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder, name")
    fun observeAll(): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY sortOrder, name")
    suspend fun getAll(): List<Category>

    @Insert
    suspend fun insert(category: Category): Long

    @Insert
    suspend fun insertAll(categories: List<Category>)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name")
    fun observeActive(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name")
    fun observeAll(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name")
    suspend fun getAll(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): Product?

    @Query("SELECT * FROM products WHERE barcode = :barcode AND isActive = 1 LIMIT 1")
    suspend fun getByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE isActive = 1 AND trackStock = 1 AND stockQty <= lowStockThreshold ORDER BY stockQty")
    fun observeLowStock(): Flow<List<Product>>

    @Insert
    suspend fun insert(product: Product): Long

    @Insert
    suspend fun insertAll(products: List<Product>)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("UPDATE products SET stockQty = stockQty + :delta WHERE id = :productId")
    suspend fun adjustStock(productId: Long, delta: Double)
}

@Dao
interface SaleDao {
    @Transaction
    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getById(id: Long): Sale?

    @Query("SELECT * FROM sales WHERE createdAt BETWEEN :from AND :to AND isVoided = 0 ORDER BY createdAt DESC")
    suspend fun getBetween(from: Long, to: Long): List<Sale>

    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    suspend fun getAll(): List<Sale>

    @Query("SELECT COUNT(*) FROM sales")
    suspend fun count(): Int

    @Insert
    suspend fun insert(sale: Sale): Long

    @Insert
    suspend fun insertAll(sales: List<Sale>)

    @Update
    suspend fun update(sale: Sale)
}

@Dao
interface SaleItemDao {
    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getForSale(saleId: Long): List<SaleItem>

    @Query("SELECT * FROM sale_items")
    suspend fun getAll(): List<SaleItem>

    @Insert
    suspend fun insert(item: SaleItem): Long

    @Insert
    suspend fun insertAll(items: List<SaleItem>)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE createdAt BETWEEN :from AND :to ORDER BY createdAt DESC")
    suspend fun getBetween(from: Long, to: Long): List<Expense>

    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    suspend fun getAll(): List<Expense>

    @Insert
    suspend fun insert(expense: Expense): Long

    @Insert
    suspend fun insertAll(expenses: List<Expense>)

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}

@Dao
interface ExpenseCategoryDao {
    @Query("SELECT * FROM expense_categories ORDER BY name")
    fun observeAll(): Flow<List<ExpenseCategory>>

    @Query("SELECT * FROM expense_categories ORDER BY name")
    suspend fun getAll(): List<ExpenseCategory>

    @Insert
    suspend fun insert(category: ExpenseCategory): Long

    @Insert
    suspend fun insertAll(categories: List<ExpenseCategory>)

    @Delete
    suspend fun delete(category: ExpenseCategory)
}

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY createdAt DESC")
    fun observeForProduct(productId: Long): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements ORDER BY createdAt DESC LIMIT 200")
    fun observeRecent(): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements")
    suspend fun getAll(): List<StockMovement>

    @Insert
    suspend fun insert(movement: StockMovement): Long

    @Insert
    suspend fun insertAll(movements: List<StockMovement>)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name")
    fun observeAll(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY name")
    suspend fun getAll(): List<Customer>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Long): Customer?

    @Insert
    suspend fun insert(customer: Customer): Long

    @Insert
    suspend fun insertAll(customers: List<Customer>)

    @Update
    suspend fun update(customer: Customer)

    @Delete
    suspend fun delete(customer: Customer)
}

@Dao
interface HeldOrderDao {
    @Query("SELECT * FROM held_orders ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<HeldOrder>>

    @Query("SELECT * FROM held_orders")
    suspend fun getAll(): List<HeldOrder>

    @Insert
    suspend fun insert(order: HeldOrder): Long

    @Insert
    suspend fun insertAll(orders: List<HeldOrder>)

    @Delete
    suspend fun delete(order: HeldOrder)
}

@Dao
interface RefundDao {
    @Query("SELECT * FROM refunds WHERE saleId = :saleId ORDER BY createdAt DESC")
    suspend fun getForSale(saleId: Long): List<Refund>

    @Query("SELECT * FROM refunds")
    suspend fun getAll(): List<Refund>

    @Insert
    suspend fun insert(refund: Refund): Long

    @Insert
    suspend fun insertAll(refunds: List<Refund>)
}

@Dao
interface RefundItemDao {
    @Query("SELECT * FROM refund_items WHERE refundId = :refundId")
    suspend fun getForRefund(refundId: Long): List<RefundItem>

    @Query("SELECT * FROM refund_items WHERE saleItemId = :saleItemId")
    suspend fun getForSaleItem(saleItemId: Long): List<RefundItem>

    @Query("SELECT * FROM refund_items")
    suspend fun getAll(): List<RefundItem>

    @Insert
    suspend fun insert(item: RefundItem): Long

    @Insert
    suspend fun insertAll(items: List<RefundItem>)
}

@Dao
interface StaffUserDao {
    @Query("SELECT * FROM staff_users ORDER BY name")
    fun observeAll(): Flow<List<StaffUser>>

    @Query("SELECT * FROM staff_users ORDER BY name")
    suspend fun getAll(): List<StaffUser>

    @Query("SELECT * FROM staff_users WHERE pin = :pin AND isActive = 1 LIMIT 1")
    suspend fun getByPin(pin: String): StaffUser?

    @Insert
    suspend fun insert(staff: StaffUser): Long

    @Insert
    suspend fun insertAll(staff: List<StaffUser>)

    @Update
    suspend fun update(staff: StaffUser)

    @Delete
    suspend fun delete(staff: StaffUser)
}
