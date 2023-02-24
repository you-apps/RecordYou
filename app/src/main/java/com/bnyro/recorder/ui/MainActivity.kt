package com.bnyro.recorder.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.bnyro.recorder.enums.Recorder
import com.bnyro.recorder.ui.screens.RecorderView
import com.bnyro.recorder.ui.theme.RecordYouTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialRecorder = when (intent?.getStringExtra("action")) {
            "audio" -> Recorder.AUDIO
            "screen" -> Recorder.SCREEN
            else -> Recorder.NONE
        }

        setContent {
            RecordYouTheme {
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
