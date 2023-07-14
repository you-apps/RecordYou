package com.bnyro.recorder.ui

interface Destination {
    val route: String
}

object Home : Destination {
    override val route = "home"
}

object Settings : Destination {
    override val route = "settings"
}

object RecordingPlayer : Destination {
    override val route = "player"
}