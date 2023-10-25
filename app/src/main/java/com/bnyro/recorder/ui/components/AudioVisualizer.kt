package com.bnyro.recorder.ui.components

import android.text.format.DateUtils
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.ui.models.RecorderModel
import com.bnyro.recorder.util.Preferences

@Composable
fun AudioVisualizer(
    modifier: Modifier = Modifier
) {
    val viewModel: RecorderModel = viewModel(LocalContext.current as ComponentActivity)
    val showTimestamps = remember {
        Preferences.prefs.getBoolean(
            Preferences.showVisualizerTimestamps,
            false
        )
    }

    /**Set max amplitude to tune the sensitivity
     * Higher the value, lower the sensitivity of the visualizer
     */
    val maxAmplitude = 10000
    val primary = MaterialTheme.colorScheme.primary
    val primaryMuted = primary.copy(alpha = 0.3f)

    /** Recorded amplitudes with 100ms intervals */
    val amplitudes = viewModel.recordedAmplitudes
    val measurer = rememberTextMeasurer()
    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        Canvas(modifier = Modifier.padding(vertical = 50.dp).fillMaxWidth().height(350.dp)) {
            val height = this.size.height
            val width = this.size.width
            val y = height / 2

            translate(width, y) {
                if (showTimestamps) {
                    drawLine(
                        color = primaryMuted,
                        start = Offset(-width, -y),
                        end = Offset(0f, -y),
                        strokeWidth = 5f
                    )
                    drawLine(
                        color = primaryMuted,
                        start = Offset(-width, y),
                        end = Offset(0f, y),
                        strokeWidth = 5f
                    )
                }
                amplitudes.forEachIndexed { index, amplitude ->
                    val amplitudePercentage = (amplitude.toFloat() / maxAmplitude).coerceAtMost(1f)
                    val boxHeight = height * amplitudePercentage
                    val reverseIndex = index - viewModel.recordedAmplitudes.size
                    val x = 30f * reverseIndex
                    drawRoundRect(
                        color = if (amplitudePercentage > 0.05f) primary else primaryMuted,
                        topLeft = Offset(
                            x,
                            -boxHeight / 2f
                        ),
                        size = Size(15f, boxHeight),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                    if (showTimestamps) {
                        viewModel.recordedTime?.let {
                            val timeStamp = it + reverseIndex
                            if (timeStamp.mod(10) == 0) {
                                drawLine(
                                    color = primaryMuted,
                                    start = Offset(x, -y),
                                    end = Offset(x, -y + 60f),
                                    strokeWidth = 5f
                                )
                                drawLine(
                                    color = primaryMuted,
                                    start = Offset(x, y),
                                    end = Offset(x, y - 60f),
                                    strokeWidth = 5f
                                )
                                drawText(
                                    measurer,
                                    DateUtils.formatElapsedTime(timeStamp / 10),
                                    topLeft = Offset(x - 54f, -y - 60f),
                                    style = TextStyle(primaryMuted)
                                )
                            } else if (timeStamp.mod(5) == 0) {
                                drawLine(
                                    color = primaryMuted,
                                    start = Offset(x, -y),
                                    end = Offset(x, -y + 30f),
                                    strokeWidth = 5f
                                )
                                drawLine(
                                    color = primaryMuted,
                                    start = Offset(x, y),
                                    end = Offset(x, y - 30f),
                                    strokeWidth = 5f
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
