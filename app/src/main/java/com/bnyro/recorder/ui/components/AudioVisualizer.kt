package com.bnyro.recorder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.translate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.ui.models.RecorderModel

@Composable
fun AudioVisualizer(
    modifier: Modifier = Modifier
) {
    val viewModel: RecorderModel = viewModel()
    val maxAmplitude = 3000
    val primary = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val height = this.size.height / 2
        val width = this.size.width * 0.75f
        translate(width, height) {
            viewModel.recordedAmplitudes.forEachIndexed { index, i ->
                val boxHeight = (height * (i.toFloat() / maxAmplitude))
                drawRect(
                    color = primary,
                    topLeft = Offset(
                        30f * (index - viewModel.recordedAmplitudes.size),
                        -boxHeight / 2
                    ),
                    size = Size(15f, boxHeight)
                )
            }
        }
    }
}
