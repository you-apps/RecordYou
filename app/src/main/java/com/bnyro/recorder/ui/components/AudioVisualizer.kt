package com.bnyro.recorder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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
    val maxAmplitude = 7000
    val primary = MaterialTheme.colorScheme.primary
    // set the maximum amplitude to avoid overflows
    val amplitudes = viewModel.recordedAmplitudes.map { if (it <= maxAmplitude) it else maxAmplitude }
    Canvas(modifier = modifier) {
        val height = this.size.height / 2f
        val width = this.size.width

        translate(width, height) {
            amplitudes.forEachIndexed { index, amplitude ->
                val boxHeight = height * (amplitude.toFloat() / maxAmplitude)
                drawRoundRect(
                    color = primary,
                    topLeft = Offset(
                        30f * (index - viewModel.recordedAmplitudes.size),
                        -boxHeight / 2f
                    ),
                    size = Size(15f, boxHeight),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }
        }
    }
}
