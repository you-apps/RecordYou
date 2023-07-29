package com.bnyro.recorder.ui.common

import android.view.SoundEffectConstants
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

@Composable
fun DialogButton(
    text: String,
    onClick: () -> Unit
) {
    val view = LocalView.current
    TextButton(onClick = {
        view.playSoundEffect(SoundEffectConstants.CLICK)
        onClick.invoke()
    }) {
        Text(text)
    }
}
