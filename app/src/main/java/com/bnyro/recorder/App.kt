package com.bnyro.recorder

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        preferences = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREF_FILE_NAME = "RecordYou"
        lateinit var preferences: SharedPreferences
        val editor: SharedPreferences.Editor get() = preferences.edit()

        const val targetFolderKey = "targetFolder"
        const val audioFormatKey = "audioFormat"
    }
}
