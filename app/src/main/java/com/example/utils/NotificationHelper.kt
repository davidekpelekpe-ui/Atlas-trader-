package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    private const val CHANNEL_ID = "atlas_profit_channel"
    private const val CHANNEL_NAME = "Atlas Profit Milestones"
    private const val CHANNEL_DESC = "Notifications for Bybit profit achievements"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                
                // Configure custom sound for the notification channel
                val soundUri = Uri.parse("android.resource://${context.packageName}/${com.example.R.raw.atlas_alert}")
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
                
                // Enable vibration and lights
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendProfitNotification(context: Context, symbol: String, profit: Double) {
        val largeIcon = try {
            android.graphics.BitmapFactory.decodeResource(context.resources, com.example.R.mipmap.ic_launcher)
        } catch (e: Exception) {
            null
        }

        val soundUri = Uri.parse("android.resource://${context.packageName}/${com.example.R.raw.atlas_alert}")

        // Intent to launch MainActivity when clicking the notification
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.example.R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle("📈 Profit Milestone Achieved!")
            .setContentText("$symbol Perpetual reached +$${String.format("%.2f", profit)} USDT profit.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        } catch (e: SecurityException) {
            // Safe fallback if permission is not granted yet
        }
    }

    fun sendAlertNotification(context: Context, title: String, text: String) {
        val largeIcon = try {
            android.graphics.BitmapFactory.decodeResource(context.resources, com.example.R.mipmap.ic_launcher)
        } catch (e: Exception) {
            null
        }

        val soundUri = Uri.parse("android.resource://${context.packageName}/${com.example.R.raw.atlas_alert}")

        // Intent to launch MainActivity when clicking the notification
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.example.R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        } catch (e: SecurityException) {
            // Safe fallback if permission is not granted yet
        }
    }
}

