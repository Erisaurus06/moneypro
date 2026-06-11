package com.example.data.repository

import com.example.data.database.FinanceDao
import com.example.data.model.Debt
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val financeDao: FinanceDao) {

    // --- TRANSACTIONS ---
    val allTransactions: Flow<List<Transaction>> = financeDao.getAllTransactions()

    suspend fun insertTransaction(transaction: Transaction) {
        financeDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        financeDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Int) {
        financeDao.deleteTransactionById(id)
    }

    // --- DEBTS ---
    val allDebts: Flow<List<Debt>> = financeDao.getAllDebts()

    suspend fun insertDebt(debt: Debt) {
        financeDao.insertDebt(debt)
    }

    suspend fun deleteDebt(debt: Debt) {
        financeDao.deleteDebt(debt)
    }

    suspend fun deleteDebtById(id: Int) {
        financeDao.deleteDebtById(id)
    }

    suspend fun updateDebtRemainingAmount(id: Int, amount: Double) {
        financeDao.updateDebtRemainingAmount(id, amount)
    }

    // --- SAVINGS GOALS ---
    val allSavingsGoals: Flow<List<SavingsGoal>> = financeDao.getAllSavingsGoals()

    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal) {
        financeDao.insertSavingsGoal(savingsGoal)
    }

    suspend fun deleteSavingsGoal(savingsGoal: SavingsGoal) {
        financeDao.deleteSavingsGoal(savingsGoal)
    }

    suspend fun deleteSavingsGoalById(id: Int) {
        financeDao.deleteSavingsGoalById(id)
    }

    suspend fun updateSavingsGoalCurrentAmount(id: Int, amount: Double) {
        financeDao.updateSavingsGoalCurrentAmount(id, amount)
    }
}
