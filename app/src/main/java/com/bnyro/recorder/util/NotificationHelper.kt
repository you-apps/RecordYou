package com.bnyro.recorder.util

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.bnyro.recorder.R

object NotificationHelper {
    const val RECORDING_NOTIFICATION_CHANNEL = "active_recording"
    const val RECORDING_FINISHED_N_CHANNEL = "recording_finished"
    const val RECORDING_NOTIFICATION_ID = 1
    const val RECORDING_FINISHED_N_ID = 2

    fun buildNotificationChannels(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)

        listOf(
            RECORDING_NOTIFICATION_CHANNEL to R.string.active_recording,
            RECORDING_FINISHED_N_CHANNEL to R.string.recording_finished
        ).forEach { (channelName, stringResource) ->
            val channelCompat = NotificationChannelCompat.Builder(
                channelName,
                NotificationManagerCompat.IMPORTANCE_HIGH
            )
                .setName(context.getString(stringResource))
                .setLightsEnabled(true)
                .setShowBadge(true)
                .setVibrationEnabled(true)
                .build()

            notificationManager.createNotificationChannel(channelCompat)
        }
    }
}
