package com.bnyro.recorder.services

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.bnyro.recorder.R
import com.bnyro.recorder.enums.RecorderState
import com.bnyro.recorder.util.NotificationHelper

abstract class RecorderService : Service() {
    private val binder = LocalBinder()
    var recorder: MediaRecorder? = null
    var fileDescriptor: ParcelFileDescriptor? = null
    var onRecorderStateChanged: (RecorderState) -> Unit = {}
    open val fgServiceType: Int? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onDestroy()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    inner class LocalBinder : Binder() {
        // Return this instance of [BackgroundMode] so clients can call public methods
        fun getService(): RecorderService = this@RecorderService
    }

    override fun onCreate() {
        startNotification()
    }

    private fun startNotification() {
        val intent = Intent(STOP_INTENT_ACTION)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            2,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopAction = NotificationCompat.Action.Builder(
            null,
            getString(R.string.stop),
            pendingIntent
        )

        val notification = NotificationCompat.Builder(
            this,
            NotificationHelper.RECORDING_NOTIFICATION_CHANNEL
        )
            .setContentTitle(getString(R.string.recording_screen))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(stopAction.build())
            .setUsesChronometer(true)

        if (fgServiceType != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationHelper.RECORDING_NOTIFICATION_ID,
                notification.build(),
                fgServiceType!!
            )
        } else {
            startForeground(NotificationHelper.RECORDING_NOTIFICATION_ID, notification.build())
        }

        runCatching {
            unregisterReceiver(receiver)
        }
        registerReceiver(receiver, IntentFilter(STOP_INTENT_ACTION))
    }

    open fun start() {
        runCatching {
            onRecorderStateChanged(RecorderState.ACTIVE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    open fun pause() {
        recorder?.pause()
        runCatching {
            onRecorderStateChanged(RecorderState.PAUSED)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    open fun resume() {
        recorder?.resume()
        runCatching {
            onRecorderStateChanged(RecorderState.ACTIVE)
        }
    }

    override fun onDestroy() {
        runCatching {
            onRecorderStateChanged(RecorderState.IDLE)
        }

        NotificationManagerCompat.from(this)
            .cancel(NotificationHelper.RECORDING_NOTIFICATION_ID)

        recorder?.runCatching {
            stop()
            release()
        }
        recorder = null
        fileDescriptor?.close()

        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    companion object {
        const val STOP_INTENT_ACTION = "com.bnyro.recorder.STOP"
    }
}
