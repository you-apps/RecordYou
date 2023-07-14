package com.bnyro.recorder.ui.components

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.R
import com.bnyro.recorder.ui.models.PlayerModel
import kotlinx.coroutines.delay

@Composable
fun MiniPlayer(playerModel: PlayerModel = viewModel()) {
    ElevatedCard(
        modifier = Modifier
            .height(140.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 15.dp, horizontal = 15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fileName = playerModel.currentPlayingFile?.name.orEmpty()
                Text(
                    modifier = Modifier.weight(1f),
                    text = fileName.substringBeforeLast(".").takeIf {
                        it.isNotBlank()
                    } ?: fileName
                )
                FloatingActionButton(
                    onClick = {
                        if (playerModel.isPlaying) {
                            playerModel.pausePlaying()
                        } else {
                            playerModel.resumePlaying()
                        }
                    }
                ) {
                    Icon(
                        if (playerModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = stringResource(
                            if (playerModel.isPlaying) R.string.pause else R.string.resume
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            playerModel.player?.let { player ->
                var position by remember {
                    mutableStateOf(0)
                }

                LaunchedEffect(Unit) {
                    while (true) {
                        runCatching {
                            position = player.currentPosition
                        }
                        delay(50)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val duration = player.duration.toFloat()
                    Text(
                        text = DateUtils.formatElapsedTime(
                            (position / 1000).toLong()
                        )
                    )
                    Slider(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .weight(1f),
                        value = position.toFloat(),
                        onValueChange = {
                            runCatching {
                                player.seekTo(it.toInt())
                                position = it.toInt()
                            }
                        },
                        valueRange = 0f..duration
                    )
                    Text(
                        text = DateUtils.formatElapsedTime(
                            (duration / 1000).toLong()
                        )
                    )
                }
            }
        }
    }
}