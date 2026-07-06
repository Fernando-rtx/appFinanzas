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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.SectionHeader
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
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }
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
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = stringResource(R.string.cd_logo),
                            tint = Color.Unspecified,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.settings_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                SectionHeader("L\u00edmites Presupuestales")
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
                            label = { Text(stringResource(R.string.settings_monthly_limit)) },
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
                            label = { Text(stringResource(R.string.settings_daily_limit)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = stringResource(R.string.settings_daily_limit_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                SectionHeader("Cuentas Bancarias y Efectivo")
            }

            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.settings_add_account))
                        OutlinedTextField(
                            value = newAccountName,
                            onValueChange = { newAccountName = it },
                            label = { Text(stringResource(R.string.settings_account_name_hint)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newAccountBalance,
                                onValueChange = { newAccountBalance = it },
                                label = { Text(stringResource(R.string.settings_initial_balance)) },
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
                                Text(stringResource(R.string.add))
                            }
                        }
                    }
                }
            }

            items(allAccounts, key = { it.id }) { acc ->
                ListItem(
                    headlineContent = { Text(acc.name) },
                    supportingContent = { Text(stringResource(R.string.settings_initial_balance_label, acc.initialBalance.centsToCurrency())) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteAccount(acc.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.settings_delete), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                HorizontalDivider()
            }

            item {
                SectionHeader("Categor\u00edas")
            }

            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.settings_create_category))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = isExpenseCategory, onClick = { isExpenseCategory = true }, label = { Text(stringResource(R.string.add_txn_expense)) })
                            FilterChip(selected = !isExpenseCategory, onClick = { isExpenseCategory = false }, label = { Text(stringResource(R.string.add_txn_income)) })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                label = { Text(stringResource(R.string.settings_category_name)) },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                if (newCategoryName.isNotBlank()) {
                                    viewModel.addCategory(newCategoryName.trim(), isExpenseCategory)
                                    newCategoryName = ""
                                }
                            }) {
                                Text(stringResource(R.string.add))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.settings_my_expenses), modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            items(categories.filter { it.isExpense }, key = { it.id }) { cat ->
                ListItem(
                    headlineContent = { Text(cat.name) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteCategory(cat.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.settings_delete), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                HorizontalDivider()
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.settings_my_income), modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            items(categories.filter { !it.isExpense }, key = { it.id }) { cat ->
                ListItem(
                    headlineContent = { Text(cat.name) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteCategory(cat.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.settings_delete), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
