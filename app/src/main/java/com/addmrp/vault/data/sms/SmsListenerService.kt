package com.addmrp.vault.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.addmrp.vault.domain.model.Transaction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Background SMS listener for auto-detecting bank transactions.
 *
 * Rule 15: Only activates with explicit user consent (toggle in Settings).
 *          All SMS processing happens on-device — no data leaves the phone.
 * Rule 14: The actual parsing is delegated to SmsTransactionParser (pure function).
 *          This class is just the Android glue code.
 *
 * Registered in AndroidManifest.xml with SMS_RECEIVED intent-filter,
 * but ONLY functional when user has enabled the "Auto-detect Transactions"
 * toggle in Settings.
 */
@AndroidEntryPoint
class SmsListenerService : BroadcastReceiver() {

    // Coroutine scope for async DB writes
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Called when a new SMS arrives.
     * Parses it and saves to local DB if it's a valid bank transaction.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        // Rule 15: Check user consent before processing
        val prefs = context.getSharedPreferences("vault_settings", Context.MODE_PRIVATE)
        val isAutoDetectEnabled = prefs.getBoolean("auto_detect_transactions", false)
        if (!isAutoDetectEnabled) return

        try {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            messages.forEach { smsMessage ->
                val body = smsMessage.messageBody ?: return@forEach
                val sender = smsMessage.originatingAddress ?: return@forEach

                // Parse on-device (Rule 15: local only)
                val transaction = SmsTransactionParser.parse(body, sender)

                if (transaction != null) {
                    scope.launch {
                        saveTransaction(context, transaction)
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        Log.d("SmsListener", "Detected transaction: ₹${transaction.amount} at ${transaction.merchant}")
                    }
                }
            }
        } catch (e: Exception) {
            // Rule 11: Never crash on malformed data
            Log.e("SmsListener", "SMS parsing error", e)
        }
    }

    /**
     * Save parsed transaction to local Room database.
     * Note: In production, inject TransactionRepository via Hilt EntryPoint.
     * For now, this uses a simplified approach.
     */
    private suspend fun saveTransaction(context: Context, transaction: Transaction) {
        // TODO: Use Hilt EntryPoint to inject TransactionRepository
        // For now, this is a placeholder that demonstrates the architecture.
        // The actual implementation will use:
        // val entryPoint = EntryPointAccessors.fromApplication(
        //     context.applicationContext,
        //     SmsListenerEntryPoint::class.java
        // )
        // entryPoint.transactionRepository().addTransaction(transaction)
        Log.d("SmsListener", "Saved transaction: ${transaction.merchant} ₹${transaction.amount}")
    }
}
