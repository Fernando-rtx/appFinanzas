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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "finanzas-db"
    ).fallbackToDestructiveMigration().build()

    private val repository = AppRepository(db.accountDao(), db.categoryDao(), db.transactionDao())

    private val sharedPrefs = application.getSharedPreferences("finanzas_prefs", Context.MODE_PRIVATE)

    private val _monthlyLimit = MutableStateFlow(sharedPrefs.getLong("monthly_limit", 0L))
    val monthlyLimit: StateFlow<Long> = _monthlyLimit

    private val _dailyLimit = MutableStateFlow(sharedPrefs.getLong("daily_limit", 0L))
    val dailyLimit: StateFlow<Long> = _dailyLimit

    private val _monthYear = MutableStateFlow(getCurrentMonthStartEnd())

    val allAccounts = repository.allAccounts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
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

    fun updateMonthlyLimit(amount: Long) {
        sharedPrefs.edit().putLong("monthly_limit", amount).apply()
        _monthlyLimit.value = amount
    }

    fun updateDailyLimit(amount: Long) {
        sharedPrefs.edit().putLong("daily_limit", amount).apply()
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

    fun addTransaction(amount: Long, description: String, accountId: Int?, isExpense: Boolean, categoryId: Int?) {
        viewModelScope.launch {
            val txn = Transaction(
                amount = amount,
                date = System.currentTimeMillis(),
                description = description,
                categoryId = categoryId,
                accountId = accountId,
                isExpense = isExpense
            )
            repository.insertTransaction(txn)
        }
    }

    fun addAccount(name: String, initialBalance: Long) {
        viewModelScope.launch {
            repository.insertAccount(Account(name = name, initialBalance = initialBalance))
        }
    }

    fun deleteAccount(id: Int) {
        viewModelScope.launch {
            repository.deleteAccountById(id)
        }
    }

    fun nextMonth() {
        _monthYear.value = shiftMonth(_monthYear.value, +1)
    }

    fun previousMonth() {
        _monthYear.value = shiftMonth(_monthYear.value, -1)
    }

    fun resetToCurrentMonth() {
        _monthYear.value = getCurrentMonthStartEnd()
    }

    private fun shiftMonth(current: Pair<Long, Long>, months: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = current.first
        cal.add(Calendar.MONTH, months)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        val end = cal.timeInMillis
        return Pair(start, end)
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

    init {
        viewModelScope.launch {
            val accounts = repository.allAccounts.first()
            if (accounts.isEmpty()) {
                repository.insertAccount(Account(name = "Efectivo", initialBalance = 0L))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        db.close()
    }
}
