package com.addmrp.vault.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.addmrp.vault.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for transaction CRUD and aggregation queries.
 *
 * Supports SpendAuditor monthly analysis and category breakdowns.
 */
@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY transaction_date_utc DESC")
    fun observeAllTransactions(userId: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE user_id = :userId 
        AND transaction_date_utc >= :startUtc 
        AND transaction_date_utc < :endUtc
        ORDER BY transaction_date_utc DESC
    """)
    fun observeTransactionsByDateRange(
        userId: String,
        startUtc: Long,
        endUtc: Long
    ): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE user_id = :userId 
        AND category = :category
        AND transaction_date_utc >= :startUtc 
        AND transaction_date_utc < :endUtc
        ORDER BY amount DESC
    """)
    fun observeByCategory(
        userId: String,
        category: String,
        startUtc: Long,
        endUtc: Long
    ): Flow<List<TransactionEntity>>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE user_id = :userId 
        AND card_id = :cardId 
        AND transaction_date_utc >= :startUtc 
        AND transaction_date_utc < :endUtc
    """)
    suspend fun getTotalSpendByCard(
        userId: String,
        cardId: String,
        startUtc: Long,
        endUtc: Long
    ): Double?

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE user_id = :userId 
        AND category = :category 
        AND transaction_date_utc >= :startUtc 
        AND transaction_date_utc < :endUtc
    """)
    suspend fun getTotalSpendByCategory(
        userId: String,
        category: String,
        startUtc: Long,
        endUtc: Long
    ): Double?

    @Query("""
        SELECT * FROM transactions 
        WHERE user_id = :userId 
        AND is_debit_card = 1
        AND transaction_date_utc >= :startUtc 
        AND transaction_date_utc < :endUtc
        ORDER BY amount DESC
    """)
    fun observeDebitCardTransactions(
        userId: String,
        startUtc: Long,
        endUtc: Long
    ): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteById(transactionId: String)

    @Query("SELECT COUNT(*) FROM transactions WHERE user_id = :userId")
    suspend fun getTransactionCount(userId: String): Int
}
