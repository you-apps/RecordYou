package com.bnyro.recorder.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.bnyro.recorder.enums.RecorderType
import com.bnyro.recorder.enums.ThemeMode
import com.bnyro.recorder.ui.models.ThemeModel
import com.bnyro.recorder.ui.screens.RecorderView
import com.bnyro.recorder.ui.theme.RecordYouTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themeModel: ThemeModel = ViewModelProvider(this).get()

        val initialRecorder = when (intent?.getStringExtra("action")) {
            "audio" -> RecorderType.AUDIO
            "screen" -> RecorderType.VIDEO
            else -> RecorderType.NONE
        }

        setContent {
            RecordYouTheme(
                when (val mode = themeModel.themeMode) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    else -> mode == ThemeMode.DARK
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RecorderView(initialRecorder)
                }
            }
        }
    }
}
