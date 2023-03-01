package com.bnyro.recorder.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.recorder.R
import com.bnyro.recorder.ui.common.FullscreenDialog
import com.bnyro.recorder.ui.components.PlayerView

@Composable
fun PlayerScreen(
    showVideoModeInitially: Boolean,
    onDismissRequest: () -> Unit
) {
    val orientation = LocalConfiguration.current.orientation

    FullscreenDialog(
        title = if (
            orientation == Configuration.ORIENTATION_LANDSCAPE
        ) {
            stringResource(R.string.recordings)
        } else {
            ""
        },
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 20.dp
            )
        ) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                Spacer(modifier = Modifier.height(70.dp))
                Text(
                    text = stringResource(R.string.recordings),
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize
                )
                Spacer(modifier = Modifier.height(15.dp))
            }
            PlayerView(showVideoModeInitially)
        }
    }
}
