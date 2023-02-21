package com.bnyro.recorder.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.bnyro.recorder.App
import java.util.Calendar

object StorageHelper {
    fun getOutputFile(context: Context, extension: String): DocumentFile {
        val currentTime = Calendar.getInstance().time.toString().replace(" ", "_")

        val recordingFile = getOutputDir(context).createFile("audio/*", "$currentTime.$extension")
        Log.d("TAG", "Created file: " + recordingFile?.name)
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
