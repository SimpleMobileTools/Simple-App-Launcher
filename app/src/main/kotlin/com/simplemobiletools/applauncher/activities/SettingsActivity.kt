package com.simplemobiletools.applauncher.activities

import android.os.Bundle
import android.support.v4.app.TaskStackBuilder
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.extensions.isDarkTheme
import com.simplemobiletools.applauncher.extensions.preferences
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupDarkTheme()
    }

    private fun setupDarkTheme() {
        settings_dark_theme.isChecked = preferences.isDarkTheme

        settings_dark_theme_holder.setOnClickListener {
            settings_dark_theme.toggle()
            preferences.isDarkTheme = settings_dark_theme.isChecked
            restartActivity()
        }
    }

    private fun restartActivity() {
        TaskStackBuilder.create(applicationContext).addNextIntentWithParentStack(intent).startActivities()
    }
}
