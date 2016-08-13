package com.simplemobiletools.applauncher.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import com.simplemobiletools.applauncher.R

class AddAppDialog() : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.add_apps)
        builder.setPositiveButton(android.R.string.ok, { dialogInterface, i ->

        })

        builder.setNegativeButton(android.R.string.cancel, null)
        return builder.create()
    }
}
