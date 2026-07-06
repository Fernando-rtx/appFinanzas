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

    suspend fun insertAccount(account: Account) = accountDao.insertAccount(account)
    suspend fun deleteAccountById(id: Int) = accountDao.deleteAccountById(id)

    suspend fun insertCategory(category: Category) = categoryDao.insertCategory(category)
    suspend fun deleteCategoryById(id: Int) = categoryDao.deleteCategoryById(id)

    suspend fun insertTransaction(transaction: Transaction) = transactionDao.insertTransaction(transaction)

    suspend fun getOrCreateCategory(name: String, isExpense: Boolean): Category {
        val existing = categoryDao.getCategoryByName(name)
        if (existing != null) return existing
        
        val newCat = Category(name = name, isExpense = isExpense)
        categoryDao.insertCategory(newCat)
        return categoryDao.getCategoryByName(name) ?: newCat
    }
}
