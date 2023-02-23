package com.bnyro.recorder.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.bnyro.recorder.App
import java.text.SimpleDateFormat
import java.util.Calendar

object StorageHelper {
    @SuppressLint("SimpleDateFormat")
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")

    fun getOutputFile(context: Context, extension: String): DocumentFile {
        val currentTime = dateTimeFormat.format(Calendar.getInstance().time)

        val recordingFile = getOutputDir(context).createFile("audio/*", "$currentTime.$extension")
        return recordingFile!!
    }

    fun getOutputDir(context: Context): DocumentFile {
        val prefDir = App.preferences.getString(App.targetFolderKey, "")
        val audioDir = when {
            prefDir.isNullOrBlank() -> {
                val dir = context.getExternalFilesDir(null) ?: context.filesDir
                DocumentFile.fromFile(dir)
            }
            else -> DocumentFile.fromTreeUri(context, Uri.parse(prefDir))
        }
        return audioDir!!
    }
}
