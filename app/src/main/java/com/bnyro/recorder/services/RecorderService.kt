package com.bnyro.recorder.services

import android.Manifest
import android.annotation.SuppressLint
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
import com.bnyro.recorder.util.PermissionHelper

abstract class RecorderService : Service() {
    abstract val notificationTitle: String

    private val binder = LocalBinder()
    var recorder: MediaRecorder? = null
    var fileDescriptor: ParcelFileDescriptor? = null
    var onRecorderStateChanged: (RecorderState) -> Unit = {}
    open val fgServiceType: Int? = null
    private var recorderState: RecorderState = RecorderState.IDLE

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("NewApi")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.getStringExtra("action")) {
                STOP_ACTION -> onDestroy()
                PAUSE_RESUME_ACTION -> {
                    if (recorderState == RecorderState.ACTIVE) pause() else resume()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    inner class LocalBinder : Binder() {
        // Return this instance of [BackgroundMode] so clients can call public methods
        fun getService(): RecorderService = this@RecorderService
    }

    override fun onCreate() {
        val notification = buildNotification()
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
        registerReceiver(receiver, IntentFilter(RECORDER_INTENT_ACTION))
    }

    private fun buildNotification(): NotificationCompat.Builder {
        val stopIntent = Intent(RECORDER_INTENT_ACTION).apply {
            putExtra("action", STOP_ACTION)
        }
        val stopAction = NotificationCompat.Action.Builder(
            null,
            getString(R.string.stop),
            getPendingIntent(stopIntent, 2)
        )

        val resumeOrPauseIntent = Intent(RECORDER_INTENT_ACTION).apply {
            putExtra("action", PAUSE_RESUME_ACTION)
        }
        val resumeOrPauseAction = NotificationCompat.Action.Builder(
            null,
            if (recorderState == RecorderState.ACTIVE) {
                getString(R.string.pause)
            } else {
                getString(
                    R.string.resume
                )
            },
            getPendingIntent(resumeOrPauseIntent, 3)
        )

        return NotificationCompat.Builder(
            this,
            NotificationHelper.RECORDING_NOTIFICATION_CHANNEL
        )
            .setContentTitle(notificationTitle)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(recorderState == RecorderState.ACTIVE)
            .addAction(stopAction.build())
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    addAction(
                        resumeOrPauseAction.build()
                    )
                }
            }
            .setUsesChronometer(true)
    }

    @SuppressLint("MissingPermission")
    fun updateNotification() {
        if (!PermissionHelper.hasPermission(this, Manifest.permission.POST_NOTIFICATIONS)) return
        val notification = buildNotification().build()
        NotificationManagerCompat.from(this).notify(
            NotificationHelper.RECORDING_NOTIFICATION_ID,
            notification
        )
    }

    private fun getPendingIntent(intent: Intent, requestCode: Int): PendingIntent = PendingIntent.getBroadcast(
        this,
        requestCode,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    open fun start() {
        runCatching {
            recorderState = RecorderState.ACTIVE
            onRecorderStateChanged(recorderState)
        }
        updateNotification()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    open fun pause() {
        recorder?.pause()
        runCatching {
            recorderState = RecorderState.PAUSED
            onRecorderStateChanged(recorderState)
        }
        updateNotification()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    open fun resume() {
        recorder?.resume()
        runCatching {
            recorderState = RecorderState.ACTIVE
            onRecorderStateChanged(recorderState)
        }
        updateNotification()
    }

    override fun onDestroy() {
        runCatching {
            recorderState = RecorderState.IDLE
            onRecorderStateChanged(recorderState)
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
        const val RECORDER_INTENT_ACTION = "com.bnyro.recorder.RECORDER_ACTION"
        const val STOP_ACTION = "STOP"
        const val PAUSE_RESUME_ACTION = "PR"
    }
}
