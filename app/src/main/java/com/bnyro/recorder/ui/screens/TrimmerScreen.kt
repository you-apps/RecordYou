package com.bnyro.recorder.ui.screens

import android.os.Build
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.R
import com.bnyro.recorder.enums.TrimmerState
import com.bnyro.recorder.ui.components.PlayerController
import com.bnyro.recorder.ui.components.playPause
import com.bnyro.recorder.ui.models.TrimmerModel
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationGraphicsApi::class)
@Composable
fun TrimmerScreen(onDismissRequest: () -> Unit, inputFile: DocumentFile) {
    val trimmerModel: TrimmerModel = viewModel(factory = TrimmerModel.Factory)
    val sheetState = rememberModalBottomSheetState(true)

    DisposableEffect(Unit) {
        with(trimmerModel.player) {
            val mediaItem = MediaItem.Builder().setUri(inputFile.uri).build()
            setMediaItem(mediaItem)
            prepare()
            onDispose {
                stop()
            }
        }
    }
    LaunchedEffect(Unit) {
        with(trimmerModel) {
            startTimeStamp = 0L
            endTimeStamp = null
        }
    }
    ModalBottomSheet(onDismissRequest = { onDismissRequest.invoke() }, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CenterAlignedTopAppBar(title = {
                Text(
                    stringResource(R.string.select_trim_range)
                )
            })
            if (inputFile.type?.startsWith("video") == true) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(factory = { context ->
                        PlayerView(context).apply {
                            player = trimmerModel.player
                            useController = false
                        }
                    }, modifier = Modifier.fillMaxSize())
                }
            }
            PlayerController(trimmerModel.player)

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    trimmerModel.startTimeStamp = trimmerModel.player.currentPosition
                }) {
                    Text(
                        text = "${stringResource(R.string.start_timestamp)} ${
                            DateUtils.formatElapsedTime(
                                trimmerModel.startTimeStamp / 1000
                            )
                        }"
                    )
                }
                with(trimmerModel.player) {
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        shape = CircleShape
                    ) {
                        var playState by remember { mutableStateOf(false) }

                        DisposableEffect(key1 = this) {
                            val listener = object : Player.Listener {
                                override fun onIsPlayingChanged(isPlaying: Boolean) {
                                    playState = isPlaying
                                }
                            }
                            addListener(listener)
                            onDispose {
                                removeListener(listener)
                            }
                        }
                        IconButton(
                            onClick = {
                                playPause()
                            }
                        ) {
                            if (playState) {
                                Icon(
                                    Icons.Default.Pause,
                                    contentDescription = stringResource(id = R.string.pause)
                                )
                            } else {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = stringResource(id = R.string.play)
                                )
                            }
                        }
                    }
                }
                Button(
                    onClick = {
                        (trimmerModel.player.currentPosition).let {
                            if (it > trimmerModel.startTimeStamp) {
                                trimmerModel.endTimeStamp = it
                            }
                        }
                    }
                ) {
                    Text(
                        text = "${stringResource(R.string.stop_timestamp)} ${
                            trimmerModel.endTimeStamp?.let {
                                DateUtils.formatElapsedTime(it / 1000)
                            } ?: stringResource(R.string.not_set)
                        }"

                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                val context = LocalContext.current
                Button(
                    onClick = { trimmerModel.startTrimmer(context, inputFile = inputFile) },
                    enabled = (trimmerModel.endTimeStamp != null)
                ) {
                    Text(stringResource(R.string.start_trimming))
                }
            }
            Spacer(Modifier.height(64.dp))
        }
    }
    with(trimmerModel) {
        if (trimmerState != TrimmerState.NoJob) {
            val notRunning = (trimmerState != TrimmerState.Running)
            AlertDialog(onDismissRequest = {
                if (notRunning) {
                    trimmerState = TrimmerState.NoJob
                }
            }, confirmButton = {
                Button(
                    onClick = { trimmerState = TrimmerState.NoJob },
                    enabled = (notRunning)
                ) {
                    Text(stringResource(id = R.string.okay))
                }
            }, title = {
                when (trimmerState) {
                    TrimmerState.Failed -> {
                        Text(stringResource(R.string.trim_failed))
                    }

                    TrimmerState.Running -> {
                        Text(stringResource(R.string.trimming))
                    }

                    TrimmerState.Success -> {
                        Text(stringResource(R.string.trim_successful))
                    }

                    else -> {}
                }
            }, text = {
                val image = AnimatedImageVector.animatedVectorResource(
                    id = R.drawable.ic_trimmer
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .alpha(0.3f)
                ) {
                    Image(
                        modifier = Modifier.size(350.dp),
                        painter = painterResource(id = R.drawable.blob),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondaryContainer)
                    )
                    Image(
                        modifier = Modifier.size(250.dp),
                        painter = rememberAnimatedVectorPainter(
                            animatedImageVector = image,
                            atEnd = notRunning
                        ),
                        colorFilter = ColorFilter.tint(
                            MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        contentDescription = null
                    )
                }
            })
        }
    }
}
