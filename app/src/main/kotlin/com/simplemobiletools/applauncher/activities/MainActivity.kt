package com.simplemobiletools.applauncher.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.RecyclerAdapter
import com.simplemobiletools.applauncher.databases.DbHelper
import com.simplemobiletools.applauncher.dialogs.AddAppDialog
import com.simplemobiletools.applauncher.extensions.isFirstRun
import com.simplemobiletools.applauncher.extensions.preferences
import com.simplemobiletools.applauncher.extensions.viewIntent
import com.simplemobiletools.applauncher.models.AppLauncher
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.comparisons.compareBy

class MainActivity : SimpleActivity(), AddAppDialog.AddLaunchersInterface {
    lateinit var dbHelper: DbHelper
    lateinit var launchers: ArrayList<AppLauncher>
    lateinit var remainingLaunchers: ArrayList<AppLauncher>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbHelper = DbHelper(applicationContext)
        setupLaunchers()

        fab.setOnClickListener {
            AddAppDialog.newInstance(this, remainingLaunchers).show(fragmentManager, "")
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

    private fun setupLaunchers() {
        launchers = dbHelper.getLaunchers()
        launchers_holder.adapter = RecyclerAdapter(this, launchers) {
            val launchIntent = packageManager.getLaunchIntentForPackage(it.pkgName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                startActivity(viewIntent("https://play.google.com/store/apps/details?id=" + it.pkgName))
            }
        }

        remainingLaunchers = getNotDisplayedLaunchers()
    }

    private fun getNotDisplayedLaunchers(): ArrayList<AppLauncher> {
        val apps = ArrayList<AppLauncher>()
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val list = packageManager.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED)
        for (info in list) {
            val componentInfo = info.activityInfo.applicationInfo
            val label = componentInfo.loadLabel(packageManager).toString()
            val pkgName = componentInfo.packageName
            apps.add(AppLauncher(label, pkgName, 0))
        }

        val sorted = apps.sortedWith(compareBy { it.name.toLowerCase() })
        val unique = sorted.distinctBy { it.pkgName }
        val filtered = unique.filter { !launchers.contains(it) }
        return filtered as ArrayList<AppLauncher>
    }

    override fun selectedLaunchers(launchers: ArrayList<AppLauncher>) {
        for ((name, pkgName) in launchers) {
            dbHelper.addLauncher(name, pkgName)
        }
        setupLaunchers()
    }

    override fun onDestroy() {
        super.onDestroy()
        preferences.isFirstRun = false
    }
}
