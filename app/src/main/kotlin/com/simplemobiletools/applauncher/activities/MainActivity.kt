package com.simplemobiletools.applauncher.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.MyCursorAdapter
import com.simplemobiletools.applauncher.databases.DbHelper
import com.simplemobiletools.applauncher.dialogs.AddAppDialog
import com.simplemobiletools.applauncher.extensions.isFirstRun
import com.simplemobiletools.applauncher.extensions.preferences
import com.simplemobiletools.applauncher.extensions.viewIntent
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : SimpleActivity() {
    lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbHelper = DbHelper(applicationContext)
        launchers_holder.adapter = MyCursorAdapter(applicationContext, dbHelper.getLaunchers()) {
            val launchIntent = packageManager.getLaunchIntentForPackage(it.pkgName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                startActivity(viewIntent("https://play.google.com/store/apps/details?id=" + it.pkgName))
            }
        }

        fab.setOnClickListener {
            AddAppDialog().show(fragmentManager, "")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.settings -> {
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
                return true
            }
            R.id.about -> {
                startActivity(Intent(applicationContext, AboutActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        preferences.isFirstRun = false
    }
}
