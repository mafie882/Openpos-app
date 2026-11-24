package com.openpos.app.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.openpos.app.data.entity.Product
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    productId: Long,
    onDone: () -> Unit
) {
    val vm = com.openpos.app.ui.common.appViewModel { app -> ProductViewModel(app.repository) }
    val categories by vm.categories.collectAsState()
    val scope = rememberCoroutineScope()

    var loaded by remember { mutableStateOf(productId == 0L) }
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var sellPrice by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var taxPercent by remember { mutableStateOf("0") }
    var stockQty by remember { mutableStateOf("0") }
    var lowStockThreshold by remember { mutableStateOf("5") }
    var trackStock by remember { mutableStateOf(true) }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var existing by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(productId) {
        if (productId != 0L) {
            val p = vm.getProduct(productId)
            if (p != null) {
                existing = p
                name = p.name
                sku = p.sku ?: ""
                barcode = p.barcode ?: ""
                sellPrice = p.sellPrice.toString()
                costPrice = p.costPrice.toString()
                taxPercent = p.taxPercent.toString()
                stockQty = p.stockQty.toString()
                lowStockThreshold = p.lowStockThreshold.toString()
                trackStock = p.trackStock
                categoryId = p.categoryId
            }
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productId == 0L) "New product" else "Edit product") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (existing != null) {
                        IconButton(onClick = {
                            existing?.let { vm.deleteProduct(it) }
                            onDone()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (!loaded) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(name, { name = it }, label = { Text("Product name*") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(sku, { sku = it }, label = { Text("SKU") }, modifier = Modifier.weight(1f))
                OutlinedTextField(barcode, { barcode = it }, label = { Text("Barcode") }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    sellPrice, { sellPrice = it }, label = { Text("Sell price*") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    costPrice, { costPrice = it }, label = { Text("Cost price") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                taxPercent, { taxPercent = it }, label = { Text("Tax %") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Switch(checked = trackStock, onCheckedChange = { trackStock = it })
                Spacer(Modifier.width(8.dp))
                Text("Track stock for this product")
            }

            if (trackStock) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        stockQty, { stockQty = it }, label = { Text("Stock on hand") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        lowStockThreshold, { lowStockThreshold = it }, label = { Text("Low stock alert at") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Category", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(selected = categoryId == null, onClick = { categoryId = null }, label = { Text("None") })
                }
                items(categories) { cat ->
                    FilterChip(selected = categoryId == cat.id, onClick = { categoryId = cat.id }, label = { Text(cat.name) })
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val product = Product(
                        id = productId,
                        name = name.trim(),
                        sku = sku.trim().ifBlank { null },
                        barcode = barcode.trim().ifBlank { null },
                        categoryId = categoryId,
                        sellPrice = sellPrice.toDoubleOrNull() ?: 0.0,
                        costPrice = costPrice.toDoubleOrNull() ?: 0.0,
                        taxPercent = taxPercent.toDoubleOrNull() ?: 0.0,
                        stockQty = stockQty.toDoubleOrNull() ?: 0.0,
                        trackStock = trackStock,
                        lowStockThreshold = lowStockThreshold.toDoubleOrNull() ?: 5.0,
                        isActive = true
                    )
                    if (product.name.isBlank() || product.sellPrice <= 0.0) return@Button
                    vm.saveProduct(product) { onDone() }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save product")
            }
        }
    }
}
