package com.bnyro.recorder.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.App
import com.bnyro.recorder.R
import com.bnyro.recorder.obj.AudioFormat
import com.bnyro.recorder.ui.common.ChipSelector
import com.bnyro.recorder.ui.models.RecorderModel
import com.bnyro.recorder.util.PickFolderContract

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioOptionsSheet(
    onDismissRequest: () -> Unit
) {
    val recorderModel: RecorderModel = viewModel()
    var audioFormat by remember {
        mutableStateOf(recorderModel.audioFormat)
    }
    val directoryPicker = rememberLauncherForActivityResult(PickFolderContract()) {
        it ?: return@rememberLauncherForActivityResult
        App.editor.putString(App.targetFolderKey, it.toString()).apply()
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
                if (newValue) {
                    audioFormat = AudioFormat.formats[index]
                    App.editor.putString(App.audioFormatKey, audioFormat.name).apply()
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    val lastDir = App.preferences.getString(App.targetFolderKey, "")
                        .takeIf { !it.isNullOrBlank() }
                    directoryPicker.launch(lastDir?.let { Uri.parse(it) })
                }
            ) {
                Text(stringResource(R.string.choose_dir))
            }
        }
    }
}
