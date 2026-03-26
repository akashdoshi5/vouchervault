package com.addmrp.vault.ui.sharing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.addmrp.vault.ui.theme.VaultNeonBlue

/**
 * Bottom Sheet for Rule 21: Zero-Interruption Ingestion.
 * Shows OCR progress without navigating away from the current screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngestionBottomSheet(
    sharingState: SharingState,
    onDismissRequest: () -> Unit,
    onProceedToSave: (com.addmrp.vault.domain.usecase.OcrResult) -> Unit
) {
    if (sharingState is SharingState.Idle) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 24.dp), // Safe area
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (sharingState) {
                is SharingState.Caching -> {
                    CircularProgressIndicator(color = VaultNeonBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Securing shared image...", style = MaterialTheme.typography.bodyLarge)
                }
                is SharingState.ProcessingOcr -> {
                    CircularProgressIndicator(color = VaultNeonBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Extracting reward data...", style = MaterialTheme.typography.bodyLarge)
                }
                is SharingState.Error -> {
                    Text("Error", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(sharingState.message)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onDismissRequest) { Text("Dismiss") }
                }
                is SharingState.Success -> {
                    Text("Extraction Complete!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val brand = sharingState.result.brand ?: "Unknown Brand"
                    val code = sharingState.result.code ?: "Unknown Code"
                    val expiry = sharingState.result.expiryDate?.toString() ?: "Unknown Expiry"
                    
                    Text("Brand: $brand", style = MaterialTheme.typography.bodyLarge)
                    Text("Code: $code", style = MaterialTheme.typography.bodyLarge)
                    Text("Expiry: $expiry", style = MaterialTheme.typography.bodyLarge)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { onProceedToSave(sharingState.result) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VaultNeonBlue)
                    ) {
                        Text("Review & Save")
                    }
                }
                SharingState.Idle -> {} // Should not be reached due to early return
            }
        }
    }
}
