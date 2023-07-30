package com.bnyro.recorder.util

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.bnyro.recorder.enums.RecorderType
import com.bnyro.recorder.enums.SortOrder
import com.bnyro.recorder.obj.RecordingItemData
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface FileRepository {
    suspend fun getVideoRecordingItems(sortOrder: SortOrder): List<RecordingItemData>
    suspend fun getAudioRecordingItems(sortOrder: SortOrder): List<RecordingItemData>
    suspend fun deleteFiles(files: List<DocumentFile>)
    suspend fun deleteAllFiles()
    fun getOutputFile(extension: String): DocumentFile
    fun getOutputDir(): DocumentFile
}

class FileRepositoryImpl(val context: Context) : FileRepository {

    private fun getVideoFiles(): List<DocumentFile> =
        getOutputDir().listFiles().filter {
            it.isFile && it.name.orEmpty().endsWith("mp4")
        }

    private fun getAudioFiles(): List<DocumentFile> =
        getOutputDir().listFiles().filter {
            it.isFile && !it.name.orEmpty().endsWith("mp4")
        }

    override suspend fun getVideoRecordingItems(sortOrder: SortOrder): List<RecordingItemData> {
        val items = withContext(Dispatchers.IO) {
            getVideoFiles().sortedBy(sortOrder).map {
                val thumbnail =
                    MediaMetadataRetriever().apply {
                        setDataSource(
                            context,
                            it.uri
                        )
                    }.frameAtTime
                RecordingItemData(it, RecorderType.VIDEO, thumbnail)
            }
        }
        return items
    }

    override suspend fun getAudioRecordingItems(sortOrder: SortOrder): List<RecordingItemData> {
        val items = withContext(Dispatchers.IO) {
            getAudioFiles().sortedBy(sortOrder).map { RecordingItemData(it, RecorderType.AUDIO) }
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

    override fun getOutputFile(extension: String): DocumentFile {
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

        val recordingFile = getOutputDir().createFile("audio/*", "$fileName.$extension")
        return recordingFile!!
    }

    override fun getOutputDir(): DocumentFile {
        val prefDir = Preferences.prefs.getString(Preferences.targetFolderKey, "")
        val audioDir = when {
            prefDir.isNullOrBlank() -> {
                val dir = context.getExternalFilesDir(null) ?: context.filesDir
                DocumentFile.fromFile(dir)
            }

            else -> DocumentFile.fromTreeUri(context, Uri.parse(prefDir))
        }
        return audioDir!!
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
        SortOrder.SIZE_REV -> sortedBy {
            it.length()
        }

        SortOrder.SIZE -> sortedByDescending {
            it.length()
        }
    }
}
