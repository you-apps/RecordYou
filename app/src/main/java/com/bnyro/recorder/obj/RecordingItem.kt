package com.bnyro.recorder.obj

import android.graphics.Bitmap
import androidx.documentfile.provider.DocumentFile
import com.bnyro.recorder.enums.RecorderType

data class RecordingItemData(
    val recordingFile: DocumentFile,
    val recorderType: RecorderType,
    val thumbnail: Bitmap? = null
) {
    val isAudio get() = recorderType == RecorderType.AUDIO
    val isVideo get() = recorderType == RecorderType.VIDEO
}
