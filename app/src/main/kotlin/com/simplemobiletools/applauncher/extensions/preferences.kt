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

val SharedPreferences.isFirstRun: Boolean get() = getBoolean(IS_FIRST_RUN, true)

fun SharedPreferences.isFirstRun(isFirstRun: Boolean) {
    edit().putBoolean(IS_FIRST_RUN, isFirstRun).apply()
}
