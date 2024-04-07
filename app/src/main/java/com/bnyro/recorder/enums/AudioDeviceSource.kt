package com.bnyro.recorder.enums

import android.media.MediaRecorder

enum class AudioDeviceSource(val value: Int) {
    DEFAULT(MediaRecorder.AudioSource.DEFAULT),
    MIC(MediaRecorder.AudioSource.MIC),
    CAMCORDER(MediaRecorder.AudioSource.CAMCORDER),
    UNPROCESSED(MediaRecorder.AudioSource.UNPROCESSED),
    REMOTE_SUBMIX(MediaRecorder.AudioSource.REMOTE_SUBMIX);

    companion object {
        fun fromInt(value: Int) = AudioDeviceSource.values().first { it.value == value }
    }
}
