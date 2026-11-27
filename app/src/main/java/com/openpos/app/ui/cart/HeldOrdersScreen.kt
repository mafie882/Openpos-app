package com.openpos.app.ui.cart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openpos.app.data.HeldOrderCodec
import com.openpos.app.data.entity.HeldOrder
import com.openpos.app.ui.common.appViewModel
import com.openpos.app.util.Money
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Orders parked mid-sale (e.g. to serve another customer) via the "Hold"
 * button on the Cart screen. Tapping one loads it back into the active
 * cart; nothing here touches the network or needs an account.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeldOrdersScreen(
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onResumed: () -> Unit
) {
    val vm = appViewModel { app -> HeldOrdersViewModel(app.repository) }
    val heldOrders by vm.heldOrders.collectAsState()
    var pendingResume by remember { mutableStateOf<HeldOrder?>(null) }
    val cartHasItems = cartViewModel.lines.value.isNotEmpty()

    pendingResume?.let { order ->
        AlertDialog(
            onDismissRequest = { pendingResume = null },
            title = { Text("Replace current cart?") },
            text = { Text("Your current cart isn't empty. Resuming \"${order.label}\" will replace it.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingResume = null
                    cartViewModel.resumeHeldOrder(order, onDone = onResumed)
                }) { Text("Replace") }
            },
            dismissButton = {
                TextButton(onClick = { pendingResume = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Held orders") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        if (heldOrders.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No orders on hold. Park a cart with the Hold button from the Cart screen.")
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(heldOrders, key = { it.id }) { order ->
                    val decoded = HeldOrderCodec.decode(order.linesJson)
                    val lineCount = decoded.size
                    val total = (decoded.sumOf { it.quantity * it.unitPrice } - order.orderDiscount)
                        .coerceAtLeast(0.0)
                    ListItem(
                        headlineContent = { Text(order.label, fontWeight = FontWeight.Medium) },
                        supportingContent = {
                            Text(
                                "$lineCount item(s) · ${formatTime(order.createdAt)}" +
                                    (order.customerName?.let { " · $it" } ?: "")
                            )
                        },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(Money.format(total), fontWeight = FontWeight.Bold)
                                IconButton(onClick = { vm.delete(order) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Discard")
                                }
                            }
                        },
                        modifier = Modifier.clickable {
                            if (cartHasItems) {
                                pendingResume = order
                            } else {
                                cartViewModel.resumeHeldOrder(order, onDone = onResumed)
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun formatTime(epochMillis: Long): String =
    SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(epochMillis))
