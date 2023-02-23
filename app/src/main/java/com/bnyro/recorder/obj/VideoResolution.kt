package com.bnyro.recorder.obj

data class VideoResolution(
    val width: Int,
    val height: Int
) {
    companion object {
        val resolutions = listOf(
            VideoResolution(240, 360),
            VideoResolution(360, 480),
            VideoResolution(480, 720),
            VideoResolution(720, 1080),
            VideoResolution(1080, 1920)
        )
    }
}
