package com.openpos.app.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Money {
    // Change this symbol if you want a different currency — there's no
    // region lock of any kind, it's just a display string.
    var symbol: String = "$"

    fun format(amount: Double): String {
        val nf = NumberFormat.getNumberInstance(Locale.US)
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2
        return "$symbol${nf.format(amount)}"
    }
}

object DateFmt {
    private val dateTime = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val dateOnly = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun full(millis: Long): String = dateTime.format(Date(millis))
    fun day(millis: Long): String = dateOnly.format(Date(millis))

    fun startOfDay(millis: Long = System.currentTimeMillis()): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun daysAgo(days: Int): Long = startOfDay() - days.toLong() * 24 * 60 * 60 * 1000
}

fun Double.trimZeros(): String {
    return if (this == this.toLong().toDouble()) this.toLong().toString()
    else String.format(Locale.US, "%.2f", this)
}
