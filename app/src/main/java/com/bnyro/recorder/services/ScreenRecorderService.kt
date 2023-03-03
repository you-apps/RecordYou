package com.bnyro.recorder.services

import android.app.Activity
import android.content.Context
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.Surface
import androidx.activity.result.ActivityResult
import com.bnyro.recorder.R
import com.bnyro.recorder.enums.AudioSource
import com.bnyro.recorder.obj.VideoResolution
import com.bnyro.recorder.util.PlayerHelper
import com.bnyro.recorder.util.Preferences
import com.bnyro.recorder.util.StorageHelper

class ScreenRecorderService : RecorderService() {
    override val notificationTitle: String
        get() = getString(R.string.recording_screen)

    private var virtualDisplay: VirtualDisplay? = null
    private var mediaProjection: MediaProjection? = null
    private var activityResult: ActivityResult? = null
    override val fgServiceType: Int?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        } else {
            null
        }

    fun prepare(data: ActivityResult) {
        this.activityResult = data
        initMediaProjection()
    }

    private fun initMediaProjection() {
        val mProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        try {
            mediaProjection = mProjectionManager.getMediaProjection(
                Activity.RESULT_OK,
                activityResult?.data!!
            )
        } catch (e: Exception) {
            Log.e("Media Projection Error", e.toString())
            onDestroy()
        }
    }

    override fun start() {
        val audioSource = AudioSource.fromInt(
            Preferences.prefs.getInt(Preferences.audioSourceKey, 0)
        )
        val resolution = getScreenResolution()

        recorder = PlayerHelper.newRecorder(this).apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)

            if (audioSource == AudioSource.MICROPHONE) {
                setAudioSource(MediaRecorder.AudioSource.MIC)
            }

            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoEncodingBitRate(
                (BPP * resolution.frameRate * resolution.width * resolution.height).toInt()
            )

            if (audioSource == AudioSource.MICROPHONE) {
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            }

            setVideoSize(resolution.width, resolution.height)
            setVideoFrameRate(resolution.frameRate)

            virtualDisplay = mediaProjection!!.createVirtualDisplay(
                getString(R.string.app_name),
                resolution.width,
                resolution.height,
                resolution.density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                null,
                null,
                null
            )

            outputFile = StorageHelper.getOutputFile(this@ScreenRecorderService, "mp4")
            fileDescriptor = contentResolver.openFileDescriptor(outputFile!!.uri, "w")
            setOutputFile(fileDescriptor?.fileDescriptor)

            runCatching {
                prepare()
            }

            start()

            virtualDisplay?.surface = surface
        }

        super.start()
    }
    private fun getScreenResolution(): VideoResolution {
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)

        // TODO Use the window API instead on newer devices
        val metrics = DisplayMetrics()
        display.getRealMetrics(metrics)

        val orientationOnStart = display.rotation

        val screenHeightNormal: Int
        val screenWidthNormal: Int
        if (orientationOnStart == Surface.ROTATION_90 || orientationOnStart == Surface.ROTATION_270) {
            screenWidthNormal = metrics.heightPixels
            screenHeightNormal = metrics.widthPixels
        } else {
            screenWidthNormal = metrics.widthPixels
            screenHeightNormal = metrics.heightPixels
        }

        return VideoResolution(
            screenWidthNormal,
            screenHeightNormal,
            metrics.densityDpi,
            display.refreshRate.toInt()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        virtualDisplay?.release()
    }

    companion object {
        private const val BPP = 0.25f
    }
}
