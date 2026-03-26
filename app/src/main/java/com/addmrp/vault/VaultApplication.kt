package com.addmrp.vault

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.addmrp.vault.notification.VoucherAlarmReceiver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VaultApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    /**
     * Creates two notification channels for voucher expiry alerts:
     *
     * 1. vault_expiry (DEFAULT): 24-hour-before reminder
     * 2. vault_expiry_urgent (HIGH): 2-hour-before final window alert
     *
     * HIGH importance ensures the 2h alert bypasses DND and shows as heads-up.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // Channel 1: 24h reminder (DEFAULT importance)
            val defaultChannel = NotificationChannel(
                VoucherAlarmReceiver.CHANNEL_DEFAULT,
                "Voucher Expiry Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts 24 hours before your vouchers expire"
                enableVibration(true)
            }

            // Channel 2: 2h urgent alert (HIGH importance)
            val urgentChannel = NotificationChannel(
                VoucherAlarmReceiver.CHANNEL_URGENT,
                "Urgent Expiry Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Final window alerts 2 hours before expiry — don't miss this!"
                enableVibration(true)
                enableLights(true)
            }

            manager.createNotificationChannel(defaultChannel)
            manager.createNotificationChannel(urgentChannel)
        }
    }
}
