package com.bnyro.recorder.util

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.bnyro.recorder.R

object NotificationHelper {
    const val RECORDING_NOTIFICATION_CHANNEL = "recording_screen"
    const val RECORDING_NOTIFICATION_ID = 1

    fun buildNotificationChannels(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)

        val channelCompat = NotificationChannelCompat.Builder(
            RECORDING_NOTIFICATION_CHANNEL,
            NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName(context.getString(R.string.screen_recorder))
            .setLightsEnabled(true)
            .setShowBadge(true)
            .setVibrationEnabled(true)
            .build()

        notificationManager.createNotificationChannel(channelCompat)
    }
}
