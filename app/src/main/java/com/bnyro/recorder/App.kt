package com.bnyro.recorder

import android.app.Application
import com.bnyro.recorder.util.FileRepository
import com.bnyro.recorder.util.FileRepositoryImpl
import com.bnyro.recorder.util.NotificationHelper
import com.bnyro.recorder.util.Preferences
import com.bnyro.recorder.util.ShortcutHelper

class App : Application() {
    val fileRepository: FileRepository by lazy {
        FileRepositoryImpl(this)
    }
    override fun onCreate() {
        super.onCreate()
        Preferences.init(this)
        NotificationHelper.buildNotificationChannels(this)
        ShortcutHelper.createShortcuts(this)
    }
}
