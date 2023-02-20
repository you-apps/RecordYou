package com.bnyro.recorder.ui.models

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.recorder.util.StorageHelper
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerModel : ViewModel() {
    private var player: MediaPlayer? = null
    val files = mutableStateListOf<File>()
    private var onFinish: () -> Unit = {}

    fun loadFiles(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val f = getAvailableFiles(context)
                files.clear()
                files.addAll(f)
            }
        }
    }

    fun startPlaying(context: Context, file: File, onEnded: () -> Unit) {
        onFinish.invoke()
        onFinish = onEnded

        stopPlaying()

        player = getMediaPlayer().apply {
            try {
                context.contentResolver.openFileDescriptor(Uri.fromFile(file), "r")?.use {
                    setDataSource(it.fileDescriptor)
                }
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("reading file", e.toString())
            }
        }
        player?.setOnCompletionListener {
            onFinish.invoke()
        }
    }

    fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun getAvailableFiles(context: Context): List<File> {
        return StorageHelper.getOutputDir(context).listFiles().orEmpty().toList()
    }

    private fun getMediaPlayer(): MediaPlayer {
        return MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }
    }
}
