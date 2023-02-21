package com.bnyro.recorder.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.recorder.R
import com.bnyro.recorder.ui.common.FullscreenDialog
import com.bnyro.recorder.ui.components.PlayerView

@Composable
fun PlayerScreen(onDismissRequest: () -> Unit) {
    FullscreenDialog(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 10.dp
            )
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = stringResource(R.string.recordings),
                fontSize = MaterialTheme.typography.headlineMedium.fontSize
            )
            Spacer(modifier = Modifier.height(15.dp))
            PlayerView()
        }
    }
}
