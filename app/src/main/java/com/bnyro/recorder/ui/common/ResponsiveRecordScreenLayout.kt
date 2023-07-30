package com.bnyro.recorder.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun ResponsiveRecordScreenLayout(
    modifier: Modifier = Modifier,
    PaneOne: @Composable () -> Unit,
    PaneTwo: @Composable () -> Unit
) {
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    if (isPortrait) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.weight(1f)) {
                PaneOne()
            }
            Box(modifier = Modifier.padding(bottom = 20.dp)) {
                PaneTwo()
            }
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                PaneOne()
            }
            Box(modifier = Modifier.padding(start = 50.dp, end = 30.dp)) {
                PaneTwo()
            }
        }
    }
}
