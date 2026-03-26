package com.addmrp.vault.ui.audit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TrendingDown
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.addmrp.vault.domain.model.InsightSeverity
import com.addmrp.vault.ui.components.DebtWarningBanner
import com.addmrp.vault.ui.components.PrivacyShieldBadge
import com.addmrp.vault.ui.components.SavingsDialWidget
import com.addmrp.vault.ui.theme.*

@Composable
fun SpendAuditScreen(
    viewModel: SpendAuditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VaultBlack)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header ──
        item {
            Text("SPEND AUDIT", style = MaterialTheme.typography.labelMedium, color = VaultOrange)
            Spacer(Modifier.height(4.dp))
            Text("Savings Report", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
            Text("See where you could have saved more.", style = MaterialTheme.typography.bodyMedium, color = VaultTextTertiary)
            Spacer(Modifier.height(8.dp))
            PrivacyShieldBadge()
        }

        // ── Debt Warning ──
        if (state.debtModeActive) {
            item {
                DebtWarningBanner(
                    headline = "Debt Payoff Mode Active",
                    description = "Focus on clearing outstanding balances before reward optimization."
                )
            }
        }

        // ── Savings Dial ──
        item {
            SavingsDialWidget(
                totalSaved = state.totalMissedSavings,
                monthLabel = "Missed Savings This Month"
            )
        }

        // ── Insights ──
        if (state.insights.isEmpty() && !state.isLoading) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎉", style = MaterialTheme.typography.displayLarge)
                    Spacer(Modifier.height(12.dp))
                    Text("You're spending smart!", style = MaterialTheme.typography.titleMedium, color = VaultGreen)
                    Text("No missed savings detected this period.", style = MaterialTheme.typography.bodySmall, color = VaultTextTertiary)
                }
            }
        }

        items(state.insights) { insight ->
            val (cardBg, accentColor) = when (insight.severity) {
                InsightSeverity.SUCCESS -> VaultGreen.copy(alpha = 0.1f) to VaultGreen
                InsightSeverity.WARNING -> VaultOrange.copy(alpha = 0.1f) to VaultOrange
                InsightSeverity.CRITICAL -> VaultRed.copy(alpha = 0.1f) to VaultRed
                InsightSeverity.INFO -> VaultPrimary.copy(alpha = 0.1f) to VaultPrimary
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.TrendingDown,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = insight.type.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(insight.headline, style = MaterialTheme.typography.titleSmall, color = VaultTextPrimary, fontWeight = FontWeight.Bold)
                if (insight.savingsAmount > 0) {
                    Text(insight.formattedSavings, style = MaterialTheme.typography.titleMedium, color = accentColor, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(6.dp))
                Text(insight.description, style = MaterialTheme.typography.bodySmall, color = VaultTextSecondary)
                insight.recommendedCardName?.let {
                    Spacer(Modifier.height(8.dp))
                    Text("💳 Recommended: $it", style = MaterialTheme.typography.labelMedium, color = VaultGold)
                }
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}
