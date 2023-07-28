package com.bnyro.recorder

import android.app.Application
import com.bnyro.recorder.util.NotificationHelper
import com.bnyro.recorder.util.Preferences
import com.bnyro.recorder.util.ShortcutHelper

class App : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        Preferences.init(this)
        NotificationHelper.buildNotificationChannels(this)
        ShortcutHelper.createShortcuts(this)
        container = AppContainer(this)
    }
}
