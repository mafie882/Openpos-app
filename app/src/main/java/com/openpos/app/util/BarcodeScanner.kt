package com.openpos.app.util

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/**
 * A single-shot camera barcode scan. Opens ZXing's own scanner screen
 * (fully offline, no network, no Google Play Services dependency) and
 * calls [onResult] with the scanned text, or null if the user backed out.
 *
 * Usage: val scan = rememberBarcodeScanner { code -> ... }
 *        Button(onClick = { scan() }) { Text("Scan") }
 */
@Composable
fun rememberBarcodeScanner(onResult: (String?) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        onResult(result.contents)
    }
    return remember {
        {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                setPrompt("Scan a barcode")
                setBeepEnabled(true)
                setOrientationLocked(false)
                setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity::class.java)
            }
            launcher.launch(options)
        }
    }
}
