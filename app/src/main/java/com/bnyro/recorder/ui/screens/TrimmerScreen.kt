package com.bnyro.recorder.ui.screens

import android.os.Build
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.R
import com.bnyro.recorder.ui.components.PlayerController
import com.bnyro.recorder.ui.models.TrimmerModel
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
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
        }
    }
}
