package com.openpos.app.ui.sales

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openpos.app.data.entity.Sale
import com.openpos.app.data.entity.SaleItem
import com.openpos.app.ui.common.appViewModel
import com.openpos.app.util.DateFmt
import com.openpos.app.util.Money
import com.openpos.app.util.trimZeros

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(saleId: Long, onDone: () -> Unit) {
    val vm = appViewModel { app -> SalesViewModel(app.repository) }
    val context = LocalContext.current
    var sale by remember { mutableStateOf<Sale?>(null) }
    var items by remember { mutableStateOf<List<SaleItem>>(emptyList()) }

    LaunchedEffect(saleId) {
        sale = vm.getSale(saleId)
        items = vm.getItems(saleId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Receipt") }) },
        bottomBar = {
            Surface(tonalElevation = 4.dp) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            sale?.let { s ->
                                val text = buildReceiptText(s, items)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share receipt"))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Share")
                    }
                    Button(onClick = onDone, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Done")
                    }
                }
            }
        }
    ) { padding ->
        val s = sale
        if (s == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(Modifier.padding(padding).padding(16.dp)) {
            item {
                Text(s.invoiceNo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(DateFmt.full(s.createdAt), style = MaterialTheme.typography.bodySmall)
                if (!s.customerName.isNullOrBlank()) {
                    Text("Customer: ${s.customerName}")
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
            }
            items(items) { item ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(item.productName)
                        Text(
                            "${item.quantity.trimZeros()} × ${Money.format(item.unitPrice)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(Money.format(item.quantity * item.unitPrice - item.lineDiscount))
                }
            }
            item {
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                ReceiptTotalRow("Subtotal", s.subTotal)
                ReceiptTotalRow("Discount", -s.discountAmount)
                ReceiptTotalRow("Tax", s.taxAmount)
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                ReceiptTotalRow("Total", s.total, bold = true)
                ReceiptTotalRow("Paid (${s.paymentMethod})", s.amountPaid)
                if (s.changeDue > 0) ReceiptTotalRow("Change", s.changeDue)
                Spacer(Modifier.height(24.dp))
                Text(
                    "Thank you!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ReceiptTotalRow(label: String, amount: Double, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(Money.format(amount), fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

private fun buildReceiptText(sale: Sale, items: List<SaleItem>): String {
    val sb = StringBuilder()
    sb.appendLine(sale.invoiceNo)
    sb.appendLine(DateFmt.full(sale.createdAt))
    sb.appendLine("------------------------------")
    items.forEach {
        sb.appendLine("${it.productName}  x${it.quantity.trimZeros()}  ${Money.format(it.quantity * it.unitPrice - it.lineDiscount)}")
    }
    sb.appendLine("------------------------------")
    sb.appendLine("Subtotal: ${Money.format(sale.subTotal)}")
    sb.appendLine("Discount: ${Money.format(sale.discountAmount)}")
    sb.appendLine("Tax: ${Money.format(sale.taxAmount)}")
    sb.appendLine("Total: ${Money.format(sale.total)}")
    sb.appendLine("Paid (${sale.paymentMethod}): ${Money.format(sale.amountPaid)}")
    sb.appendLine("Thank you!")
    return sb.toString()
}
