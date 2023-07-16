package com.bnyro.recorder.ui.screens

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.res.Configuration
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.R
import com.bnyro.recorder.enums.RecorderState
import com.bnyro.recorder.enums.RecorderType
import com.bnyro.recorder.ui.common.BlobIconBox
import com.bnyro.recorder.ui.common.ClickableIcon
import com.bnyro.recorder.ui.components.AudioVisualizer
import com.bnyro.recorder.ui.models.RecorderModel

@Composable
fun RecorderView(
    initialRecorder: RecorderType,
    recordScreenMode: Boolean
) {
    val recorderModel: RecorderModel = viewModel()
    val context = LocalContext.current
    val mProjectionManager =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    val orientation = LocalConfiguration.current.orientation

    val requestRecording = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@rememberLauncherForActivityResult
        recorderModel.startVideoRecorder(context, result)
    }

    fun requestScreenRecording() {
        if (!recorderModel.hasScreenRecordingPermissions(context)) return
        requestRecording.launch(
            mProjectionManager.createScreenCaptureIntent()
        )
    }

    LaunchedEffect(Unit) {
        when (initialRecorder) {
            RecorderType.AUDIO -> {
                recorderModel.startAudioRecorder(context)
            }

            RecorderType.VIDEO -> {
                requestScreenRecording()
            }

            RecorderType.NONE -> {}
        }
    }

    LaunchedEffect(recorderModel.recorderState) {
        // update the UI when the recorder gets destroyed by the notification
        if (recorderModel.recorderState == RecorderState.IDLE) {
            recorderModel.stopRecording()
        }
    }

    Scaffold { pV ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pV),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Crossfade(
                modifier = Modifier.weight(1f),
                targetState = recorderModel.recordedAmplitudes
            ) {
                when (it.isEmpty() && orientation == Configuration.ORIENTATION_PORTRAIT) {
                    true -> BlobIconBox(
                        icon = if (recordScreenMode) R.drawable.ic_screen_record else R.drawable.ic_mic
                    )
                    false -> AudioVisualizer(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                recorderModel.recordedTime?.let {
                    Text(
                        text = DateUtils.formatElapsedTime(it),
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isSystemInDarkTheme()) {
                                Color(0xA8EE665B)
                            } else {
                                Color(
                                    0xffdd6f62
                                )
                            },
                            contentColor = Color.White
                        ),
                        shape = CircleShape
                    ) {
                        val buttonDescription = stringResource(
                            if (recorderModel.recorderState != RecorderState.IDLE) {
                                R.string.stop
                            } else {
                                R.string.record
                            }
                        )
                        IconButton(
                            onClick = {
                                when {
                                    recorderModel.recorderState != RecorderState.IDLE -> recorderModel.stopRecording()
                                    recordScreenMode -> requestScreenRecording()
                                    else -> recorderModel.startAudioRecorder(context)
                                }
                            },
                            modifier = Modifier
                                .padding(16.dp)
                                .semantics { contentDescription = buttonDescription }
                        ) {
                            when {
                                recorderModel.recorderState != RecorderState.IDLE -> {
                                    Icon(
                                        Icons.Default.Stop,
                                        modifier = Modifier.size(36.dp),
                                        contentDescription = stringResource(R.string.pause)
                                    )
                                }

                                else -> {
                                    Box(
                                        Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x9FFFFFFF))
                                    )
                                    Box(
                                        Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x9FFFFFFF))
                                    )
                                }
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && recorderModel.recorderState != RecorderState.IDLE) {
                        Spacer(modifier = Modifier.width(20.dp))
                        ElevatedCard(
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            ClickableIcon(
                                imageVector = if (recorderModel.recorderState == RecorderState.PAUSED) {
                                    Icons.Default.PlayArrow
                                } else {
                                    Icons.Default.Pause
                                },
                                contentDescription = stringResource(
                                    if (recorderModel.recorderState == RecorderState.PAUSED) {
                                        R.string.resume
                                    } else {
                                        R.string.pause
                                    }
                                )
                            ) {
                                if (recorderModel.recorderState == RecorderState.PAUSED) {
                                    recorderModel.resumeRecording()
                                } else {
                                    recorderModel.pauseRecording()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
