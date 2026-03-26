package com.addmrp.vault.domain.repository

import com.addmrp.vault.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for transaction operations.
 * Rule 15: Transactions processed on-device only.
 */
interface TransactionRepository {
    fun observeAllTransactions(): Flow<List<Transaction>>
    fun observeTransactionsByDateRange(startUtc: Long, endUtc: Long): Flow<List<Transaction>>
    fun observeDebitCardTransactions(startUtc: Long, endUtc: Long): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun addTransactions(transactions: List<Transaction>)
    suspend fun deleteTransaction(transactionId: String)
    suspend fun getTotalSpendByCard(cardId: String, startUtc: Long, endUtc: Long): Double
    suspend fun getTotalSpendByCategory(category: String, startUtc: Long, endUtc: Long): Double
}
