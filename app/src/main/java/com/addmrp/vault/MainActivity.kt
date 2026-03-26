package com.addmrp.vault

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.addmrp.vault.ui.components.BottomNavBar
import com.addmrp.vault.ui.navigation.Screen
import com.addmrp.vault.ui.navigation.VaultNavGraph
import com.addmrp.vault.ui.theme.VaultBlack
import com.addmrp.vault.ui.theme.VoucherVaultTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedImageUri: Uri? = extractSharedImageUri(intent)
        val sharedText: String? = extractSharedText(intent)
        
        var deepLinkRewardId: String? = null
        val intentData: Uri? = intent?.data
        if (intent?.action == Intent.ACTION_VIEW && intentData != null && intentData.host == "vouchervault.addmrp.com") {
            if (intentData.pathSegments.firstOrNull() == "reward") {
                deepLinkRewardId = intentData.lastPathSegment
            }
        }

        setContent {
            VoucherVaultTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = VaultBlack,
                    bottomBar = {
                        BottomNavBar(
                            currentRoute = currentRoute,
                            onNavigate = { screen ->
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Wallet.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    VaultNavGraph(
                        navController = navController,
                        sharedImageUri = sharedImageUri,
                        sharedText = sharedText,
                        deepLinkRewardId = deepLinkRewardId
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        recreate()
    }

    private fun extractSharedImageUri(intent: Intent?): Uri? {
        if (intent?.action != Intent.ACTION_SEND) return null
        if (intent.type?.startsWith("image/") != true) return null
        return intent.getParcelableExtra(Intent.EXTRA_STREAM)
    }

    private fun extractSharedText(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        if (intent.type == "text/plain") {
            return intent.getStringExtra(Intent.EXTRA_TEXT)
        }
        return null
    }
}
