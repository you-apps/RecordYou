package com.bnyro.recorder.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun OverlayView(onDismissRequest: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        MainCanvas()
        Card(Modifier.align(Alignment.TopEnd)) {
            Row {
                IconButton(onClick = { onDismissRequest() }) {
                    Icon(Icons.Default.Close, "Close Overlay")
                }
            }
        }
    }

}