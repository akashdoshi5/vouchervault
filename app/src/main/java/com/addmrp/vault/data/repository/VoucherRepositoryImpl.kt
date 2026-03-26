package com.addmrp.vault.data.repository

import com.addmrp.vault.data.local.VoucherDao
import com.addmrp.vault.data.mapper.VoucherMapper
import com.addmrp.vault.data.remote.FirestoreDataSource
import com.addmrp.vault.domain.model.Voucher
import com.addmrp.vault.domain.repository.VoucherRepository
import com.addmrp.vault.notification.VoucherNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first repository: Room is the primary data source.
 * Firestore is the sync layer. This prevents UI flicker and provides
 * instant responsiveness even without network.
 *
 * Notification Integration:
 *   - addVoucher() → schedules 24h + 2h expiry alerts
 *   - redeemVoucher() / deleteVoucher() → cancels pending alerts (no ghost notifications)
 */
@Singleton
class VoucherRepositoryImpl @Inject constructor(
    private val dao: VoucherDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val notificationManager: VoucherNotificationManager
) : VoucherRepository {

    private val syncScope = CoroutineScope(Dispatchers.IO)

    init {
        // Start real-time Firestore → Room sync (Single Listener pattern)
        syncScope.launch {
            firestoreDataSource.observeVouchers().collect { cloudVouchers ->
                // Fetch deleted IDs to filter zombies
                val deletedIds = firestoreDataSource.fetchDeletedVoucherIds()
                val validVouchers = cloudVouchers.filter { it.id !in deletedIds }
                // Upsert cloud data into Room
                val entities = validVouchers.map { VoucherMapper.domainToEntity(it) }
                dao.upsertAll(entities)
                // Clean up any local zombies
                if (deletedIds.isNotEmpty()) {
                    dao.deleteByIds(deletedIds.toList())
                }
            }
        }
    }

    override fun observeVouchers(): Flow<List<Voucher>> {
        return dao.observeAll().map { entities ->
            entities.map { VoucherMapper.entityToDomain(it) }
        }
    }

    override suspend fun getVoucherById(id: String): Voucher? {
        return dao.getById(id)?.let { VoucherMapper.entityToDomain(it) }
    }

    override suspend fun addVoucher(voucher: Voucher) {
        val toSave = voucher.copy(
            id = voucher.id.ifBlank { UUID.randomUUID().toString() },
            createdAtUtc = Instant.now(),
            updatedAtUtc = Instant.now()
        )
        // Save locally first (offline-first)
        dao.upsert(VoucherMapper.domainToEntity(toSave))

        // ── Schedule expiry notifications (24h + 2h before expiry) ──
        notificationManager.scheduleExpiryAlerts(toSave)

        // Then sync to cloud asynchronously
        syncScope.launch {
            try {
                firestoreDataSource.saveVoucher(toSave)
            } catch (e: Exception) {
                // Silently fail — Room has the data, it will sync on next connection
            }
        }
    }

    override suspend fun updateVoucher(voucher: Voucher) {
        val updated = voucher.copy(updatedAtUtc = Instant.now())
        dao.upsert(VoucherMapper.domainToEntity(updated))

        // ── Reschedule notifications (expiry date may have changed) ──
        notificationManager.scheduleExpiryAlerts(updated)

        syncScope.launch {
            try {
                firestoreDataSource.saveVoucher(updated)
            } catch (_: Exception) { }
        }
    }

    override suspend fun deleteVoucher(id: String) {
        // ── Cancel pending notifications — prevent ghost alerts ──
        notificationManager.cancelAlerts(id)

        // Delete locally first
        dao.deleteById(id)
        // Then propagate to cloud with zombie guard
        syncScope.launch {
            try {
                firestoreDataSource.deleteVoucher(id)
            } catch (_: Exception) { }
        }
    }

    override suspend fun redeemVoucher(id: String) {
        // ── Cancel pending notifications — voucher is used ──
        notificationManager.cancelAlerts(id)

        dao.markRedeemed(id, System.currentTimeMillis())
        val voucher = getVoucherById(id)
        if (voucher != null) {
            syncScope.launch {
                try {
                    firestoreDataSource.saveVoucher(voucher.copy(isRedeemed = true, updatedAtUtc = Instant.now()))
                } catch (_: Exception) { }
            }
        }
    }

    override suspend fun acquireLock(id: String, userId: String) {
        val voucher = getVoucherById(id) ?: return
        if (voucher.inUseByUserId != null && voucher.inUseByUserId != userId) return // Already locked by someone else
        val updated = voucher.copy(
            inUseByUserId = userId,
            inUseTimestampMillis = System.currentTimeMillis(),
            lastUpdatedBy = userId
        )
        dao.upsert(VoucherMapper.domainToEntity(updated))
        syncScope.launch {
            try { firestoreDataSource.saveVoucher(updated) } catch (_: Exception) {}
        }
    }

    override suspend fun releaseLock(id: String) {
        val voucher = getVoucherById(id) ?: return
        val updated = voucher.copy(
            inUseByUserId = null,
            inUseTimestampMillis = null
        )
        dao.upsert(VoucherMapper.domainToEntity(updated))
        syncScope.launch {
            try { firestoreDataSource.saveVoucher(updated) } catch (_: Exception) {}
        }
    }
}
