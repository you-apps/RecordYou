package com.bnyro.recorder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.ui.common.ClickableIcon
import com.bnyro.recorder.ui.models.PlayerModel

@Composable
fun PlayerView(
    modifier: Modifier = Modifier
) {
    val playerModel: PlayerModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        playerModel.loadFiles(context)
    }

    if (playerModel.files.isNotEmpty()) {
        ElevatedCard(
            modifier = modifier
        ) {
            LazyColumn(
                modifier = Modifier.padding(15.dp)
            ) {
                items(playerModel.files) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        var playing by remember {
                            mutableStateOf(false)
                        }
                        Text(
                            modifier = Modifier.weight(1f),
                            text = it.name
                        )
                        ClickableIcon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow
                        ) {
                            if (!playing) {
                                playerModel.startPlaying(context, it) {
                                    playing = false
                                }
                            } else {
                                playerModel.stopPlaying()
                            }
                            playing = !playing
                        }
                        ClickableIcon(imageVector = Icons.Default.Delete) {
                            playerModel.stopPlaying()
                            it.delete()
                            playerModel.files.remove(it)
                        }
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    modifier = Modifier.size(120.dp),
                    imageVector = Icons.Default.ImportContacts,
                    contentDescription = null
                )
            }
        }
    }
}
