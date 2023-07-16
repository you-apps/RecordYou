package com.bnyro.recorder.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bnyro.recorder.R

@Composable
fun BlobIconBox(@DrawableRes icon: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.3f)
    ) {
        Image(
            modifier = Modifier.size(350.dp),
            painter = painterResource(id = R.drawable.blob),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondaryContainer)
        )
        Image(
            modifier = Modifier.size(250.dp),
            painter = painterResource(id = icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
        )
    }
}
