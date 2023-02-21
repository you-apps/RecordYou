package com.bnyro.recorder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
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
import com.bnyro.recorder.ui.common.ClickableIcon
import com.bnyro.recorder.ui.common.DialogButton
import com.bnyro.recorder.ui.models.PlayerModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingItem(recordingFile: File) {
    val playerModel: PlayerModel = viewModel()
    val context = LocalContext.current

    var showRenameDialog by remember {
        mutableStateOf(false)
    }

    ElevatedCard(
        modifier = Modifier.padding(vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 10.dp)
        ) {
            var playing by remember {
                mutableStateOf(false)
            }
            Text(
                modifier = Modifier.weight(1f),
                text = recordingFile.name
            )
            ClickableIcon(
                imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow
            ) {
                if (!playing) {
                    playerModel.startPlaying(context, recordingFile) {
                        playing = false
                    }
                } else {
                    playerModel.stopPlaying()
                }
                playing = !playing
            }
            ClickableIcon(imageVector = Icons.Default.Edit) {
                playerModel.stopPlaying()
                showRenameDialog = true
            }
            ClickableIcon(imageVector = Icons.Default.Delete) {
                playerModel.stopPlaying()
                recordingFile.delete()
                playerModel.files.remove(recordingFile)
            }
        }
    }

    if (showRenameDialog) {
        var fileName by remember {
            mutableStateOf(recordingFile.name)
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
                    val newFile = File(recordingFile.parent.orEmpty(), fileName)
                    val index = playerModel.files.indexOf(recordingFile)
                    recordingFile.renameTo(newFile)
                    playerModel.files.removeAt(index)
                    playerModel.files.add(index, newFile)
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
}
