package com.simplemobiletools.applauncher.extensions

import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.simplemobiletools.applauncher.R

fun Resources.getLauncherDrawable(packageName: String): Drawable {
    return getDrawable(when (packageName) {
        "com.simplemobiletools.calculator" -> R.drawable.ic_launcher_calculator
        "com.simplemobiletools.calendar" -> R.drawable.ic_launcher_calendar
        "com.simplemobiletools.camera" -> R.drawable.ic_launcher_camera
        "com.simplemobiletools.clock" -> R.drawable.ic_launcher_clock
        "com.simplemobiletools.contacts" -> R.drawable.ic_launcher_contacts
        "com.simplemobiletools.draw" -> R.drawable.ic_launcher_draw
        "com.simplemobiletools.filemanager" -> R.drawable.ic_launcher_filemanager
        "com.simplemobiletools.flashlight" -> R.drawable.ic_launcher_flashlight
        "com.simplemobiletools.gallery" -> R.drawable.ic_launcher_gallery
        "com.simplemobiletools.musicplayer" -> R.drawable.ic_launcher_musicplayer
        "com.simplemobiletools.notes" -> R.drawable.ic_launcher_notes
        "com.simplemobiletools.thankyou" -> R.drawable.ic_launcher_thankyou
        else -> throw RuntimeException("Invalid launcher package name $packageName")
    })
}
