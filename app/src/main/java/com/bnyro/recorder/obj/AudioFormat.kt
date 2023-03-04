package com.bnyro.recorder.obj

import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import com.bnyro.recorder.util.Preferences

data class AudioFormat(
    val format: Int,
    val codec: Int,
    val name: String,
    val extension: String
) {
    companion object {
        private val m4a = AudioFormat(
            MediaRecorder.OutputFormat.MPEG_4,
            MediaRecorder.AudioEncoder.AAC,
            "M4A",
            "m4a"
        )
        private val aac = AudioFormat(
            MediaRecorder.OutputFormat.AAC_ADTS,
            MediaRecorder.AudioEncoder.AAC,
            "AAC",
            "aac"
        )
        private val tgp = AudioFormat(
            MediaRecorder.OutputFormat.THREE_GPP,
            MediaRecorder.AudioEncoder.AAC,
            "3GP",
            "3gp"
        )

        @RequiresApi(Build.VERSION_CODES.Q)
        private val opus = AudioFormat(
            MediaRecorder.OutputFormat.OGG,
            MediaRecorder.AudioEncoder.OPUS,
            "OPUS",
            "ogg"
        )

        val formats = mutableListOf(m4a, aac, tgp).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) it.add(opus)
        }

        fun getCurrent() = formats.firstOrNull {
            it.name == Preferences.prefs.getString(Preferences.audioFormatKey, m4a.name)
        } ?: m4a
    }
}
