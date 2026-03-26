package com.addmrp.vault.domain.repository

import com.addmrp.vault.domain.model.Voucher
import kotlinx.coroutines.flow.Flow

/**
 * Clean Architecture boundary — the domain layer defines WHAT it needs,
 * the data layer implements HOW.
 */
interface VoucherRepository {
    /** Observe all vouchers for the current user, including shared ones. */
    fun observeVouchers(): Flow<List<Voucher>>

    /** Get a single voucher by ID. */
    suspend fun getVoucherById(id: String): Voucher?

    /** Add a new voucher (saves to Room first, then syncs to Firestore). */
    suspend fun addVoucher(voucher: Voucher)

    /** Update an existing voucher. */
    suspend fun updateVoucher(voucher: Voucher)

    /** Delete a voucher (writes to deletedRecords for zombie guard). */
    suspend fun deleteVoucher(id: String)

    /** Mark a voucher as redeemed. */
    suspend fun redeemVoucher(id: String)

    /** Acquire a real-time lock on a shared voucher to prevent double redemption. */
    suspend fun acquireLock(id: String, userId: String)

    /** Release the real-time lock on a shared voucher. */
    suspend fun releaseLock(id: String)
}
