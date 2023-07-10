package com.bnyro.recorder.canvas_overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.R

@Composable
fun OverlayView(onDismissRequest: () -> Unit, canvasViewModel: CanvasViewModel = viewModel()) {
    Box(Modifier.fillMaxSize()) {
        MainCanvas()
        var currentDrawMode by remember { mutableStateOf(canvasViewModel.currentPath.drawMode) }
        Card(Modifier.align(Alignment.TopEnd)) {
            Row {
                IconButton(
                    onClick = {
                        currentDrawMode = DrawMode.Pen
                        canvasViewModel.currentPath.drawMode = currentDrawMode
                    }) {
                    Icon(imageVector = Icons.Default.Draw, contentDescription = "Draw Mode")
                }
                IconButton(
                    onClick = {
                        currentDrawMode = DrawMode.Eraser
                        canvasViewModel.currentPath.drawMode = currentDrawMode
                    }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_eraser_black_24dp),
                        contentDescription = "Erase Mode"
                    )
                }
                IconButton(onClick = { onDismissRequest() }) {
                    Icon(Icons.Default.Close, "Close Overlay")
                }
            }
        }
    }

}