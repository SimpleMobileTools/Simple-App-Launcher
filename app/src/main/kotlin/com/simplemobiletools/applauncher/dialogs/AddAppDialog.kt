package com.simplemobiletools.applauncher.dialogs

import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.RecyclerAdapter
import com.simplemobiletools.applauncher.models.AppLauncher
import kotlinx.android.synthetic.main.launcher_picker.view.*
import java.util.*

class AddAppDialog() : DialogFragment() {
    val LAUNCHERS = "launchers"
    lateinit var launchers: ArrayList<AppLauncher>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.add_apps)

        val json = arguments.getString(LAUNCHERS)
        val listType = object : TypeToken<ArrayList<AppLauncher>>() {}.type
        launchers = Gson().fromJson(json, listType)

        val recyclerView = View.inflate(activity, R.layout.launcher_picker, null)
        recyclerView.launchers_holder.adapter = RecyclerAdapter(activity, true, launchers) {

        }

        builder.setView(recyclerView)

        builder.setPositiveButton(android.R.string.ok, { dialogInterface, i ->

        })

        builder.setNegativeButton(android.R.string.cancel, null)
        return builder.create()
    }
}
