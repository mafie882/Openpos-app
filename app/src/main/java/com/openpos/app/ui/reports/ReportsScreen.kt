package com.openpos.app.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openpos.app.ui.common.appViewModel
import com.openpos.app.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen() {
    val vm = appViewModel { app -> ReportsViewModel(app.repository) }
    val summary = vm.summary

    Scaffold(topBar = { TopAppBar(title = { Text("Reports") }) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportRange.entries.forEach { range ->
                    FilterChip(
                        selected = vm.range == range,
                        onClick = { vm.setRange(range) },
                        label = { Text(range.label) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Sales vs expenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            BarChart(summary.points, modifier = Modifier.fillMaxWidth().height(180.dp))

            Spacer(Modifier.height(24.dp))
            SummaryCard(summary)

            if (summary.topProducts.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text("Top products", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                summary.topProducts.forEach { (name, revenue) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name)
                        Text(Money.format(revenue), fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SummaryCard(summary: ReportSummary) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            StatRow("Sales", summary.totalSales)
            StatRow("Discounts given", -summary.totalDiscounts)
            StatRow("Cost of goods sold", -summary.totalCostOfGoods)
            HorizontalDivider(Modifier.padding(vertical = 6.dp))
            StatRow("Gross profit", summary.grossProfit, bold = true)
            StatRow("Expenses", -summary.totalExpenses)
            HorizontalDivider(Modifier.padding(vertical = 6.dp))
            StatRow("Net profit", summary.netProfit, bold = true, highlight = true)
            Spacer(Modifier.height(8.dp))
            Text("${summary.saleCount} sale(s) in this period", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun StatRow(label: String, amount: Double, bold: Boolean = false, highlight: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(
            Money.format(amount),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = when {
                highlight && amount < 0 -> MaterialTheme.colorScheme.error
                highlight -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun BarChart(points: List<DayPoint>, modifier: Modifier = Modifier) {
    if (points.isEmpty()) {
        Box(modifier) { Text("No data for this period yet.") }
        return
    }
    val maxValue = (points.maxOfOrNull { maxOf(it.sales, it.expenses) } ?: 0.0).coerceAtLeast(1.0)
    val salesColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        val chartHeight = size.height - 24.dp.toPx()
        val slotWidth = size.width / points.size
        val barWidth = (slotWidth / 2.5f).coerceAtMost(28.dp.toPx())

        points.forEachIndexed { index, point ->
            val slotStart = index * slotWidth
            val salesHeight = (point.sales / maxValue * chartHeight).toFloat()
            val expenseHeight = (point.expenses / maxValue * chartHeight).toFloat()

            drawRect(
                color = salesColor,
                topLeft = Offset(slotStart + slotWidth / 2 - barWidth, chartHeight - salesHeight),
                size = Size(barWidth, salesHeight)
            )
            drawRect(
                color = expenseColor,
                topLeft = Offset(slotStart + slotWidth / 2, chartHeight - expenseHeight),
                size = Size(barWidth, expenseHeight)
            )
        }
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        LegendDot(salesColor, "Sales")
        Spacer(Modifier.width(16.dp))
        LegendDot(expenseColor, "Expenses")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
