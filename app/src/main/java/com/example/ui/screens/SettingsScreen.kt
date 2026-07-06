package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.centsToCurrency
import com.example.util.toCentsOrNull
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val monthlyLimit by viewModel.monthlyLimit.collectAsStateWithLifecycle()
    val dailyLimit by viewModel.dailyLimit.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()

    var newMonthlyLimit by remember { mutableStateOf(monthlyLimit.takeIf { it > 0L }?.toString() ?: "") }
    var newDailyLimit by remember { mutableStateOf(dailyLimit.takeIf { it > 0L }?.toString() ?: "") }

    var newCategoryName by remember { mutableStateOf("") }
    var isExpenseCategory by remember { mutableStateOf(true) }

    // Estado para nueva cuenta (hoisted al scope del composable, no dentro del LazyColumn)
    var newAccountName by remember { mutableStateOf("") }
    var newAccountBalance by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ajustes") }) }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = com.example.R.drawable.ic_app_logo),
                            contentDescription = "Logo de Mis Finanzas",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Mis Finanzas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Control Inteligente",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = "L\u00edmites Presupuestales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newMonthlyLimit,
                            onValueChange = {
                                newMonthlyLimit = it
                                val parsed = it.toCentsOrNull() ?: 0L
                                viewModel.updateMonthlyLimit(parsed)
                            },
                            label = { Text("L\u00edmite Mensual (\$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newDailyLimit,
                            onValueChange = {
                                newDailyLimit = it
                                val parsed = it.toCentsOrNull() ?: 0L
                                viewModel.updateDailyLimit(parsed)
                            },
                            label = { Text("L\u00edmite Diario Personalizado (\$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Si el l\u00edmite diario es 0, se calcular\u00e1 dividiendo lo que te resta del mes entre los d\u00edas del mes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Cuentas Bancarias y Efectivo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("A\u00f1adir nueva cuenta")
                        OutlinedTextField(
                            value = newAccountName,
                            onValueChange = { newAccountName = it },
                            label = { Text("Nombre (ej. BBVA, Efectivo)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newAccountBalance,
                                onValueChange = { newAccountBalance = it },
                                label = { Text("Saldo Inicial") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                if (newAccountName.isNotBlank()) {
                                    val balance = newAccountBalance.toCentsOrNull() ?: 0L
                                    viewModel.addAccount(newAccountName.trim(), balance)
                                    newAccountName = ""
                                    newAccountBalance = ""
                                }
                            }) {
                                Text("A\u00f1adir")
                            }
                        }
                    }
                }
            }

            items(allAccounts, key = { it.id }) { acc ->
                ListItem(
                    headlineContent = { Text(acc.name) },
                    supportingContent = { Text("Monto inicial: ${acc.initialBalance.centsToCurrency()}") },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteAccount(acc.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                HorizontalDivider()
            }

            item {
                Text(
                    text = "Categor\u00edas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Crear nueva categor\u00eda")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = isExpenseCategory, onClick = { isExpenseCategory = true }, label = { Text("Gasto") })
                            FilterChip(selected = !isExpenseCategory, onClick = { isExpenseCategory = false }, label = { Text("Ingreso") })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                label = { Text("Nombre") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                if (newCategoryName.isNotBlank()) {
                                    viewModel.addCategory(newCategoryName.trim(), isExpenseCategory)
                                    newCategoryName = ""
                                }
                            }) {
                                Text("A\u00f1adir")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text("Mis Gastos", modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            items(categories.filter { it.isExpense }, key = { it.id }) { cat ->
                ListItem(
                    headlineContent = { Text(cat.name) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteCategory(cat.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                HorizontalDivider()
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text("Mis Ingresos", modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            items(categories.filter { !it.isExpense }, key = { it.id }) { cat ->
                ListItem(
                    headlineContent = { Text(cat.name) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteCategory(cat.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
