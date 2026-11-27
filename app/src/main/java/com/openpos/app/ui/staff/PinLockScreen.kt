package com.openpos.app.ui.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openpos.app.ui.common.appViewModel
import kotlinx.coroutines.launch

/**
 * Shown before the main app when Settings > "Require staff PIN" is on.
 * Checks the entered PIN against the local StaffUser table only — there
 * is no server round-trip, no account, nothing to fail without a network.
 */
@Composable
fun PinLockScreen(onUnlocked: (id: Long, name: String, role: String) -> Unit) {
    val vm = appViewModel { app -> StaffViewModel(app.repository) }
    val scope = rememberCoroutineScope()
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit() {
        scope.launch {
            val staff = vm.byPin(pin)
            if (staff != null) {
                onUnlocked(staff.id, staff.name, staff.role)
            } else {
                error = "Incorrect PIN"
                pin = ""
            }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Enter staff PIN", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Text(
                pin.map { "•" }.joinToString(" ").ifBlank { "— — — —" },
                style = MaterialTheme.typography.displaySmall
            )
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.width(260.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items((1..9).toList()) { digit ->
                    OutlinedButton(
                        onClick = { if (pin.length < 8) pin += digit; error = null },
                        modifier = Modifier.size(72.dp)
                    ) { Text(digit.toString(), style = MaterialTheme.typography.titleLarge) }
                }
                item {
                    OutlinedButton(
                        onClick = { pin = pin.dropLast(1); error = null },
                        modifier = Modifier.size(72.dp)
                    ) { Text("⌫") }
                }
                item {
                    OutlinedButton(
                        onClick = { if (pin.length < 8) pin += "0"; error = null },
                        modifier = Modifier.size(72.dp)
                    ) { Text("0", style = MaterialTheme.typography.titleLarge) }
                }
                item {
                    Button(
                        onClick = { submit() },
                        enabled = pin.length >= 4,
                        modifier = Modifier.size(72.dp)
                    ) { Text("OK") }
                }
            }
        }
    }
}
