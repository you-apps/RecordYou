package com.bnyro.recorder.enums

import com.bnyro.recorder.util.Preferences

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun getCurrent() = valueOf(Preferences.getString(Preferences.themeModeKey, SYSTEM.name))
    }
}
