package com.addmrp.vault.ui.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.addmrp.vault.domain.usecase.RewardIntelligenceUseCase
import com.addmrp.vault.ui.components.DebtWarningBanner
import com.addmrp.vault.ui.components.PrivacyShieldBadge
import com.addmrp.vault.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RewardDashboardScreen(
    viewModel: RewardDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val rupeeFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VaultBlack)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header ──
        item {
            Text("REWARD INTELLIGENCE", style = MaterialTheme.typography.labelMedium, color = VaultGold)
            Spacer(Modifier.height(4.dp))
            Text("Your Points", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
            Text("Track, value, and optimize your credit card rewards.", style = MaterialTheme.typography.bodyMedium, color = VaultTextTertiary)
            Spacer(Modifier.height(8.dp))
            PrivacyShieldBadge()
        }

        // ── Debt Warning ──
        if (state.debtModeActive) {
            item {
                DebtWarningBanner(
                    headline = "Debt Payoff Mode Active",
                    description = "Card suggestions disabled. Focus on clearing balances first."
                )
            }
        }

        // ── Portfolio Value Hero Card ──
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(VaultGold.copy(alpha = 0.1f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Stars, contentDescription = null, tint = VaultGold, modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${state.totalPointsBalance} pts",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = VaultGold
                )
                Text(
                    text = "Worth ${rupeeFormat.format(state.totalPortfolioValue).replace(".00", "")}",
                    style = MaterialTheme.typography.titleMedium,
                    color = VaultTextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Text("Across all cards", style = MaterialTheme.typography.bodySmall, color = VaultTextTertiary)
            }
        }

        // ── Expiring Rewards Alert ──
        if (state.expiringRewards.isNotEmpty()) {
            item {
                Text("⏰ EXPIRING SOON", style = MaterialTheme.typography.labelMedium, color = VaultOrange)
            }

            items(state.expiringRewards) { reward ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (reward.isUrgent) VaultRed.copy(alpha = 0.1f)
                            else VaultOrange.copy(alpha = 0.1f)
                        )
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (reward.isUrgent) Icons.Filled.Warning else Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = if (reward.isUrgent) VaultRed else VaultOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(reward.cardName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "${reward.pointBalance} pts (${rupeeFormat.format(reward.valueInRupees).replace(".00", "")})",
                            style = MaterialTheme.typography.bodySmall,
                            color = VaultTextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (reward.isUrgent) VaultRed.copy(alpha = 0.2f) else VaultOrange.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            reward.expiryLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (reward.isUrgent) VaultRed else VaultOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ── Monthly Card Summary ──
        if (state.monthlySummaries.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Text("THIS MONTH", style = MaterialTheme.typography.labelMedium, color = VaultTextTertiary)
            }

            items(state.monthlySummaries) { summary ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(VaultCardSurface)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(VaultPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = VaultPrimary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(summary.cardName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(summary.issuer.name, style = MaterialTheme.typography.labelSmall, color = VaultTextTertiary)
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatColumn("Spent", rupeeFormat.format(summary.totalSpend).replace(".00", ""), VaultTextSecondary)
                        StatColumn("Earned", "+${summary.pointsEarned} pts", VaultGreen)
                        StatColumn("Pending", "${summary.pointsPending} pts", VaultGold)
                    }

                    if (summary.topCategory != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Top: ${summary.topCategory.displayName} (${rupeeFormat.format(summary.topCategorySpend).replace(".00", "")})",
                            style = MaterialTheme.typography.bodySmall,
                            color = VaultTextTertiary
                        )
                    }
                }
            }
        }

        // ── AI Card Suggestions ──
        if (state.cardSuggestions.isNotEmpty() && !state.debtModeActive) {
            item {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = VaultGold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("AI ADVISOR", style = MaterialTheme.typography.labelMedium, color = VaultGold)
                }
            }

            items(state.cardSuggestions) { suggestion ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(VaultGold.copy(alpha = 0.08f))
                        .padding(16.dp)
                ) {
                    Text(
                        "💡 ${suggestion.reason}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VaultTextPrimary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Extra savings: ${rupeeFormat.format(suggestion.potentialExtraSavings).replace(".00", "")}/mo",
                        style = MaterialTheme.typography.titleSmall,
                        color = VaultGold,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // ── Empty State ──
        if (state.cards.isEmpty() && !state.isLoading) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = VaultTextTertiary, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Add your credit cards", style = MaterialTheme.typography.titleMedium, color = VaultTextSecondary)
                    Text("Your reward intelligence will appear here.", style = MaterialTheme.typography.bodySmall, color = VaultTextTertiary)
                }
            }
        }

        item { Spacer(Modifier.height(100.dp)) }
    }
}

@Composable
private fun StatColumn(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = VaultTextTertiary)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = valueColor, fontSize = 13.sp)
    }
}
