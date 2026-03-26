package com.addmrp.vault.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.addmrp.vault.data.local.entity.RewardPointEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for reward point balances.
 *
 * Supports expiry alerts and valuation dashboard.
 */
@Dao
interface RewardPointDao {

    @Query("SELECT * FROM reward_points WHERE user_id = :userId ORDER BY best_value_in_rupees DESC")
    fun observeAllRewardPoints(userId: String): Flow<List<RewardPointEntity>>

    @Query("""
        SELECT * FROM reward_points 
        WHERE user_id = :userId 
        AND expiry_utc IS NOT NULL 
        AND expiry_utc <= :thresholdUtc
        AND expiry_utc > :nowUtc
        ORDER BY expiry_utc ASC
    """)
    fun observeExpiringPoints(
        userId: String,
        nowUtc: Long,
        thresholdUtc: Long
    ): Flow<List<RewardPointEntity>>

    @Query("SELECT * FROM reward_points WHERE card_id = :cardId")
    suspend fun getPointsByCardId(cardId: String): RewardPointEntity?

    @Query("SELECT SUM(best_value_in_rupees) FROM reward_points WHERE user_id = :userId")
    suspend fun getTotalRewardValue(userId: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(points: RewardPointEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(points: List<RewardPointEntity>)

    @Query("DELETE FROM reward_points WHERE card_id = :cardId")
    suspend fun deleteByCardId(cardId: String)
}
