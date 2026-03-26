package com.addmrp.vault.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.addmrp.vault.data.local.entity.VoucherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoucherDao {

    @Query("SELECT * FROM vouchers ORDER BY expiryUtcMillis ASC")
    fun observeAll(): Flow<List<VoucherEntity>>

    @Query("SELECT * FROM vouchers WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): VoucherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: VoucherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<VoucherEntity>)

    @Query("DELETE FROM vouchers WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM vouchers WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("UPDATE vouchers SET isRedeemed = 1, updatedAtUtcMillis = :now WHERE id = :id")
    suspend fun markRedeemed(id: String, now: Long)

    /** Synchronous query for boot rescheduling — loads all active vouchers. */
    @Query("SELECT * FROM vouchers WHERE isRedeemed = 0 ORDER BY expiryUtcMillis ASC")
    suspend fun getAllVouchersSync(): List<VoucherEntity>
}
