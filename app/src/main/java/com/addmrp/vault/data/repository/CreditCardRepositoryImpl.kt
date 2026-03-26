package com.addmrp.vault.data.repository

import com.addmrp.vault.data.local.CreditCardDao
import com.addmrp.vault.data.mapper.CreditCardMapper
import com.addmrp.vault.domain.model.CreditCard
import com.addmrp.vault.domain.repository.CreditCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first CreditCard repository implementation.
 *
 * Rule 8: Room is the single source of truth.
 * Rule 15: Financial data processed locally; Firestore sync is optional.
 */
@Singleton
class CreditCardRepositoryImpl @Inject constructor(
    private val creditCardDao: CreditCardDao
) : CreditCardRepository {

    // TODO: Inject userId from Firebase Auth when available
    private val userId: String get() = "local_user"

    override fun observeAllCards(): Flow<List<CreditCard>> =
        creditCardDao.observeAllCards(userId).map { entities ->
            entities.map { CreditCardMapper.entityToDomain(it) }
        }

    override fun observeRevolvingCreditCards(): Flow<List<CreditCard>> =
        creditCardDao.observeRevolvingCreditCards(userId).map { entities ->
            entities.map { CreditCardMapper.entityToDomain(it) }
        }

    override fun observeCardsWithPoints(): Flow<List<CreditCard>> =
        creditCardDao.observeCardsWithPoints(userId).map { entities ->
            entities.map { CreditCardMapper.entityToDomain(it) }
        }

    override suspend fun getCardById(cardId: String): CreditCard? =
        creditCardDao.getCardById(cardId)?.let { CreditCardMapper.entityToDomain(it) }

    override suspend fun addCard(card: CreditCard) {
        val entity = CreditCardMapper.domainToEntity(card.copy(userId = userId))
        creditCardDao.upsert(entity)
    }

    override suspend fun updateCard(card: CreditCard) {
        val entity = CreditCardMapper.domainToEntity(card.copy(userId = userId))
        creditCardDao.update(entity)
    }

    override suspend fun deleteCard(cardId: String) {
        creditCardDao.deleteById(cardId)
    }

    override suspend fun getCardCount(): Int =
        creditCardDao.getCardCount(userId)
}
