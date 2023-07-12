package com.bnyro.recorder.ui.models

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.recorder.enums.SortOrder
import com.bnyro.recorder.obj.RecordingItemData
import com.bnyro.recorder.util.PlayerHelper
import com.bnyro.recorder.util.StorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.absoluteValue

class PlayerModel : ViewModel() {
    var isPlaying by mutableStateOf(false)
    var player by mutableStateOf<MediaPlayer?>(null)
    var currentPlayingFile by mutableStateOf<DocumentFile?>(null)

    var files = mutableStateListOf<DocumentFile>()

    val recordingItems = mutableStateListOf<RecordingItemData>()

    fun loadFiles(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                files = getAvailableFiles(context).toMutableStateList()
            }
            loadRecordingItems(context)
        }
    }

    fun sortRecordingItems(context: Context, sortOrder: SortOrder) {
        files = when (sortOrder) {
            SortOrder.ALPHABETIC -> files.sortedBy { it.name }
            SortOrder.ALPHABETIC_REV -> files.sortedByDescending { it.name }
            SortOrder.SIZE_REV -> files.sortedBy {
                context.contentResolver.query(it.uri, null, null, null, null)?.use { cursor ->
                    cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE).absoluteValue)
                }
            }

            SortOrder.SIZE -> files.sortedByDescending {
                context.contentResolver.query(it.uri, null, null, null, null)?.use { cursor ->
                    cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE).absoluteValue)
                }
            }
        }.toMutableStateList()
        loadRecordingItems(context)
    }

    private fun loadRecordingItems(context: Context) {
        viewModelScope.launch {
            recordingItems.clear()
            files.forEach { file ->
                if (file.name.orEmpty()
                        .endsWith("mp4" /* Currently there are only mp4 video files*/)
                ) {
                    val thumbnail =
                        MediaMetadataRetriever().apply {
                            setDataSource(
                                context,
                                file.uri
                            )
                        }.frameAtTime
                    recordingItems.add(RecordingItemData(file, true, thumbnail))
                } else {
                    recordingItems.add(RecordingItemData(file))
                }
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
