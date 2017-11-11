package com.simplemobiletools.applauncher.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.RecyclerDialogAdapter
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_pick_launchers.view.*
import java.util.*

class AddAppDialog(val activity: Activity, val availableLaunchers: ArrayList<AppLauncher>, val callback: () -> Unit) {
    var dialog: AlertDialog
    var view = (activity.layoutInflater.inflate(R.layout.dialog_pick_launchers, null) as ViewGroup)

    init {
        dialog = AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, { dialogInterface, i -> confirmSelection() })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            activity.setupDialogStuff(view, this)
            view.pick_launchers_holder.adapter = RecyclerDialogAdapter(activity, availableLaunchers)
        }
    }

    private fun confirmSelection() {
        //val selectedApps = availableLaunchers.filter { it.isChecked } as ArrayList<AppLauncher>
        callback()
    }
}
