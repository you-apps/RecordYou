package com.bnyro.recorder.services

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import com.bnyro.recorder.R
import com.bnyro.recorder.enums.RecorderState
import com.bnyro.recorder.util.PcmConverter
import com.bnyro.recorder.util.StorageHelper
import java.io.File
import kotlin.concurrent.thread
import kotlin.experimental.and
import kotlin.experimental.or

@RequiresApi(Build.VERSION_CODES.M)
class LosslessRecorderService : RecorderService() {
    override val notificationTitle: String
        get() = getString(R.string.recording_audio)

    private var audioRecorder: AudioRecord? = null
    private var recorderThread: Thread? = null
    private var pcmConverter: PcmConverter? = null
    private var currentMaxAmplitude: Int? = null

    @SuppressLint("MissingPermission")
    override fun start() {
        super.start()

        val audioFormat: AudioFormat = AudioFormat.Builder()
            .setSampleRate(SAMPLING_RATE)
            .setChannelMask(CHANNEL_IN)
            .setEncoding(FORMAT)
            .build()

        audioRecorder = AudioRecord(
            MediaRecorder.AudioSource.DEFAULT,
            audioFormat.sampleRate,
            audioFormat.channelMask,
            audioFormat.encoding,
            BUFFER_SIZE_IN_BYTES
        )

        pcmConverter = PcmConverter(
            audioFormat.sampleRate.toLong(),
            audioFormat.channelCount,
            2 * 8
        )

        audioRecorder?.startRecording()

        outputFile = DocumentFile.fromFile(
            File(filesDir, "temp.pcm").also {
                it.createNewFile()
            }
        )

        recorderThread = thread(true) {
            writeAudioDataToFile()
        }
    }

    private fun writeAudioDataToFile() {
        val data = ByteArray(BUFFER_SIZE_IN_BYTES / 2)
        outputFile?.uri?.let { uri ->
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                while (recorderState != RecorderState.IDLE) {
                    audioRecorder?.read(data, 0, data.size)?.let {
                        if (recorderState == RecorderState.ACTIVE) {
                            outputStream.write(data)
                            currentMaxAmplitude = getAmplitudesFromBytes(data).max()
                        }
                    }
                }
            }
        }
    }

    private fun getAmplitudesFromBytes(bytes: ByteArray): IntArray {
        val amps = IntArray(bytes.size / 2)
        var i = 0
        while (i < bytes.size) {
            var buff = bytes[i + 1].toShort()
            var buff2 = bytes[i].toShort()

            buff = (buff.toInt() and 0xFF shl 8).toShort()
            buff2 = (buff2 and 0xFF)

            val res = (buff or buff2)
            amps[if (i == 0) 0 else i / 2] = res.toInt()
            i += 2
        }
        return amps
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun pause() {
        super.pause()
        audioRecorder?.stop()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun resume() {
        super.resume()
        audioRecorder?.startRecording()
    }

    private fun convertToWav() {
        val inputStream = contentResolver.openInputStream(outputFile?.uri ?: return) ?: return
        val outputStream = StorageHelper.getOutputFile(this, FILE_NAME_EXTENSION_WAV).let {
            contentResolver.openOutputStream(it.uri) ?: return
        }
        pcmConverter?.convertToWave(inputStream, outputStream, BUFFER_SIZE_IN_BYTES)
        outputFile?.delete()
    }

    override fun onDestroy() {
        recorderState = RecorderState.IDLE
        audioRecorder?.stop()
        audioRecorder?.release()
        audioRecorder = null
        recorderThread = null

        convertToWav()

        super.onDestroy()
    }

    override fun getCurrentAmplitude() = currentMaxAmplitude

    companion object {
        private const val FILE_NAME_EXTENSION_WAV = "wav"
        private const val SAMPLING_RATE = 44100
        private const val CHANNEL_IN = AudioFormat.CHANNEL_IN_STEREO
        private const val FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private val BUFFER_SIZE_IN_BYTES = 2 * AudioRecord.getMinBufferSize(
            SAMPLING_RATE,
            CHANNEL_IN,
            FORMAT
        )
    }
}
