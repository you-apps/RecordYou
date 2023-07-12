package com.bnyro.recorder.obj

import android.graphics.Bitmap
import androidx.documentfile.provider.DocumentFile

data class RecordingItemData(
    val recordingFile: DocumentFile,
    val isVideo: Boolean = false,
    val thumbnail: Bitmap? = null
)

val RecordingItemData.isAudio
    get() = !isVideo