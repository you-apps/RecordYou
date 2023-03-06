package com.bnyro.recorder.enums

enum class AudioChannels(val value: Int) {
    MONO(1),
    STEREO(2);

    companion object {
        fun fromInt(value: Int) = AudioChannels.values().first { it.value == value }
    }
}
