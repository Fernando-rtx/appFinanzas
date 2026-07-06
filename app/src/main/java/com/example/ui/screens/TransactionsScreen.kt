package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.centsToCurrency
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: MainViewModel) {
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Historial de Movimientos") })

        LazyColumn {
            items(allTransactions, key = { it.id }) { txn ->
                ListItem(
                    headlineContent = { Text(txn.description) },
                    supportingContent = { Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(txn.date))) },
                    trailingContent = {
                        Text(
                            text = (if(txn.isExpense) "-" else "+") + txn.amount.centsToCurrency(),
                            color = if (txn.isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                HorizontalDivider()
            }
            if (allTransactions.isEmpty()) {
                item {
                    Text("No hay movimientos registrados", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
