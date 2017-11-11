package com.simplemobiletools.applauncher.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.simplemobiletools.applauncher.BuildConfig
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.RecyclerAdapter
import com.simplemobiletools.applauncher.dialogs.AddAppDialog
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.checkWhatsNew
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.LICENSE_KOTLIN
import com.simplemobiletools.commons.helpers.LICENSE_MULTISELECT
import com.simplemobiletools.commons.helpers.LICENSE_STETHO
import com.simplemobiletools.commons.models.Release
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : SimpleActivity(), RecyclerAdapter.AppLaunchersListener {
    private var launchers = ArrayList<AppLauncher>()
    private var remainingLaunchers = ArrayList<AppLauncher>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupLaunchers()
        checkWhatsNewDialog()

        fab.setOnClickListener {
            AddAppDialog(this, getNotDisplayedLaunchers()) {
                refreshLaunchers()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(coordinator_layout)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        updateMenuTextSize(resources, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> launchSettings()
            R.id.about -> launchAbout()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun launchSettings() {
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        startAboutActivity(R.string.app_name, LICENSE_KOTLIN or LICENSE_MULTISELECT or LICENSE_STETHO, BuildConfig.VERSION_NAME)
    }

    private fun setupLaunchers() {
        launchers = dbHelper.getLaunchers()
        checkInvalidApps()
        launchers_holder.adapter = RecyclerAdapter(this, launchers, this) {
            val launchIntent = packageManager.getLaunchIntentForPackage(it.pkgName)
            if (launchIntent != null) {
                startActivity(launchIntent)
                finish()
            } else {
                val url = "https://play.google.com/store/apps/details?id=${it.pkgName}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }

        remainingLaunchers = getNotDisplayedLaunchers()
    }

    private fun getNotDisplayedLaunchers(): ArrayList<AppLauncher> {
        val allApps = ArrayList<AppLauncher>()
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val list = packageManager.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED)
        for (info in list) {
            val componentInfo = info.activityInfo.applicationInfo
            val label = componentInfo.loadLabel(packageManager).toString()
            val pkgName = componentInfo.packageName
            allApps.add(AppLauncher(0, label, pkgName, 0))
        }

        val sorted = allApps.sortedWith(compareBy { it.name.toLowerCase() })
        val unique = sorted.distinctBy { it.pkgName }
        val filtered = unique.filter { !launchers.contains(it) }
        return filtered as ArrayList<AppLauncher>
    }

    private fun checkInvalidApps() {
        val invalidIds = ArrayList<String>()
        for ((id, name, pkgName) in launchers) {
            val launchIntent = packageManager.getLaunchIntentForPackage(pkgName)
            if (launchIntent == null && !pkgName.startsWith("com.simplemobiletools")) {
                invalidIds.add(id.toString())
            }
        }
        dbHelper.deleteLaunchers(invalidIds)
        launchers = launchers.filter { !invalidIds.contains(it.id.toString()) } as ArrayList<AppLauncher>
    }

    override fun refreshLaunchers() {
        setupLaunchers()
    }

    private fun checkWhatsNewDialog() {
        arrayListOf<Release>().apply {
            checkWhatsNew(this, BuildConfig.VERSION_CODE)
        }
    }
}
