package com.bnyro.recorder.ui

interface Destination {
    val route: String
}

object Home : Destination {
    override val route = "home"
}