package com.addmrp.vault.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.addmrp.vault.data.local.entity.CreditCardEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for credit card CRUD operations.
 *
 * All read queries return Flow for reactive UI updates (Rule 2).
 */
@Dao
interface CreditCardDao {

    @Query("SELECT * FROM credit_cards WHERE user_id = :userId ORDER BY card_name ASC")
    fun observeAllCards(userId: String): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE id = :cardId")
    suspend fun getCardById(cardId: String): CreditCardEntity?

    @Query("SELECT * FROM credit_cards WHERE user_id = :userId AND issuer = :issuer")
    fun observeCardsByIssuer(userId: String, issuer: String): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE user_id = :userId AND is_revolving_credit = 1")
    fun observeRevolvingCreditCards(userId: String): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE user_id = :userId AND reward_points_balance > 0")
    fun observeCardsWithPoints(userId: String): Flow<List<CreditCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(card: CreditCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(cards: List<CreditCardEntity>)

    @Update
    suspend fun update(card: CreditCardEntity)

    @Delete
    suspend fun delete(card: CreditCardEntity)

    @Query("DELETE FROM credit_cards WHERE id = :cardId")
    suspend fun deleteById(cardId: String)

    @Query("SELECT COUNT(*) FROM credit_cards WHERE user_id = :userId")
    suspend fun getCardCount(userId: String): Int
}
