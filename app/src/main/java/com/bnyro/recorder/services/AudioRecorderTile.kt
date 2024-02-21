package com.bnyro.recorder.services

import android.content.Intent
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
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        startActivityAndCollapse(intent)
    }
}
