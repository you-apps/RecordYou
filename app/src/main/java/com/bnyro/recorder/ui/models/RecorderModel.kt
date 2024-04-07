package com.bnyro.recorder.ui.models

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.bnyro.recorder.R
import com.bnyro.recorder.canvas_overlay.CanvasOverlay
import com.bnyro.recorder.enums.AudioDeviceSource
import com.bnyro.recorder.enums.AudioSource
import com.bnyro.recorder.enums.RecorderState
import com.bnyro.recorder.services.AudioRecorderService
import com.bnyro.recorder.services.LosslessRecorderService
import com.bnyro.recorder.services.RecorderService
import com.bnyro.recorder.services.ScreenRecorderService
import com.bnyro.recorder.util.PermissionHelper
import com.bnyro.recorder.util.Preferences

class RecorderModel : ViewModel() {
    private val supportsOverlay = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    var recorderState by mutableStateOf(RecorderState.IDLE)
    var recordedTime by mutableStateOf<Long?>(null)
    val recordedAmplitudes = mutableStateListOf<Int>()
    private var activityResult: ActivityResult? = null
    private var canvasOverlay: CanvasOverlay? = null

    private val handler = Handler(Looper.getMainLooper())

    @SuppressLint("StaticFieldLeak")
    private var recorderService: RecorderService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            recorderService = (service as RecorderService.LocalBinder).getService()
            recorderService?.onRecorderStateChanged = {
                recorderState = it
            }
            (recorderService as? ScreenRecorderService)?.prepare(activityResult!!)
            recorderService?.start()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            recorderService = null
        }
    }

    fun startVideoRecorder(context: Context, result: ActivityResult) {
        activityResult = result
        val serviceIntent = Intent(context, ScreenRecorderService::class.java)
        startRecorderService(context, serviceIntent)
        val showOverlayAnnotation =
            Preferences.prefs.getBoolean(Preferences.showOverlayAnnotationToolKey, false)
        if (supportsOverlay && showOverlayAnnotation) {
            canvasOverlay = CanvasOverlay(context)
        }
    }

    @SuppressLint("NewApi")
    fun startAudioRecorder(context: Context) {
        val audioPermission = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            audioPermission.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val internalAudio = Preferences.prefs.getInt(
            Preferences.audioDeviceSourceKey,
            0
        ) == AudioDeviceSource.REMOTE_SUBMIX.value
        if (internalAudio) {
            audioPermission.add(Manifest.permission.CAPTURE_AUDIO_OUTPUT)
        }

        if (!PermissionHelper.checkPermissions(context, audioPermission.toTypedArray())) {
            Toast.makeText(
                context,
                context.getString(R.string.no_enough_permissions), Toast.LENGTH_SHORT
            )
                .show()
            return
        }

        val serviceIntent =
            if (Preferences.prefs.getBoolean(Preferences.losslessRecorderKey, false)) {
                Intent(context, LosslessRecorderService::class.java)
            } else {
                Intent(context, AudioRecorderService::class.java)
            }

        startRecorderService(context, serviceIntent)
    }

    private fun startRecorderService(context: Context, intent: Intent) {
        runCatching {
            context.unbindService(connection)
        }

        listOfNotNull(
            AudioRecorderService::class.java,
            ScreenRecorderService::class.java,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) LosslessRecorderService::class.java else null
        ).forEach {
            runCatching {
                context.stopService(Intent(context, it))
            }
        }
        ContextCompat.startForegroundService(context, intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        startElapsedTimeCounter()
        handler.postDelayed(this::updateAmplitude, 100)
    }

    fun stopRecording() {
        recorderService?.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) canvasOverlay?.remove()
        recordedTime = null
        recordedAmplitudes.clear()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun pauseRecording() {
        recorderService?.pause()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun resumeRecording() {
        recorderService?.resume()
        handler.postDelayed(this::updateTime, 1000)
        if (recorderService is AudioRecorderService) {
            handler.postDelayed(this::updateAmplitude, 100)
        }
    }

    private fun updateTime() {
        if (recorderState != RecorderState.ACTIVE) return

        recordedTime = recordedTime?.plus(1)
        handler.postDelayed(this::updateTime, 100)
    }

    private fun updateAmplitude() {
        if (recorderState != RecorderState.ACTIVE) return

        recorderService?.getCurrentAmplitude()?.let {
            if (recordedAmplitudes.size >= 90) recordedAmplitudes.removeAt(0)
            recordedAmplitudes.add(it)
        }

        handler.postDelayed(this::updateAmplitude, 100)
    }

    private fun startElapsedTimeCounter() {
        recordedTime = 0L
        handler.postDelayed(this::updateTime, 100)
    }

    @SuppressLint("NewApi")
    fun hasScreenRecordingPermissions(context: Context): Boolean {
        val overlayEnabled = Preferences.prefs.getBoolean(
            Preferences.showOverlayAnnotationToolKey,
            false
        )
        if (supportsOverlay && overlayEnabled && !Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.packageName)
            )
            context.startActivity(intent)
            return false
        }

        val requiredPermissions = arrayListOf<String>()

        val recordAudio =
            Preferences.prefs.getInt(Preferences.audioSourceKey, 0) == AudioSource.MICROPHONE.value

        if (recordAudio) requiredPermissions.add(Manifest.permission.RECORD_AUDIO)

        val internalAudio = Preferences.prefs.getInt(
            Preferences.audioDeviceSourceKey,
            0
        ) == AudioDeviceSource.REMOTE_SUBMIX.value
        if (internalAudio) requiredPermissions.add(Manifest.permission.CAPTURE_AUDIO_OUTPUT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (requiredPermissions.isEmpty()) return true

        val granted = PermissionHelper.checkPermissions(context, requiredPermissions.toTypedArray())
        if (granted) {
            return true
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.no_enough_permissions), Toast.LENGTH_SHORT
            )
                .show()
            return false
        }
    }
}
