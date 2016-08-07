package com.simplemobiletools.applauncher.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.extensions.isDarkTheme
import com.simplemobiletools.applauncher.extensions.preferences

open class SimpleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(if (preferences.isDarkTheme) R.style.AppTheme_Dark else R.style.AppTheme)
        super.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
