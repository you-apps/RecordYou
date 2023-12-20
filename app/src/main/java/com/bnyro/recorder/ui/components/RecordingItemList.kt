package com.bnyro.recorder.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Icon
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
import com.bnyro.recorder.obj.RecordingItem
import com.bnyro.recorder.ui.models.PlayerModel
import com.bnyro.recorder.ui.screens.TrimmerScreen

@Composable
fun RecordingItemList(
    items: List<RecordingItem>,
    isVideoList: Boolean,
    playerModel: PlayerModel = viewModel()
) {
    val context = LocalContext.current
    val icon = if (isVideoList) Icons.Default.VideoFile else Icons.Default.AudioFile
    var chosenItem by remember { mutableStateOf<RecordingItem?>(null) }
    var showTrimmer by remember { mutableStateOf(false) }
    if (items.isNotEmpty()) {
        Column {
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .weight(1f),
                columns = GridCells.Adaptive(350.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) {
                    when (it) {
                        is RecordingItem.Audio -> {
                            AudioRecordingItem(
                                it,
                                isSelected = playerModel.selectedFiles.contains(it),
                                onClick = { wasLongPress ->
                                    when {
                                        wasLongPress -> playerModel.selectedFiles += it
                                        playerModel.selectedFiles.isNotEmpty() -> {
                                            if (playerModel.selectedFiles.contains(it)) {
                                                playerModel.selectedFiles -= it
                                            } else {
                                                playerModel.selectedFiles += it
                                            }
                                        }
                                    }
                                },
                                onEdit = {
                                    chosenItem = it
                                    showTrimmer = true
                                }
                            ) {
                                playerModel.startPlaying(context, it.recordingFile)
                            }
                        }

                        is RecordingItem.Video -> {
                            VideoRecordingItem(
                                it,
                                isSelected = playerModel.selectedFiles.contains(it),
                                onClick = { wasLongPress ->
                                    when {
                                        wasLongPress -> playerModel.selectedFiles += it
                                        playerModel.selectedFiles.isNotEmpty() -> {
                                            if (playerModel.selectedFiles.contains(it)) {
                                                playerModel.selectedFiles -= it
                                            } else {
                                                playerModel.selectedFiles += it
                                            }
                                        }
                                    }
                                },
                                onEdit = {
                                    chosenItem = it
                                    showTrimmer = true
                                }
                            )
                        }
                    }
                }
            }
            if (!isVideoList) {
                AnimatedVisibility(
                    modifier = Modifier
                        .padding(bottom = 10.dp),
                    visible = playerModel.currentPlayingFile != null
                ) {
                    MiniPlayer()
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(120.dp),
                    imageVector = icon,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = stringResource(R.string.nothing_here))
            }
        }
    }
    if (showTrimmer && chosenItem != null && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
        TrimmerScreen(onDismissRequest = { showTrimmer = false }, inputItem = chosenItem!!)
    }
}
