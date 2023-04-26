package com.bnyro.recorder.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.text.SimpleDateFormat
import java.util.Calendar

object StorageHelper {
    @SuppressLint("SimpleDateFormat")
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
    const val DEFAULT_NAMING_PATTERN = "%d_%t"

    fun getOutputFile(context: Context, extension: String): DocumentFile {
        val currentTimeMillis = Calendar.getInstance().time
        val currentDateTime = dateTimeFormat.format(currentTimeMillis)
        val currentDate = currentDateTime.split("_").first()
        val currentTime = currentDateTime.split("_").last()

        val fileName = Preferences.getString(Preferences.namingPatternKey, DEFAULT_NAMING_PATTERN)
            .replace("%d", currentDate)
            .replace("%t", currentTime)
            .replace("%m", currentTimeMillis.time.toString())
            .replace("%s", currentTimeMillis.time.div(1000).toString())

        val recordingFile = getOutputDir(context).createFile("audio/*", "$fileName.$extension")
        return recordingFile!!
    }

    fun getOutputDir(context: Context): DocumentFile {
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
}
