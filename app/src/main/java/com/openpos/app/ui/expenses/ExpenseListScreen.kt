package com.openpos.app.ui.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.openpos.app.ui.common.appViewModel
import com.openpos.app.util.DateFmt
import com.openpos.app.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(onAdd: () -> Unit, onEdit: (Long) -> Unit) {
    val vm = appViewModel { app -> ExpenseViewModel(app.repository) }
    val expenses by vm.expenses.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Expenses") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, contentDescription = "Add expense") }
        }
    ) { padding ->
        if (expenses.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No expenses recorded yet.")
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(expenses, key = { it.id }) { expense ->
                    ListItem(
                        headlineContent = { Text(expense.categoryName, fontWeight = FontWeight.Medium) },
                        supportingContent = {
                            Text(expense.note?.takeIf { it.isNotBlank() } ?: DateFmt.day(expense.createdAt))
                        },
                        trailingContent = { Text(Money.format(expense.amount), fontWeight = FontWeight.Bold) },
                        modifier = Modifier.clickable { onEdit(expense.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
