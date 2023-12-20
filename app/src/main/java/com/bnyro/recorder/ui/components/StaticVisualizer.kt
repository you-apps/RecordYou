package com.bnyro.recorder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun StaticVisualizer(modifier: Modifier = Modifier, amplitudes: List<Int>) {
    val spikeWidth = 4.dp
    val spikePadding = 3.dp
    val spikeTotalWidth = spikeWidth + spikePadding
    var canvasSize by remember { mutableStateOf(Size(0f, 0f)) }
    var spikes by remember { mutableFloatStateOf(0F) }
    val spikesAmplitudes = remember(amplitudes, spikes) {
        amplitudes.toDrawableAmplitudes(
            spikes = spikes.toInt(),
            minHeight = 1f,
            maxHeight = canvasSize.height.coerceAtLeast(1f)
        )
    }
    val primary = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = modifier
    ) {
        canvasSize = size
        spikes = size.width / spikeTotalWidth.toPx()
        spikesAmplitudes.forEachIndexed { index, amplitude ->
            drawRoundRect(
                brush = SolidColor(primary),
                topLeft = Offset(
                    x = index * spikeTotalWidth.toPx(),
                    y = size.height / 2F - amplitude / 2F
                ),
                size = Size(
                    width = spikeWidth.toPx(),
                    height = amplitude
                ),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }
    }
}

private fun List<Int>.toDrawableAmplitudes(
    spikes: Int,
    minHeight: Float,
    maxHeight: Float
): List<Float> {
    val amplitudes = map(Int::toFloat)
    if (amplitudes.isEmpty() || spikes == 0) {
        return List(spikes) { minHeight }
    }
    val transform = { data: List<Float> ->
        data.average().toFloat().coerceIn(minHeight, maxHeight)
    }
    return when {
        spikes > amplitudes.count() -> amplitudes.fillToSize(spikes, transform)
        else -> amplitudes.chunkToSize(spikes, transform)
    }.normalize(minHeight, maxHeight)
}

internal fun <T> Iterable<T>.fillToSize(size: Int, transform: (List<T>) -> T): List<T> {
    val capacity = ceil(size.safeDiv(count())).roundToInt()
    return map { data -> List(capacity) { data } }.flatten().chunkToSize(size, transform)
}

internal fun <T> Iterable<T>.chunkToSize(size: Int, transform: (List<T>) -> T): List<T> {
    val chunkSize = count() / size
    val remainder = count() % size
    val remainderIndex = ceil(count().safeDiv(remainder)).roundToInt()
    val chunkIteration = filterIndexed { index, _ ->
        remainderIndex == 0 || index % remainderIndex != 0
    }.chunked(chunkSize, transform)
    return when (size) {
        chunkIteration.count() -> chunkIteration
        else -> chunkIteration.chunkToSize(size, transform)
    }
}

internal fun Iterable<Float>.normalize(min: Float, max: Float): List<Float> {
    return map { (max - min) * ((it - min()) / (max() - min())) + min }
}

private fun Int.safeDiv(value: Int): Float {
    return if (value == 0) return 0F else this / value.toFloat()
}
