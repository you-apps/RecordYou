package com.bnyro.recorder.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
        LazyColumn(
            modifier = modifier
        ) {
            items(playerModel.files) {
                RecordingItem(recordingFile = it)
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
