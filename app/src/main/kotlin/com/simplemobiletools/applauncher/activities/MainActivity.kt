package com.simplemobiletools.applauncher.activities

import android.content.Intent
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.simplemobiletools.applauncher.BuildConfig
import com.simplemobiletools.applauncher.LauncherAdapterUpdateListener
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.LaunchersAdapter
import com.simplemobiletools.applauncher.dialogs.AddLaunchersDialog
import com.simplemobiletools.applauncher.dialogs.ChangeSortingDialog
import com.simplemobiletools.applauncher.extensions.config
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.extensions.getAllLaunchers
import com.simplemobiletools.applauncher.extensions.isAPredefinedApp
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.models.FAQItem
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.commons.models.Release
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.simplemobiletools.commons.views.MyRecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : SimpleActivity(), LauncherAdapterUpdateListener {
    private val MAX_COLUMN_COUNT = 15

    private var launchersIgnoringSearch = ArrayList<AppLauncher>()
    private var allLaunchers: ArrayList<AppLauncher>? = null
    private var zoomListener: MyRecyclerView.MyZoomListener? = null

    private var mStoredPrimaryColor = 0
    private var mStoredTextColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appLaunched(BuildConfig.APPLICATION_ID)
        setupOptionsMenu()
        refreshMenuItems()

        updateMaterialActivityViews(main_coordinator, launchers_grid, useTransparentNavigation = true, useTopSearchMenu = true)

        setupEmptyView()
        setupLaunchers()
        checkWhatsNewDialog()
        storeStateVariables()
        setupGridLayoutManager()

        fab.setOnClickListener {
            fabClicked()
        }
    }

    override fun onResume() {
        super.onResume()
        updateMenuColors()
        if (mStoredTextColor != getProperTextColor()) {
            getGridAdapter()?.updateTextColor(getProperTextColor())
        }

        val properPrimaryColor = getProperPrimaryColor()
        if (mStoredPrimaryColor != properPrimaryColor) {
            getGridAdapter()?.apply {
                updatePrimaryColor()
                notifyDataSetChanged()
            }
        }

        updateTextColors(coordinator_layout)
        no_items_placeholder_2.setTextColor(properPrimaryColor)
        launchers_fastscroller.updateColors(properPrimaryColor)
        (fab.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = navigationBarHeight + resources.getDimension(R.dimen.activity_margin).toInt()
    }

    override fun onPause() {
        super.onPause()
        storeStateVariables()
    }

    private fun refreshMenuItems() {
        main_menu.getToolbar().menu.apply {
            findItem(R.id.more_apps_from_us).isVisible = !resources.getBoolean(R.bool.hide_google_relations)
        }
    }

    private fun setupOptionsMenu() {
        main_menu.getToolbar().inflateMenu(R.menu.menu)
        main_menu.toggleHideOnScroll(false)
        main_menu.setupMenu()

        main_menu.onSearchTextChangedListener = { text ->
            searchTextChanged(text)
        }

        main_menu.getToolbar().setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sort -> showSortingDialog()
                R.id.toggle_app_name -> toggleAppName()
                R.id.column_count -> changeColumnCount()
                R.id.more_apps_from_us -> launchMoreAppsFromUsIntent()
                R.id.settings -> launchSettings()
                R.id.about -> launchAbout()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun searchTextChanged(text: String) {
        val launchers = launchersIgnoringSearch.filter { it.title.contains(text, true) }.toMutableList() as ArrayList<AppLauncher>
        setupAdapter(launchers)
    }

    private fun updateMenuColors() {
        updateStatusbarColor(getProperBackgroundColor())
        main_menu.updateColors()
    }

    private fun getGridAdapter() = launchers_grid.adapter as? LaunchersAdapter

    private fun setupLaunchers() {
        launchersIgnoringSearch = dbHelper.getLaunchers()
        checkInvalidApps()
        initZoomListener()
        setupAdapter(launchersIgnoringSearch)
    }

    private fun setupAdapter(launchers: ArrayList<AppLauncher>) {
        AppLauncher.sorting = config.sorting
        launchers.sort()

        LaunchersAdapter(
            activity = this,
            launchers = launchers,
            listener = this,
            recyclerView = launchers_grid,
        ) {
            hideKeyboard()
            val launchIntent = packageManager.getLaunchIntentForPackage((it as AppLauncher).packageName)
            if (launchIntent != null) {
                try {
                    startActivity(launchIntent)
                    if (config.closeApp) {
                        finish()
                    }
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            } else {
                try {
                    launchViewIntent("market://details?id=${it.packageName}")
                } catch (ignored: Exception) {
                    launchViewIntent("https://play.google.com/store/apps/details?id=${it.packageName}")
                }
            }
        }.apply {
            setupZoomListener(zoomListener)
            launchers_grid.adapter = this
        }

        maybeShowEmptyView()
        ensureBackgroundThread {
            allLaunchers = getAllLaunchers()
        }
    }

    private fun showSortingDialog() {
        ChangeSortingDialog(this) {
            setupAdapter(launchersIgnoringSearch)
        }
    }

    private fun toggleAppName() {
        config.showAppName = !config.showAppName
        setupAdapter(launchersIgnoringSearch)
    }

    private fun changeColumnCount() {
        val items = ArrayList<RadioItem>()
        for (i in 1..MAX_COLUMN_COUNT) {
            items.add(RadioItem(i, resources.getQuantityString(R.plurals.column_counts, i, i)))
        }

        val currentColumnCount = (launchers_grid.layoutManager as MyGridLayoutManager).spanCount
        RadioGroupDialog(this, items, currentColumnCount) {
            val newColumnCount = it as Int
            if (currentColumnCount != newColumnCount) {
                (launchers_grid.layoutManager as MyGridLayoutManager).spanCount = newColumnCount
                if (portrait) {
                    config.portraitColumnCnt = newColumnCount
                } else {
                    config.landscapeColumnCnt = newColumnCount
                }
                columnCountChanged()
            }
        }
    }

    private fun increaseColumnCount() {
        val newColumnCount = ++(launchers_grid.layoutManager as MyGridLayoutManager).spanCount
        if (portrait) {
            config.portraitColumnCnt = newColumnCount
        } else {
            config.landscapeColumnCnt = newColumnCount
        }
        columnCountChanged()
    }

    private fun reduceColumnCount() {
        val newColumnCount = --(launchers_grid.layoutManager as MyGridLayoutManager).spanCount
        if (portrait) {
            config.portraitColumnCnt = newColumnCount
        } else {
            config.landscapeColumnCnt = newColumnCount
        }
        columnCountChanged()
    }

    private fun columnCountChanged() {
        refreshMenuItems()
        getGridAdapter()?.apply {
            calculateIconWidth()
            notifyItemRangeChanged(0, launchers.size)
        }
    }

    private fun setupGridLayoutManager() {
        val layoutManager = launchers_grid.layoutManager as MyGridLayoutManager
        if (portrait) {
            layoutManager.spanCount = config.portraitColumnCnt
        } else {
            layoutManager.spanCount = config.landscapeColumnCnt
        }
    }

    private fun initZoomListener() {
        val layoutManager = launchers_grid.layoutManager as MyGridLayoutManager
        zoomListener = object : MyRecyclerView.MyZoomListener {
            override fun zoomIn() {
                if (layoutManager.spanCount > 1) {
                    reduceColumnCount()
                    getGridAdapter()?.finishActMode()
                }
            }

            override fun zoomOut() {
                if (layoutManager.spanCount < MAX_COLUMN_COUNT) {
                    increaseColumnCount()
                    getGridAdapter()?.finishActMode()
                }
            }
        }
    }

    private fun checkInvalidApps() {
        val invalidIds = ArrayList<String>()
        for ((id, name, packageName) in launchersIgnoringSearch) {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent == null && !packageName.isAPredefinedApp()) {
                invalidIds.add(id.toString())
            }
        }

        dbHelper.deleteLaunchers(invalidIds)
        launchersIgnoringSearch = launchersIgnoringSearch.filter { !invalidIds.contains(it.id.toString()) } as ArrayList<AppLauncher>
    }

    private fun storeStateVariables() {
        mStoredPrimaryColor = getProperPrimaryColor()
        mStoredTextColor = getProperTextColor()
    }

    override fun refreshItems() {
        main_menu.closeSearch()
        setupLaunchers()
    }

    override fun refetchItems() {
        launchersIgnoringSearch = dbHelper.getLaunchers()
    }

    private fun checkWhatsNewDialog() {
        arrayListOf<Release>().apply {
            add(Release(7, R.string.release_7))
            checkWhatsNew(this, BuildConfig.VERSION_CODE)
        }
    }

    private fun fabClicked() {
        if (allLaunchers != null) {
            val shownLaunchers = launchersIgnoringSearch
            AddLaunchersDialog(this, allLaunchers!!, shownLaunchers) {
                setupLaunchers()
            }
        }
    }

    private fun setupEmptyView() {
        val properPrimaryColor = getProperPrimaryColor()
        no_items_placeholder_2.underlineText()
        no_items_placeholder_2.setTextColor(properPrimaryColor)
        no_items_placeholder_2.setOnClickListener {
            fabClicked()
        }
    }

    private fun maybeShowEmptyView() {
        if (getGridAdapter()?.launchers?.isEmpty() == true) {
            launchers_fastscroller.beGone()
            no_items_placeholder_2.beVisibleIf(main_menu.getCurrentQuery().isEmpty())
            no_items_placeholder.beVisible()
        } else {
            no_items_placeholder_2.beGone()
            no_items_placeholder.beGone()
            launchers_fastscroller.beVisible()
        }
    }

    private fun launchSettings() {
        hideKeyboard()
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        val licenses = 0L

        val faqItems = arrayListOf(
            FAQItem(R.string.faq_1_title, R.string.faq_1_text),
        )

        if (!resources.getBoolean(R.bool.hide_google_relations)) {
            faqItems.add(FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons))
            faqItems.add(FAQItem(R.string.faq_6_title_commons, R.string.faq_6_text_commons))
        }

        startAboutActivity(R.string.app_name, licenses, BuildConfig.VERSION_NAME, faqItems, false)
    }
}
