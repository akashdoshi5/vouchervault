package com.addmrp.vault.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.addmrp.vault.data.local.VaultDatabase
import com.addmrp.vault.data.mapper.VoucherMapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Reschedules all active voucher expiry alarms after device reboot.
 *
 * Android clears all AlarmManager alarms on reboot — this receiver
 * restores them from Room DB to ensure no expiry is missed.
 *
 * Registered in AndroidManifest with BOOT_COMPLETED intent-filter.
 */
@AndroidEntryPoint
class BootRescheduleReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationManager: VoucherNotificationManager
    @Inject lateinit var database: VaultDatabase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.d("BootReceiver", "Device rebooted — rescheduling voucher alarms")

        val pendingResult = goAsync() // keep receiver alive during async work

        scope.launch {
            try {
                // Load all active (non-redeemed) vouchers from Room
                val entities = database.voucherDao().getAllVouchersSync()
                val vouchers = entities
                    .map { VoucherMapper.entityToDomain(it) }
                    .filter { !it.isRedeemed && !it.isExpired }

                notificationManager.rescheduleAlarms(vouchers)

                Log.d("BootReceiver", "Rescheduled ${vouchers.size} voucher alarms")
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to reschedule alarms", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
