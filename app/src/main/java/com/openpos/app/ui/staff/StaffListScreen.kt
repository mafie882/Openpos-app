package com.openpos.app.ui.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.openpos.app.data.entity.StaffUser
import com.openpos.app.ui.common.appViewModel

private val roles = listOf("CASHIER", "ADMIN")

/**
 * Manage local, on-device staff PIN accounts. Nothing here is a real
 * login — just a 4+ digit code this app checks against its own local
 * database, so you can tell who rang up a sale.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffListScreen(onBack: () -> Unit) {
    val vm = appViewModel { app -> StaffViewModel(app.repository) }
    val staff by vm.staff.collectAsState()
    var editing by remember { mutableStateOf<StaffUser?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    if (showEditor) {
        StaffEditDialog(
            initial = editing,
            onDismiss = { showEditor = false },
            onSave = {
                vm.saveStaff(it) { showEditor = false }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showEditor = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add staff")
            }
        }
    ) { padding ->
        if (staff.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No staff accounts yet. Tap + to add one.")
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(staff, key = { it.id }) { s ->
                    ListItem(
                        headlineContent = { Text(s.name) },
                        supportingContent = { Text(if (s.isActive) s.role else "${s.role} · disabled") },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { editing = s; showEditor = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { vm.deleteStaff(s) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
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
private fun StaffEditDialog(
    initial: StaffUser?,
    onDismiss: () -> Unit,
    onSave: (StaffUser) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var pin by remember { mutableStateOf(initial?.pin ?: "") }
    var role by remember { mutableStateOf(initial?.role ?: "CASHIER") }
    var isActive by remember { mutableStateOf(initial?.isActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New staff account" else "Edit staff account") },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text("Name*") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    pin, { pin = it.filter { c -> c.isDigit() }.take(8) },
                    label = { Text("PIN (4+ digits)*") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    roles.forEach { r ->
                        FilterChip(selected = role == r, onClick = { role = r }, label = { Text(r) })
                    }
                }
                if (initial != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                        Spacer(Modifier.width(8.dp))
                        Text("Active")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && pin.length >= 4,
                onClick = {
                    onSave(
                        (initial ?: StaffUser(name = "", pin = "")).copy(
                            name = name.trim(),
                            pin = pin,
                            role = role,
                            isActive = isActive
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
