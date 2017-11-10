package com.simplemobiletools.applauncher.extensions

import android.content.Context
import com.simplemobiletools.applauncher.helpers.Config

val Context.config: Config get() = Config.newInstance(applicationContext)
