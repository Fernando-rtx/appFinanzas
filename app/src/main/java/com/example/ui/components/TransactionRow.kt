package com.example.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Transaction
import com.example.util.centsToCurrency

/**
 * Fila de transaccion reutilizable.
 * Muestra descripcion, fecha formateada y monto con signo +/-.
 */
@Composable
fun TransactionRow(
    txn: Transaction,
    dateFormatted: String,
    showDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(txn.description, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(dateFormatted) },
        trailingContent = {
            Text(
                text = (if (txn.isExpense) "-" else "+") + txn.amount.centsToCurrency(),
                color = if (txn.isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        },
        modifier = modifier
    )
    if (showDivider) {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}
