package com.addmrp.vault.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.addmrp.vault.ui.navigation.Screen
import com.addmrp.vault.ui.theme.VaultBlack
import com.addmrp.vault.ui.theme.VaultDarkSurface
import com.addmrp.vault.ui.theme.VaultNeonBlue
import com.addmrp.vault.ui.theme.VaultTextTertiary

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Wallet, "Wallet", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    BottomNavItem(Screen.Scan, "Scan", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner),
    BottomNavItem(Screen.Rewards, "Rewards", Icons.Filled.Stars, Icons.Outlined.Stars),
    BottomNavItem(Screen.Concierge, "Concierge", Icons.Filled.Psychology, Icons.Outlined.Psychology),
    BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

/**
 * Bottom Navigation Bar with 4 items (Rule 3: separate composable).
 * All items have ≥48dp touch targets (Rule 6).
 */
@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(72.dp),
        containerColor = VaultDarkSurface,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VaultNeonBlue,
                    selectedTextColor = VaultNeonBlue,
                    unselectedIconColor = VaultTextTertiary,
                    unselectedTextColor = VaultTextTertiary,
                    indicatorColor = VaultNeonBlue.copy(alpha = 0.12f)
                )
            )
        }
    }
}
