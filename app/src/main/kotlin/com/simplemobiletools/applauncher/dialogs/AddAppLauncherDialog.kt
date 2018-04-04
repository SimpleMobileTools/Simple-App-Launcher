package com.simplemobiletools.applauncher.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.LaunchersDialogAdapter
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_pick_launchers.view.*

class AddAppLauncherDialog(val activity: Activity, val notDisplayedLaunchers: ArrayList<AppLauncher>, val callback: () -> Unit) {
    private var view = (activity.layoutInflater.inflate(R.layout.dialog_pick_launchers, null) as ViewGroup)
    private var adapter: LaunchersDialogAdapter? = null

    init {
        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, { dialogInterface, i -> confirmSelection() })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this) {
                        adapter = LaunchersDialogAdapter(activity, notDisplayedLaunchers)
                        view.pick_launchers_holder.adapter = adapter
                    }
                }
    }

    private fun confirmSelection() {
        adapter?.getSelectedLaunchers()?.forEach {
            activity.dbHelper.insertAppLauncher(it)
        }
        callback()
    }
}
