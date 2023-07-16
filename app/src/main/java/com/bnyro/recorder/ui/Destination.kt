package com.bnyro.recorder.ui

sealed class Destination(val route: String) {
    object Home : Destination("home")
    object Settings : Destination("settings")
    object RecordingPlayer : Destination("player")
}
