package com.bnyro.recorder.ui.models

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.ParcelFileDescriptor
import androidx.activity.result.ActivityResult
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.bnyro.recorder.enums.AudioSource
import com.bnyro.recorder.ext.newRecorder
import com.bnyro.recorder.obj.AudioFormat
import com.bnyro.recorder.services.ScreenRecorderService
import com.bnyro.recorder.util.PermissionHelper
import com.bnyro.recorder.util.Preferences
import com.bnyro.recorder.util.StorageHelper

class RecorderModel : ViewModel() {
    private val audioPermission = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var recorder: MediaRecorder? = null

    var isRecording: Boolean by mutableStateOf(false)
    var recordedTime by mutableStateOf<Long?>(null)
    var isPaused by mutableStateOf(false)
    val recordedAmplitudes = mutableStateListOf<Int>()
    private var activityResult: ActivityResult? = null

    private var fileDescriptor: ParcelFileDescriptor? = null
    var audioFormat = AudioFormat.getCurrent()
    private val handler = Handler(Looper.getMainLooper())

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ScreenRecorderService.LocalBinder
            binder.getService().startRecording(activityResult!!)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {}
    }

    fun startVideoRecorder(context: Context, result: ActivityResult) {
        activityResult = result
        val serviceIntent = Intent(context, ScreenRecorderService::class.java)
        context.stopService(serviceIntent)
        ContextCompat.startForegroundService(context, serviceIntent)
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }

    fun startAudioRecorder(context: Context) {
        if (!PermissionHelper.checkPermissions(context, audioPermission)) return

        recorder = newRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(audioFormat.format)
            setAudioEncoder(audioFormat.codec)

            val file = StorageHelper.getOutputFile(context, audioFormat.extension)
            fileDescriptor = context.contentResolver.openFileDescriptor(file.uri, "w")
            setOutputFile(fileDescriptor?.fileDescriptor)

            runCatching {
                prepare()
            }

            start()
        }
        isRecording = true
        recordedTime = 0L
        handler.postDelayed(this::updateTime, 1000)
        handler.postDelayed(this::updateAmplitude, 100)
    }

    fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        isPaused = false
        recorder = null
        isRecording = false
        recordedTime = null
        recordedAmplitudes.clear()
        fileDescriptor?.close()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun pauseRecording() {
        isPaused = true
        recorder?.pause()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun resumeRecording() {
        isPaused = false
        recorder?.resume()
        handler.postDelayed(this::updateTime, 1000)
        handler.postDelayed(this::updateAmplitude, 100)
    }

    private fun updateTime() {
        if (!isRecording || isPaused) return
        recordedTime = recordedTime?.plus(1)
        handler.postDelayed(this::updateTime, 1000)
    }

    private fun updateAmplitude() {
        if (!isRecording || isPaused) return

        recorder?.maxAmplitude?.let {
            if (recordedAmplitudes.size >= 90) recordedAmplitudes.removeAt(0)
            recordedAmplitudes.add(it)
        }

        handler.postDelayed(this::updateAmplitude, 100)
    }

    fun hasScreenRecordingPermissions(context: Context): Boolean {
        val requiredPermissions = arrayListOf<String>()

        val recordAudio = Preferences.prefs.getInt(Preferences.audioSourceKey, 0) == AudioSource.MICROPHONE.value

        if (recordAudio) requiredPermissions.add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (requiredPermissions.isEmpty()) return true

        return PermissionHelper.checkPermissions(context, requiredPermissions.toTypedArray())
    }
}
