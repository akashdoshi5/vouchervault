package com.addmrp.vault.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.addmrp.vault.ui.theme.*

/**
 * Privacy Shield Badge — shows processing location transparency.
 * Rule 15: Must be visible on every screen that processes financial data.
 */
@Composable
fun PrivacyShieldBadge(
    isProcessingLocally: Boolean = true,
    modifier: Modifier = Modifier
) {
    val (icon, label, color) = if (isProcessingLocally) {
        Triple("🔒", "Processing locally", VaultGreen)
    } else {
        Triple("☁️", "Syncing to cloud (encrypted)", VaultPrimary)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Shield,
            contentDescription = "Privacy",
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "$icon $label",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Savings Dial Widget — central dashboard indicator.
 * Shows "Total Money Saved This Month" (Rule 12: not "Points Earned").
 */
@Composable
fun SavingsDialWidget(
    totalSaved: Double,
    monthLabel: String = "This Month",
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "savings_glow")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        VaultGold.copy(alpha = glowAlpha * 0.2f),
                        VaultCardSurface
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "💰 MONEY SAVED",
                style = MaterialTheme.typography.labelMedium,
                color = VaultGold,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "₹${totalSaved.toLong()}",
                style = MaterialTheme.typography.displayMedium,
                color = VaultTextPrimary,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.bodySmall,
                color = VaultTextTertiary
            )
        }
    }
}

/**
 * Debt Warning Banner — shown when revolving credit detected (Rule 16).
 */
@Composable
fun DebtWarningBanner(
    headline: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VaultRed.copy(alpha = 0.12f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Text("⚠️", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleSmall,
                color = VaultRed,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = VaultTextSecondary
            )
        }
    }
}

/**
 * 3D Credit Card render with glassmorphism effect.
 * Uses animated rotation for premium feel.
 */
@Composable
fun CreditCard3D(
    cardName: String,
    issuerName: String,
    lastFour: String,
    gradientColors: List<androidx.compose.ui.graphics.Color> = listOf(VaultPrimary, VaultSecondary),
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "card_tilt")
    val tiltX by transition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tilt"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .graphicsLayer {
                rotationX = tiltX
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(gradientColors)
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Issuer / chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = issuerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = VaultTextPrimary.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )
                // Chip indicator
                Box(
                    modifier = Modifier
                        .size(40.dp, 30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(VaultGold.copy(alpha = 0.6f))
                )
            }

            Spacer(Modifier.height(32.dp))

            // Card number (masked)
            Text(
                text = "•••• •••• •••• $lastFour",
                style = MaterialTheme.typography.headlineSmall,
                color = VaultTextPrimary,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(16.dp))

            // Card name
            Text(
                text = cardName.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = VaultTextPrimary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
