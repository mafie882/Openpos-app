package com.openpos.app.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.openpos.app.ui.common.openPosApp
import com.openpos.app.util.Money
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val app = openPosApp()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currencySymbol by remember { mutableStateOf(Money.symbol) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                app.backupManager.exportTo(context, uri)
                statusMessage = "Backup saved."
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) pendingImportUri = uri
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Currency symbol", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = currencySymbol,
                onValueChange = {
                    currencySymbol = it
                    Money.symbol = it.ifBlank { "$" }
                },
                modifier = Modifier.fillMaxWidth(0.4f)
            )

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("Backup & restore", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "This app has no account and no cloud sync. Everything is stored only " +
                    "on this device. Use these buttons to save a backup file yourself, " +
                    "or move your data to a new phone.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { exportLauncher.launch("openpos-backup-${System.currentTimeMillis()}.json") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Export backup file") }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Import backup file") }

            statusMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text(
                "OpenPOS is free, open-source point-of-sale software. " +
                    "No login, no subscription, no feature is ever locked behind payment.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    pendingImportUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("Replace all data?") },
            text = { Text("Importing a backup replaces everything currently stored in this app. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        app.backupManager.importFrom(context, uri)
                        statusMessage = "Backup restored."
                        pendingImportUri = null
                    }
                }) { Text("Replace data") }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) { Text("Cancel") }
            }
        )
    }
}
