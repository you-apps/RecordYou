package com.bnyro.recorder.services

import android.media.MediaRecorder
import com.bnyro.recorder.obj.AudioFormat
import com.bnyro.recorder.util.PlayerHelper
import com.bnyro.recorder.util.StorageHelper

class AudioRecorderService : RecorderService() {
    override fun start() {
        val audioFormat = AudioFormat.getCurrent()

        recorder = PlayerHelper.newRecorder(this).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(audioFormat.format)
            setAudioEncoder(audioFormat.codec)

            val file = StorageHelper.getOutputFile(this@AudioRecorderService, audioFormat.extension)
            fileDescriptor = contentResolver.openFileDescriptor(file.uri, "w")
            setOutputFile(fileDescriptor?.fileDescriptor)

            runCatching {
                prepare()
            }

            start()
        }

        super.start()
    }
}
