package com.simplemobiletools.applauncher.extensions

import android.content.Context
import com.simplemobiletools.applauncher.helpers.Config
import com.simplemobiletools.applauncher.helpers.DBHelper

val Context.config: Config get() = Config.newInstance(applicationContext)

val Context.dbHelper: DBHelper get() = DBHelper.newInstance(applicationContext)
