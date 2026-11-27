package com.openpos.app.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

/**
 * Talks to a thermal receipt printer over classic Bluetooth SPP (the
 * profile almost every cheap receipt printer speaks). No network, no
 * pairing flow of our own — the user pairs the printer once in Android's
 * Bluetooth settings, and we just pick it from the already-bonded list.
 */
object BtPrinter {
    // Standard Serial Port Profile UUID — the same one every ESC/POS
    // Bluetooth printer and every "generic" SPP app uses.
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    data class PairedPrinter(val name: String, val address: String)

    @SuppressLint("MissingPermission") // caller checks BLUETOOTH_CONNECT first
    fun pairedDevices(context: Context): List<PairedPrinter> {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return emptyList()
        if (!adapter.isEnabled) return emptyList()
        return try {
            adapter.bondedDevices
                .map { PairedPrinter(it.name ?: it.address, it.address) }
                .sortedBy { it.name }
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun print(context: Context, address: String, bytes: ByteArray): Result<Unit> =
        withContext(Dispatchers.IO) {
            var socket: BluetoothSocket? = null
            try {
                val adapter = BluetoothAdapter.getDefaultAdapter()
                    ?: return@withContext Result.failure(IOException("No Bluetooth adapter on this device"))
                val device: BluetoothDevice = adapter.getRemoteDevice(address)
                adapter.cancelDiscovery()
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket.connect()
                socket.outputStream.use { out ->
                    out.write(bytes)
                    out.flush()
                }
                Result.success(Unit)
            } catch (e: IOException) {
                Result.failure(e)
            } catch (e: SecurityException) {
                Result.failure(e)
            } finally {
                try {
                    socket?.close()
                } catch (_: IOException) {
                }
            }
        }
}
