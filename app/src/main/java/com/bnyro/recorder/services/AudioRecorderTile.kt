package com.bnyro.recorder.services

import android.app.PendingIntent
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.bnyro.recorder.enums.RecorderType
import com.bnyro.recorder.ui.MainActivity

@RequiresApi(Build.VERSION_CODES.N)
class AudioRecorderTile : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_ACTION_KEY, RecorderType.AUDIO.name)
            .apply {
                flags = FLAG_ACTIVITY_NEW_TASK
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(
                PendingIntent.getActivity(
                    this, PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE
                )
            )
        } else {
            startActivityAndCollapse(intent)
        }
    }

    companion object {
        const val PENDING_INTENT_REQUEST_CODE = 20
    }
}
