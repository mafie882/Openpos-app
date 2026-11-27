package com.openpos.app.util

import com.openpos.app.data.entity.Sale
import com.openpos.app.data.entity.SaleItem
import java.io.ByteArrayOutputStream

/**
 * Builds a plain ESC/POS byte stream for a 58mm/80mm thermal receipt
 * printer. No vendor SDK, no network — just the standard escape codes
 * almost every cheap Bluetooth receipt printer understands.
 */
object EscPos {
    private const val ESC = 0x1B
    private const val GS = 0x1D
    private const val LINE_WIDTH = 32

    private fun ByteArrayOutputStream.text(s: String) {
        write(s.toByteArray(Charsets.UTF_8))
    }

    private fun ByteArrayOutputStream.line(s: String = "") {
        text(s)
        write('\n'.code)
    }

    private fun ByteArrayOutputStream.align(center: Boolean) {
        write(ESC); write('a'.code); write(if (center) 1 else 0)
    }

    private fun ByteArrayOutputStream.bold(on: Boolean) {
        write(ESC); write('E'.code); write(if (on) 1 else 0)
    }

    private fun ByteArrayOutputStream.feedAndCut() {
        write('\n'.code); write('\n'.code); write('\n'.code)
        write(GS); write('V'.code); write(1) // partial cut; ignored harmlessly if unsupported
    }

    fun buildReceipt(storeName: String, sale: Sale, items: List<SaleItem>): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(ESC); out.write('@'.code) // init

        out.align(true)
        out.bold(true)
        out.line(storeName)
        out.bold(false)
        out.line(sale.invoiceNo)
        out.line(DateFmt.full(sale.createdAt))
        out.align(false)
        out.line("-".repeat(LINE_WIDTH))

        items.forEach { item ->
            val lineTotal = item.quantity * item.unitPrice - item.lineDiscount
            out.line(item.productName)
            val left = "  ${item.quantity.trimZeros()} x ${Money.format(item.unitPrice)}"
            out.line(left + padStart(Money.format(lineTotal), (LINE_WIDTH - left.length).coerceAtLeast(0)))
        }

        out.line("-".repeat(LINE_WIDTH))
        out.line(totalRow("Subtotal", Money.format(sale.subTotal)))
        if (sale.discountAmount > 0) out.line(totalRow("Discount", "-" + Money.format(sale.discountAmount)))
        out.line(totalRow("Tax", Money.format(sale.taxAmount)))
        out.bold(true)
        out.line(totalRow("TOTAL", Money.format(sale.total)))
        out.bold(false)
        out.line(totalRow("Paid (${sale.paymentMethod})", Money.format(sale.amountPaid)))
        if (sale.changeDue > 0) out.line(totalRow("Change", Money.format(sale.changeDue)))
        if (sale.refundedAmount > 0) out.line(totalRow("Refunded", "-" + Money.format(sale.refundedAmount)))

        out.align(true)
        out.line()
        out.line("Thank you!")
        out.feedAndCut()
        return out.toByteArray()
    }

    private fun totalRow(label: String, value: String): String =
        label + padStart(value, (LINE_WIDTH - label.length).coerceAtLeast(0))

    private fun padStart(value: String, totalPad: Int): String {
        val pad = totalPad - value.length
        return if (pad > 0) " ".repeat(pad) + value else value
    }
}
