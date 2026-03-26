package com.addmrp.vault.ui.concierge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.addmrp.vault.ui.components.GlowingCard
import com.addmrp.vault.ui.theme.*

@Composable
fun ConciergeScreen(
    viewModel: ConciergeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VaultBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VAULT INTELLIGENCE",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(VaultNeonBlue.copy(alpha = 0.15f))
                    .border(1.dp, VaultNeonBlue.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "LIVE STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = VaultNeonBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Status Row ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusCard(
                icon = Icons.Filled.Email,
                title = "GMAIL\nSCRAPING",
                status = if (state.isGmailScrapingActive) "Active" else "Inactive",
                isActive = state.isGmailScrapingActive,
                modifier = Modifier.weight(1f)
            )
            StatusCard(
                icon = Icons.Filled.Sms,
                title = "SMS\nLISTENER",
                status = if (state.isSmsListenerConnected) "Connected" else "Disconnected",
                isActive = state.isSmsListenerConnected,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── AI Search ──
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            placeholder = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome, null, Modifier.size(18.dp), tint = VaultGold)
                    Spacer(Modifier.width(8.dp))
                    Text("e.g., Ordering on Zomato", color = VaultTextTertiary)
                }
            },
            trailingIcon = {
                Icon(Icons.Rounded.Search, "Search", tint = VaultTextSecondary)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = VaultSurface,
                focusedContainerColor = VaultSurface,
                unfocusedBorderColor = VaultOutline,
                focusedBorderColor = VaultGold,
                cursorColor = VaultGold
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── AI Recommendation Card ──
        GlowingCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = VaultGoldGlow,
            backgroundColor = VaultCardSurface
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(VaultRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Z", color = VaultTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(state.recommendedBrand, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    // AI Logic Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(VaultGold)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✨ ", fontSize = 10.sp)
                            Text(
                                "AI LOGIC",
                                style = MaterialTheme.typography.labelSmall,
                                color = VaultBlack,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.recommendedOffer,
                    style = MaterialTheme.typography.titleLarge,
                    color = VaultGold,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(VaultElevatedSurface)
                        .padding(12.dp)
                ) {
                    Text(
                        text = state.recommendedReason,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = VaultTextSecondary
                    )
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { /* Claim flow */ },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VaultGold,
                        contentColor = VaultBlack
                    )
                ) {
                    Text("Claim Reward", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Family Group ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("FAMILY GROUP", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VaultTextPrimary)
            Text("+ Manage", style = MaterialTheme.typography.labelLarge, color = VaultGold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(state.familyMembers) { member ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(VaultElevatedSurface)
                            .border(2.dp, VaultGold.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            member.name.take(1),
                            fontWeight = FontWeight.Bold,
                            color = VaultGold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(member.name, style = MaterialTheme.typography.bodySmall, color = VaultTextSecondary)
                }
            }
            // Add member
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(2.dp, VaultOutline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add member", tint = VaultTextTertiary)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Add", style = MaterialTheme.typography.bodySmall, color = VaultTextTertiary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Shared Vault ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = VaultGold, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("SHARED VAULT", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = VaultTextPrimary)
        }

        Spacer(modifier = Modifier.height(12.dp))

        state.sharedVaultItems.forEach { item ->
            GlowingCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                glowColor = VaultNeonBlueGlow.copy(alpha = 0.1f),
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${item.addedBy} • ${item.timeAgo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = VaultTextTertiary
                        )
                    }
                    IconButton(onClick = { /* Copy code */ }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = VaultTextTertiary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun StatusCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    status: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    GlowingCard(
        modifier = modifier,
        glowColor = if (isActive) VaultGreen.copy(alpha = 0.2f) else VaultRed.copy(alpha = 0.2f),
        elevation = 4.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(VaultElevatedSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = VaultGold, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall,
                    color = VaultTextSecondary,
                    lineHeight = 14.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isActive) VaultGreen else VaultRed)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        status,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isActive) VaultGreen else VaultRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
