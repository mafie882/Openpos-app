package com.openpos.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.openpos.app.data.dao.*
import com.openpos.app.data.entity.*

@Database(
    entities = [
        Category::class,
        Product::class,
        Sale::class,
        SaleItem::class,
        Expense::class,
        ExpenseCategory::class,
        StockMovement::class,
        Customer::class,
        HeldOrder::class,
        Refund::class,
        RefundItem::class,
        StaffUser::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun expenseCategoryDao(): ExpenseCategoryDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun customerDao(): CustomerDao
    abstract fun heldOrderDao(): HeldOrderDao
    abstract fun refundDao(): RefundDao
    abstract fun refundItemDao(): RefundItemDao
    abstract fun staffUserDao(): StaffUserDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "openpos.db"
                )
                    // This app has no server to migrate against, and early
                    // adopters' local data is small — on a schema bump we
                    // just recreate the tables rather than hand-writing a
                    // migration path. A newer release can add a real
                    // Migration here later if that ever matters.
                    .fallbackToDestructiveMigration(true)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
