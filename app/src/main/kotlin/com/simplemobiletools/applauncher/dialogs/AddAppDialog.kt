package com.simplemobiletools.applauncher.dialogs

import android.app.Dialog
import android.app.DialogFragment
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.RecyclerAdapter
import com.simplemobiletools.applauncher.models.AppLauncher
import kotlinx.android.synthetic.main.launcher_picker.view.*
import java.util.*

class AddAppDialog() : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.add_apps)

        val recyclerView = View.inflate(activity, R.layout.launcher_picker, null)
        fillGrid(recyclerView)
        builder.setView(recyclerView)

        builder.setPositiveButton(android.R.string.ok, { dialogInterface, i ->

        })

        builder.setNegativeButton(android.R.string.cancel, null)
        return builder.create()
    }

    private fun fillGrid(recyclerView: View) {
        val apps = ArrayList<AppLauncher>()
        val packageManager = activity.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val list = packageManager.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED)
        for (info in list) {
            val componentInfo = info.activityInfo.applicationInfo
            apps.add(AppLauncher(componentInfo.loadLabel(packageManager).toString(), componentInfo.packageName, 0, componentInfo.loadIcon(packageManager)))
        }

        apps.sortBy { it.name }
        recyclerView.launchers_holder.adapter = RecyclerAdapter(apps) {

        }
    }
}
