package com.bnyro.recorder.ui.screens

import android.os.Build
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.ui.common.ClickableIcon
import com.bnyro.recorder.ui.components.AudioOptionsSheet
import com.bnyro.recorder.ui.components.AudioVisualizer
import com.bnyro.recorder.ui.models.RecorderModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecorderView() {
    val recorderModel: RecorderModel = viewModel()
    val context = LocalContext.current

    var showBottomSheet by remember {
        mutableStateOf(false)
    }
    var showPlayerScreen by remember {
        mutableStateOf(false)
    }

    Scaffold { pV ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pV)
        ) {
            AudioVisualizer(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 50.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                recorderModel.recordedTime?.let {
                    Text(
                        text = DateUtils.formatElapsedTime(it),
                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClickableIcon(imageVector = Icons.Default.Settings) {
                        showBottomSheet = true
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    FloatingActionButton(
                        onClick = {
                            if (!recorderModel.isRecording) {
                                recorderModel.startRecording(context)
                            } else {
                                recorderModel.stopRecording()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (recorderModel.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && recorderModel.isRecording) {
                        ClickableIcon(
                            imageVector = if (recorderModel.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause
                        ) {
                            if (recorderModel.isPaused) {
                                recorderModel.resumeRecording()
                            } else {
                                recorderModel.pauseRecording()
                            }
                        }
                    } else {
                        ClickableIcon(
                            imageVector = Icons.Default.AudioFile
                        ) {
                            showPlayerScreen = true
                        }
                    }
                }
            }
        }

        if (showBottomSheet) {
            AudioOptionsSheet {
                showBottomSheet = false
            }
        }
        if (showPlayerScreen) {
            PlayerScreen {
                showPlayerScreen = false
            }
        }
    }
}
