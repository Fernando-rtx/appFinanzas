package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.centsToCurrency
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Formateadores cacheados (no recrear en cada recomposición)
private val dateFormatShort = SimpleDateFormat("dd MMM", Locale.getDefault())
private val dateFormatMonth = SimpleDateFormat("MMMM yyyy", Locale("es", "MX"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val monthlyLimit by viewModel.monthlyLimit.collectAsStateWithLifecycle()
    val dailyLimitCfg by viewModel.dailyLimit.collectAsStateWithLifecycle()
    val monthlyTransactions by viewModel.currentMonthTransactions.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val currentMonthLabel by viewModel.currentMonthLabel.collectAsStateWithLifecycle()

    // Cálculos cacheados con remember para evitar recomposiciones innecesarias
    val totalSpent = remember(monthlyTransactions) { monthlyTransactions.filter { it.isExpense }.sumOf { it.amount } }
    val totalIncome = remember(monthlyTransactions) { monthlyTransactions.filter { !it.isExpense }.sumOf { it.amount } }

    val remainingBudget = remember(monthlyLimit, totalIncome, totalSpent) { monthlyLimit + totalIncome - totalSpent }

    val dailyLimit = remember(dailyLimitCfg, remainingBudget) {
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val daysLeft = daysInMonth - currentDay + 1
        dailyLimitCfg.takeIf { it > 0L } ?: (if (daysLeft > 0 && remainingBudget > 0L) remainingBudget / daysLeft else 0L)
    }

    // Datos globales
    val globalIncome = remember(allTransactions) { allTransactions.filter { !it.isExpense }.sumOf { it.amount } }
    val globalExpense = remember(allTransactions) { allTransactions.filter { it.isExpense }.sumOf { it.amount } }
    val baseAccounts = remember(allAccounts) { allAccounts.sumOf { it.initialBalance } }
    val saldoTotal = remember(baseAccounts, globalIncome, globalExpense) { baseAccounts + globalIncome - globalExpense }

    // Chart Data Preparation
    val expensesByCategory = remember(monthlyTransactions, allCategories) {
        monthlyTransactions
            .filter { it.isExpense }
            .groupBy { it.categoryId }
            .map { (catId, txns) ->
                val catName = allCategories.find { it.id == catId }?.name ?: "Sin categor\u00eda"
                catName to txns.sumOf { it.amount }
            }
            .sortedByDescending { it.second }
    }

    val chartColors = listOf(
        Color(0xFF4CAF50), Color(0xFF66BB6A), Color(0xFF81C784),
        Color(0xFFA5D6A7), Color(0xFFC8E6C9), Color(0xFF2E7D32),
        Color(0xFF388E3C), Color(0xFF1B5E20)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = com.example.R.drawable.ic_app_logo),
                        contentDescription = "Logo de Mis Finanzas",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(36.dp)
                            .padding(end = 8.dp)
                    )
                    Text("Resumen General", fontWeight = FontWeight.Bold)
                }
            },
            actions = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentMonthLabel.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mes anterior")
                    }
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Mes siguiente")
                    }
                }
            }
        )

        val globalIncome = allTransactions.filter { !it.isExpense }.sumOf { it.amount }
        val globalExpense = allTransactions.filter { it.isExpense }.sumOf { it.amount }
        val baseAccounts = allAccounts.sumOf { it.initialBalance }
        val saldoTotal = baseAccounts + globalIncome - globalExpense

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Saldo Real Total", style = MaterialTheme.typography.titleMedium)
                        Text(saldoTotal.centsToCurrency(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Cuentas + Ingresos - Gastos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Presupuesto del mes", style = MaterialTheme.typography.titleMedium)
                        Text(monthlyLimit.centsToCurrency(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Gastos", style = MaterialTheme.typography.bodySmall)
                                Text("-" + totalSpent.centsToCurrency(), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                            }
                            Column {
                                Text("Restante", style = MaterialTheme.typography.bodySmall)
                                Text(remainingBudget.centsToCurrency(), color = if (remainingBudget > 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (expensesByCategory.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Distribuci\u00f3n de Gastos (Mes)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 24.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                                    Canvas(modifier = Modifier.size(120.dp)) {
                                        var startAngle = -90f
                                        val total = expensesByCategory.sumOf { it.second.toFloat() }

                                        expensesByCategory.forEachIndexed { index, (_, amount) ->
                                            val sweepAngle = (amount.toFloat() / total) * 360f
                                            drawArc(
                                                color = chartColors[index % chartColors.size],
                                                startAngle = startAngle,
                                                sweepAngle = sweepAngle,
                                                useCenter = false,
                                                topLeft = Offset(16f, 16f),
                                                size = Size(size.width - 32f, size.height - 32f),
                                                style = Stroke(width = 32f, cap = StrokeCap.Round)
                                            )
                                            startAngle += sweepAngle
                                        }
                                    }
                                    Text(
                                        text = "${expensesByCategory.size}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(24.dp))

                                Column {
                                    expensesByCategory.take(4).forEachIndexed { index, (cat, amount) ->
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                            Surface(color = chartColors[index % chartColors.size], shape = androidx.compose.foundation.shape.CircleShape, modifier = Modifier.size(12.dp)) {}
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(cat, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                                Text(amount.centsToCurrency(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("L\u00edmite Diario Recomendado", style = MaterialTheme.typography.titleMedium)
                        Text(dailyLimit.centsToCurrency(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("Para que tu dinero rinda todo el mes", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                Text("\u00daltimos movimientos", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp, start = 8.dp))
            }

            item {
                Card(shape = RoundedCornerShape(24.dp)) {
                    Column {
                        if (monthlyTransactions.isEmpty()) {
                            Text("A\u00fan no tienes movimientos este mes", modifier = Modifier.padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            monthlyTransactions.take(5).forEachIndexed { index, txn ->
                                ListItem(
                                    headlineContent = { Text(txn.description, fontWeight = FontWeight.Medium) },
                                    supportingContent = { Text(dateFormatShort.format(Date(txn.date))) },
                                    trailingContent = {
                                        Text(
                                            text = (if(txn.isExpense) "-" else "+") + txn.amount.centsToCurrency(),
                                            color = if (txn.isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                )
                                if (index < monthlyTransactions.take(5).size - 1) {
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
