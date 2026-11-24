package com.openpos.app.ui.products

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openpos.app.data.entity.Category
import com.openpos.app.data.entity.Product
import com.openpos.app.data.repository.PosRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: PosRepository) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.observeAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStock: StateFlow<List<Product>> = repository.observeLowStock()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var searchQuery by mutableStateOf("")
    var selectedCategoryId by mutableStateOf<Long?>(null)

    fun filtered(products: List<Product>): List<Product> {
        return products.filter { p ->
            p.isActive &&
                (selectedCategoryId == null || p.categoryId == selectedCategoryId) &&
                (searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    (p.sku?.contains(searchQuery, ignoreCase = true) == true) ||
                    (p.barcode?.contains(searchQuery, ignoreCase = true) == true))
        }
    }

    suspend fun getProduct(id: Long): Product? = repository.getProduct(id)

    fun saveProduct(product: Product, onDone: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.saveProduct(product)
            onDone(id)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch { repository.deleteProduct(product) }
    }

    fun restock(product: Product, qty: Double, note: String?) {
        viewModelScope.launch { repository.restock(product, qty, note) }
    }

    fun adjustStock(product: Product, newQty: Double, note: String?) {
        viewModelScope.launch { repository.adjustStock(product, newQty, note) }
    }
}
