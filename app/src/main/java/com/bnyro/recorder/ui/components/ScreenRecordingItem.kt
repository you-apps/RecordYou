package com.bnyro.recorder.ui.components

import android.media.MediaMetadataRetriever
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.R
import com.bnyro.recorder.ui.common.ClickableIcon
import com.bnyro.recorder.ui.common.DialogButton
import com.bnyro.recorder.ui.common.FullscreenDialog
import com.bnyro.recorder.ui.dialogs.ConfirmationDialog
import com.bnyro.recorder.ui.models.PlayerModel
import com.bnyro.recorder.ui.views.VideoView
import com.bnyro.recorder.util.IntentHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenRecordingItem(
    recordingFile: DocumentFile,
    isVideo: Boolean,
    isSelected: Boolean,
    onClick: (wasLongClick: Boolean) -> Unit,
    startPlayingAudio: () -> Unit
) {
    val playerModel: PlayerModel = viewModel()
    val context = LocalContext.current

    var showRenameDialog by remember {
        mutableStateOf(false)
    }
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }
    var showDropDown by remember {
        mutableStateOf(false)
    }
    var showPlayer by remember {
        mutableStateOf(false)
    }

    val thumbnail = remember {
        MediaMetadataRetriever().apply { setDataSource(context, recordingFile.uri) }.frameAtTime
    }

    val cardColor =
        if (!isSelected) MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp) else MaterialTheme.colorScheme.primary
    ElevatedCard(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .clip(CardDefaults.shape)
            .combinedClickable(
                onClick = {
                    onClick.invoke(false)
                },
                onLongClick = {
                    onClick.invoke(true)
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = cardColor,
            contentColor = contentColorFor(cardColor)
        )
    ) {
        Column() {
            thumbnail?.let { thumbnail ->
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.8f)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    bitmap = thumbnail.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp)
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = recordingFile.name.orEmpty()
                )
                ClickableIcon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.play)
                ) {
                    if (isVideo) {
                        showPlayer = true
                    } else {
                        startPlayingAudio.invoke()
                    }
                }
                Box {
                    ClickableIcon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.options)
                    ) {
                        showDropDown = true
                    }

                    DropdownMenu(
                        expanded = showDropDown,
                        onDismissRequest = { showDropDown = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.open))
                            },
                            onClick = {
                                playerModel.stopPlaying()
                                IntentHelper.openFile(context, recordingFile)
                                showDropDown = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.share))
                            },
                            onClick = {
                                playerModel.stopPlaying()
                                IntentHelper.shareFile(context, recordingFile)
                                showDropDown = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.rename))
                            },
                            onClick = {
                                playerModel.stopPlaying()
                                showRenameDialog = true
                                showDropDown = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.delete))
                            },
                            onClick = {
                                playerModel.stopPlaying()
                                showDeleteDialog = true
                                showDropDown = false
                            }
                        )
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        var fileName by remember {
            mutableStateOf(recordingFile.name.orEmpty())
        }

        AlertDialog(
            onDismissRequest = {
                showRenameDialog = false
            },
            title = {
                Text(stringResource(R.string.rename))
            },
            text = {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = {
                        fileName = it
                    },
                    label = {
                        Text(stringResource(R.string.file_name))
                    }
                )
            },
            confirmButton = {
                DialogButton(stringResource(R.string.okay)) {
                    recordingFile.renameTo(fileName)
                    val index = playerModel.files.indexOf(recordingFile)
                    playerModel.files.removeAt(index)
                    playerModel.files.add(index, recordingFile)
                    showRenameDialog = false
                }
            },
            dismissButton = {
                DialogButton(stringResource(R.string.cancel)) {
                    showRenameDialog = false
                }
            }
        )
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = R.string.delete,
            onDismissRequest = { showDeleteDialog = false }
        ) {
            playerModel.stopPlaying()
            recordingFile.delete()
            playerModel.files.remove(recordingFile)
        }
    }

    if (showPlayer) {
        FullscreenDialog(
            title = recordingFile.name.orEmpty().substringBeforeLast("."),
            onDismissRequest = {
                showPlayer = false
            }
        ) {
            VideoView(videoUri = recordingFile.uri)
        }
    }
}
