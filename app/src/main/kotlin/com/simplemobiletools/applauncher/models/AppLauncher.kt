package com.simplemobiletools.applauncher.models

import android.graphics.drawable.Drawable

data class AppLauncher(val id: Int, var name: String, val packageName: String, val drawable: Drawable? = null) {
    override fun equals(other: Any?) = packageName.equals((other as AppLauncher).packageName, true)

    fun getBubbleText() = name
}
