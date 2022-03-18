package com.simplemobiletools.applauncher.helpers

import android.content.Context
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.commons.helpers.BaseConfig

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    var wasRemoveInfoShown: Boolean
        get() = prefs.getBoolean(WAS_REMOVE_INFO_SHOWN, false)
        set(wasRemoveInfoShown) = prefs.edit().putBoolean(WAS_REMOVE_INFO_SHOWN, wasRemoveInfoShown).apply()

    var closeApp: Boolean
        get() = prefs.getBoolean(CLOSE_APP, true)
        set(closeApp) = prefs.edit().putBoolean(CLOSE_APP, closeApp).apply()

    var portraitColumnCnt: Int
        get() = prefs.getInt(PORTRAIT_COLUMN_COUNT, context.resources.getInteger(R.integer.portrait_column_count))
        set(portraitColumnCnt) = prefs.edit().putInt(PORTRAIT_COLUMN_COUNT, portraitColumnCnt).apply()

    var landscapeColumnCnt: Int
        get() = prefs.getInt(LANDSCAPE_COLUMN_COUNT, context.resources.getInteger(R.integer.landscape_column_count))
        set(landscapeColumnCnt) = prefs.edit().putInt(LANDSCAPE_COLUMN_COUNT, landscapeColumnCnt).apply()
}
