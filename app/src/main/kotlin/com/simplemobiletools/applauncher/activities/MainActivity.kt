package com.simplemobiletools.applauncher.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.LaunchersAdapter
import com.simplemobiletools.applauncher.extensions.isFirstRun
import com.simplemobiletools.applauncher.extensions.preferences
import com.simplemobiletools.applauncher.models.AppLauncher
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fillGrid()
    }

    private fun fillGrid() {
        val apps = ArrayList<AppLauncher>()
        val pm = this.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val list = pm.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED)
        for (info in list) {
            val componentInfo = info.activityInfo.applicationInfo
            apps.add(AppLauncher(componentInfo.loadLabel(pm).toString(), componentInfo.loadIcon(pm), componentInfo.packageName))
        }

        apps.sortBy { it.name }
        launchers_holder.adapter = LaunchersAdapter(apps) {
            val launchIntent = packageManager.getLaunchIntentForPackage(it.pkgName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
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
