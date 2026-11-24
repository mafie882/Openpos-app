package com.openpos.app.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.openpos.app.data.entity.Product
import com.openpos.app.ui.common.appViewModel
import com.openpos.app.ui.products.ProductViewModel
import com.openpos.app.util.trimZeros

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen() {
    val vm = appViewModel { app -> ProductViewModel(app.repository) }
    val products by vm.products.collectAsState()
    val tracked = products.filter { it.isActive && it.trackStock }
    var restockTarget by remember { mutableStateOf<Product?>(null) }
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(topBar = { TopAppBar(title = { Text("Inventory") }) }) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("All stock") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Low stock") })
            }

            val list = if (tab == 0) tracked else tracked.filter { it.stockQty <= it.lowStockThreshold }

            if (list.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (tab == 1) "Nothing is low on stock." else "No stock-tracked products yet.")
                }
            } else {
                LazyColumn {
                    items(list, key = { it.id }) { product ->
                        val low = product.stockQty <= product.lowStockThreshold
                        ListItem(
                            headlineContent = { Text(product.name) },
                            supportingContent = {
                                Text(
                                    "On hand: ${product.stockQty.trimZeros()}",
                                    color = if (low) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingContent = {
                                OutlinedButton(onClick = { restockTarget = product }) {
                                    Text("Restock")
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    restockTarget?.let { product ->
        RestockDialog(
            product = product,
            onDismiss = { restockTarget = null },
            onConfirm = { qty, note ->
                vm.restock(product, qty, note)
                restockTarget = null
            }
        )
    }
}

@Composable
private fun RestockDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Double, String?) -> Unit
) {
    var qtyText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restock ${product.name}") },
        text = {
            Column {
                Text("Current stock: ${product.stockQty.trimZeros()}", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("Quantity to add") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional, e.g. supplier)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val qty = qtyText.toDoubleOrNull() ?: 0.0
                if (qty > 0) onConfirm(qty, note.ifBlank { null })
            }) { Text("Add stock") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
