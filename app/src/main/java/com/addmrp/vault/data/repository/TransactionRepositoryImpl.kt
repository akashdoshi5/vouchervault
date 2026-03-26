package com.addmrp.vault.data.repository

import com.addmrp.vault.data.local.TransactionDao
import com.addmrp.vault.data.mapper.CreditCardMapper
import com.addmrp.vault.domain.model.Transaction
import com.addmrp.vault.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Transaction repository — offline-first, on-device-only processing.
 *
 * Rule 15: Transaction data never leaves the device.
 */
@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    private val userId: String get() = "local_user"

    override fun observeAllTransactions(): Flow<List<Transaction>> =
        transactionDao.observeAllTransactions(userId).map { entities ->
            entities.map { CreditCardMapper.transactionEntityToDomain(it) }
        }

    override fun observeTransactionsByDateRange(
        startUtc: Long,
        endUtc: Long
    ): Flow<List<Transaction>> =
        transactionDao.observeTransactionsByDateRange(userId, startUtc, endUtc).map { entities ->
            entities.map { CreditCardMapper.transactionEntityToDomain(it) }
        }

    override fun observeDebitCardTransactions(
        startUtc: Long,
        endUtc: Long
    ): Flow<List<Transaction>> =
        transactionDao.observeDebitCardTransactions(userId, startUtc, endUtc).map { entities ->
            entities.map { CreditCardMapper.transactionEntityToDomain(it) }
        }

    override suspend fun addTransaction(transaction: Transaction) {
        val entity = CreditCardMapper.transactionDomainToEntity(transaction.copy(userId = userId))
        transactionDao.upsert(entity)
    }

    override suspend fun addTransactions(transactions: List<Transaction>) {
        val entities = transactions.map {
            CreditCardMapper.transactionDomainToEntity(it.copy(userId = userId))
        }
        transactionDao.upsertAll(entities)
    }

    override suspend fun deleteTransaction(transactionId: String) {
        transactionDao.deleteById(transactionId)
    }

    override suspend fun getTotalSpendByCard(
        cardId: String,
        startUtc: Long,
        endUtc: Long
    ): Double = transactionDao.getTotalSpendByCard(userId, cardId, startUtc, endUtc) ?: 0.0

    override suspend fun getTotalSpendByCategory(
        category: String,
        startUtc: Long,
        endUtc: Long
    ): Double = transactionDao.getTotalSpendByCategory(userId, category, startUtc, endUtc) ?: 0.0
}
