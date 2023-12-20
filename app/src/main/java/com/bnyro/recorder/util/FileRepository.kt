package com.bnyro.recorder.util

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.bnyro.recorder.enums.SortOrder
import com.bnyro.recorder.obj.RecordingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import linc.com.amplituda.Amplituda
import linc.com.amplituda.AmplitudaResult
import java.text.SimpleDateFormat
import java.util.Calendar

interface FileRepository {
    suspend fun getVideoRecordingItems(sortOrder: SortOrder): List<RecordingItem.Video>
    suspend fun getAudioRecordingItems(sortOrder: SortOrder): List<RecordingItem.Audio>
    suspend fun deleteFiles(files: List<DocumentFile>)
    suspend fun deleteAllFiles()
    fun getOutputFile(extension: String, prefix: String = ""): DocumentFile?
    fun getOutputDir(): DocumentFile
}

class FileRepositoryImpl(val context: Context) : FileRepository {
    private val commonAudioExtensions = listOf(
        ".mp3",
        ".aac",
        ".ogg",
        ".wma",
        ".3gp",
        ".wav",
        ".m4a"
    )

    private val commonVideoExtensions = listOf(
        ".mp4",
        ".mov",
        ".avi",
        ".mkv",
        ".webm",
        ".mpg"
    )

    private val amplituda = Amplituda(context)

    private fun getVideoFiles(): List<DocumentFile> =
        getOutputDir().listFiles().filter { file ->
            file.isFile && commonVideoExtensions.any { file.name?.endsWith(it) ?: false }
        }

    private fun getAudioFiles(): List<DocumentFile> =
        getOutputDir().listFiles().filter { file ->
            file.isFile && commonAudioExtensions.any { file.name?.endsWith(it) ?: false }
        }

    override suspend fun getVideoRecordingItems(sortOrder: SortOrder): List<RecordingItem.Video> {
        val items = withContext(Dispatchers.IO) {
            getVideoFiles().sortedBy(sortOrder).map {
                val thumbnail =
                    kotlin.runCatching {
                        MediaMetadataRetriever().apply {
                            setDataSource(context, it.uri)
                        }.frameAtTime
                    }.getOrNull()
                RecordingItem.Video(it, thumbnail)
            }
        }
        return items
    }

    override suspend fun getAudioRecordingItems(sortOrder: SortOrder): List<RecordingItem.Audio> {
        val items = withContext(Dispatchers.IO) {
            getAudioFiles().sortedBy(sortOrder).map {
                val result = amplituda.processAudio(
                    it.uri.path
                ).get()
                val duration = result.getAudioDuration(AmplitudaResult.DurationUnit.SECONDS)
                val samples = result.amplitudesAsList()
                RecordingItem.Audio(it, duration = duration, samples = samples)
            }
        }
        return items
    }

    override suspend fun deleteFiles(files: List<DocumentFile>) {
        withContext(Dispatchers.IO) {
            files.forEach {
                if (it.exists()) it.delete()
            }
        }
    }

    override suspend fun deleteAllFiles() {
        withContext(Dispatchers.IO) {
            getOutputDir().listFiles().forEach {
                if (it.isFile) it.delete()
            }
        }
    }

    override fun getOutputFile(extension: String, prefix: String): DocumentFile? {
        val currentTimeMillis = Calendar.getInstance().time
        val currentDateTime = dateTimeFormat.format(currentTimeMillis)
        val currentDate = currentDateTime.split("_").first()
        val currentTime = currentDateTime.split("_").last()

        val fileName = Preferences.getString(
            Preferences.namingPatternKey,
            DEFAULT_NAMING_PATTERN
        )
            .replace("%d", currentDate)
            .replace("%t", currentTime)
            .replace("%m", currentTimeMillis.time.toString())
            .replace("%s", currentTimeMillis.time.div(1000).toString())

        val outputDir = getOutputDir()
        if (!outputDir.exists() || !outputDir.canRead() || !outputDir.canWrite()) return null

        Log.e("out", Preferences.prefs.getString(Preferences.targetFolderKey, "").toString())

        val fullFileName = "$prefix$fileName.$extension"
        val existingFile = outputDir.findFile(fullFileName)

        return existingFile ?: outputDir.createFile("audio/*", fullFileName)
    }

    override fun getOutputDir(): DocumentFile {
        val prefDir = Preferences.prefs.getString(Preferences.targetFolderKey, "")
        return when {
            prefDir.isNullOrBlank() -> {
                val dir = context.getExternalFilesDir(null) ?: context.filesDir
                DocumentFile.fromFile(dir)
            }

            else -> DocumentFile.fromTreeUri(context, prefDir.toUri())!!
        }
    }

    companion object {
        @SuppressLint("SimpleDateFormat")
        private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
        const val DEFAULT_NAMING_PATTERN = "%d_%t"
    }
}

fun List<DocumentFile>.sortedBy(sortOrder: SortOrder): List<DocumentFile> {
    return when (sortOrder) {
        SortOrder.DEFAULT -> this
        SortOrder.ALPHABETIC -> sortedBy { it.name }
        SortOrder.ALPHABETIC_REV -> sortedByDescending { it.name }
        SortOrder.SIZE_REV -> sortedBy { it.length() }
        SortOrder.SIZE -> sortedByDescending { it.length() }
    }
}
