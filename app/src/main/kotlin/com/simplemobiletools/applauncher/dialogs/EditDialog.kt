package com.simplemobiletools.applauncher.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.view.ViewGroup
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_edit_launcher.view.*

class EditDialog(val activity: Activity, val appLauncher: AppLauncher, val callback: () -> Unit) {

    init {
        val view = (activity.layoutInflater.inflate(R.layout.dialog_edit_launcher, null) as ViewGroup)
        view.edit_launcher_edittext.setText(appLauncher.title)

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.rename) { alertDialog ->
                    alertDialog.showKeyboard(view.edit_launcher_edittext)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val newName = view.edit_launcher_edittext.value
                        if (!newName.isEmpty()) {
                            if (activity.dbHelper.updateLauncherName(appLauncher.id, newName)) {
                                callback()
                                alertDialog.dismiss()
                            } else {
                                activity.toast(R.string.unknown_error_occurred)
                            }
                        } else {
                            activity.toast(R.string.enter_launcher_name)
                        }
                    }
                }
            }
    }
}
