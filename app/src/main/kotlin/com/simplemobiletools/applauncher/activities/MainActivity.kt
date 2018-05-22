package com.simplemobiletools.applauncher.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.simplemobiletools.applauncher.BuildConfig
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.LaunchersAdapter
import com.simplemobiletools.applauncher.dialogs.AddAppLauncherDialog
import com.simplemobiletools.applauncher.extensions.config
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.extensions.getNotDisplayedLaunchers
import com.simplemobiletools.applauncher.extensions.isAPredefinedApp
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.appLaunched
import com.simplemobiletools.commons.extensions.checkWhatsNew
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.LICENSE_MULTISELECT
import com.simplemobiletools.commons.helpers.LICENSE_STETHO
import com.simplemobiletools.commons.interfaces.RefreshRecyclerViewListener
import com.simplemobiletools.commons.models.FAQItem
import com.simplemobiletools.commons.models.Release
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : SimpleActivity(), RefreshRecyclerViewListener {
    private var displayedLaunchers = ArrayList<AppLauncher>()
    private var notDisplayedLaunchers: ArrayList<AppLauncher>? = null
    private var mStoredPrimaryColor = 0
    private var mStoredTextColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appLaunched(BuildConfig.APPLICATION_ID)
        setupLaunchers()
        checkWhatsNewDialog()
        storeStateVariables()

        fab.setOnClickListener {
            if (notDisplayedLaunchers != null) {
                AddAppLauncherDialog(this, notDisplayedLaunchers!!) {
                    setupLaunchers()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mStoredTextColor != config.textColor) {
            getGridAdapter()?.updateTextColor(config.textColor)
        }

        if (mStoredPrimaryColor != config.primaryColor) {
            getGridAdapter()?.apply {
                updatePrimaryColor(config.primaryColor)
                notifyDataSetChanged()
            }
        }

        updateTextColors(coordinator_layout)

        launchers_fastscroller.apply {
            updatePrimaryColor()
            updateBubbleColors()
            allowBubbleDisplay = config.showInfoBubble
        }
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
        val faqItems = arrayListOf(
                FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons)
        )

        startAboutActivity(R.string.app_name, LICENSE_MULTISELECT or LICENSE_STETHO, BuildConfig.VERSION_NAME, faqItems)
    }

    private fun getGridAdapter() = launchers_grid.adapter as? LaunchersAdapter

    private fun setupLaunchers() {
        displayedLaunchers = dbHelper.getLaunchers()
        checkInvalidApps()
        val adapter = LaunchersAdapter(this, displayedLaunchers, this, launchers_grid, launchers_fastscroller) {
            val launchIntent = packageManager.getLaunchIntentForPackage((it as AppLauncher).packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
                finish()
            } else {
                val url = "https://play.google.com/store/apps/details?id=${it.packageName}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }
        launchers_grid.adapter = adapter

        launchers_fastscroller.allowBubbleDisplay = config.showInfoBubble
        launchers_fastscroller.setViews(launchers_grid) {
            launchers_fastscroller.updateBubbleText(displayedLaunchers.getOrNull(it)?.getBubbleText() ?: "")
        }

        fillNotDisplayedLaunchers()
    }

    private fun fillNotDisplayedLaunchers() {
        Thread {
            notDisplayedLaunchers = getNotDisplayedLaunchers(displayedLaunchers)
        }.start()
    }

    private fun checkInvalidApps() {
        val invalidIds = ArrayList<String>()
        for ((id, name, packageName) in displayedLaunchers) {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent == null && !packageName.isAPredefinedApp()) {
                invalidIds.add(id.toString())
            }
        }
        dbHelper.deleteLaunchers(invalidIds)
        displayedLaunchers = displayedLaunchers.filter { !invalidIds.contains(it.id.toString()) } as ArrayList<AppLauncher>
    }

    private fun storeStateVariables() {
        config.apply {
            mStoredPrimaryColor = primaryColor
            mStoredTextColor = textColor
        }
    }

    override fun refreshItems() {
        setupLaunchers()
    }

    private fun checkWhatsNewDialog() {
        arrayListOf<Release>().apply {
            add(Release(7, R.string.release_7))
            checkWhatsNew(this, BuildConfig.VERSION_CODE)
        }
    }
}
