package com.simplemobiletools.applauncher.extensions

import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.simplemobiletools.applauncher.R

fun Resources.getLauncherDrawable(packageName: String): Drawable {
    return getDrawable(when (packageName) {
        "com.simplemobiletools.calculator" -> R.mipmap.ic_calculator
        "com.simplemobiletools.calendar.pro" -> R.mipmap.ic_calendar
        "com.simplemobiletools.camera" -> R.mipmap.ic_camera
        "com.simplemobiletools.clock" -> R.mipmap.ic_clock
        "com.simplemobiletools.contacts.pro" -> R.mipmap.ic_contacts
        "com.simplemobiletools.dialer" -> R.mipmap.ic_dialer
        "com.simplemobiletools.draw.pro" -> R.mipmap.ic_draw
        "com.simplemobiletools.filemanager.pro" -> R.mipmap.ic_file_manager
        "com.simplemobiletools.flashlight" -> R.mipmap.ic_flashlight
        "com.simplemobiletools.gallery.pro" -> R.mipmap.ic_gallery
        "com.simplemobiletools.keyboard" -> R.mipmap.ic_keyboard
        "com.simplemobiletools.launcher" -> R.mipmap.ic_simple_launcher
        "com.simplemobiletools.musicplayer" -> R.mipmap.ic_music_player
        "com.simplemobiletools.notes.pro" -> R.mipmap.ic_notes
        "com.simplemobiletools.smsmessenger" -> R.mipmap.ic_sms_messenger
        "com.simplemobiletools.thankyou" -> R.mipmap.ic_thank_you
        "com.simplemobiletools.voicerecorder" -> R.mipmap.ic_voice_recorder
        else -> throw RuntimeException("Invalid launcher package name $packageName")
    })
}
