package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.Account
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "finanzas-db"
    ).build()

    private val repository = AppRepository(db.accountDao(), db.categoryDao(), db.transactionDao())

    private val sharedPrefs = application.getSharedPreferences("finanzas_prefs", Context.MODE_PRIVATE)

    private val _monthlyLimit = MutableStateFlow(sharedPrefs.getFloat("monthly_limit", 0f).toDouble())
    val monthlyLimit: StateFlow<Double> = _monthlyLimit

    private val _dailyLimit = MutableStateFlow(sharedPrefs.getFloat("daily_limit", 0f).toDouble())
    val dailyLimit: StateFlow<Double> = _dailyLimit

    private val _monthYear = MutableStateFlow(getCurrentMonthStartEnd())
    
    val allAccounts = repository.allAccounts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentMonthTransactions = _monthYear.flatMapLatest { (start, end) ->
        repository.getTransactionsByMonth(start, end)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val allTransactions = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCategories = repository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateMonthlyLimit(amount: Double) {
        sharedPrefs.edit().putFloat("monthly_limit", amount.toFloat()).apply()
        _monthlyLimit.value = amount
    }

    fun updateDailyLimit(amount: Double) {
        sharedPrefs.edit().putFloat("daily_limit", amount.toFloat()).apply()
        _dailyLimit.value = amount
    }

    fun addCategory(name: String, isExpense: Boolean) {
        viewModelScope.launch {
            repository.insertCategory(com.example.data.Category(name = name, isExpense = isExpense))
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            repository.deleteCategoryById(id)
        }
    }

    fun addTransaction(amount: Double, description: String, accountId: Int, isExpense: Boolean, categoryId: Int) {
        viewModelScope.launch {
            val currentDate = System.currentTimeMillis()
            
            val txn = Transaction(
                amount = amount,
                date = currentDate,
                description = description,
                categoryId = categoryId,
                accountId = accountId,
                isExpense = isExpense
            )
            repository.insertTransaction(txn)
        }
    }

    fun addAccount(name: String, initialBalance: Double) {
        viewModelScope.launch {
            repository.insertAccount(Account(name = name, initialBalance = initialBalance))
        }
    }

    fun deleteAccount(id: Int) {
        viewModelScope.launch {
            repository.deleteAccountById(id)
        }
    }

    private fun getCurrentMonthStartEnd(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }

    // Initialize with a default account if empty
    init {
        viewModelScope.launch {
            allAccounts.collect { accounts ->
                if (accounts.isEmpty()) {
                    addAccount("Efectivo", 0.0)
                }
            }
        }
    }
}
