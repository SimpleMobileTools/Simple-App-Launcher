package com.simplemobiletools.applauncher.dialogs

import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.RecyclerAdapter
import com.simplemobiletools.applauncher.models.AppLauncher
import kotlinx.android.synthetic.main.launcher_picker.view.*
import java.util.*

class AddAppDialog() : DialogFragment() {
    companion object {
        lateinit var launchers: ArrayList<AppLauncher>
        var callback: AddLaunchersInterface? = null
        fun newInstance(cb: AddLaunchersInterface, appLaunchers: ArrayList<AppLauncher>): AddAppDialog {
            callback = cb
            launchers = appLaunchers
            return AddAppDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.add_apps)

        val recyclerView = View.inflate(activity, R.layout.launcher_picker, null)
        recyclerView.launchers_holder.adapter = RecyclerAdapter(activity, true, launchers) {

        }

        builder.setView(recyclerView)

        builder.setPositiveButton(android.R.string.ok, { dialogInterface, i ->
            val selectedApps = launchers.filter { it.isChecked } as ArrayList<AppLauncher>
            callback?.selectedLaunchers(selectedApps)
        })

        builder.setNegativeButton(android.R.string.cancel, null)
        return builder.create()
    }

    interface AddLaunchersInterface {
        fun selectedLaunchers(launchers: ArrayList<AppLauncher>)
    }
}
