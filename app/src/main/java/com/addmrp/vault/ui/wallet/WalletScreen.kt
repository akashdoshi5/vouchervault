package com.addmrp.vault.ui.wallet

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import com.addmrp.vault.ui.components.CategoryChip
import com.addmrp.vault.ui.components.VoucherCard
import com.addmrp.vault.ui.theme.*
import java.text.NumberFormat
import java.util.Locale
import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import com.addmrp.vault.domain.model.Voucher
import com.google.firebase.auth.FirebaseAuth


private val categories = listOf("All", "Food", "Fashion", "Travel", "Electronics", "Health")

@Composable
fun WalletScreen(
    onNavigateToScan: () -> Unit = {},
    viewModel: WalletViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = VaultBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToScan,
                containerColor = VaultNeonBlue,
                contentColor = VaultBlack,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add voucher")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Header: Status Indicators ──
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusIndicator("SCRAPING ACTIVE", "(GMAIL/SMS)", VaultGreen)
                        StatusIndicator("FAMILY SYNCED", "(4 MEMBERS)", VaultNeonBlue)
                    }
                }
            }

            // ── Total Assets ──
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "TOTAL ASSETS",
                        style = MaterialTheme.typography.labelMedium,
                        color = VaultTextTertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatCurrency(state.totalAssets),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 38.sp
                            ),
                            color = VaultTextPrimary
                        )
                        // Family member avatars
                        Row {
                            listOf(VaultNeonBlue, VaultGold, VaultPhonePePurple).forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .offset(x = (-8).dp)
                                        .clip(CircleShape)
                                        .background(color),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "•",
                                        color = VaultBlack,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Search Bar ──
            item {
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = VaultTextTertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Where are you spending today? e.g.",
                                color = VaultTextTertiary
                            )
                        }
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = VaultTextSecondary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = VaultSurface,
                        focusedContainerColor = VaultSurface,
                        unfocusedBorderColor = VaultOutline,
                        focusedBorderColor = VaultNeonBlue,
                        cursorColor = VaultNeonBlue
                    ),
                    singleLine = true
                )
            }

            // ── Category Chips ──
            item {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryChip(
                            label = category,
                            isSelected = state.selectedCategory == category,
                            onClick = { viewModel.onCategorySelected(category) }
                        )
                    }
                }
            }

            // ── Loading State ──
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = VaultNeonBlue)
                    }
                }
            }

            // ── Empty State ──
            if (!state.isLoading && state.vouchers.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏦",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your vault is empty",
                            style = MaterialTheme.typography.titleLarge,
                            color = VaultTextSecondary
                        )
                        Text(
                            text = "Tap + to add your first voucher",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VaultTextTertiary
                        )
                    }
                }
            }

            // ── Voucher Cards ──
            items(
                items = state.vouchers,
                key = { it.id }
            ) { voucher ->
                VoucherCard(
                    voucher = voucher,
                    onRedeem = { viewModel.startRedemption(voucher) },
                    onShare = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Use my reward for ${voucher.brand}: https://vouchervault.addmrp.com/reward/${voucher.id}")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }
    }

    // ── Redeem / Lock Dialog ──
    state.redeemingVoucher?.let { voucher ->
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (voucher.inUseByUserId != null && voucher.inUseByUserId != currentUserId) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelRedemption() },
                containerColor = VaultSurface,
                titleContentColor = VaultTextPrimary,
                textContentColor = VaultTextSecondary,
                title = { Text("Reward Locked 🔒") },
                text = { Text("Another family member (${voucher.lastUpdatedBy}) is currently viewing or using this reward to prevent double-redemption.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.cancelRedemption() }) { Text("OK", color = VaultNeonBlue) }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { viewModel.cancelRedemption() },
                containerColor = VaultSurface,
                titleContentColor = VaultTextPrimary,
                textContentColor = VaultTextSecondary,
                title = { Text("Redeem ${voucher.brand}") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Show this code at the store:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(VaultElevatedSurface, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = voucher.code.ifBlank { "NO CODE" },
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                letterSpacing = 2.sp,
                                color = VaultNeonBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Marking as used will permanently remove this from your active vault.", style = MaterialTheme.typography.labelSmall)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmRedemption() },
                        colors = ButtonDefaults.buttonColors(containerColor = VaultNeonBlue, contentColor = VaultBlack)
                    ) {
                        Text("Mark Used", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelRedemption() }) { Text("Cancel", color = VaultTextSecondary) }
                }
            )
        }
    }
}

@Composable
private fun StatusIndicator(title: String, subtitle: String, dotColor: androidx.compose.ui.graphics.Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_pulse"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = alpha))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$title ",
            style = MaterialTheme.typography.labelSmall,
            color = VaultTextSecondary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = VaultTextTertiary
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    formatter.maximumFractionDigits = 2
    return formatter.format(amount)
}
