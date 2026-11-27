package com.openpos.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * All data in OpenPOS lives in this local Room database only.
 * There is no server, no account and no license/subscription check —
 * every feature here is unlocked by default.
 */

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#1565C0",
    val sortOrder: Int = 0
)

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sku: String? = null,
    val barcode: String? = null,
    val categoryId: Long? = null,
    val sellPrice: Double,
    val costPrice: Double = 0.0,
    val taxPercent: Double = 0.0,
    val stockQty: Double = 0.0,
    val trackStock: Boolean = true,
    val lowStockThreshold: Double = 5.0,
    val isActive: Boolean = true,
    /** Path to a copy of the photo in this app's own internal storage — never a remote URL. */
    val imageUri: String? = null
)

@Entity(
    tableName = "sales",
    indices = [Index(value = ["customerId"]), Index(value = ["staffId"])]
)
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNo: String,
    val createdAt: Long,
    val subTotal: Double,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val total: Double,
    val amountPaid: Double,
    val changeDue: Double = 0.0,
    val paymentMethod: String = "Cash",
    val customerName: String? = null,
    val note: String? = null,
    val isVoided: Boolean = false,
    val customerId: Long? = null,
    val staffId: Long? = null,
    val staffName: String? = null,
    val refundedAmount: Double = 0.0
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["saleId"]), Index(value = ["productId"])]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long?,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val unitCost: Double = 0.0,
    val taxPercent: Double = 0.0,
    val lineDiscount: Double = 0.0
)

@Entity(tableName = "expense_categories")
data class ExpenseCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryName: String,
    val amount: Double,
    val note: String? = null,
    val createdAt: Long
)

@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]
)
data class StockMovement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val changeQty: Double,
    val reason: String,
    val note: String? = null,
    val createdAt: Long
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * A cart snapshot parked mid-sale (e.g. to serve another customer while
 * this one decides). [linesJson] is a small JSON blob of line items —
 * see HeldOrderCodec — rather than its own line-item table, since a held
 * order is a short-lived draft, not data anything else needs to query.
 */
@Entity(tableName = "held_orders")
data class HeldOrder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val linesJson: String,
    val orderDiscount: Double = 0.0,
    val customerName: String? = null,
    val note: String? = null,
    val createdAt: Long
)

@Entity(
    tableName = "refunds",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["saleId"])]
)
data class Refund(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val amount: Double,
    val note: String? = null,
    val createdAt: Long
)

@Entity(
    tableName = "refund_items",
    foreignKeys = [
        ForeignKey(
            entity = Refund::class,
            parentColumns = ["id"],
            childColumns = ["refundId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SaleItem::class,
            parentColumns = ["id"],
            childColumns = ["saleItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["refundId"]), Index(value = ["saleItemId"])]
)
data class RefundItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val refundId: Long,
    val saleItemId: Long,
    val productId: Long?,
    val productName: String,
    val quantity: Double,
    val amount: Double
)

/** A local, on-device staff login — a 4-digit PIN, never an internet account. */
@Entity(tableName = "staff_users")
data class StaffUser(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val pin: String,
    val role: String = "CASHIER", // "ADMIN" or "CASHIER"
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
