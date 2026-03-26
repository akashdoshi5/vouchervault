package com.addmrp.vault.domain.repository

import com.addmrp.vault.domain.model.CreditCard
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for credit card operations.
 * Rule 8: Room is source of truth; Firestore syncs asynchronously.
 */
interface CreditCardRepository {
    fun observeAllCards(): Flow<List<CreditCard>>
    fun observeRevolvingCreditCards(): Flow<List<CreditCard>>
    fun observeCardsWithPoints(): Flow<List<CreditCard>>
    suspend fun getCardById(cardId: String): CreditCard?
    suspend fun addCard(card: CreditCard)
    suspend fun updateCard(card: CreditCard)
    suspend fun deleteCard(cardId: String)
    suspend fun getCardCount(): Int
}
