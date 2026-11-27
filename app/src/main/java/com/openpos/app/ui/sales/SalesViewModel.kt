package com.openpos.app.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openpos.app.data.entity.Sale
import com.openpos.app.data.entity.SaleItem
import com.openpos.app.data.repository.PosRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SalesViewModel(private val repository: PosRepository) : ViewModel() {
    val sales: StateFlow<List<Sale>> = repository.observeSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getSale(id: Long): Sale? = repository.getSale(id)
    suspend fun getItems(saleId: Long): List<SaleItem> = repository.getSaleItems(saleId)

    /** How much of this sale item has already been refunded (0 if never). */
    suspend fun refundedQtyFor(saleItemId: Long): Double =
        repository.refundItemsForSaleItem(saleItemId).sumOf { it.quantity }

    fun refund(sale: Sale, lines: List<PosRepository.RefundLine>, note: String?, onDone: () -> Unit) {
        if (lines.isEmpty()) return
        viewModelScope.launch {
            repository.refundSale(sale, lines, note)
            onDone()
        }
    }
}
