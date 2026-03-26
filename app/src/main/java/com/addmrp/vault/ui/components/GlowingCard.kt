package com.addmrp.vault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.addmrp.vault.ui.theme.VaultCardSurface
import com.addmrp.vault.ui.theme.VaultNeonBlueGlow

/**
 * Reusable glassmorphic card wrapper with subtle glow effect.
 * Soft rounded corners (16-24dp) as per design spec.
 */
@Composable
fun GlowingCard(
    modifier: Modifier = Modifier,
    glowColor: Color = VaultNeonBlueGlow,
    backgroundColor: Color = VaultCardSurface,
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        content()
    }
}
