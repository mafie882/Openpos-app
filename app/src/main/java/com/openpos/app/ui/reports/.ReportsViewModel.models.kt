package com.openpos.app.ui.reports

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openpos.app.data.repository.PosRepository
import com.openpos.app.util.DateFmt
import kotlinx.coroutines.launch

enum class ReportRange(val label: String, val days: Int) {
    WEEK("7 days", 7),
    MONTH("30 days", 30),
    QUARTER("90 days", 90)
}

data class DayPoint(val label: String, val sales: Double, val expenses: Double)

data class ReportSummary(
    val totalSales: Double = 0.0,
    val totalCostOfGoods: Double = 0.0,
    val totalDiscounts: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val grossProfit: Double = 0.0,
    val netProfit: Double = 0.0,
    val saleCount: Int = 0,
    val points: List<DayPoint> = emptyList(),
    val topProducts: List<Pair<String, Double>> = emptyList()
)
