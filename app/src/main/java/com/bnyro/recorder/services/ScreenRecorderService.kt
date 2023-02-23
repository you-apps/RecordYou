package com.bnyro.recorder.services

import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.view.Display
import androidx.activity.result.ActivityResult
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.bnyro.recorder.App
import com.bnyro.recorder.R
import com.bnyro.recorder.enums.AudioSource
import com.bnyro.recorder.ext.newRecorder
import com.bnyro.recorder.obj.VideoResolution
import com.bnyro.recorder.util.NotificationHelper
import com.bnyro.recorder.util.StorageHelper

class ScreenRecorderService : Service() {
    private val binder = LocalBinder()

    private var recorder: MediaRecorder? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var mediaProjection: MediaProjection? = null
    private var activityResult: ActivityResult? = null
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onDestroy()
        }
    }

    override fun onCreate() {
        startNotification()
    }

    override fun onDestroy() {
        NotificationManagerCompat.from(this).cancel(NotificationHelper.RECORDING_NOTIFICATION_ID)

        recorder?.apply {
            stop()
            release()
        }
        virtualDisplay?.release()
        recorder = null

        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    fun startRecording(result: ActivityResult) {
        this.activityResult = result
        initMediaProjection()
        startRecording()
    }

    private fun initMediaProjection() {
        val mProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mProjectionManager.getMediaProjection(
            Activity.RESULT_OK,
            activityResult?.data!!
        )
    }

    private fun startNotification() {
        val intent = Intent(getIntentActon())
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationHelper.RECORDING_NOTIFICATION_ID,
                notification.build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NotificationHelper.RECORDING_NOTIFICATION_ID, notification.build())
        }

        registerReceiver(receiver, IntentFilter(getIntentActon()))
    }

    private fun startRecording() {
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        val audioSource = AudioSource.fromInt(App.preferences.getInt(App.audioSourceKey, 0))

        recorder = newRecorder(this).apply {
            val resolution = VideoResolution.resolutions[2]

            setVideoSource(MediaRecorder.VideoSource.SURFACE)

            if (audioSource == AudioSource.MICROPHONE) {
                setAudioSource(MediaRecorder.AudioSource.MIC)
            }

            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP)

            if (audioSource == AudioSource.MICROPHONE) {
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            }

            setVideoSize(resolution.width, resolution.height)
            setVideoFrameRate(display.refreshRate.toInt())

            virtualDisplay = mediaProjection!!.createVirtualDisplay(
                getString(R.string.app_name),
                resolution.width,
                resolution.height,
                resources.displayMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                null,
                null,
                null
            )

            val file = StorageHelper.getOutputFile(this@ScreenRecorderService, "mp4")
            fileDescriptor = contentResolver.openFileDescriptor(file.uri, "w")
            setOutputFile(fileDescriptor?.fileDescriptor)

            runCatching {
                prepare()
            }

            start()

            virtualDisplay?.surface = surface
        }
    }

    private fun getIntentActon(): String {
        return "$packageName.STOP"
    }

    inner class LocalBinder : Binder() {
        // Return this instance of [BackgroundMode] so clients can call public methods
        fun getService(): ScreenRecorderService = this@ScreenRecorderService
    }
}
