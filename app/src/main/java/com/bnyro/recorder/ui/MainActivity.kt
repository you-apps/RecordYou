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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themeModel: ThemeModel by viewModels()

        val initialRecorder = when (intent?.getStringExtra("action")) {
            "audio" -> RecorderType.AUDIO
            "screen" -> RecorderType.VIDEO
            else -> RecorderType.NONE
        }

        setContent {
            RecordYouTheme(
                when (themeModel.themeMode) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.DARK -> true
                    else -> false
                },
                amoledDark = themeModel.themeMode == ThemeMode.AMOLED,
            ) {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier,
                        initialRecorder = initialRecorder,
                    )
                }
            }
        }
    }
}
