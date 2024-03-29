package com.bnyro.recorder.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.bnyro.recorder.enums.RecorderType
import com.bnyro.recorder.enums.ThemeMode
import com.bnyro.recorder.ui.models.ThemeModel
import com.bnyro.recorder.ui.theme.RecordYouTheme

class MainActivity : ComponentActivity() {
    private var initialRecorder = RecorderType.NONE
    private var exitAfterRecordingStart = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeModel: ThemeModel by viewModels()

        initialRecorder = intent?.getStringExtra(EXTRA_ACTION_KEY)?.let {
            RecorderType.valueOf(it)
        } ?: RecorderType.NONE
        intent?.putExtra(EXTRA_ACTION_KEY, "")

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

    override fun onPause() {
        super.onPause()
        if (initialRecorder != RecorderType.NONE) {
            exitAfterRecordingStart = true
            initialRecorder = RecorderType.NONE
        }
    }

    override fun onResume() {
        super.onResume()
        if (exitAfterRecordingStart) {
            exitAfterRecordingStart = false
            finish()
        }
    }

    companion object {
        const val EXTRA_ACTION_KEY = "action"
    }
}
