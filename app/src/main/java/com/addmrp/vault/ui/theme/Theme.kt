package com.addmrp.vault.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Obsidian Glass — VoucherVault Dark Theme
 *
 * Based on the "Obsidian Glass" palette:
 *   Primary   = #4285F4 (Google Blue)
 *   Secondary = #5F259F (Deep Purple)
 *   Tertiary  = #D4AF37 (Classic Gold)
 *   Neutral   = #0A0A0B
 */
private val ObsidianGlassColorScheme = darkColorScheme(
    // Primary axis — #4285F4
    primary = VaultPrimary,
    onPrimary = VaultBlack,
    primaryContainer = VaultPrimaryDim,
    onPrimaryContainer = VaultTextPrimary,

    // Secondary axis — #5F259F
    secondary = VaultSecondary,
    onSecondary = VaultTextPrimary,
    secondaryContainer = VaultSecondaryDim,
    onSecondaryContainer = VaultSecondaryLight,

    // Tertiary axis — #D4AF37
    tertiary = VaultGold,
    onTertiary = VaultBlack,
    tertiaryContainer = VaultGoldDim,
    onTertiaryContainer = VaultGoldLight,

    // Backgrounds
    background = VaultBlack,
    onBackground = VaultTextPrimary,

    // Surfaces
    surface = VaultDarkSurface,
    onSurface = VaultTextPrimary,
    surfaceVariant = VaultSurface,
    onSurfaceVariant = VaultTextSecondary,

    // Outlines
    outline = VaultOutline,
    outlineVariant = VaultDivider,

    // Error
    error = VaultRed,
    onError = VaultBlack,
)

@Composable
fun VoucherVaultTheme(content: @Composable () -> Unit) {
    val colorScheme = ObsidianGlassColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = VaultBlack.toArgb()
            window.navigationBarColor = VaultBlack.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VaultTypography,
        content = content
    )
}
