package com.bnyro.recorder.util

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.util.Calendar

object StorageHelper {
    fun getOutputFile(context: Context, extension: String): File {
        val currentTime = Calendar.getInstance().time.toString().replace(" ", "_")

        val recordingFile = File(getOutputDir(context), "$currentTime.$extension")
        Log.d("TAG", "Created file: " + recordingFile.name)
        return recordingFile
    }

    fun getOutputDir(context: Context): File {
        val audioDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!
        audioDir.mkdirs()
        return audioDir
    }
}
