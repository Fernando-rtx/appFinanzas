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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.toCentsOrNull
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

    val categoryText = filteredCategories.find { it.id == selectedCategoryId }?.name ?: "Selecciona una categor\u00eda"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Movimiento") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Atr\u00e1s") }
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
                FilterChip(selected = isExpense, onClick = { isExpense = true }, label = { Text("Gasto") })
                FilterChip(selected = !isExpense, onClick = { isExpense = false }, label = { Text("Ingreso") })
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Monto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripci\u00f3n (ej. Comida en restaurante)") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = it }
            ) {
                OutlinedTextField(
                    value = categoryText,
                    onValueChange = { },
                    label = { Text("Categor\u00eda") },
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
                            text = { Text("No hay categor\u00edas configuradas") },
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
                Text("Seleccionar cuenta:")
                Column {
                    accounts.forEach { acc ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedAccountId == acc.id, onClick = { selectedAccountId = acc.id })
                            Text(acc.name)
                        }
                    }
                }
            } else {
                Text("No hay cuentas creadas, ve a Ajustes o Cuentas para crear una.")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amountCents = amount.toCentsOrNull()
                    if (amountCents != null && amountCents > 0L && description.isNotEmpty() && selectedAccountId != null && selectedCategoryId != null) {
                        isSaving = true
                        viewModel.addTransaction(amountCents, description, selectedAccountId!!, isExpense, selectedCategoryId!!)
                        Toast.makeText(context, "Movimiento guardado", Toast.LENGTH_SHORT).show()
                        onBack()
                    } else {
                        val msg = if (selectedAccountId == null) "Crea una cuenta en Ajustes primero"
                            else if (selectedCategoryId == null) "Selecciona una categor\u00eda v\u00e1lida"
                            else "Llena todos los campos"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Guardando..." else "Guardar Movimiento")
            }
        }
    }
}
