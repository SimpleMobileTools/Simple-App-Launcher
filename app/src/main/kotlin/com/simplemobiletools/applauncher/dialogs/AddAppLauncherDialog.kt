package com.simplemobiletools.applauncher.dialogs

import android.app.Activity
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.LaunchersDialogAdapter
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.areSystemAnimationsEnabled
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_pick_launchers.view.*

class AddAppLauncherDialog(
    val activity: Activity,
    val allLaunchers: ArrayList<AppLauncher>,
    val shownLaunchers: ArrayList<AppLauncher>,
    val callback: () -> Unit
) {
    private var view = (activity.layoutInflater.inflate(R.layout.dialog_pick_launchers, null) as ViewGroup)
    private var adapter: LaunchersDialogAdapter? = null

    init {
        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok) { dialogInterface, i -> confirmSelection() }
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this) {
                    adapter = LaunchersDialogAdapter(activity, allLaunchers, shownLaunchers)
                    view.pick_launchers_holder.adapter = adapter

                    if (activity.areSystemAnimationsEnabled) {
                        view.pick_launchers_holder.scheduleLayoutAnimation()
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
