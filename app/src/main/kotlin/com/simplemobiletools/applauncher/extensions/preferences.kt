package com.simplemobiletools.applauncher.extensions

import android.content.Context
import android.content.SharedPreferences

val PREFS_KEY = "App Launcher"
val IS_FIRST_RUN = "is_first_run"

private val defaultInit: Any.() -> Unit = {}

fun Context.preferences(init: SharedPreferences.() -> Unit = defaultInit): SharedPreferences {
    val defaultPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    defaultPreferences.init()
    return defaultPreferences
}

var SharedPreferences.isFirstRun: Boolean
    set(isFirstRun: Boolean) {
        edit().putBoolean(IS_FIRST_RUN, isFirstRun).apply()
    }
    get() {
        return getBoolean(IS_FIRST_RUN, true)
    }
