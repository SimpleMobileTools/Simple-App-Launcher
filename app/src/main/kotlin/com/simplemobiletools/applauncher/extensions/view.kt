package com.simplemobiletools.applauncher.extensions

import android.view.View

val View.isVisible: Boolean get() = visibility == View.VISIBLE

fun View.hide() {
    visibility = View.INVISIBLE
}

fun View.show() {
    visibility = View.VISIBLE
}
