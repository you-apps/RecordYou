package com.bnyro.recorder.ui.models

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.recorder.util.PlayerHelper
import com.bnyro.recorder.util.StorageHelper
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerModel : ViewModel() {
    var isPlaying by mutableStateOf(false)
    var player by mutableStateOf<MediaPlayer?>(null)
    var currentPlayingFile by mutableStateOf<DocumentFile?>(null)

    val files = mutableStateListOf<DocumentFile>()

    fun loadFiles(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val f = getAvailableFiles(context)
                files.clear()
                files.addAll(f)
            }
        }
    }

    fun startPlaying(context: Context, file: DocumentFile) {
        stopPlaying()

        currentPlayingFile = file
        player = getMediaPlayer().apply {
            try {
                context.contentResolver.openFileDescriptor(file.uri, "r")?.use {
                    setDataSource(it.fileDescriptor)
                }
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("reading file", e.toString())
            }
        }
        player?.setOnCompletionListener {
            stopPlaying()
        }
        isPlaying = true
    }

    fun stopPlaying() {
        currentPlayingFile = null
        player?.release()
        player = null
        isPlaying = false
    }

    fun pausePlaying() {
        player?.pause()
        isPlaying = false
    }

    fun resumePlaying() {
        player?.start()
        isPlaying = true
    }

    private fun getAvailableFiles(context: Context): List<DocumentFile> {
        return StorageHelper.getOutputDir(context).listFiles().filter { it.isFile }.toList()
    }

    private fun getMediaPlayer(): MediaPlayer {
        return MediaPlayer().apply {
            setAudioAttributes(PlayerHelper.getAudioAttributes())
        }
    }
}
