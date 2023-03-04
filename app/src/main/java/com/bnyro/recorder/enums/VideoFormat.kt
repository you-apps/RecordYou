package com.bnyro.recorder.enums

import android.media.MediaRecorder
import android.os.Build
import com.bnyro.recorder.util.Preferences

data class VideoFormat(
    val name: String,
    val codec: Int,
    val extension: String,
    val format: Int
) {
    companion object {
        val codecs = mutableListOf(
            VideoFormat(
                "H.264",
                MediaRecorder.VideoEncoder.H264,
                "mp4",
                MediaRecorder.OutputFormat.MPEG_4
            )
        ).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                it.add(
                    VideoFormat(
                        "H.265",
                        MediaRecorder.VideoEncoder.HEVC,
                        "mp4",
                        MediaRecorder.OutputFormat.MPEG_4
                    )
                )
            }
        }

        fun getCurrent() = codecs.first {
            it.codec == Preferences.prefs.getInt(
                Preferences.videoCodecKey,
                MediaRecorder.VideoEncoder.H264
            )
        }
    }
}
