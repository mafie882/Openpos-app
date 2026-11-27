package com.openpos.app.ui.products

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openpos.app.data.entity.Product
import com.openpos.app.ui.cart.CartViewModel
import com.openpos.app.ui.common.appViewModel
import com.openpos.app.util.Money
import com.openpos.app.util.rememberBarcodeScanner
import kotlinx.coroutines.launch
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    cartViewModel: CartViewModel,
    onOpenCart: () -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (Long) -> Unit,
    onOpenCategories: () -> Unit = {},
    onOpenSalesHistory: () -> Unit = {}
) {
    val vm = appViewModel { app -> ProductViewModel(app.repository) }
    val allProducts by vm.products.collectAsState()
    val categories by vm.categories.collectAsState()
    val visible = vm.filtered(allProducts)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val scanToSell = rememberBarcodeScanner { code ->
        if (code != null) {
            scope.launch {
                val product = vm.getProductByBarcode(code)
                if (product != null) {
                    cartViewModel.addProduct(product)
                    Toast.makeText(context, "Added ${product.name}", Toast.LENGTH_SHORT).show()
                } else {
                    vm.searchQuery = code
                    Toast.makeText(context, "No product found for that barcode", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OpenPOS") },
                actions = {
                    IconButton(onClick = scanToSell) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan barcode")
                    }
                    IconButton(onClick = onOpenSalesHistory) {
                        Icon(Icons.Default.History, contentDescription = "Sales history")
                    }
                    IconButton(onClick = onOpenCategories) {
                        Icon(Icons.Default.Sell, contentDescription = "Categories")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct) {
                Icon(Icons.Default.Add, contentDescription = "Add product")
            }
        },
        bottomBar = {
            if (cartViewModel.itemCount > 0) {
                Surface(tonalElevation = 4.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenCart)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("${cartViewModel.itemCount.toInt()} item(s) in cart", fontWeight = FontWeight.Medium)
                        }
                        Text(Money.format(cartViewModel.grandTotal), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = vm.searchQuery,
                onValueChange = { vm.searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Search product, SKU or barcode") },
                singleLine = true
            )
            if (categories.isNotEmpty()) {
                LazyRowCategoryChips(
                    categories = categories.map { it.id to it.name },
                    selected = vm.selectedCategoryId,
                    onSelect = { vm.selectedCategoryId = it }
                )
            }
            if (visible.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (allProducts.isEmpty())
                            "No products yet. Tap + to add your first product."
                        else "No products match your search."
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(visible, key = { it.id }) { product ->
                        ProductTile(
                            product = product,
                            onClick = { cartViewModel.addProduct(product) },
                            onLongClick = { onEditProduct(product.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyRowCategoryChips(
    categories: List<Pair<Long, String>>,
    selected: Long?,
    onSelect: (Long?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All") })
        }
        listItems(categories) { (id, name) ->
            FilterChip(selected = selected == id, onClick = { onSelect(id) }, label = { Text(name) })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProductTile(product: Product, onClick: () -> Unit, onLongClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(product.name, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Spacer(Modifier.height(6.dp))
            Text(Money.format(product.sellPrice), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            if (product.trackStock) {
                Spacer(Modifier.height(4.dp))
                val low = product.stockQty <= product.lowStockThreshold
                Text(
                    "Stock: ${product.stockQty}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (low) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
