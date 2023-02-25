package com.bnyro.recorder.util

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.bnyro.recorder.R

object NotificationHelper {
    const val RECORDING_NOTIFICATION_CHANNEL = "active_recording"
    const val RECORDING_NOTIFICATION_ID = 1

    fun buildNotificationChannels(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)

        val channelCompat = NotificationChannelCompat.Builder(
            RECORDING_NOTIFICATION_CHANNEL,
            NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName(context.getString(R.string.active_recording))
            .setLightsEnabled(true)
            .setShowBadge(true)
            .setVibrationEnabled(true)
            .build()

        notificationManager.createNotificationChannel(channelCompat)
    }
}
