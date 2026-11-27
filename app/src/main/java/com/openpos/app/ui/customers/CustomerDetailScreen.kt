package com.openpos.app.ui.customers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openpos.app.data.entity.Customer
import com.openpos.app.data.entity.Sale
import com.openpos.app.ui.common.appViewModel
import com.openpos.app.util.DateFmt
import com.openpos.app.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: Long,
    onBack: () -> Unit,
    onOpenReceipt: (Long) -> Unit
) {
    val vm = appViewModel { app -> CustomerViewModel(app.repository) }
    var customer by remember { mutableStateOf<Customer?>(null) }
    var sales by remember { mutableStateOf<List<Sale>>(emptyList()) }
    var showEditDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(customerId, refreshTrigger) {
        customer = vm.getCustomer(customerId)
        sales = vm.salesFor(customerId).sortedByDescending { it.createdAt }
    }

    if (showEditDialog) {
        CustomerEditDialog(
            initial = customer,
            onDismiss = { showEditDialog = false },
            onSave = {
                vm.saveCustomer(it) {
                    showEditDialog = false
                    refreshTrigger++
                }
            }
        )
    }

    val totalSpent = sales.filter { !it.isVoided }.sumOf { it.total - it.refundedAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer?.name ?: "Customer") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            customer?.let { c ->
                Column(Modifier.padding(16.dp)) {
                    val contact = listOfNotNull(c.phone, c.email).joinToString(" · ")
                    if (contact.isNotBlank()) Text(contact, style = MaterialTheme.typography.bodyMedium)
                    if (!c.note.isNullOrBlank()) Text(c.note, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Lifetime spend: ${Money.format(totalSpent)} · ${sales.size} sale(s)",
                        fontWeight = FontWeight.Medium
                    )
                }
                HorizontalDivider()
            }
            if (sales.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No sales for this customer yet.")
                }
            } else {
                LazyColumn {
                    items(sales, key = { it.id }) { sale ->
                        ListItem(
                            headlineContent = { Text(sale.invoiceNo, fontWeight = FontWeight.Medium) },
                            supportingContent = { Text(DateFmt.full(sale.createdAt)) },
                            trailingContent = {
                                Text(Money.format(sale.total - sale.refundedAmount), fontWeight = FontWeight.Bold)
                            },
                            modifier = Modifier.clickable { onOpenReceipt(sale.id) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
