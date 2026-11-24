package com.openpos.app.ui.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openpos.app.data.entity.Category
import com.openpos.app.ui.common.appViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(onBack: () -> Unit) {
    val vm = appViewModel { app -> CategoryViewModel(app.repository) }
    val categories by vm.categories.collectAsState()
    var newName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New category name") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    if (newName.isNotBlank()) {
                        vm.save(Category(name = newName.trim(), sortOrder = categories.size))
                        newName = ""
                    }
                }) { Text("Add") }
            }

            Spacer(Modifier.height(16.dp))

            if (categories.isEmpty()) {
                Text("No categories yet. Categories help group products on the sales screen.")
            } else {
                LazyColumn {
                    items(categories, key = { it.id }) { category ->
                        ListItem(
                            headlineContent = { Text(category.name) },
                            trailingContent = {
                                IconButton(onClick = { vm.delete(category) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
