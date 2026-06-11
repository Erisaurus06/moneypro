package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.theme.DebtOrange
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SavingsTeal

@Composable
fun CategoryPieChart(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    // Only chart active EXPENSES
    val expenses = transactions.filter { !it.isIncome }
    val totalExpense = expenses.sumOf { it.amount }

    if (totalExpense <= 0.0) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay egresos registrados aún para graficar.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
        return
    }

    // Group expenses by category
    val groupedExpenses = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    // Color definitions for categories
    val categoryColors = mapOf(
        "Alimentos" to Color(0xFFF39C12),
        "Vivienda" to Color(0xFF3498DB),
        "Impuestos" to Color(0xFF9B59B6),
        "Deudas" to ExpenseRed,
        "Ahorros" to SavingsTeal,
        "Servicios" to Color(0xFF1ABC9C),
        "Transporte" to Color(0xFF34495E),
        "Entretenimiento" to Color(0xFFE74C3C),
        "Otros" to Color(0xFF95A5A6)
    )

    val defaultColors = listOf(
        Color(0xFF2ECC71), Color(0xFFE74C3C), Color(0xFF3498DB), Color(0xFFF1C40F),
        Color(0xFF9B59B6), Color(0xFF1ABC9C), Color(0xFFE67E22), Color(0xFF34495E)
    )

    var colorIndex = 0

    val chartData = groupedExpenses.map { (cat, amt) ->
        val color = categoryColors[cat] ?: defaultColors[colorIndex++ % defaultColors.size]
        PieSlice(
            category = cat,
            amount = amt,
            percentage = (amt / totalExpense) * 100.0,
            color = color
        )
    }.sortedByDescending { it.amount }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Draw Chart
        Box(
            modifier = Modifier
                .size(140.dp)
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(120.dp)) {
                var startAngle = 0f
                chartData.forEach { slice ->
                    val sweepAngle = (slice.percentage / 100.0 * 360f).toFloat()
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        size = Size(size.width, size.height),
                        style = Stroke(width = 30f)
                    )
                    startAngle += sweepAngle
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Egresos",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format("$%,.0f", totalExpense),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Legends
        Column(
            modifier = Modifier
                .weight(1.2f)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            chartData.take(5).forEach { slice ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(color = slice.color)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${slice.category} (${slice.percentage.toInt()}%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = String.format("$%,.0f", slice.amount),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
            if (chartData.size > 5) {
                Text(
                    text = "+ ${chartData.size - 5} categorías más",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 18.dp)
                )
            }
        }
    }
}

data class PieSlice(
    val category: String,
    val amount: Double,
    val percentage: Double,
    val color: Color
)
