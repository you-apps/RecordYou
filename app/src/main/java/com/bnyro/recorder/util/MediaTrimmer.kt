package com.bnyro.recorder.util

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.SparseIntArray
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import com.bnyro.recorder.App
import java.nio.ByteBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaTrimmer {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Recycle")
    suspend fun trimMedia(
        context: Context,
        inputFile: DocumentFile,
        startMs: Long,
        endMs: Long
    ): Boolean {
        assert(endMs > startMs && endMs > 0)
        return withContext(Dispatchers.IO) {
            var extension = inputFile.uri.path!!.split('.').lastOrNull()
            if (extension == "aac") { // Special case if trimming raw AAC input mux it into MP4 container
                extension = "m4a"
            }
            val outputFile = (context.applicationContext as App).fileRepository.getOutputFile(
                extension = extension ?: "mp4",
                prefix = "Trim_"
            )
            val inputPfd = context.contentResolver.openFileDescriptor(inputFile.uri, "r")!!
            val outputPfd = context.contentResolver.openFileDescriptor(outputFile!!.uri, "w")!!
            trimMediaFile(inputPfd, outputPfd, startMs, endMs, extension)
        }
    }

    // https://android.googlesource.com/platform/packages/apps/Gallery2/+/634248d/src/com/android/gallery3d/app/VideoUtils.java
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("WrongConstant")
    private suspend fun trimMediaFile(
        inputPfd: ParcelFileDescriptor,
        outputPfd: ParcelFileDescriptor,
        startMs: Long,
        endMs: Long,
        extension: String?
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val extractor = MediaExtractor()
            extractor.setDataSource(inputPfd.fileDescriptor)
            val trackCount = extractor.trackCount
            var outputFormat = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
            if (extension == "3gp") {
                outputFormat = MediaMuxer.OutputFormat.MUXER_OUTPUT_3GPP
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && extension == "ogg") {
                outputFormat = MediaMuxer.OutputFormat.MUXER_OUTPUT_OGG
            }
            if (extension == "webm") { // No need to add API level 21 here as the whole app is minSdk 21
                outputFormat = MediaMuxer.OutputFormat.MUXER_OUTPUT_WEBM
            }
            val muxer =
                MediaMuxer(outputPfd.fileDescriptor, outputFormat)
            val indexMap = SparseIntArray(trackCount)
            var bufferSize = -1

            return@withContext try {
                for (i in 0 until trackCount) {
                    val format = extractor.getTrackFormat(i)
                    val mime = format.getString(MediaFormat.KEY_MIME)
                    val selectCurrentTrack =
                        (mime?.startsWith("audio/") == true || mime?.startsWith("video/") == true)

                    if (selectCurrentTrack) {
                        extractor.selectTrack(i)
                        val destinationIndex = muxer.addTrack(format)
                        indexMap.put(i, destinationIndex)
                        if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                            val newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                            bufferSize = if (newSize > bufferSize) newSize else bufferSize
                        }
                    }
                }

                if (bufferSize < 0) {
                    bufferSize = DEFAULT_BUFFER_SIZE
                }

                if (startMs > 0) {
                    extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                }

                val offset = 0
                var trackIndex: Int
                val destinationBuffer = ByteBuffer.allocate(bufferSize)
                val bufferInfo = MediaCodec.BufferInfo()
                muxer.start()
                Log.d("Media Trimmer", "Muxer started")
                while (true) {
                    // Start copying samples from input file to output file
                    bufferInfo.offset = offset
                    bufferInfo.size = extractor.readSampleData(destinationBuffer, offset)
                    if (bufferInfo.size < 0) {
                        bufferInfo.size = 0
                        Log.d("Media Trimmer", "No more samples left in the input")
                        break
                    } else {
                        bufferInfo.presentationTimeUs = extractor.sampleTime
                        if (bufferInfo.presentationTimeUs > endMs * 1000) {
                            Log.d("Media Trimmer", "Reached the end")
                            break
                        } else {
                            bufferInfo.flags = extractor.sampleFlags
                            trackIndex = extractor.sampleTrackIndex
                            muxer.writeSampleData(
                                indexMap.get(trackIndex),
                                destinationBuffer,
                                bufferInfo
                            )
                            extractor.advance()
                        }
                    }
                }
                muxer.stop()
                true
            } catch (e: Exception) {
                Log.e("Media trimmer", e.message, e)
                false
            } finally {
                muxer.release()
                inputPfd.close()
                outputPfd.close()
            }
        }
    }
}
