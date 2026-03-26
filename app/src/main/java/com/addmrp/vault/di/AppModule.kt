package com.addmrp.vault.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.addmrp.vault.data.local.CreditCardDao
import com.addmrp.vault.data.local.RewardPointDao
import com.addmrp.vault.data.local.TransactionDao
import com.addmrp.vault.data.local.VaultDatabase
import com.addmrp.vault.data.local.VoucherDao
import com.addmrp.vault.data.repository.CreditCardRepositoryImpl
import com.addmrp.vault.data.repository.TransactionRepositoryImpl
import com.addmrp.vault.data.repository.VoucherRepositoryImpl
import com.addmrp.vault.domain.repository.CreditCardRepository
import com.addmrp.vault.domain.repository.TransactionRepository
import com.addmrp.vault.domain.repository.VoucherRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ═══════════════════════════════════════════
    //  Database
    // ═══════════════════════════════════════════

    /**
     * Migration from v1 (vouchers-only) to v2 (MVP2 credit card optimizer).
     */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create credit_cards table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `credit_cards` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `card_name` TEXT NOT NULL,
                    `issuer` TEXT NOT NULL,
                    `last_four_digits` TEXT NOT NULL,
                    `reward_type` TEXT NOT NULL,
                    `reward_rules_json` TEXT NOT NULL,
                    `default_cashback_percent` REAL NOT NULL,
                    `annual_fee` REAL NOT NULL,
                    `annual_fee_waiver_spend` REAL,
                    `credit_limit` REAL,
                    `current_balance` REAL,
                    `is_revolving_credit` INTEGER NOT NULL DEFAULT 0,
                    `reward_points_balance` INTEGER NOT NULL DEFAULT 0,
                    `points_expiry_utc` INTEGER,
                    `current_month_spend` REAL NOT NULL DEFAULT 0.0,
                    `current_month_redeemed` REAL NOT NULL DEFAULT 0.0,
                    `created_at_utc` INTEGER NOT NULL,
                    `updated_at_utc` INTEGER NOT NULL,
                    `user_id` TEXT NOT NULL
                )
            """.trimIndent())

            // Create transactions table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `transactions` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `amount` REAL NOT NULL,
                    `merchant` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `card_id` TEXT NOT NULL,
                    `card_name` TEXT NOT NULL,
                    `is_debit_card` INTEGER NOT NULL DEFAULT 0,
                    `is_international` INTEGER NOT NULL DEFAULT 0,
                    `source` TEXT NOT NULL,
                    `transaction_date_utc` INTEGER NOT NULL,
                    `created_at_utc` INTEGER NOT NULL,
                    `user_id` TEXT NOT NULL,
                    FOREIGN KEY(`card_id`) REFERENCES `credit_cards`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())

            // Create indexes for transactions
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_card_id` ON `transactions` (`card_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_transaction_date_utc` ON `transactions` (`transaction_date_utc`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_category` ON `transactions` (`category`)")

            // Create reward_points table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `reward_points` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `card_id` TEXT NOT NULL,
                    `card_name` TEXT NOT NULL,
                    `issuer` TEXT NOT NULL,
                    `balance` INTEGER NOT NULL DEFAULT 0,
                    `valuation_tiers_json` TEXT NOT NULL,
                    `best_value_in_rupees` REAL NOT NULL DEFAULT 0.0,
                    `expiry_utc` INTEGER,
                    `last_updated_utc` INTEGER NOT NULL,
                    `user_id` TEXT NOT NULL,
                    FOREIGN KEY(`card_id`) REFERENCES `credit_cards`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())

            db.execSQL("CREATE INDEX IF NOT EXISTS `index_reward_points_card_id` ON `reward_points` (`card_id`)")
        }
    }

    @Provides
    @Singleton
    fun provideVaultDatabase(@ApplicationContext context: Context): VaultDatabase {
        return Room.databaseBuilder(
            context,
            VaultDatabase::class.java,
            "vouchervault.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    // ═══════════════════════════════════════════
    //  DAOs
    // ═══════════════════════════════════════════

    @Provides
    @Singleton
    fun provideVoucherDao(database: VaultDatabase): VoucherDao =
        database.voucherDao()

    @Provides
    @Singleton
    fun provideCreditCardDao(database: VaultDatabase): CreditCardDao =
        database.creditCardDao()

    @Provides
    @Singleton
    fun provideTransactionDao(database: VaultDatabase): TransactionDao =
        database.transactionDao()

    @Provides
    @Singleton
    fun provideRewardPointDao(database: VaultDatabase): RewardPointDao =
        database.rewardPointDao()

    // ═══════════════════════════════════════════
    //  Firebase
    // ═══════════════════════════════════════════

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}

/**
 * Binds repository interfaces to their implementations.
 * Separate module for abstract bindings as per Hilt best practice.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCreditCardRepository(
        impl: CreditCardRepositoryImpl
    ): CreditCardRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindVoucherRepository(
        impl: VoucherRepositoryImpl
    ): VoucherRepository
}
