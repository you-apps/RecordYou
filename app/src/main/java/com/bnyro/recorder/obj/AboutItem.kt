package com.bnyro.recorder.obj

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class AboutItem(
    @StringRes val title: Int,
    val icon: ImageVector,
    val url: String? = null
)
