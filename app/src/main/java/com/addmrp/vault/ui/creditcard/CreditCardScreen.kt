package com.addmrp.vault.ui.creditcard

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.addmrp.vault.domain.model.CardIssuer
import com.addmrp.vault.domain.usecase.DebtStatus
import com.addmrp.vault.ui.components.CreditCard3D
import com.addmrp.vault.ui.components.DebtWarningBanner
import com.addmrp.vault.ui.components.PrivacyShieldBadge
import com.addmrp.vault.ui.theme.*

@Composable
fun CreditCardScreen(
    viewModel: CreditCardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            Toast.makeText(context, "✅ Card added!", Toast.LENGTH_SHORT).show()
            viewModel.resetSaveSuccess()
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = VaultSurface,
        focusedContainerColor = VaultSurface,
        unfocusedBorderColor = VaultOutline,
        focusedBorderColor = VaultPrimary,
        unfocusedLabelColor = VaultTextTertiary,
        focusedLabelColor = VaultPrimary,
        cursorColor = VaultPrimary,
        unfocusedTextColor = VaultTextPrimary,
        focusedTextColor = VaultTextPrimary,
        unfocusedPlaceholderColor = VaultTextTertiary,
        focusedPlaceholderColor = VaultTextTertiary
    )

    Box(modifier = Modifier.fillMaxSize().background(VaultBlack)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header ──
            item {
                Text("MY CARDS", style = MaterialTheme.typography.labelMedium, color = VaultPrimary)
                Spacer(Modifier.height(4.dp))
                Text("Credit Card Vault", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(4.dp))
                PrivacyShieldBadge(isProcessingLocally = true)
            }

            // ── Debt Warning ──
            if (state.debtStatus is DebtStatus.CRITICAL) {
                item {
                    val critical = state.debtStatus as DebtStatus.CRITICAL
                    DebtWarningBanner(
                        headline = "Revolving Credit Detected",
                        description = critical.recommendation
                    )
                }
            }

            // ── Card List ──
            if (state.cards.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💳", style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.height(12.dp))
                        Text("No cards yet", style = MaterialTheme.typography.titleMedium, color = VaultTextSecondary)
                        Text("Tap + to add your first credit card", style = MaterialTheme.typography.bodySmall, color = VaultTextTertiary)
                    }
                }
            }

            items(state.cards, key = { it.id }) { card ->
                val gradient = when (card.issuer) {
                    CardIssuer.HDFC -> listOf(VaultPrimary, VaultSecondary)
                    CardIssuer.SBI -> listOf(VaultPrimary, VaultGreen)
                    CardIssuer.ICICI -> listOf(VaultOrange, VaultRed)
                    CardIssuer.AXIS -> listOf(VaultSecondary, VaultRed)
                    CardIssuer.AMEX -> listOf(VaultGold, VaultGoldDim)
                    else -> listOf(VaultPrimary, VaultSecondary)
                }

                Column {
                    CreditCard3D(
                        cardName = card.cardName,
                        issuerName = card.issuer.displayName,
                        lastFour = card.lastFourDigits.ifBlank { "••••" },
                        gradientColors = gradient
                    )
                    // Card details row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${card.rewardType.displayName} • ${card.defaultCashbackPercent}% default",
                                style = MaterialTheme.typography.bodySmall,
                                color = VaultTextTertiary
                            )
                            if (card.isInDebtDanger) {
                                Text(
                                    text = "⚠️ High utilization: ${card.utilizationPercent.toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VaultRed
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.deleteCard(card.id) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Filled.Delete, "Delete card", tint = VaultRed.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // ── Add Card Form ──
            if (state.showAddCardForm) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(VaultCardSurface)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("ADD NEW CARD", style = MaterialTheme.typography.labelMedium, color = VaultGold, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = state.formCardName,
                            onValueChange = viewModel::onCardNameChanged,
                            label = { Text("CARD NAME", style = MaterialTheme.typography.labelSmall) },
                            placeholder = { Text("e.g., HDFC Regalia") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = textFieldColors,
                            singleLine = true
                        )

                        // Issuer Dropdown
                        Box {
                            OutlinedTextField(
                                value = state.formIssuer.displayName,
                                onValueChange = {},
                                label = { Text("ISSUER BANK", style = MaterialTheme.typography.labelSmall) },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleIssuerDropdown() },
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors,
                                singleLine = true
                            )
                            DropdownMenu(
                                expanded = state.showIssuerDropdown,
                                onDismissRequest = { viewModel.toggleIssuerDropdown() },
                                modifier = Modifier.background(VaultCardSurface)
                            ) {
                                CardIssuer.entries.forEach { issuer ->
                                    DropdownMenuItem(
                                        text = { Text(issuer.displayName, color = VaultTextPrimary) },
                                        onClick = { viewModel.onIssuerSelected(issuer) }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = state.formLastFour,
                            onValueChange = viewModel::onLastFourChanged,
                            label = { Text("LAST 4 DIGITS", style = MaterialTheme.typography.labelSmall) },
                            placeholder = { Text("1234") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = textFieldColors,
                            singleLine = true
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = state.formDefaultCashback,
                                onValueChange = viewModel::onDefaultCashbackChanged,
                                label = { Text("DEFAULT %", style = MaterialTheme.typography.labelSmall) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors,
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state.formAnnualFee,
                                onValueChange = viewModel::onAnnualFeeChanged,
                                label = { Text("ANNUAL FEE", style = MaterialTheme.typography.labelSmall) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors,
                                singleLine = true
                            )
                        }

                        if (state.error != null) {
                            Text(state.error!!, color = VaultRed, style = MaterialTheme.typography.bodySmall)
                        }

                        Button(
                            onClick = viewModel::saveCard,
                            enabled = !state.isSaving,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VaultGold, contentColor = VaultBlack)
                        ) {
                            Text(if (state.isSaving) "Saving..." else "Add Card", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        // ── FAB ──
        FloatingActionButton(
            onClick = viewModel::toggleAddCardForm,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = VaultGold,
            contentColor = VaultBlack
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Card")
        }
    }
}
