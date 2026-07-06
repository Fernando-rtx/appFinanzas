package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.TransactionRow
import com.example.util.centsToCurrency
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Formateador cacheado
private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: MainViewModel) {
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.transactions_title)) })

        LazyColumn {
            items(allTransactions, key = { it.id }) { txn ->
                TransactionRow(
                    txn = txn,
                    dateFormatted = dateTimeFormat.format(Date(txn.date)),
                    showDivider = true
                )
            }
            if (allTransactions.isEmpty()) {
                item {
                    Text(stringResource(R.string.transactions_empty), modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
