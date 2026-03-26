package com.addmrp.vault.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.addmrp.vault.data.local.entity.CreditCardEntity
import com.addmrp.vault.data.local.entity.RewardPointEntity
import com.addmrp.vault.data.local.entity.TransactionEntity
import com.addmrp.vault.data.local.entity.VoucherEntity

@Database(
    entities = [
        VoucherEntity::class,
        CreditCardEntity::class,
        TransactionEntity::class,
        RewardPointEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun voucherDao(): VoucherDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun transactionDao(): TransactionDao
    abstract fun rewardPointDao(): RewardPointDao
}
