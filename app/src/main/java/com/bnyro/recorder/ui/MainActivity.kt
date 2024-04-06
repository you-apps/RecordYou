package com.bnyro.recorder.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.bnyro.recorder.enums.RecorderType
import com.bnyro.recorder.enums.ThemeMode
import com.bnyro.recorder.ui.models.RecorderModel
import com.bnyro.recorder.ui.models.ThemeModel
import com.bnyro.recorder.ui.theme.RecordYouTheme

class MainActivity : ComponentActivity() {
    private var initialRecorder = RecorderType.NONE
    private var exitAfterRecordingStart = false
    private lateinit var mProjectionManager: MediaProjectionManager
    private val recorderModel: RecorderModel by viewModels()
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeModel: ThemeModel by viewModels()
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    recorderModel.startVideoRecorder(this, result)
                }
            }
        mProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        processIntent(intent)

        setContent {
            RecordYouTheme(
                when (themeModel.themeMode) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.DARK, ThemeMode.AMOLED -> true
                    else -> false
                },
                amoledDark = themeModel.themeMode == ThemeMode.AMOLED
            ) {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier,
                        initialRecorder = initialRecorder
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        processIntent(intent)
        super.onNewIntent(intent)
    }

    private fun processIntent(intent: Intent) {
        val initialRecorderType = intent.getStringExtra(EXTRA_ACTION_KEY)?.let {
            RecorderType.valueOf(it)
        } ?: RecorderType.NONE
        initialRecorder = initialRecorderType
        if (initialRecorderType == RecorderType.AUDIO) {
            recorderModel.startAudioRecorder(this)
        } else if (initialRecorderType == RecorderType.VIDEO) {
            launcher.launch(mProjectionManager.createScreenCaptureIntent())
        }
        intent.removeExtra(EXTRA_ACTION_KEY)
    }

    override fun onPause() {
        super.onPause()
        if (initialRecorder == RecorderType.VIDEO) {
            exitAfterRecordingStart = true
            initialRecorder = RecorderType.NONE
        }
    }

    override fun onResume() {
        super.onResume()
        if (exitAfterRecordingStart) {
            exitAfterRecordingStart = false
            moveTaskToBack(true)
        }
    }

    companion object {
        const val EXTRA_ACTION_KEY = "action"
    }
}
