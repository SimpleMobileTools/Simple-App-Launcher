package com.simplemobiletools.applauncher

import android.support.multidex.MultiDexApplication
import com.facebook.stetho.Stetho
import com.simplemobiletools.applauncher.extensions.config
import java.util.*

class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        if (config.useEnglish) {
            val conf = resources.configuration
            conf.locale = Locale.ENGLISH
            resources.updateConfiguration(conf, resources.displayMetrics)
        }

        Stetho.initializeWithDefaults(this)
    }
}
