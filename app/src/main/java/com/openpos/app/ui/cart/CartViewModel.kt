package com.openpos.app.ui.cart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openpos.app.data.entity.Product
import com.openpos.app.data.repository.PosRepository
import kotlinx.coroutines.launch

data class CartLineUi(
    val product: Product?,
    val name: String,
    var quantity: Double,
    val unitPrice: Double,
    val unitCost: Double,
    val taxPercent: Double,
    var lineDiscount: Double = 0.0
) {
    val subtotal get() = quantity * unitPrice
    val tax get() = (subtotal - lineDiscount) * (taxPercent / 100.0)
    val total get() = subtotal - lineDiscount + tax
}

/**
 * Lives at NavGraph scope so Products, Cart and Checkout all share one cart.
 */
class CartViewModel(private val repository: PosRepository) : ViewModel() {

    var lines = mutableStateOf<List<CartLineUi>>(emptyList())
        private set

    var orderDiscount by mutableStateOf(0.0)
    var customerName by mutableStateOf("")
    var note by mutableStateOf("")
    var lastCompletedSaleId by mutableStateOf<Long?>(null)
    var errorMessage by mutableStateOf<String?>(null)

    val subTotal get() = lines.value.sumOf { it.subtotal }
    val taxTotal get() = lines.value.sumOf { it.tax }
    val grandTotal get() = (subTotal - orderDiscount + taxTotal).coerceAtLeast(0.0)
    val itemCount get() = lines.value.sumOf { it.quantity }

    fun addProduct(product: Product) {
        val existing = lines.value.toMutableList()
        val idx = existing.indexOfFirst { it.product?.id == product.id }
        if (idx >= 0) {
            val line = existing[idx]
            existing[idx] = line.copy(quantity = line.quantity + 1)
        } else {
            existing.add(
                CartLineUi(
                    product = product,
                    name = product.name,
                    quantity = 1.0,
                    unitPrice = product.sellPrice,
                    unitCost = product.costPrice,
                    taxPercent = product.taxPercent
                )
            )
        }
        lines.value = existing
    }

    fun setQuantity(index: Int, quantity: Double) {
        val existing = lines.value.toMutableList()
        if (index in existing.indices) {
            if (quantity <= 0) {
                existing.removeAt(index)
            } else {
                existing[index] = existing[index].copy(quantity = quantity)
            }
            lines.value = existing
        }
    }

    fun removeLine(index: Int) {
        val existing = lines.value.toMutableList()
        if (index in existing.indices) {
            existing.removeAt(index)
            lines.value = existing
        }
    }

    fun clearCart() {
        lines.value = emptyList()
        orderDiscount = 0.0
        customerName = ""
        note = ""
    }

    fun checkout(amountPaid: Double, paymentMethod: String) {
        if (lines.value.isEmpty()) {
            errorMessage = "Cart is empty."
            return
        }
        if (amountPaid < grandTotal) {
            errorMessage = "Amount paid is less than the total due."
            return
        }
        viewModelScope.launch {
            val repoLines = lines.value.map {
                PosRepository.CartLine(
                    product = it.product,
                    name = it.name,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice,
                    unitCost = it.unitCost,
                    taxPercent = it.taxPercent,
                    lineDiscount = it.lineDiscount
                )
            }
            val saleId = repository.completeSale(
                lines = repoLines,
                discountAmount = orderDiscount,
                amountPaid = amountPaid,
                paymentMethod = paymentMethod,
                customerName = customerName.ifBlank { null },
                note = note.ifBlank { null }
            )
            lastCompletedSaleId = saleId
            clearCart()
        }
    }
}
