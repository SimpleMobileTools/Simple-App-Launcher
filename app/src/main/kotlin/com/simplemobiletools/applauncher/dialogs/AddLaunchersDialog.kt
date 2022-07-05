package com.simplemobiletools.applauncher.dialogs

import android.app.Activity
import android.view.ViewGroup
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.AddLaunchersAdapter
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.areSystemAnimationsEnabled
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_add_launchers.view.*

class AddLaunchersDialog(
    val activity: Activity,
    val allLaunchers: ArrayList<AppLauncher>,
    val shownLaunchers: ArrayList<AppLauncher>,
    val callback: () -> Unit
) {
    private var view = (activity.layoutInflater.inflate(R.layout.dialog_add_launchers, null) as ViewGroup)
    private var adapter: AddLaunchersAdapter? = null

    init {
        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { dialogInterface, i -> confirmSelection() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this) {
                    adapter = AddLaunchersAdapter(activity, allLaunchers, shownLaunchers)
                    view.add_launchers_holder.adapter = adapter

                    if (activity.areSystemAnimationsEnabled) {
                        view.add_launchers_holder.scheduleLayoutAnimation()
                    }
                }
            }
    }

    private fun confirmSelection() {
        val selectedLaunchers = adapter?.getSelectedLaunchers() as ArrayList<AppLauncher> ?: return
        val selectedPackageNames = selectedLaunchers.map { it.packageName }
        val filtered = shownLaunchers.map { it.packageName }.filter { !selectedPackageNames.contains(it) }
        filtered.forEach {
            activity.dbHelper.deleteLauncher(it)
        }
        selectedLaunchers.forEach {
            activity.dbHelper.insertAppLauncher(it)
        }
        callback()
    }
}
