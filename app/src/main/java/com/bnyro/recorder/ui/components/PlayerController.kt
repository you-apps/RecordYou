package com.bnyro.recorder.ui.components

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

@Composable
fun PlayerController(exoPlayer: ExoPlayer) {
    with(exoPlayer) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val positionAndDuration by positionAndDurationState()
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text(DateUtils.formatElapsedTime(positionAndDuration.first / 1000))
                var tempSliderPosition by remember { mutableStateOf<Float?>(null) }
                Slider(
                    modifier = Modifier.weight(1f),
                    value = tempSliderPosition ?: positionAndDuration.first.toFloat(),
                    onValueChange = { tempSliderPosition = it },
                    valueRange = 0f.rangeTo(
                        positionAndDuration.second?.toFloat() ?: Float.MAX_VALUE
                    ),
                    onValueChangeFinished = {
                        tempSliderPosition?.let {
                            exoPlayer.seekTo(it.toLong())
                        }
                        tempSliderPosition = null
                    }
                )
                Text(
                    positionAndDuration.second?.let { DateUtils.formatElapsedTime(it / 1000) }
                        ?: ""
                )
            }
        }
    }
}

@Composable
fun WaveformPlayerController(exoPlayer: ExoPlayer, samples: List<Int>) {
    with(exoPlayer) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val positionAndDuration by positionAndDurationState()
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text(DateUtils.formatElapsedTime(positionAndDuration.first / 1000))
                Column(Modifier.weight(1f)) {
                    var tempSliderPosition by remember { mutableStateOf<Float?>(null) }
                    StaticVisualizer(
                        modifier = Modifier.height(100.dp).fillMaxWidth(),
                        amplitudes = samples
                    )
                    Slider(
                        modifier = Modifier.fillMaxWidth(),
                        value = tempSliderPosition ?: positionAndDuration.first.toFloat(),
                        onValueChange = { tempSliderPosition = it },
                        valueRange = 0f.rangeTo(
                            positionAndDuration.second?.toFloat() ?: Float.MAX_VALUE
                        ),
                        onValueChangeFinished = {
                            tempSliderPosition?.let {
                                exoPlayer.seekTo(it.toLong())
                            }
                            tempSliderPosition = null
                        }
                    )
                }
                Text(
                    positionAndDuration.second?.let { DateUtils.formatElapsedTime(it / 1000) }
                        ?: ""
                )
            }
        }
    }
}

@Composable
fun Player.positionAndDurationState(): State<Pair<Long, Long?>> {
    return produceState(
        initialValue = (currentPosition to duration.let { if (it < 0) null else it }),
        this
    ) {
        var isSeeking = false
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    isSeeking = false
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                value = currentPosition to value.second
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    isSeeking = true
                    value = currentPosition to duration.let { if (it < 0) null else it }
                }
            }
        }
        addListener(listener)

        val pollJob = launch {
            while (isActive) {
                delay(1000)
                if (!isSeeking) {
                    value = currentPosition to duration.let { if (it < 0) null else it }
                }
            }
        }
        try {
            suspendCancellableCoroutine<Nothing> { }
        } finally {
            pollJob.cancel()
            removeListener(listener)
        }
    }
}

fun Player.playPause() {
    if (isPlaying) pause() else play()
}
