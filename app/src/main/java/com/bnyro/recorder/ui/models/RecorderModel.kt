package com.bnyro.recorder.ui.models

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.postDelayed
import androidx.lifecycle.ViewModel
import com.bnyro.recorder.obj.AudioFormat
import com.bnyro.recorder.util.PermissionHelper
import com.bnyro.recorder.util.StorageHelper

class RecorderModel : ViewModel() {
    private val permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var recorder: MediaRecorder? = null
    var isRecording: Boolean by mutableStateOf(false)
    var recordedTime by mutableStateOf<Long?>(null)
    var isPaused by mutableStateOf(false)
    val recordedAmplitudes = mutableStateListOf<Int>()

    private var fileDescriptor: ParcelFileDescriptor? = null
    var audioFormat = AudioFormat.m4a
    private val handler = Handler(Looper.getMainLooper())

    fun startRecording(context: Context) {
        if (!PermissionHelper.checkPermissions(context, permissions)) return

        recorder = newRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(audioFormat.format)
            setAudioEncoder(audioFormat.codec)

            val file = StorageHelper.getOutputFile(context, audioFormat.extension)
            fileDescriptor = context.contentResolver.openFileDescriptor(Uri.fromFile(file), "w")
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

    private fun newRecorder(context: Context): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
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
}
