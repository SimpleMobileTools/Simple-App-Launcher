package com.simplemobiletools.applauncher.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.simplemobiletools.applauncher.BuildConfig
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.RecyclerAdapter
import com.simplemobiletools.applauncher.dialogs.AddAppLauncherDialog
import com.simplemobiletools.applauncher.extensions.config
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.extensions.isAPredefinedApp
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.checkWhatsNew
import com.simplemobiletools.commons.extensions.restartActivity
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.LICENSE_KOTLIN
import com.simplemobiletools.commons.helpers.LICENSE_MULTISELECT
import com.simplemobiletools.commons.helpers.LICENSE_STETHO
import com.simplemobiletools.commons.models.Release
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : SimpleActivity(), RecyclerAdapter.AppLaunchersListener {
    private var launchers = ArrayList<AppLauncher>()
    private var mStoredPrimaryColor = 0
    private var mStoredTextColor = 0
    private var mStoredUseEnglish = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupLaunchers()
        checkWhatsNewDialog()
        setupGridLayoutManager()
        storeStateVariables()

        fab.setOnClickListener {
            AddAppLauncherDialog(this, launchers) {
                refreshLaunchers()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mStoredUseEnglish != config.useEnglish) {
            restartActivity()
            return
        }

        if (mStoredTextColor != config.textColor) {
            getGridAdapter()?.updateTextColor(config.textColor)
        }

        if (mStoredPrimaryColor != config.primaryColor) {
            getGridAdapter()?.updatePrimaryColor(config.primaryColor)
        }

        updateTextColors(coordinator_layout)
    }

    override fun onPause() {
        super.onPause()
        storeStateVariables()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
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

    private fun getGridAdapter() = launchers_grid.adapter as? RecyclerAdapter

    private fun setupGridLayoutManager() {
        /*launchers_grid.isDragSelectionEnabled = true
        launchers_grid.listener = object : MyScalableRecyclerView.MyScalableRecyclerViewListener {
            override fun zoomIn() {}

            override fun zoomOut() {}

            override fun selectItem(position: Int) {
                getGridAdapter()?.selectItem(position)
            }

            override fun selectRange(initialSelection: Int, lastDraggedIndex: Int, minReached: Int, maxReached: Int) {
                getGridAdapter()?.selectRange(initialSelection, lastDraggedIndex, minReached, maxReached)
            }
        }*/
    }

    private fun setupLaunchers() {
        launchers = dbHelper.getLaunchers()
        checkInvalidApps()
        launchers_grid.adapter = RecyclerAdapter(this, launchers, this) {
            val launchIntent = packageManager.getLaunchIntentForPackage(it.packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
                finish()
            } else {
                val url = "https://play.google.com/store/apps/details?id=${it.packageName}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }
    }

    private fun checkInvalidApps() {
        val invalidIds = ArrayList<String>()
        for ((id, name, packageName) in launchers) {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent == null && !packageName.isAPredefinedApp()) {
                invalidIds.add(id.toString())
            }
        }
        dbHelper.deleteLaunchers(invalidIds)
        launchers = launchers.filter { !invalidIds.contains(it.id.toString()) } as ArrayList<AppLauncher>
    }

    private fun storeStateVariables() {
        config.apply {
            mStoredPrimaryColor = primaryColor
            mStoredTextColor = textColor
            mStoredUseEnglish = useEnglish
        }
    }

    override fun refreshLaunchers() {
        setupLaunchers()
    }

    override fun itemLongClicked(position: Int) {
        //launchers_grid.setDragSelectActive(position)
    }

    private fun checkWhatsNewDialog() {
        arrayListOf<Release>().apply {
            add(Release(7, R.string.release_7))
            checkWhatsNew(this, BuildConfig.VERSION_CODE)
        }
    }
}
