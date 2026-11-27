package com.openpos.app.ui.sales

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openpos.app.data.entity.Sale
import com.openpos.app.data.entity.SaleItem
import com.openpos.app.data.repository.PosRepository
import com.openpos.app.ui.common.appViewModel
import com.openpos.app.util.BtPrinter
import com.openpos.app.util.DateFmt
import com.openpos.app.util.EscPos
import com.openpos.app.util.Money
import com.openpos.app.util.trimZeros
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(saleId: Long, onDone: () -> Unit) {
    val vm = appViewModel { app -> SalesViewModel(app.repository) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sale by remember { mutableStateOf<Sale?>(null) }
    var items by remember { mutableStateOf<List<SaleItem>>(emptyList()) }
    var alreadyRefunded by remember { mutableStateOf<Map<Long, Double>>(emptyMap()) }
    var showRefundDialog by remember { mutableStateOf(false) }
    var showPrinterPicker by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(saleId, refreshTrigger) {
        sale = vm.getSale(saleId)
        items = vm.getItems(saleId)
        alreadyRefunded = items.associate { it.id to vm.refundedQtyFor(it.id) }
    }

    if (showRefundDialog) {
        sale?.let { s ->
            RefundDialog(
                items = items,
                alreadyRefunded = alreadyRefunded,
                onDismiss = { showRefundDialog = false },
                onConfirm = { lines, note ->
                    vm.refund(s, lines, note) {
                        showRefundDialog = false
                        refreshTrigger++
                    }
                }
            )
        }
    }

    if (showPrinterPicker) {
        sale?.let { s ->
            PrinterPickerDialog(
                onDismiss = { showPrinterPicker = false },
                onPrinterChosen = { address ->
                    showPrinterPicker = false
                    scope.launch {
                        val bytes = EscPos.buildReceipt("OpenPOS", s, items)
                        val result = BtPrinter.print(context, address, bytes)
                        val msg = if (result.isSuccess) "Sent to printer" else "Couldn't reach printer"
                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt") },
                actions = {
                    IconButton(onClick = { showPrinterPicker = true }) {
                        Icon(Icons.Default.Print, contentDescription = "Print receipt")
                    }
                    val canRefund = sale?.let { it.refundedAmount < it.total } ?: false
                    if (canRefund) {
                        IconButton(onClick = { showRefundDialog = true }) {
                            Icon(Icons.Default.Undo, contentDescription = "Refund")
                        }
                    }
                }
            )
        },
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
                if (s.refundedAmount > 0) ReceiptTotalRow("Refunded", -s.refundedAmount)
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

/**
 * Lets staff pick which line items (and how much of each) to refund from a
 * past sale. Refunding restocks tracked products and records the refund so
 * reports stay accurate — it never deletes or rewrites the original sale.
 */
@Composable
private fun RefundDialog(
    items: List<SaleItem>,
    alreadyRefunded: Map<Long, Double>,
    onDismiss: () -> Unit,
    onConfirm: (List<PosRepository.RefundLine>, String?) -> Unit
) {
    val refundQty = remember {
        mutableStateMapOf<Long, Double>().apply {
            items.forEach { put(it.id, 0.0) }
        }
    }
    var note by remember { mutableStateOf("") }

    val refundableItems = items.filter { (alreadyRefunded[it.id] ?: 0.0) < it.quantity }
    val totalToRefund = items.sumOf { (refundQty[it.id] ?: 0.0) * it.unitPrice }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Refund items") },
        text = {
            Column {
                if (refundableItems.isEmpty()) {
                    Text("Everything on this sale has already been refunded.")
                } else {
                    refundableItems.forEach { item ->
                        val remaining = item.quantity - (alreadyRefunded[item.id] ?: 0.0)
                        val qty = refundQty[item.id] ?: 0.0
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(item.productName)
                                Text(
                                    "Up to ${remaining.trimZeros()} refundable",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = {
                                refundQty[item.id] = (qty - 1).coerceAtLeast(0.0)
                            }) { Text("–", style = MaterialTheme.typography.titleLarge) }
                            Text(qty.trimZeros(), modifier = Modifier.padding(horizontal = 4.dp))
                            IconButton(onClick = {
                                refundQty[item.id] = (qty + 1).coerceAtMost(remaining)
                            }) { Text("+", style = MaterialTheme.typography.titleLarge) }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Reason (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Refund total: ${Money.format(totalToRefund)}", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = totalToRefund > 0,
                onClick = {
                    val lines = items.mapNotNull { item ->
                        val qty = refundQty[item.id] ?: 0.0
                        if (qty > 0) PosRepository.RefundLine(saleItem = item, quantity = qty) else null
                    }
                    onConfirm(lines, note.ifBlank { null })
                }
            ) { Text("Refund") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Lists already-paired Bluetooth devices to print to — no scanning or
 * pairing UI of our own, since the user pairs a printer once in Android's
 * own Bluetooth settings (requires BLUETOOTH_CONNECT on Android 12+).
 */
@Composable
private fun PrinterPickerDialog(
    onDismiss: () -> Unit,
    onPrinterChosen: (String) -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            android.os.Build.VERSION.SDK_INT < 31 ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.BLUETOOTH_CONNECT
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT)
    }

    val printers = remember(hasPermission) {
        if (hasPermission) BtPrinter.pairedDevices(context) else emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose a printer") },
        text = {
            Column {
                if (!hasPermission) {
                    Text("Bluetooth permission is needed to see paired printers.")
                } else if (printers.isEmpty()) {
                    Text("No paired Bluetooth printers found. Pair your thermal printer in Android's Bluetooth settings first, then try again.")
                } else {
                    printers.forEach { printer ->
                        ListItem(
                            headlineContent = { Text(printer.name) },
                            supportingContent = { Text(printer.address) },
                            modifier = Modifier.fillMaxWidth().clickableRow { onPrinterChosen(printer.address) }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.then(Modifier.run { androidx.compose.foundation.clickable(onClick = onClick) })
