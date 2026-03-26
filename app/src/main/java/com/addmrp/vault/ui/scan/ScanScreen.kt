package com.addmrp.vault.ui.scan

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.addmrp.vault.domain.model.RedemptionSource
import com.addmrp.vault.domain.model.VoucherCategory
import com.addmrp.vault.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScanScreen(
    viewModel: ScanViewModel = hiltViewModel(),
    sharedImageUri: Uri? = null
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // ── Trigger OCR when a shared image arrives (once) ──
    var ocrTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(sharedImageUri) {
        if (sharedImageUri != null && !ocrTriggered) {
            ocrTriggered = true
            viewModel.processSharedImage(sharedImageUri)
        }
    }

    // Show success toast
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            Toast.makeText(context, "✅ Voucher added to vault!", Toast.LENGTH_SHORT).show()
            viewModel.resetSaveSuccess()
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = VaultSurface,
        focusedContainerColor = VaultSurface,
        unfocusedBorderColor = VaultOutline,
        focusedBorderColor = VaultPrimary,
        unfocusedLabelColor = VaultTextTertiary,
        focusedLabelColor = VaultPrimary,
        cursorColor = VaultPrimary,
        unfocusedLeadingIconColor = VaultTextTertiary,
        focusedLeadingIconColor = VaultPrimary,
        unfocusedTrailingIconColor = VaultTextTertiary,
        focusedTrailingIconColor = VaultPrimary,
        unfocusedTextColor = VaultTextPrimary,
        focusedTextColor = VaultTextPrimary,
        unfocusedPlaceholderColor = VaultTextTertiary,
        focusedPlaceholderColor = VaultTextTertiary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VaultBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // ── Header ──
        Text(
            text = "NEW ENTRY",
            style = MaterialTheme.typography.labelMedium,
            color = VaultPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Secure Your Reward",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Input your manual voucher details to keep your digital\nvault organized and track upcoming expirations.",
            style = MaterialTheme.typography.bodyMedium,
            color = VaultTextTertiary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ═══════════════════════════════════════════════════════
        // OCR Processing Banner (Rule 11 compliance)
        // Non-blocking shimmer effect during ML Kit processing
        // ═══════════════════════════════════════════════════════
        if (state.isProcessingOcr) {
            OcrProcessingBanner()
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ── OCR Pre-fill Success Banner ──
        if (state.isPrefilledFromOcr && !state.isProcessingOcr) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(VaultGold.copy(alpha = 0.12f))
                    .border(1.dp, VaultGold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = VaultGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "✨ AI detected data from your screenshot. Please review before saving.",
                    style = MaterialTheme.typography.bodySmall,
                    color = VaultGold,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Form Card ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(VaultCardSurface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Issuing Brand
            OutlinedTextField(
                value = state.brand,
                onValueChange = viewModel::onBrandChanged,
                label = { Text("ISSUING BRAND", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("e.g., Starbucks, Amazon") },
                leadingIcon = {
                    Icon(Icons.Outlined.Store, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors,
                singleLine = true
            )

            // Voucher Value
            OutlinedTextField(
                value = state.value,
                onValueChange = viewModel::onValueChanged,
                label = { Text("VOUCHER VALUE", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("20% OFF or ₹50") },
                leadingIcon = {
                    Icon(Icons.Outlined.CurrencyRupee, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors,
                singleLine = true
            )

            // Voucher Code
            OutlinedTextField(
                value = state.code,
                onValueChange = viewModel::onCodeChanged,
                label = { Text("VOUCHER CODE", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("XXXX-XXXX-XXXX") },
                leadingIcon = {
                    Icon(Icons.Outlined.Password, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val clip = clipboardManager.getText()?.text ?: ""
                            if (clip.isNotBlank()) viewModel.onCodeChanged(clip)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Outlined.ContentPaste, contentDescription = "Paste", modifier = Modifier.size(20.dp))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors,
                singleLine = true
            )

            // Expiry Date
            OutlinedTextField(
                value = state.expiryDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
                onValueChange = {},
                label = { Text("EXPIRY DATE", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("dd/mm/yyyy") },
                leadingIcon = {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    IconButton(
                        onClick = viewModel::toggleDatePicker,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = "Pick date", modifier = Modifier.size(20.dp))
                    }
                },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleDatePicker() },
                shape = RoundedCornerShape(14.dp),
                colors = textFieldColors,
                singleLine = true
            )

            // Category Dropdown
            Box {
                OutlinedTextField(
                    value = state.selectedCategory.displayName,
                    onValueChange = {},
                    label = { Text("CATEGORY", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Category, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleCategoryDropdown() },
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors,
                    singleLine = true
                )
                DropdownMenu(
                    expanded = state.categoryDropdownExpanded,
                    onDismissRequest = { viewModel.toggleCategoryDropdown() },
                    modifier = Modifier.background(VaultCardSurface)
                ) {
                    VoucherCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName, color = VaultTextPrimary) },
                            onClick = { viewModel.onCategorySelected(category) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Redemption Source ──
        Text(
            text = "REDEMPTION SOURCE",
            style = MaterialTheme.typography.labelSmall,
            color = VaultTextTertiary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(RedemptionSource.GPAY, RedemptionSource.PHONEPE, RedemptionSource.CRED).forEach { source ->
                val isSelected = state.selectedSource == source
                val chipColor = when (source) {
                    RedemptionSource.GPAY -> VaultGPayBlue
                    RedemptionSource.PHONEPE -> VaultPhonePePurple
                    RedemptionSource.CRED -> VaultCredMint
                    else -> VaultTextSecondary
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) chipColor.copy(alpha = 0.2f) else VaultElevatedSurface)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) chipColor else VaultOutline,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.onSourceSelected(source) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(chipColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = source.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) chipColor else VaultTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // ── Error ──
        if (state.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.error!!,
                color = VaultRed,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Add to Vault Button ──
        Button(
            onClick = viewModel::saveVoucher,
            enabled = !state.isSaving && !state.isProcessingOcr,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VaultPrimary,
                contentColor = VaultBlack,
                disabledContainerColor = VaultPrimaryDim
            )
        ) {
            if (state.isSaving) {
                Text("Saving...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            } else {
                Text("Add to Vault", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // ── Date Picker Dialog ──
    if (state.showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = viewModel::toggleDatePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            viewModel.onExpiryDateSelected(date)
                        }
                    }
                ) { Text("Confirm", color = VaultPrimary) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::toggleDatePicker) { Text("Cancel", color = VaultTextSecondary) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ═══════════════════════════════════════════════════════════
// OCR Processing Banner — Non-blocking shimmer effect
// Complies with Rule 11: never crash, show loading gracefully
// ═══════════════════════════════════════════════════════════
@Composable
private fun OcrProcessingBanner() {
    val infiniteTransition = rememberInfiniteTransition(label = "ocr_shimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            VaultPrimary.copy(alpha = 0.1f),
            VaultPrimary.copy(alpha = 0.3f),
            VaultPrimary.copy(alpha = 0.1f)
        ),
        start = Offset(shimmerTranslate - 200f, 0f),
        end = Offset(shimmerTranslate, 0f)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(shimmerBrush)
            .border(1.dp, VaultPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = VaultPrimary,
            strokeWidth = 2.dp
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Extracting data from screenshot...",
            style = MaterialTheme.typography.bodyMedium,
            color = VaultPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
