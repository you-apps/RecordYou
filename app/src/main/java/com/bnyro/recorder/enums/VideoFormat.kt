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
            ),
            VideoFormat(
                "webm (VP8)",
                MediaRecorder.VideoEncoder.VP8,
                "webm",
                MediaRecorder.OutputFormat.WEBM
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.add(
                    VideoFormat(
                        "webm (VP9)",
                        MediaRecorder.VideoEncoder.VP9,
                        "webm",
                        MediaRecorder.OutputFormat.WEBM
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
