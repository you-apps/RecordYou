package com.bnyro.recorder.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.R
import com.bnyro.recorder.obj.RecordingItemData
import com.bnyro.recorder.ui.models.PlayerModel

@Composable
fun RecordingItemList(
    items: List<RecordingItemData>,
    isVideoList: Boolean,
    playerModel: PlayerModel = viewModel()
) {
    val context = LocalContext.current
    val icon = if (isVideoList) Icons.Default.VideoFile else Icons.Default.AudioFile
    if (items.isNotEmpty()) {
        Column {
            LazyColumn(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .weight(1f)
            ) {
                items(items) {
                    RecordingItem(
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
                        }
                    ) {
                        playerModel.startPlaying(context, it.recordingFile)
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
}
