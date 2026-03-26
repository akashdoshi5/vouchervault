package com.addmrp.vault.data.remote

import com.addmrp.vault.domain.model.GroupMember
import com.addmrp.vault.domain.model.VaultGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore CRUD for VaultGroup.
 *
 * Collection: /vaultGroups/{groupId}
 * SubCollection: /vaultGroups/{groupId}/members/{memberUid}
 *
 * Rule 18: Privacy — shared vouchers show brand/value/expiry but NOT codes
 *          until the owner explicitly approves.
 */
@Singleton
class VaultGroupDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val groupsRef = firestore.collection("vaultGroups")

    private val currentUid: String
        get() = auth.currentUser?.uid ?: ""

    /**
     * Create a new vault group (called from Settings → Manage Shared Group).
     */
    suspend fun createGroup(name: String): VaultGroup {
        val groupId = UUID.randomUUID().toString()
        val user = auth.currentUser ?: throw IllegalStateException("Not authenticated")

        val group = mapOf(
            "id" to groupId,
            "name" to name,
            "ownerUid" to user.uid,
            "ownerDisplayName" to (user.displayName ?: "Vault Owner"),
            "memberUids" to listOf(user.uid),
            "createdAtUtc" to Instant.now().toEpochMilli()
        )

        groupsRef.document(groupId).set(group).await()
        return VaultGroup(
            id = groupId,
            name = name,
            ownerUid = user.uid,
            ownerDisplayName = user.displayName ?: "Vault Owner"
        )
    }

    /**
     * Observe the user's active group (they can only be in one).
     */
    fun observeMyGroup(): Flow<VaultGroup?> = callbackFlow {
        val listener = groupsRef
            .whereArrayContains("memberUids", currentUid)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val doc = snapshot.documents.first()
                val memberUids = (doc.get("memberUids") as? List<*>) ?: emptyList<String>()
                val membersData = (doc.get("members") as? List<Map<String, Any>>) ?: emptyList()

                val group = VaultGroup(
                    id = doc.getString("id") ?: "",
                    name = doc.getString("name") ?: "My Household",
                    ownerUid = doc.getString("ownerUid") ?: "",
                    ownerDisplayName = doc.getString("ownerDisplayName") ?: "",
                    members = membersData.map { m ->
                        GroupMember(
                            uid = m["uid"] as? String ?: "",
                            email = m["email"] as? String ?: "",
                            displayName = m["displayName"] as? String ?: "",
                            voucherContributionCount = (m["voucherContributionCount"] as? Number)?.toInt() ?: 0
                        )
                    },
                    createdAtUtc = Instant.ofEpochMilli(doc.getLong("createdAtUtc") ?: 0)
                )
                trySend(group)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Invite a member by email. Firestore rules will verify the email exists.
     */
    suspend fun inviteMember(groupId: String, email: String, displayName: String, uid: String) {
        val groupDoc = groupsRef.document(groupId)

        // Add uid to memberUids array
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(groupDoc)
            val currentUids = (snapshot.get("memberUids") as? List<*>)?.toMutableList() ?: mutableListOf()
            val currentMembers = (snapshot.get("members") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()

            if (currentUids.size >= VaultGroup.MAX_MEMBERS + 1) {
                throw IllegalStateException("Group is full (max ${VaultGroup.MAX_MEMBERS} members)")
            }

            if (uid !in currentUids) {
                currentUids.add(uid)
                currentMembers.add(mapOf(
                    "uid" to uid,
                    "email" to email,
                    "displayName" to displayName,
                    "joinedAtUtc" to Instant.now().toEpochMilli(),
                    "voucherContributionCount" to 0
                ))

                transaction.update(groupDoc, mapOf(
                    "memberUids" to currentUids,
                    "members" to currentMembers
                ))
            }
        }.await()
    }

    /**
     * Remove a member from the group. Only owner can do this.
     */
    suspend fun removeMember(groupId: String, memberUid: String) {
        val groupDoc = groupsRef.document(groupId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(groupDoc)
            val currentUids = (snapshot.get("memberUids") as? List<*>)?.toMutableList() ?: mutableListOf()
            val currentMembers = (snapshot.get("members") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()

            currentUids.remove(memberUid)
            currentMembers.removeAll { (it["uid"] as? String) == memberUid }

            transaction.update(groupDoc, mapOf(
                "memberUids" to currentUids,
                "members" to currentMembers
            ))
        }.await()
    }

    /**
     * Delete the entire group. Only the owner can do this.
     */
    suspend fun deleteGroup(groupId: String) {
        groupsRef.document(groupId).delete().await()
    }

    /**
     * Get all member UIDs for the current user's group.
     * Used to populate `sharedWith` field on new vouchers.
     */
    suspend fun getGroupMemberUids(): List<String> {
        val snapshot = groupsRef
            .whereArrayContains("memberUids", currentUid)
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) return listOf(currentUid)

        @Suppress("UNCHECKED_CAST")
        return (snapshot.documents.first().get("memberUids") as? List<String>)
            ?: listOf(currentUid)
    }
}
