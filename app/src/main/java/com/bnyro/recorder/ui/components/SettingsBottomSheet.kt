package com.bnyro.recorder.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.recorder.R
import com.bnyro.recorder.enums.AudioSource
import com.bnyro.recorder.obj.AudioFormat
import com.bnyro.recorder.ui.common.ChipSelector
import com.bnyro.recorder.ui.common.ClickableIcon
import com.bnyro.recorder.ui.dialogs.AboutDialog
import com.bnyro.recorder.util.PickFolderContract
import com.bnyro.recorder.util.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    onDismissRequest: () -> Unit
) {
    var audioFormat by remember {
        mutableStateOf(AudioFormat.getCurrent())
    }
    var screenAudioSource by remember {
        mutableStateOf(
            AudioSource.fromInt(Preferences.prefs.getInt(Preferences.audioSourceKey, 0))
        )
    }
    val directoryPicker = rememberLauncherForActivityResult(PickFolderContract()) {
        it ?: return@rememberLauncherForActivityResult
        Preferences.edit { putString(Preferences.targetFolderKey, it.toString()) }
    }
    var showAbout by remember {
        mutableStateOf(false)
    }

    ModalBottomSheet(
        onDismissRequest = {
            onDismissRequest.invoke()
        }
    ) {
        Box {
            ClickableIcon(
                modifier = Modifier.align(Alignment.TopEnd),
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(R.string.about)
            ) {
                showAbout = true
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = stringResource(R.string.directory),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Button(
                    onClick = {
                        val lastDir = Preferences.prefs.getString(Preferences.targetFolderKey, "")
                            .takeIf { !it.isNullOrBlank() }
                        directoryPicker.launch(lastDir?.let { Uri.parse(it) })
                    }
                ) {
                    Text(stringResource(R.string.choose_dir))
                }
                Spacer(modifier = Modifier.height(10.dp))
                ChipSelector(
                    title = stringResource(R.string.audio_format),
                    entries = AudioFormat.formats.map { it.name },
                    values = AudioFormat.formats.map { it.format },
                    selections = listOf(audioFormat.format)
                ) { index, newValue ->
                    if (newValue) {
                        audioFormat = AudioFormat.formats[index]
                        Preferences.edit { putString(Preferences.audioFormatKey, audioFormat.name) }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                val audioValues = AudioSource.values().map { it.value }
                ChipSelector(
                    title = stringResource(R.string.screen_recorder),
                    entries = listOf(R.string.no_audio, R.string.microphone).map {
                        stringResource(it)
                    },
                    values = audioValues,
                    selections = listOf(screenAudioSource.value)
                ) { index, newValue ->
                    if (newValue) {
                        screenAudioSource = AudioSource.fromInt(audioValues[index])
                        Preferences.edit { putInt(Preferences.audioSourceKey, audioValues[index]) }
                    }
                }
            }
        }
    }

    if (showAbout) {
        AboutDialog {
            showAbout = false
        }
    }
}
