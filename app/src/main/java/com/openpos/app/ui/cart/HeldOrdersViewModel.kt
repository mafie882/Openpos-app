package com.openpos.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openpos.app.data.entity.HeldOrder
import com.openpos.app.data.repository.PosRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HeldOrdersViewModel(private val repository: PosRepository) : ViewModel() {

    val heldOrders: StateFlow<List<HeldOrder>> = repository.observeHeldOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(order: HeldOrder) {
        viewModelScope.launch { repository.deleteHeldOrder(order) }
    }
}
