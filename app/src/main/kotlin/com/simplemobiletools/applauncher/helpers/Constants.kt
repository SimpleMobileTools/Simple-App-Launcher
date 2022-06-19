package com.simplemobiletools.applauncher.helpers

import com.simplemobiletools.commons.helpers.isRPlus

const val WAS_REMOVE_INFO_SHOWN = "was_remove_info_shown"
const val CLOSE_APP = "close_app"
const val PORTRAIT_COLUMN_COUNT = "portrait_column_count"
const val LANDSCAPE_COLUMN_COUNT = "landscape_column_count"
const val SHOW_APP_NAME = "show_app_name"

fun getPredefinedPackageNames(): ArrayList<String> {
    val packages = arrayListOf(
        "com.simplemobiletools.calculator",
        "com.simplemobiletools.calendar.pro",
        "com.simplemobiletools.contacts.pro",
        "com.simplemobiletools.dialer",
        "com.simplemobiletools.draw.pro",
        "com.simplemobiletools.filemanager.pro",
        "com.simplemobiletools.flashlight",
        "com.simplemobiletools.gallery.pro",
        "com.simplemobiletools.keyboard",
        "com.simplemobiletools.musicplayer",
        "com.simplemobiletools.notes.pro",
        "com.simplemobiletools.smsmessenger",
        "com.simplemobiletools.thankyou",
        "com.simplemobiletools.voicerecorder"
    )

    if (isRPlus()) {
        packages.add(2, "com.simplemobiletools.clock")
    }

    return packages
}
