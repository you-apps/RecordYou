package com.bnyro.recorder.ui.common

import android.view.SoundEffectConstants
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView

@Composable
fun ClickableIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String? = null,
    onClick: () -> Unit
) {
    val view = LocalView.current
    IconButton(
        modifier = modifier,
        onClick = {
            view.playSoundEffect(SoundEffectConstants.CLICK)
            onClick.invoke()
        }
    ) {
        Icon(imageVector, contentDescription)
    }
}
