package com.bnyro.recorder.util

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class PcmConverter(sampleRate: Long, channels: Int, bitsPerSample: Int) {
    private val byteRate = channels * sampleRate * bitsPerSample / 8

    private val wavHeader = byteArrayOf(
        'R'.code.toByte(),
        'I'.code.toByte(),
        'F'.code.toByte(),
        'F'.code.toByte(),
        0,
        0,
        0,
        0, // data length placeholder
        'W'.code.toByte(),
        'A'.code.toByte(),
        'V'.code.toByte(),
        'E'.code.toByte(),
        'f'.code.toByte(),
        'm'.code.toByte(),
        't'.code.toByte(),
        ' '.code.toByte(), // 'fmt ' chunk
        16, // 4 bytes: size of 'fmt ' chunk
        0,
        0,
        0,
        1, // format = 1
        0,
        channels.toByte(),
        0,
        (sampleRate and 0xffL).toByte(),
        (sampleRate shr 8 and 0xffL).toByte(),
        0,
        0,
        (byteRate and 0xffL).toByte(),
        (byteRate shr 8 and 0xffL).toByte(),
        (byteRate shr 16 and 0xffL).toByte(),
        0,
        (channels * bitsPerSample / 8).toByte(), // block align
        0,
        bitsPerSample.toByte(), // bits per sample
        0,
        'd'.code.toByte(),
        'a'.code.toByte(),
        't'.code.toByte(),
        'a'.code.toByte(),
        0,
        0,
        0,
        0,
    )

    fun convertToWave(inputStream: InputStream, outputStream: OutputStream, bufferSize: Int) {
        val data = ByteArray(bufferSize)
        try {
            val audioLength = inputStream.available().toLong()
            val dataLength = audioLength + 36
            writeWaveHeader(outputStream, audioLength, dataLength)
            while (inputStream.read(data) != -1) {
                outputStream.write(data)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to convert to wav", e)
        } finally {
            inputStream.close()
            outputStream.close()
        }
    }

    // http://stackoverflow.com/questions/4440015/java-pcm-to-wav
    @Throws(IOException::class)
    private fun writeWaveHeader(
        out: OutputStream?,
        audioLength: Long,
        dataLength: Long,
    ) {
        val header = wavHeader.copyOf(wavHeader.size)
        header[4] = (dataLength and 0xffL).toByte()
        header[5] = (dataLength shr 8 and 0xffL).toByte()
        header[6] = (dataLength shr 16 and 0xffL).toByte()
        header[7] = (dataLength shr 24 and 0xffL).toByte()
        header[40] = (audioLength and 0xffL).toByte()
        header[41] = (audioLength shr 8 and 0xffL).toByte()
        header[42] = (audioLength shr 16 and 0xffL).toByte()
        header[43] = (audioLength shr 24 and 0xffL).toByte()
        out!!.write(header, 0, header.size)
    }

    companion object {
        private const val TAG = "PcmConverter"
    }
}
