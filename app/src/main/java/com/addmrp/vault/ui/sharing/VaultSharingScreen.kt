package com.addmrp.vault.ui.sharing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.addmrp.vault.domain.model.GroupMember
import com.addmrp.vault.domain.model.VaultGroup
import com.addmrp.vault.ui.theme.*

@Composable
fun VaultSharingScreen(
    viewModel: VaultSharingViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VaultBlack)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Header ──
        item {
            Text("MY HOUSEHOLD", style = MaterialTheme.typography.labelMedium, color = VaultGold)
            Spacer(Modifier.height(4.dp))
            Text("Family Vault", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
            Text("Share vouchers & rewards with your household.", style = MaterialTheme.typography.bodyMedium, color = VaultTextTertiary)
        }

        // ── Privacy Banner ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(VaultPrimary.copy(alpha = 0.1f))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = VaultPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Privacy Protected", style = MaterialTheme.typography.labelMedium, color = VaultPrimary, fontWeight = FontWeight.Bold)
                    Text("Members see brand & value — promo codes stay hidden until you share them.", style = MaterialTheme.typography.bodySmall, color = VaultTextSecondary)
                }
            }
        }

        // ── No Group State ──
        if (state.group == null && !state.isLoading) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Group, contentDescription = null, tint = VaultTextTertiary, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No household group yet", style = MaterialTheme.typography.titleMedium, color = VaultTextSecondary)
                    Text("Create one to share vouchers with family.", style = MaterialTheme.typography.bodySmall, color = VaultTextTertiary)
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = viewModel::createGroup,
                        modifier = Modifier.fillMaxWidth(0.7f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VaultGold)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Create Household", fontWeight = FontWeight.Bold, color = VaultBlack)
                    }
                }
            }
        }

        // ── Active Group ──
        state.group?.let { group ->
            // Owner badge
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(VaultCardSurface)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(VaultGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = VaultGold, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(group.ownerDisplayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Owner • You", style = MaterialTheme.typography.bodySmall, color = VaultGold)
                    }
                }
            }

            // Members
            items(state.members) { member ->
                MemberRow(
                    member = member,
                    isOwner = state.isOwner,
                    onRemove = { viewModel.removeMember(member.uid) }
                )
            }

            // Invite form
            if (state.isOwner && group.canAddMember()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("INVITE MEMBER", style = MaterialTheme.typography.labelMedium, color = VaultTextTertiary)
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.inviteEmail,
                        onValueChange = viewModel::updateInviteEmail,
                        placeholder = { Text("Enter email address", color = VaultTextTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = VaultSurface,
                            unfocusedContainerColor = VaultSurface,
                            focusedBorderColor = VaultPrimary,
                            unfocusedBorderColor = VaultElevatedSurface,
                            cursorColor = VaultPrimary,
                            focusedTextColor = VaultTextPrimary,
                            unfocusedTextColor = VaultTextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = viewModel::inviteMember,
                        enabled = state.inviteEmail.isNotBlank() && !state.isInviting,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary)
                    ) {
                        Text(if (state.isInviting) "Sending..." else "Send Invite", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Member count info
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "${group.memberCount} of ${VaultGroup.MAX_MEMBERS + 1} slots used",
                    style = MaterialTheme.typography.bodySmall,
                    color = VaultTextTertiary
                )
            }
        }

        // Success/Error messages
        state.successMessage?.let { msg ->
            item {
                Text(msg, style = MaterialTheme.typography.bodyMedium, color = VaultGreen, fontWeight = FontWeight.Bold)
            }
        }
        state.errorMessage?.let { msg ->
            item {
                Text(msg, style = MaterialTheme.typography.bodyMedium, color = VaultRed, fontWeight = FontWeight.Bold)
            }
        }

        item { Spacer(Modifier.height(100.dp)) }
    }
}

@Composable
private fun MemberRow(
    member: GroupMember,
    isOwner: Boolean,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VaultCardSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(VaultElevatedSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                member.displayName.take(1).uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = VaultPrimary
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(member.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(member.email, style = MaterialTheme.typography.bodySmall, color = VaultTextTertiary)
            if (member.voucherContributionCount > 0) {
                Text("${member.voucherContributionCount} vouchers contributed", style = MaterialTheme.typography.labelSmall, color = VaultGreen)
            }
        }
        if (isOwner) {
            IconButton(onClick = onRemove, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = VaultRed, modifier = Modifier.size(18.dp))
            }
        }
    }
}

