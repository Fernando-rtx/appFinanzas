package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsByMonth(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByMonth(startDate, endDate)
    }

    suspend fun insertAccount(account: Account): Long = accountDao.insertAccount(account)
    suspend fun deleteAccountById(id: Int) = accountDao.deleteAccountById(id)

    suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)
    suspend fun deleteCategoryById(id: Int) = categoryDao.deleteCategoryById(id)

    suspend fun insertTransaction(transaction: Transaction): Long = transactionDao.insertTransaction(transaction)

    suspend fun getOrCreateCategory(name: String, isExpense: Boolean): Category {
        val existing = categoryDao.getCategoryByName(name)
        if (existing != null) return existing

        val newId = categoryDao.insertCategory(Category(name = name, isExpense = isExpense))
        return Category(id = newId.toInt(), name = name, isExpense = isExpense)
    }
}
