package com.openpos.app.ui.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.openpos.app.data.entity.Expense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditScreen(expenseId: Long, onDone: () -> Unit) {
    val vm = com.openpos.app.ui.common.appViewModel { app -> ExpenseViewModel(app.repository) }
    val expenses by vm.expenses.collectAsState()
    val categories by vm.categories.collectAsState()
    val existing = remember(expenseId, expenses) { expenses.find { it.id == expenseId } }

    var categoryName by remember(existing) { mutableStateOf(existing?.categoryName ?: categories.firstOrNull()?.name ?: "") }
    var amountText by remember(existing) { mutableStateOf(existing?.amount?.toString() ?: "") }
    var note by remember(existing) { mutableStateOf(existing?.note ?: "") }
    var newCategoryText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (expenseId == 0L) "New expense" else "Edit expense") },
                navigationIcon = {
                    IconButton(onClick = onDone) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (existing != null) {
                        IconButton(onClick = { vm.delete(existing); onDone() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Category", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = categoryName == cat.name,
                        onClick = { categoryName = cat.name },
                        label = { Text(cat.name) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newCategoryText,
                    onValueChange = { newCategoryText = it },
                    label = { Text("New category") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = {
                    if (newCategoryText.isNotBlank()) {
                        vm.addCategory(newCategoryText.trim())
                        categoryName = newCategoryText.trim()
                        newCategoryText = ""
                    }
                }) { Text("Add") }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount*") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0 || categoryName.isBlank()) return@Button
                    vm.save(
                        Expense(
                            id = expenseId,
                            categoryName = categoryName,
                            amount = amount,
                            note = note.ifBlank { null },
                            createdAt = existing?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                    onDone()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save expense") }
        }
    }
}
