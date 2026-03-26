package com.addmrp.vault.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.addmrp.vault.domain.model.Voucher
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages voucher expiry notifications using AlarmManager exact alarms.
 *
 * Rule 17: Financial Advisor persona — focus on Loss of Value, not generic alerts.
 *
 * Schedule:
 *   • Alert 1 — 24 hours before expiry (DEFAULT importance)
 *   • Alert 2 —  2 hours before expiry (HIGH importance)
 *
 * Auto-cancels on voucher redeem/delete to prevent ghost notifications.
 */
@Singleton
class VoucherNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val EXTRA_VOUCHER_ID = "voucher_id"
        const val EXTRA_BRAND = "voucher_brand"
        const val EXTRA_VALUE = "voucher_value"
        const val EXTRA_CATEGORY = "voucher_category"
        const val EXTRA_ALERT_TYPE = "alert_type"

        const val ALERT_24H = "24h"
        const val ALERT_2H = "2h"

        // Request code offsets to avoid collisions
        private const val REQUEST_CODE_24H_OFFSET = 100_000
        private const val REQUEST_CODE_2H_OFFSET = 200_000
    }

    /**
     * Schedule both expiry alerts for a voucher.
     * Called after addVoucher() and updateVoucher().
     */
    fun scheduleExpiryAlerts(voucher: Voucher) {
        // Cancel any existing alarms first (in case of update)
        cancelAlerts(voucher.id)

        val now = Instant.now()
        val expiry = voucher.expiryUtc

        // Don't schedule for already-expired or redeemed vouchers
        if (expiry.isBefore(now) || voucher.isRedeemed) return

        // Alert 1: 24 hours before expiry
        val alert24h = expiry.minus(Duration.ofHours(24))
        if (alert24h.isAfter(now)) {
            scheduleAlarm(voucher, ALERT_24H, alert24h.toEpochMilli(), REQUEST_CODE_24H_OFFSET)
        }

        // Alert 2: 2 hours before expiry (HIGH importance)
        val alert2h = expiry.minus(Duration.ofHours(2))
        if (alert2h.isAfter(now)) {
            scheduleAlarm(voucher, ALERT_2H, alert2h.toEpochMilli(), REQUEST_CODE_2H_OFFSET)
        }

        Log.d("NotifManager", "Scheduled alerts for ${voucher.brand} (${voucher.id})")
    }

    /**
     * Cancel all pending alarms for a voucher.
     * Called on redeemVoucher() and deleteVoucher().
     * Prevents "ghost notifications" for used/deleted vouchers.
     */
    fun cancelAlerts(voucherId: String) {
        val requestCode24h = voucherId.hashCode() + REQUEST_CODE_24H_OFFSET
        val requestCode2h = voucherId.hashCode() + REQUEST_CODE_2H_OFFSET

        cancelPendingIntent(requestCode24h)
        cancelPendingIntent(requestCode2h)

        Log.d("NotifManager", "Cancelled alerts for $voucherId")
    }

    /**
     * Reschedule all active voucher alarms.
     * Called on BOOT_COMPLETED to survive device reboots.
     *
     * Note: This reads from SharedPreferences where we cache active voucher
     * alarm data. Room queries happen in coroutines, so the BootReceiver
     * delegates to this method after loading vouchers.
     */
    fun rescheduleAlarms(vouchers: List<Voucher>) {
        var scheduled = 0
        vouchers.forEach { voucher ->
            if (!voucher.isRedeemed && !voucher.isExpired) {
                scheduleExpiryAlerts(voucher)
                scheduled++
            }
        }
        Log.d("NotifManager", "Rescheduled $scheduled voucher alarms after boot")
    }

    // ═══════════════════════════════════════════
    //  Private Helpers
    // ═══════════════════════════════════════════

    private fun scheduleAlarm(
        voucher: Voucher,
        alertType: String,
        triggerAtMillis: Long,
        requestCodeOffset: Int
    ) {
        val intent = Intent(context, VoucherAlarmReceiver::class.java).apply {
            putExtra(EXTRA_VOUCHER_ID, voucher.id)
            putExtra(EXTRA_BRAND, voucher.brand)
            putExtra(EXTRA_VALUE, voucher.value)
            putExtra(EXTRA_CATEGORY, voucher.category.name)
            putExtra(EXTRA_ALERT_TYPE, alertType)
        }

        val requestCode = voucher.id.hashCode() + requestCodeOffset
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                    )
                } else {
                    // Fallback to inexact alarm if exact permission not granted
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback to inexact alarm
            Log.w("NotifManager", "Exact alarm permission denied, using inexact", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
            )
        }
    }

    private fun cancelPendingIntent(requestCode: Int) {
        val intent = Intent(context, VoucherAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
