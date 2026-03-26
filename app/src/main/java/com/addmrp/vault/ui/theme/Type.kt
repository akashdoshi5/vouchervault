package com.addmrp.vault.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Using system default sans-serif which maps to Roboto on Android.
// For a production release, add Inter/Outfit .ttf files to res/font/ and reference here.
val VaultFontFamily = FontFamily.Default

val VaultTypography = Typography(
    // Large header — Total Assets value
    displayLarge = TextStyle(
        fontFamily = VaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        letterSpacing = (-1).sp,
        color = VaultTextPrimary
    ),
    // Screen titles
    headlineLarge = TextStyle(
        fontFamily = VaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp,
        color = VaultTextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = VaultFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        color = VaultTextPrimary
    ),
    // Section headers
    titleLarge = TextStyle(
        fontFamily = VaultFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = VaultTextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = VaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = VaultTextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily = VaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = VaultTextSecondary
    ),
    // Body text
    bodyLarge = TextStyle(
        fontFamily = VaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = VaultTextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = VaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = VaultTextSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = VaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = VaultTextTertiary
    ),
    // Labels & badges
    labelLarge = TextStyle(
        fontFamily = VaultFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp,
        color = VaultTextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = VaultFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        color = VaultTextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = VaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        letterSpacing = 1.2.sp,
        color = VaultTextTertiary
    )
)
