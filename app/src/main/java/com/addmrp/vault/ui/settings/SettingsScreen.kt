package com.addmrp.vault.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.addmrp.vault.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VaultBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ── Profile Avatar ──
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(VaultElevatedSurface)
                .border(3.dp, VaultGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.userName.take(1).uppercase(),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = VaultGold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(state.userName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(state.email, style = MaterialTheme.typography.bodyMedium, color = VaultTextSecondary)

        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(VaultGold.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                "⭐ ${state.memberBadge}",
                style = MaterialTheme.typography.labelMedium,
                color = VaultGold,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Household & Family ──
        SectionHeader("HOUSEHOLD & FAMILY")
        SettingsRow(icon = Icons.Filled.Group, title = "Manage Shared Group", subtitle = "${state.familyMembersCount} members active", hasArrow = true)

        Spacer(modifier = Modifier.height(20.dp))

        // ── Automation & Intelligence ──
        SectionHeader("AUTOMATION & INTELLIGENCE")
        SettingsToggleRow(
            icon = Icons.Filled.Email,
            title = "Gmail Scraper",
            subtitle = "Auto-detect rewards from inbox",
            isChecked = state.isGmailScraperEnabled,
            onToggle = viewModel::toggleGmailScraper
        )
        SettingsToggleRow(
            icon = Icons.Filled.Sms,
            title = "SMS Listener",
            subtitle = "Parse OTPs and bank alerts",
            isChecked = state.isSmsListenerEnabled,
            onToggle = viewModel::toggleSmsListener
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Security ──
        SectionHeader("SECURITY")
        SettingsToggleRow(
            icon = Icons.Filled.Fingerprint,
            title = "Biometric Unlock",
            subtitle = null,
            isChecked = state.isBiometricEnabled,
            onToggle = viewModel::toggleBiometric
        )
        SettingsRow(
            icon = Icons.Filled.Lock,
            title = "Encrypted Vault",
            subtitle = "AES-256 Protocol Active",
            trailing = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(VaultGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("SECURE", style = MaterialTheme.typography.labelSmall, color = VaultGreen, fontWeight = FontWeight.Bold)
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Preferences ──
        SectionHeader("PREFERENCES")
        SettingsRow(icon = Icons.Filled.Notifications, title = "Notifications", subtitle = "Smart alerts & weekly summaries", hasArrow = true)
        SettingsRow(icon = Icons.Filled.Palette, title = "Theme", subtitle = state.themeName, hasArrow = true)

        Spacer(modifier = Modifier.height(20.dp))

        // ── Support & About ──
        SectionHeader("SUPPORT & ABOUT")
        SettingsRow(icon = Icons.AutoMirrored.Filled.HelpCenter, title = "Help Center", hasArrow = true)
        SettingsRow(icon = Icons.Filled.Policy, title = "Privacy Policy", hasArrow = true)
        SettingsRow(
            icon = Icons.Filled.Info,
            title = "VoucherVault ${state.appVersion}",
            trailing = {
                Text("BUILD ${state.buildId}", style = MaterialTheme.typography.labelSmall, color = VaultTextTertiary)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Sign Out Button ──
        Button(
            onClick = viewModel::signOut,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VaultRed.copy(alpha = 0.15f),
                contentColor = VaultRed
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = VaultTextTertiary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    hasArrow: Boolean = false,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VaultCardSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(VaultElevatedSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = VaultTextSecondary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = VaultTextPrimary, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = VaultTextTertiary)
            }
        }
        if (trailing != null) trailing()
        if (hasArrow) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = VaultTextTertiary, modifier = Modifier.size(20.dp))
        }
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VaultCardSurface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(VaultElevatedSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = VaultTextSecondary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = VaultTextPrimary, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = VaultTextTertiary)
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = VaultBlack,
                checkedTrackColor = VaultNeonBlue,
                uncheckedThumbColor = VaultTextTertiary,
                uncheckedTrackColor = VaultElevatedSurface,
                uncheckedBorderColor = VaultOutline
            )
        )
    }
    Spacer(Modifier.height(6.dp))
}
