package com.openpos.app.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openpos.app.util.Money
import com.openpos.app.util.trimZeros

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    onOpenHeldOrders: () -> Unit = {}
) {
    val lines by cartViewModel.lines
    var showHoldDialog by remember { mutableStateOf(false) }

    if (showHoldDialog) {
        var label by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showHoldDialog = false },
            title = { Text("Hold this order") },
            text = {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (e.g. \"Table 4\")") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showHoldDialog = false
                    cartViewModel.holdCurrentOrder(label, onDone = onBack)
                }) { Text("Hold") }
            },
            dismissButton = {
                TextButton(onClick = { showHoldDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cart") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = onOpenHeldOrders) {
                        Icon(Icons.Default.PauseCircle, contentDescription = "Held orders")
                    }
                    if (lines.isNotEmpty()) {
                        TextButton(onClick = { showHoldDialog = true }) { Text("Hold") }
                        TextButton(onClick = { cartViewModel.clearCart() }) { Text("Clear") }
                    }
                }
            )
        },
        bottomBar = {
            if (lines.isNotEmpty()) {
                Surface(tonalElevation = 4.dp) {
                    Column(Modifier.padding(16.dp)) {
                        TotalsRow("Subtotal", cartViewModel.subTotal)
                        TotalsRow("Tax", cartViewModel.taxTotal)
                        TotalsRow("Discount", -cartViewModel.orderDiscount)
                        HorizontalDivider(Modifier.padding(vertical = 6.dp))
                        TotalsRow("Total", cartViewModel.grandTotal, bold = true)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth()) {
                            Text("Proceed to checkout")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (lines.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Cart is empty. Go back and tap a product to add it.")
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(lines.size) { index ->
                    val line = lines[index]
                    ListItem(
                        headlineContent = { Text(line.name, fontWeight = FontWeight.Medium) },
                        supportingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    cartViewModel.setQuantity(index, line.quantity - 1)
                                }) { Text("–", style = MaterialTheme.typography.titleLarge) }
                                Text(line.quantity.trimZeros(), modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(onClick = {
                                    cartViewModel.setQuantity(index, line.quantity + 1)
                                }) { Text("+", style = MaterialTheme.typography.titleLarge) }
                                Spacer(Modifier.width(8.dp))
                                Text("× ${Money.format(line.unitPrice)}")
                            }
                        },
                        trailingContent = {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(Money.format(line.total), fontWeight = FontWeight.Bold)
                                IconButton(onClick = { cartViewModel.removeLine(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun TotalsRow(label: String, amount: Double, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(
            Money.format(amount),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
    }
}
