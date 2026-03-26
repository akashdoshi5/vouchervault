package com.addmrp.vault.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.addmrp.vault.R
import java.text.NumberFormat
import java.util.Locale

/**
 * BroadcastReceiver that fires expiry notifications.
 *
 * Rule 17: Financial Advisor persona — Loss of Value framing.
 *
 * 24h: "Don't let ₹[Value] slip away! Your [Brand] voucher expires tomorrow.
 *       Think if you have any necessary [Category] needs today."
 *
 * 2h:  "Final Window: Your ₹[Value] [Brand] reward expires in 2 hours.
 *       Use it now or it's gone forever."
 */
class VoucherAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_DEFAULT = "vault_expiry"
        const val CHANNEL_URGENT = "vault_expiry_urgent"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val voucherId = intent.getStringExtra(VoucherNotificationManager.EXTRA_VOUCHER_ID) ?: return
        val brand = intent.getStringExtra(VoucherNotificationManager.EXTRA_BRAND) ?: "Unknown"
        val value = intent.getDoubleExtra(VoucherNotificationManager.EXTRA_VALUE, 0.0)
        val category = intent.getStringExtra(VoucherNotificationManager.EXTRA_CATEGORY) ?: ""
        val alertType = intent.getStringExtra(VoucherNotificationManager.EXTRA_ALERT_TYPE) ?: return

        val formattedValue = formatRupees(value)
        val categoryName = category.replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() }

        val (title, body, channelId, priority) = when (alertType) {
            VoucherNotificationManager.ALERT_24H -> NotifContent(
                title = "Don't let $formattedValue slip away! 💸",
                body = "Your $brand voucher expires tomorrow. Think if you have any necessary $categoryName needs today.",
                channelId = CHANNEL_DEFAULT,
                priority = NotificationCompat.PRIORITY_DEFAULT
            )
            VoucherNotificationManager.ALERT_2H -> NotifContent(
                title = "⚠️ Final Window: $formattedValue at stake!",
                body = "Your $formattedValue $brand reward expires in 2 hours. Use it now or it's gone forever.",
                channelId = CHANNEL_URGENT,
                priority = NotificationCompat.PRIORITY_HIGH
            )
            else -> return
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Replace with vault icon
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(priority)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifId = voucherId.hashCode() + (if (alertType == VoucherNotificationManager.ALERT_24H) 1 else 2)

        try {
            notifManager.notify(notifId, notification)
            Log.d("AlarmReceiver", "$alertType notification fired for $brand ($voucherId)")
        } catch (e: SecurityException) {
            Log.e("AlarmReceiver", "POST_NOTIFICATIONS permission denied", e)
        }
    }

    private fun formatRupees(value: Double): String {
        if (value <= 0) return "your voucher"
        val fmt = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        return fmt.format(value).replace(".00", "")
    }

    private data class NotifContent(
        val title: String,
        val body: String,
        val channelId: String,
        val priority: Int
    )
}
