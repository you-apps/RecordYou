package com.bnyro.recorder.services

import android.media.MediaRecorder
import com.bnyro.recorder.R
import com.bnyro.recorder.obj.AudioFormat
import com.bnyro.recorder.util.PlayerHelper
import com.bnyro.recorder.util.Preferences
import com.bnyro.recorder.util.StorageHelper

class AudioRecorderService : RecorderService() {
    override val notificationTitle: String
        get() = getString(R.string.recording_audio)

    override fun start() {
        val audioFormat = AudioFormat.getCurrent()

        recorder = PlayerHelper.newRecorder(this).apply {
            setAudioSource(MediaRecorder.AudioSource.DEFAULT)

            Preferences.prefs.getInt(Preferences.audioSampleRateKey, -1).takeIf { it > 0 }?.let {
                setAudioSamplingRate(it)
                setAudioEncodingBitRate(it * 32 * 2)
            }

            Preferences.prefs.getInt(Preferences.audioBitrateKey, -1).takeIf { it > 0 }?.let {
                setAudioEncodingBitRate(it)
            }

            setOutputFormat(audioFormat.format)
            setAudioEncoder(audioFormat.codec)

            outputFile = StorageHelper.getOutputFile(
                this@AudioRecorderService,
                audioFormat.extension
            )
            fileDescriptor = contentResolver.openFileDescriptor(outputFile!!.uri, "w")
            setOutputFile(fileDescriptor?.fileDescriptor)

            runCatching {
                prepare()
            }

            start()
        }

        super.start()
    }
}
