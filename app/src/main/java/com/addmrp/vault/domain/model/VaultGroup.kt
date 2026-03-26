package com.addmrp.vault.domain.model

import java.time.Instant

/**
 * A household vault group for sharing vouchers and rewards with family/spouse.
 *
 * Rule 18: Group members see brand, value, and expiry — but NEVER the
 * promo code until the owner explicitly reveals it.
 *
 * Max 5 members per group (family-sized).
 */
data class VaultGroup(
    val id: String = "",
    val name: String = "My Household",
    val ownerUid: String = "",
    val ownerDisplayName: String = "",
    val members: List<GroupMember> = emptyList(),
    val createdAtUtc: Instant = Instant.now()
) {
    companion object {
        const val MAX_MEMBERS = 5
    }

    val memberCount: Int get() = members.size + 1 // +1 for owner

    fun canAddMember(): Boolean = members.size < MAX_MEMBERS

    fun isMember(uid: String): Boolean =
        uid == ownerUid || members.any { it.uid == uid }
}

/**
 * A member within a VaultGroup.
 */
data class GroupMember(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val joinedAtUtc: Instant = Instant.now(),
    val voucherContributionCount: Int = 0
)
