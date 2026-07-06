package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.util.toCentsOrNull
import com.example.viewmodel.Event
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedAccountId by remember { mutableStateOf<Int?>(null) }

    var isSaving by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    // Escuchar eventos del ViewModel
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is Event.TransactionSaved -> {
                    isSaving = false
                    Toast.makeText(context, context.getString(R.string.add_txn_saved), Toast.LENGTH_SHORT).show()
                    onBack()
                }
                is Event.Error -> {
                    isSaving = false
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val filteredCategories = remember(allCategories, isExpense) {
        allCategories.filter { it.isExpense == isExpense }
    }

    // Resetear cuenta seleccionada cuando las cuentas cargan por primera vez
    LaunchedEffect(accounts) {
        if (selectedAccountId == null && accounts.isNotEmpty()) {
            selectedAccountId = accounts.first().id
        }
    }

    // Resetear categoría seleccionada cuando cambia el filtro gasto/ingreso
    LaunchedEffect(filteredCategories) {
        if (filteredCategories.isNotEmpty() && filteredCategories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = filteredCategories.first().id
        }
    }

    val categoryText = filteredCategories.find { it.id == selectedCategoryId }?.name ?: stringResource(R.string.add_txn_select_category)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_txn_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back)) }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = isExpense, onClick = { isExpense = true }, label = { Text(stringResource(R.string.add_txn_expense)) })
                FilterChip(selected = !isExpense, onClick = { isExpense = false }, label = { Text(stringResource(R.string.add_txn_income)) })
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.add_txn_amount)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.add_txn_description_hint)) },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = it }
            ) {
                OutlinedTextField(
                    value = categoryText,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.add_txn_category)) },
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) }
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    if (filteredCategories.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.add_txn_no_categories)) },
                            onClick = { expandedCategory = false }
                        )
                    } else {
                        filteredCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }
            }

            if (accounts.isNotEmpty()) {
                Text(stringResource(R.string.add_txn_select_account))
                Column {
                    accounts.forEach { acc ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedAccountId == acc.id, onClick = { selectedAccountId = acc.id })
                            Text(acc.name)
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.add_txn_no_accounts))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amountCents = amount.toCentsOrNull()
                    if (amountCents != null && amountCents > 0L && description.isNotEmpty() && selectedAccountId != null && selectedCategoryId != null) {
                        isSaving = true
                        viewModel.addTransaction(amountCents, description, selectedAccountId!!, isExpense, selectedCategoryId!!)
                    } else {
                    val msg = if (selectedAccountId == null) context.getString(R.string.add_txn_error_account)
                        else if (selectedCategoryId == null) context.getString(R.string.add_txn_error_category)
                        else context.getString(R.string.add_txn_error_fill)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Text(if (isSaving) stringResource(R.string.saving) else stringResource(R.string.add_txn_save))
            }
        }
    }
}
