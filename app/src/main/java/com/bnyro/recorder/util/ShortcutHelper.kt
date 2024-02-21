package com.bnyro.recorder.util

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.bnyro.recorder.R
import com.bnyro.recorder.enums.RecorderType
import com.bnyro.recorder.ui.MainActivity

object ShortcutHelper {
    sealed class AppShortcut(
        val action: String,
        @DrawableRes val iconRes: Int,
        @StringRes val label: Int
    ) {
        object RecordAudio : AppShortcut(RecorderType.AUDIO.name, R.drawable.ic_audio, R.string.record_sound)
        object RecordScreen : AppShortcut(RecorderType.VIDEO.name, R.drawable.ic_screen, R.string.record_screen)
    }
    private val shortcuts = listOf(AppShortcut.RecordAudio, AppShortcut.RecordScreen)

    private fun createShortcut(context: Context, action: String, label: String, icon: IconCompat) {
        val shortcut = ShortcutInfoCompat.Builder(context, action)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(icon)
            .setIntent(
                Intent(context, MainActivity::class.java).apply {
                    this.action = Intent.ACTION_VIEW
                    putExtra(MainActivity.EXTRA_ACTION_KEY, action)
                }
            )
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }

    fun createShortcuts(context: Context) {
        ShortcutManagerCompat.getDynamicShortcuts(context).takeIf { it.isEmpty() } ?: return

        shortcuts.forEach {
            createShortcut(
                context,
                it.action,
                context.getString(it.label),
                IconCompat.createWithResource(context, it.iconRes)
            )
        }
    }
}
