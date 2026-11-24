package com.openpos.app.ui.sales

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun SalesHistoryScreen(onOpenReceipt: (Long) -> Unit) {
    val vm = appViewModel { app -> SalesViewModel(app.repository) }
    val sales by vm.sales.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Sales history") }) }) { padding ->
        if (sales.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No sales yet.")
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(sales, key = { it.id }) { sale ->
                    ListItem(
                        headlineContent = { Text(sale.invoiceNo, fontWeight = FontWeight.Medium) },
                        supportingContent = { Text(DateFmt.full(sale.createdAt)) },
                        trailingContent = { Text(Money.format(sale.total), fontWeight = FontWeight.Bold) },
                        modifier = Modifier.clickable { onOpenReceipt(sale.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
