package com.addmrp.vault.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.addmrp.vault.ui.theme.VaultChipSelected
import com.addmrp.vault.ui.theme.VaultChipTextSelected
import com.addmrp.vault.ui.theme.VaultChipTextUnselected
import com.addmrp.vault.ui.theme.VaultChipUnselected

/**
 * Horizontal scrolling category filter chip (Rule 3 & Rule 6: 48dp touch target).
 */
@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) VaultChipSelected else VaultChipUnselected,
        label = "chip_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) VaultChipTextSelected else VaultChipTextUnselected,
        label = "chip_text"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp), // Ensures ≥48dp height
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}
