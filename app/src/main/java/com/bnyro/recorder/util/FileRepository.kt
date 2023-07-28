package com.bnyro.recorder.util

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.documentfile.provider.DocumentFile
import com.bnyro.recorder.enums.RecorderType
import com.bnyro.recorder.enums.SortOrder
import com.bnyro.recorder.obj.RecordingItemData

interface FileRepository {
    suspend fun getVideoRecordingItems(sortOrder: SortOrder): List<RecordingItemData>
    suspend fun getAudioRecordingItems(sortOrder: SortOrder): List<RecordingItemData>
    suspend fun deleteSelectedFiles(files: List<DocumentFile>)
    suspend fun deleteAllFiles()
}

class FileRepositoryImpl(val context: Context) : FileRepository {

    private fun getVideoFiles(): List<DocumentFile> =
        StorageHelper.getOutputDir(context).listFiles().filter {
            it.isFile && it.name.orEmpty().endsWith("mp4")
        }

    private fun getAudioFiles(): List<DocumentFile> =
        StorageHelper.getOutputDir(context).listFiles().filter {
            it.isFile && !it.name.orEmpty().endsWith("mp4")
        }

    override suspend fun getVideoRecordingItems(sortOrder: SortOrder): List<RecordingItemData> {
        return getVideoFiles().sortedBy(sortOrder).map {
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

    override suspend fun getAudioRecordingItems(sortOrder: SortOrder): List<RecordingItemData> {
        return getAudioFiles().sortedBy(sortOrder).map { RecordingItemData(it, RecorderType.AUDIO) }
    }

    override suspend fun deleteSelectedFiles(files: List<DocumentFile>) {
        files.forEach {
            if (it.exists()) it.delete()
        }
    }

    override suspend fun deleteAllFiles() {
        StorageHelper.getOutputDir(context).listFiles().forEach {
            if (it.isFile) it.delete()
        }
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
