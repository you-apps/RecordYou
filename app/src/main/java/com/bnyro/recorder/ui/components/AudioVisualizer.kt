package com.bnyro.recorder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.math.MathUtils.clamp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.ui.models.RecorderModel

@Composable
fun AudioVisualizer(
    modifier: Modifier = Modifier
) {
    val viewModel: RecorderModel = viewModel()
    val state = rememberLazyListState()
    val maxAmplitude = 3000

    LazyRow(
        modifier = modifier,
        state = state,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(viewModel.recordedAmplitudes) {
            val height = clamp(5f, (150 * (it.toFloat() / maxAmplitude)), 150f)
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .padding(horizontal = 0.5f.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
