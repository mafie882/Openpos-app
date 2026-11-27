package com.openpos.app.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openpos.app.data.entity.StaffUser
import com.openpos.app.data.repository.PosRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StaffViewModel(private val repository: PosRepository) : ViewModel() {

    val staff: StateFlow<List<StaffUser>> = repository.observeStaff()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun byPin(pin: String): StaffUser? = repository.staffByPin(pin)

    fun saveStaff(staff: StaffUser, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveStaff(staff)
            onDone()
        }
    }

    fun deleteStaff(staff: StaffUser) {
        viewModelScope.launch { repository.deleteStaff(staff) }
    }
}
