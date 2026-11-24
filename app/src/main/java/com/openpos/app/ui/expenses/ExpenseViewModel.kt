package com.openpos.app.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openpos.app.data.entity.Expense
import com.openpos.app.data.entity.ExpenseCategory
import com.openpos.app.data.repository.PosRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repository: PosRepository) : ViewModel() {
    val expenses: StateFlow<List<Expense>> = repository.observeExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<ExpenseCategory>> = repository.observeExpenseCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(expense: Expense) {
        viewModelScope.launch { repository.saveExpense(expense) }
    }

    fun delete(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    fun addCategory(name: String) {
        viewModelScope.launch { repository.addExpenseCategory(name) }
    }
}
