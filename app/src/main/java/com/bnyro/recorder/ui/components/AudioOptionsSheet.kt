package com.bnyro.recorder.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.R
import com.bnyro.recorder.obj.AudioFormat
import com.bnyro.recorder.ui.common.ChipSelector
import com.bnyro.recorder.ui.models.RecorderModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioOptionsSheet(
    onDismissRequest: () -> Unit
) {
    val recorderModel: RecorderModel = viewModel()
    var audioFormat by remember {
        mutableStateOf(recorderModel.audioFormat)
    }

    ModalBottomSheet(
        onDismissRequest = {
            recorderModel.audioFormat = audioFormat
            onDismissRequest.invoke()
        }
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .padding(bottom = 10.dp)
        ) {
            ChipSelector(
                title = stringResource(R.string.format),
                entries = AudioFormat.formats.map { it.name },
                values = AudioFormat.formats.map { it.format },
                selections = listOf(audioFormat.format)
            ) { index, newValue ->
                if (newValue) audioFormat = AudioFormat.formats[index]
            }
        }
    }
}
