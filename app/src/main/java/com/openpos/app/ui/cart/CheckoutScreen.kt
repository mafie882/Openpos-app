package com.openpos.app.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.openpos.app.util.Money

private val paymentMethods = listOf("Cash", "Card", "Mobile payment", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onComplete: (Long) -> Unit
) {
    var discountText by remember { mutableStateOf(if (cartViewModel.orderDiscount == 0.0) "" else cartViewModel.orderDiscount.toString()) }
    var amountPaidText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(paymentMethods.first()) }
    var customerName by remember { mutableStateOf(cartViewModel.customerName) }

    val discount = discountText.toDoubleOrNull() ?: 0.0
    LaunchedEffect(discount) { cartViewModel.orderDiscount = discount }

    val total = cartViewModel.grandTotal
    val amountPaid = amountPaidText.toDoubleOrNull() ?: 0.0
    val change = (amountPaid - total).coerceAtLeast(0.0)

    LaunchedEffect(cartViewModel.lastCompletedSaleId) {
        cartViewModel.lastCompletedSaleId?.let { id ->
            onComplete(id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Total due", style = MaterialTheme.typography.labelLarge)
            Text(Money.format(total), style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = discountText,
                onValueChange = { discountText = it },
                label = { Text("Order discount") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it; cartViewModel.customerName = it },
                label = { Text("Customer name (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Text("Payment method", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                paymentMethods.forEach { method ->
                    FilterChip(
                        selected = selectedMethod == method,
                        onClick = { selectedMethod = method },
                        label = { Text(method) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = amountPaidText,
                onValueChange = { amountPaidText = it },
                label = { Text("Amount received") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text("Change due: ${Money.format(change)}", style = MaterialTheme.typography.titleMedium)

            cartViewModel.errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    cartViewModel.errorMessage = null
                    // Non-cash methods (card/mobile/other) are assumed to be charged
                    // for the exact total; only cash tracks an amount tendered/change.
                    val paid = if (selectedMethod == "Cash") amountPaid else total
                    cartViewModel.checkout(paid, selectedMethod)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Complete sale")
            }
        }
    }
}
