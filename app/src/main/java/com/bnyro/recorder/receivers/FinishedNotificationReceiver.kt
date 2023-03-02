package com.bnyro.recorder.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.bnyro.recorder.services.RecorderService
import com.bnyro.recorder.util.IntentHelper
import com.bnyro.recorder.util.NotificationHelper
import com.bnyro.recorder.util.StorageHelper

class FinishedNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val fileName = intent?.getStringExtra(RecorderService.FILE_NAME_EXTRA_KEY) ?: return
        val file = StorageHelper.getOutputDir(context ?: return).findFile(fileName)

        when (intent.getStringExtra(RecorderService.ACTION_EXTRA_KEY)) {
            RecorderService.SHARE_ACTION -> file?.let { IntentHelper.shareFile(context, it) }
            RecorderService.DELETE_ACTION -> file?.delete()
        }
        NotificationManagerCompat.from(context)
            .cancel(NotificationHelper.RECORDING_FINISHED_N_ID)
    }
}
