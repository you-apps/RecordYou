package com.bnyro.recorder.ext

import android.content.Context
import android.media.MediaRecorder
import android.os.Build

fun newRecorder(context: Context): MediaRecorder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
    }
}
