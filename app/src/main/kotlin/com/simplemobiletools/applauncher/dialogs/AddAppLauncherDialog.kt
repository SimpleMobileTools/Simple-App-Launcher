package com.simplemobiletools.applauncher.dialogs

import android.app.Activity
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.LaunchersDialogAdapter
import com.simplemobiletools.applauncher.extensions.config
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.views.MyGridLayoutManager
import kotlinx.android.synthetic.main.dialog_pick_launchers.view.*

class AddAppLauncherDialog(val activity: Activity, val notDisplayedLaunchers: ArrayList<AppLauncher>, val callback: () -> Unit) {
    private var view = (activity.layoutInflater.inflate(R.layout.dialog_pick_launchers, null) as ViewGroup)
    private var adapter: LaunchersDialogAdapter? = null

    init {
        (view.pick_launchers_holder.layoutManager as MyGridLayoutManager).spanCount = activity.config.columnCnt

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok) { dialogInterface, i -> confirmSelection() }
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
