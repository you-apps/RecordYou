package com.bnyro.recorder.ui.components

import android.provider.OpenableColumns
import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.R
import com.bnyro.recorder.enums.SortOrder
import com.bnyro.recorder.enums.VideoFormat
import com.bnyro.recorder.ui.dialogs.ConfirmationDialog
import com.bnyro.recorder.ui.models.PlayerModel
import kotlin.math.absoluteValue
import kotlinx.coroutines.delay

@Composable
fun PlayerView(
    showVideoModeInitially: Boolean,
    showDeleteDialog: Boolean,
    sortOrder: SortOrder,
    selectedFiles: MutableState<List<DocumentFile>>,
    onDeleteAllDialogDismissed: () -> Unit
) {
    val playerModel: PlayerModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        playerModel.loadFiles(context)
    }
    var selectedTab by remember {
        mutableStateOf(
            if (showVideoModeInitially) 1 else 0
        )
    }

    val filesToShow = playerModel.files.filter { file ->
        val videoCriteria = VideoFormat.codecs.any { file.name.orEmpty().endsWith(it.extension) }
        if (selectedTab == 0) !videoCriteria else videoCriteria
    }

    val files = when (sortOrder) {
        SortOrder.ALPHABETIC, SortOrder.ALPHABETIC_REV -> filesToShow.sortedBy { it.name }
        else -> filesToShow.sortedBy {
            context.contentResolver.query(it.uri, null, null, null, null)?.use { cursor ->
                cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE).absoluteValue)
            }
        }
    }.let {
        when (sortOrder) {
            SortOrder.ALPHABETIC_REV, SortOrder.SIZE -> it.reversed()
            else -> it
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    selectedTab = 0
                },
                text = {
                    Text(stringResource(R.string.audio))
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                },
                text = {
                    Text(text = stringResource(R.string.video))
                }
            )
        }
        val icon = when (selectedTab) {
            0 -> Icons.Default.AudioFile
            else -> Icons.Default.VideoFile
        }

        if (files.isNotEmpty()) {
            Column {
                LazyColumn(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .weight(1f)
                ) {
                    items(files) {
                        RecordingItem(
                            recordingFile = it,
                            isVideo = selectedTab == 1,
                            isSelected = selectedFiles.value.contains(it),
                            onClick = { wasLongPress ->
                                when {
                                    wasLongPress -> selectedFiles.value += it
                                    selectedFiles.value.isNotEmpty() -> {
                                        if (selectedFiles.value.contains(it)) {
                                            selectedFiles.value -= it
                                        } else {
                                            selectedFiles.value += it
                                        }
                                    }
                                }
                            }
                        ) {
                            playerModel.startPlaying(context, it)
                        }
                    }
                }
                AnimatedVisibility(
                    modifier = Modifier
                        .padding(bottom = 10.dp),
                    visible = playerModel.currentPlayingFile != null
                ) {
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

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = if (selectedFiles.value.isEmpty()) R.string.delete_all else R.string.delete,
            onDismissRequest = onDeleteAllDialogDismissed
        ) {
            val filesToDelete = selectedFiles.value.takeIf { it.isNotEmpty() } ?: files
            filesToDelete.forEach {
                if (it.exists()) it.delete()
                playerModel.files.remove(it)
                selectedFiles.value -= it
            }
        }
    }
}
