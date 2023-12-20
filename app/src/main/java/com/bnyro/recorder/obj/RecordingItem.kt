package com.bnyro.recorder.obj

import android.graphics.Bitmap
import androidx.documentfile.provider.DocumentFile

sealed class RecordingItem(open val recordingFile: DocumentFile) {
    data class Video(
        override val recordingFile: DocumentFile,
        val thumbnail: Bitmap? = null
    ) : RecordingItem(recordingFile)

    data class Audio(
        override val recordingFile: DocumentFile,
        val duration: Long? = null,
        val samples: List<Int>? = null
    ) : RecordingItem(recordingFile)
}
