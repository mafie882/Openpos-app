package com.openpos.app.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openpos.app.data.entity.Customer
import com.openpos.app.data.entity.Sale
import com.openpos.app.data.repository.PosRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomerViewModel(private val repository: PosRepository) : ViewModel() {

    val customers: StateFlow<List<Customer>> = repository.observeCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getCustomer(id: Long): Customer? = repository.getCustomer(id)
    suspend fun salesFor(customerId: Long): List<Sale> = repository.salesForCustomer(customerId)

    fun saveCustomer(customer: Customer, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.saveCustomer(customer)
            onDone(id)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch { repository.deleteCustomer(customer) }
    }
}
