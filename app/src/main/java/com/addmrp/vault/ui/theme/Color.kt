package com.addmrp.vault.ui.theme

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════
// VoucherVault — "Obsidian Glass" Design System Palette
// Source: Obsidian Glass Material Theme
//   Primary   #4285F4  (Google Blue)
//   Secondary #5F259F  (Deep Purple)
//   Tertiary  #D4AF37  (Classic Gold)
//   Neutral   #0A0A0B  (True near-black)
// ══════════════════════════════════════════════════════════

// ── Backgrounds (Neutral axis) ──
val VaultBlack           = Color(0xFF0A0A0B) // Neutral base — darkest background
val VaultDarkSurface     = Color(0xFF111114) // App-bar / nav-bar background
val VaultSurface         = Color(0xFF19191E) // Input fields / search bar
val VaultCardSurface     = Color(0xFF1E1E26) // Card backgrounds
val VaultElevatedSurface = Color(0xFF28283A) // Elevated chips / badges / dropdowns

// ── Primary — #4285F4 (Google Blue) ──
val VaultPrimary         = Color(0xFF4285F4) // Buttons, active nav, main CTA
val VaultPrimaryDim      = Color(0xFF2A5DB8) // Disabled / dimmed primary
val VaultPrimaryGlow     = Color(0x404285F4) // Card glow / shadow halo
val VaultPrimaryLight    = Color(0xFF7AACFF) // Links, inline highlights

// ── Secondary — #5F259F (Deep Purple) ──
val VaultSecondary       = Color(0xFF5F259F) // PhonePe source badge, secondary CTA
val VaultSecondaryDim    = Color(0xFF3D1870) // Dimmed secondary
val VaultSecondaryGlow   = Color(0x405F259F) // Purple glow on CRED/PhonePe cards
val VaultSecondaryLight  = Color(0xFF9B6DD7) // Lighter purple for text on dark bg

// ── Tertiary — #D4AF37 (Classic Gold) ──
val VaultGold            = Color(0xFFD4AF37) // AI Logic badge, premium tags, gold accents
val VaultGoldDim         = Color(0xFF9B7D20) // Dimmed gold state
val VaultGoldGlow        = Color(0x40D4AF37) // Gold glow for AI recommendation cards
val VaultGoldLight       = Color(0xFFE8D07F) // Light gold for on-secondary text

// ── Status Colors ──
val VaultGreen           = Color(0xFF4CAF50) // Active / Success / Scraping active
val VaultRed             = Color(0xFFEF5350) // Error / Expired / Sign-out
val VaultOrange          = Color(0xFFFF9800) // Expiring soon (>24h)

// ── Text Hierarchy ──
val VaultTextPrimary     = Color(0xFFE8E8F0) // Headline, titles, primary content
val VaultTextSecondary   = Color(0xFF9090A0) // Subtitles, descriptions, labels
val VaultTextTertiary    = Color(0xFF60607A) // Hints, disabled, metadata

// ── Source Badge Colors (Indian fintech platforms) ──
val VaultGPayBlue        = Color(0xFF4285F4) // GPay — same as primary
val VaultPhonePePurple   = Color(0xFF5F259F) // PhonePe — same as secondary
val VaultCredMint        = Color(0xFF2BD9A8) // CRED — mint green accent

// ── Chip States ──
val VaultChipSelected         = VaultPrimary
val VaultChipUnselected       = VaultElevatedSurface
val VaultChipTextSelected     = Color(0xFFFFFFFF)  // White on blue chip
val VaultChipTextUnselected   = VaultTextSecondary

// ── Outlines & Dividers ──
val VaultOutline         = Color(0xFF2A2A3A) // Input borders, card outlines
val VaultDivider         = Color(0xFF1C1C2C) // Section dividers

// ── Legacy aliases (backward compatibility) ──
// These map old names to the new Obsidian Glass tokens so
// existing composables continue to compile without changes.
val VaultNeonBlue        = VaultPrimary
val VaultNeonBlueDim     = VaultPrimaryDim
val VaultNeonBlueGlow    = VaultPrimaryGlow
