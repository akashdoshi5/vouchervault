package com.addmrp.vault.data.remote

import com.addmrp.vault.data.mapper.VoucherMapper
import com.addmrp.vault.domain.model.Voucher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore data source following the SINGLE LISTENER pattern from best practices.
 * All real-time listeners are centralized here — no duplicate listeners in ViewModels.
 */
@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val currentUid: String?
        get() = auth.currentUser?.uid

    private fun vouchersCollection(uid: String) =
        firestore.collection("users").document(uid).collection("vouchers")

    private fun deletedVouchersCollection(uid: String) =
        firestore.collection("users").document(uid).collection("deletedVouchers")

    /**
     * Observe vouchers in real-time. Single listener — per best practices.
     * Uses collectionGroup query to find all vouchers shared with the current user.
     */
    fun observeVouchers(): Flow<List<Voucher>> = callbackFlow {
        val uid = currentUid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        // Rule: Observe all vouchers where 'sharedWith' array contains my UID
        val listener: ListenerRegistration = firestore.collectionGroup("vouchers")
            .whereArrayContains("sharedWith", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val vouchers = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { data ->
                        VoucherMapper.firestoreMapToDomain(doc.id, data)
                    }
                } ?: emptyList()
                trySend(vouchers)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Save a voucher to Firestore (merge to prevent overwrites).
     * Rule: Save to the owner's collection to maintain a single source of truth for Shared Rewards.
     */
    suspend fun saveVoucher(voucher: Voucher) {
        val uid = currentUid ?: return
        val targetUid = if (voucher.ownerId.isNotEmpty()) voucher.ownerId else uid
        val updatedVoucher = voucher.copy(ownerId = targetUid)
        
        val data = VoucherMapper.domainToFirestoreMap(updatedVoucher)
        vouchersCollection(targetUid)
            .document(updatedVoucher.id)
            .set(data, SetOptions.merge())
            .await()
    }

    /**
     * Delete a voucher AND write to deletedVouchers subcollection (Zombie Guard).
     */
    suspend fun deleteVoucher(id: String) {
        val uid = currentUid ?: return
        // Write to deleted records first (zombie guard)
        deletedVouchersCollection(uid)
            .document(id)
            .set(mapOf("deletedAtUtcMillis" to System.currentTimeMillis()))
            .await()
        // Then delete the actual document
        vouchersCollection(uid)
            .document(id)
            .delete()
            .await()
    }

    /**
     * Fetch all deleted voucher IDs for zombie filtering on startup.
     */
    suspend fun fetchDeletedVoucherIds(): Set<String> {
        val uid = currentUid ?: return emptySet()
        return try {
            deletedVouchersCollection(uid)
                .get()
                .await()
                .documents
                .map { it.id }
                .toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
}
