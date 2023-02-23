package com.bnyro.recorder.ui.screens

import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.R
import com.bnyro.recorder.services.ScreenRecorderService
import com.bnyro.recorder.ui.common.ClickableIcon
import com.bnyro.recorder.ui.components.AudioVisualizer
import com.bnyro.recorder.ui.components.SettingsBottomSheet
import com.bnyro.recorder.ui.models.RecorderModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecorderView() {
    val recorderModel: RecorderModel = viewModel()
    val context = LocalContext.current
    val mProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    var showBottomSheet by remember {
        mutableStateOf(false)
    }
    var showPlayerScreen by remember {
        mutableStateOf(false)
    }
    var recordScreenMode by remember {
        mutableStateOf(false)
    }
    var isRecordingScreen by remember {
        mutableStateOf(false)
    }
    val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isRecordingScreen = false
        }
    }

    val requestRecording = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@rememberLauncherForActivityResult
        recorderModel.startVideoRecorder(context, result)
        context.registerReceiver(
            stopReceiver,
            IntentFilter(ScreenRecorderService.STOP_INTENT_ACTION)
        )
        isRecordingScreen = true
    }

    Scaffold { pV ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pV)
        ) {
            if (recorderModel.recordedAmplitudes.isNotEmpty()) {
                AudioVisualizer(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 80.dp)
                )
            } else {
                Text(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 200.dp),
                    text = stringResource(
                        if (recordScreenMode) R.string.record_screen else R.string.record_sound
                    ),
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    fontWeight = MaterialTheme.typography.headlineLarge.fontWeight
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 25.dp),
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

                    Crossfade(targetState = recordScreenMode) {
                        when (it) {
                            true -> FloatingActionButton(
                                onClick = {
                                    if (!isRecordingScreen) {
                                        requestRecording.launch(
                                            mProjectionManager.createScreenCaptureIntent()
                                        )
                                    } else {
                                        context.unregisterReceiver(stopReceiver)
                                        val stopIntent = Intent(
                                            ScreenRecorderService.STOP_INTENT_ACTION
                                        )
                                        context.sendBroadcast(stopIntent)
                                        isRecordingScreen = false
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (!isRecordingScreen) Icons.Default.Videocam else Icons.Default.Stop,
                                    contentDescription = null
                                )
                            }
                            false -> FloatingActionButton(
                                onClick = {
                                    if (!recorderModel.isRecording) {
                                        recorderModel.startAudioRecorder(context)
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
                        }
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

                Spacer(modifier = Modifier.height(5.dp))
                ClickableIcon(
                    imageVector = if (recordScreenMode) Icons.Default.ExpandMore else Icons.Default.ExpandLess
                ) {
                    recordScreenMode = !recordScreenMode
                }
            }
        }

        if (showBottomSheet) {
            SettingsBottomSheet {
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
