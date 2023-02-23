package com.bnyro.recorder.enums

enum class AudioSource(val value: Int) {
    NONE(0),
    MICROPHONE(1);

    companion object {
        fun fromInt(value: Int) = AudioSource.values().first { it.value == value }
    }
}
