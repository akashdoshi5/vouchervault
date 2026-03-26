package com.addmrp.vault.ui.swiper

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.addmrp.vault.domain.model.InsightSeverity
import com.addmrp.vault.ui.components.DebtWarningBanner
import com.addmrp.vault.ui.components.PrivacyShieldBadge
import com.addmrp.vault.ui.theme.*

/**
 * "Which Card?" screen — the Optimal Swiper UI.
 *
 * User types a purchase query (e.g., "Buying Groceries at Blinkit")
 * and gets an AI recommendation card showing the best credit card to use.
 */
@Composable
fun OptimalSwiperScreen(
    viewModel: OptimalSwiperViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = VaultSurface,
        focusedContainerColor = VaultSurface,
        unfocusedBorderColor = VaultOutline,
        focusedBorderColor = VaultPrimary,
        unfocusedLabelColor = VaultTextTertiary,
        focusedLabelColor = VaultPrimary,
        cursorColor = VaultPrimary,
        unfocusedLeadingIconColor = VaultTextTertiary,
        focusedLeadingIconColor = VaultPrimary,
        unfocusedTextColor = VaultTextPrimary,
        focusedTextColor = VaultTextPrimary,
        unfocusedPlaceholderColor = VaultTextTertiary,
        focusedPlaceholderColor = VaultTextTertiary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VaultBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // ── Header ──
        Text(
            text = "WHICH CARD?",
            style = MaterialTheme.typography.labelMedium,
            color = VaultGold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Smart Card Advisor",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Tell us what you're buying and we'll find\nthe card that saves you the most.",
            style = MaterialTheme.typography.bodyMedium,
            color = VaultTextTertiary
        )

        Spacer(Modifier.height(8.dp))

        // Rule 15: Privacy badge
        PrivacyShieldBadge(isProcessingLocally = true)

        Spacer(Modifier.height(20.dp))

        // ── Debt Warning ──
        if (state.debtModeActive) {
            DebtWarningBanner(
                headline = "Debt Payoff Mode Active",
                description = "Reward optimization is paused. Clear revolving credit first — interest far exceeds rewards."
            )
            Spacer(Modifier.height(16.dp))
        }

        // ── Search Input ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(VaultCardSurface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChanged,
                label = { Text("WHAT ARE YOU BUYING?", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("e.g., Groceries at Blinkit") },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors,
                singleLine = true
            )

            OutlinedTextField(
                value = state.amount,
                onValueChange = viewModel::onAmountChanged,
                label = { Text("AMOUNT (₹)", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("1000") },
                leadingIcon = {
                    Icon(Icons.Outlined.CurrencyRupee, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors,
                singleLine = true
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Recommendation Card ──
        state.recommendation?.let { insight ->
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
                    .padding(20.dp)
            ) {
                // Type badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.CreditCard,
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

                Spacer(Modifier.height(12.dp))

                // Headline
                Text(
                    text = insight.headline,
                    style = MaterialTheme.typography.titleMedium,
                    color = VaultTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                // Savings amount
                if (insight.savingsAmount > 0) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = insight.formattedSavings,
                        style = MaterialTheme.typography.headlineSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Description
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VaultTextSecondary
                )

                // Alternative card warning
                insight.alternativeCardName?.let { altName ->
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "⚠️ Avoid $altName — ${insight.alternativeReason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = VaultOrange
                    )
                }
            }
        }

        // ── Empty State ──
        if (state.recommendation == null && state.query.isBlank() && state.cards.isEmpty()) {
            Spacer(Modifier.height(40.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("💳", style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Add your credit cards to get started",
                    style = MaterialTheme.typography.bodyLarge,
                    color = VaultTextTertiary
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
