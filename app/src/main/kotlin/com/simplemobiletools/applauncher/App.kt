package com.simplemobiletools.applauncher

import androidx.multidex.MultiDexApplication
import com.simplemobiletools.commons.extensions.checkUseEnglish

class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        checkUseEnglish()
    }
}
