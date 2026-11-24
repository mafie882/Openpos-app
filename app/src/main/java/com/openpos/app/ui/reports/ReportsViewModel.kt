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

class ReportsViewModel(private val repository: PosRepository) : ViewModel() {
    var range by mutableStateOf(ReportRange.WEEK)
        private set

    var summary by mutableStateOf(ReportSummary())
        private set

    init {
        load()
    }

    fun setRange(newRange: ReportRange) {
        range = newRange
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val days = range.days
            val from = DateFmt.daysAgo(days - 1)
            val to = System.currentTimeMillis()

            val sales = repository.salesBetween(from, to)
            val expenses = repository.expensesBetween(from, to)

            val itemsBySale = mutableMapOf<Long, List<com.openpos.app.data.entity.SaleItem>>()
            for (sale in sales) {
                itemsBySale[sale.id] = repository.getSaleItems(sale.id)
            }
            val allItems = itemsBySale.values.flatten()

            val totalSales = sales.sumOf { it.total }
            val totalDiscounts = sales.sumOf { it.discountAmount }
            val totalCostOfGoods = allItems.sumOf { it.quantity * it.unitCost }
            val totalExpenses = expenses.sumOf { it.amount }
            val grossProfit = totalSales - totalCostOfGoods
            val netProfit = grossProfit - totalExpenses

            val points = (0 until days).map { i ->
                val dayStart = DateFmt.daysAgo(days - 1 - i)
                val dayEnd = dayStart + 24 * 60 * 60 * 1000
                val daySales = sales.filter { it.createdAt in dayStart until dayEnd }.sumOf { it.total }
                val dayExpenses = expenses.filter { it.createdAt in dayStart until dayEnd }.sumOf { it.amount }
                DayPoint(label = DateFmt.day(dayStart).substringBefore(" "), sales = daySales, expenses = dayExpenses)
            }

            val topProducts = allItems
                .groupBy { it.productName }
                .mapValues { (_, items) -> items.sumOf { it.quantity * it.unitPrice - it.lineDiscount } }
                .toList()
                .sortedByDescending { it.second }
                .take(5)

            summary = ReportSummary(
                totalSales = totalSales,
                totalCostOfGoods = totalCostOfGoods,
                totalDiscounts = totalDiscounts,
                totalExpenses = totalExpenses,
                grossProfit = grossProfit,
                netProfit = netProfit,
                saleCount = sales.size,
                points = points,
                topProducts = topProducts
            )
        }
    }
}
