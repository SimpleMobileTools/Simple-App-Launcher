package com.simplemobiletools.applauncher.dialogs

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.LaunchersDialogAdapter
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.extensions.getLauncherDrawable
import com.simplemobiletools.applauncher.extensions.isAPredefinedApp
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_pick_launchers.view.*
import java.util.*

class AddAppLauncherDialog(val activity: Activity, val displayedLaunchers: ArrayList<AppLauncher>, val callback: () -> Unit) {
    private var dialog: AlertDialog
    private var view = (activity.layoutInflater.inflate(R.layout.dialog_pick_launchers, null) as ViewGroup)
    private var adapter: LaunchersDialogAdapter? = null

    init {
        dialog = AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, { dialogInterface, i -> confirmSelection() })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            activity.setupDialogStuff(view, this) {
                Thread {
                    adapter = LaunchersDialogAdapter(activity, getNotDisplayedLaunchers())
                    activity.runOnUiThread {
                        view.pick_launchers_holder.adapter = adapter
                    }
                }.start()
            }
        }
    }

    private fun confirmSelection() {
        adapter?.getSelectedLaunchers()?.forEach {
            activity.dbHelper.insertAppLauncher(it)
        }
        callback()
        dialog.dismiss()
    }

    private fun getNotDisplayedLaunchers(): ArrayList<AppLauncher> {
        val resources = activity.resources
        val packageManager = activity.packageManager
        val allApps = ArrayList<AppLauncher>()
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val list = packageManager.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED)
        for (info in list) {
            val componentInfo = info.activityInfo.applicationInfo
            val label = componentInfo.loadLabel(packageManager).toString()
            val packageName = componentInfo.packageName

            val drawable = if (packageName.isAPredefinedApp()) {
                resources.getLauncherDrawable(packageName)
            } else {
                packageManager.getApplicationIcon(packageName)
            }

            allApps.add(AppLauncher(0, label, componentInfo.packageName, drawable))
        }

        val sorted = allApps.sortedWith(compareBy { it.name.toLowerCase() })
        val unique = sorted.distinctBy { it.packageName }
        val filtered = unique.filter { !displayedLaunchers.contains(it) && it.packageName != "com.simplemobiletools.applauncher" }
        return filtered as ArrayList<AppLauncher>
    }
}
