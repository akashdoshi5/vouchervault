package com.addmrp.vault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.addmrp.vault.domain.model.RedemptionSource
import com.addmrp.vault.domain.model.Voucher
import com.addmrp.vault.ui.theme.*

/**
 * Premium voucher card component matching the design mockups.
 * Displays brand, value, source badge, expiry countdown, and redeem button.
 * Rule 3: Separate reusable composable.
 * Rule 6: All clickable areas ≥48dp.
 */
@Composable
fun VoucherCard(
    voucher: Voucher,
    onRedeem: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current

    GlowingCard(
        modifier = modifier.fillMaxWidth(),
        glowColor = when {
            voucher.isExpiringSoon -> VaultRed.copy(alpha = 0.3f)
            else -> VaultNeonBlueGlow
        }
    ) {
        Column {
            // ── Header Row: Brand + Source Badge ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Brand logo placeholder circle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(VaultElevatedSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = voucher.brand.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VaultNeonBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = voucher.brand,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = voucher.category.displayName,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Source Badge
                SourceBadge(source = voucher.source)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Value Display ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = voucher.valueLabel.ifBlank { "₹${voucher.value.toLong()}" },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp
                        ),
                        color = VaultTextPrimary
                    )
                    if (voucher.valueLabel.isNotBlank() && voucher.valueLabel != "₹${voucher.value.toLong()}") {
                        Text(
                            text = "OFF",
                            style = MaterialTheme.typography.titleSmall,
                            color = VaultTextSecondary
                        )
                    }
                }

                // Redeem Button
                Button(
                    onClick = onRedeem,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VaultNeonBlue,
                        contentColor = VaultBlack
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        text = "Redeem",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

    // ── Bottom Row: Expiry + Code Copy + Share ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expiry countdown
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (voucher.isExpiringSoon) VaultRed
                                else if (voucher.isExpired) VaultTextTertiary
                                else VaultOrange
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = voucher.expiryCountdownText().uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (voucher.isExpiringSoon) VaultRed else VaultTextSecondary
                    )
                }

                // Added By / Lock Status
                if (voucher.inUseByUserId != null) {
                    // Rule 22: Barcode Real-Time Lock indicator
                    Text(
                        text = "🔒 Currently in use",
                        style = MaterialTheme.typography.labelSmall,
                        color = VaultRed,
                        fontWeight = FontWeight.Bold
                    )
                } else if (voucher.addedBy.isNotBlank()) {
                    Text(
                        text = "Added by ${voucher.addedBy}".uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = VaultTextTertiary
                    )
                }

                Row {
                    // Share Link Button (Rule 20)
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share reward deep link",
                            tint = VaultTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Copy code button
                    if (voucher.code.isNotBlank()) {
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(voucher.code))
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Copy voucher code",
                                tint = VaultTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceBadge(source: RedemptionSource) {
    val (bgColor, text) = when (source) {
        RedemptionSource.GPAY -> VaultGPayBlue.copy(alpha = 0.2f) to "GPAY REWARDS"
        RedemptionSource.PHONEPE -> VaultPhonePePurple.copy(alpha = 0.2f) to "PHONEPE EXCLUSIVE"
        RedemptionSource.CRED -> VaultCredMint.copy(alpha = 0.2f) to "CRED RARE"
        RedemptionSource.SMS -> VaultGold.copy(alpha = 0.2f) to "SCRAPED FROM SMS"
        RedemptionSource.EMAIL -> VaultGold.copy(alpha = 0.2f) to "SCRAPED FROM EMAIL"
        RedemptionSource.MANUAL -> VaultElevatedSurface to "MANUAL"
    }
    val textColor = when (source) {
        RedemptionSource.GPAY -> VaultGPayBlue
        RedemptionSource.PHONEPE -> VaultPhonePePurple
        RedemptionSource.CRED -> VaultCredMint
        RedemptionSource.SMS, RedemptionSource.EMAIL -> VaultGold
        RedemptionSource.MANUAL -> VaultTextSecondary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}
