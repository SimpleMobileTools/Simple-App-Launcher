package com.simplemobiletools.applauncher.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showKeyboard
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.value
import kotlinx.android.synthetic.main.dialog_edit_launcher.view.*

class EditDialog(val activity: Activity, val appLauncher: AppLauncher, val callback: () -> Unit) {
    var dialog: AlertDialog
    var view = (activity.layoutInflater.inflate(R.layout.dialog_edit_launcher, null) as ViewGroup)

    init {
        view.edit_launcher_edittext.setText(appLauncher.name)

        dialog = AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.rename) {
                        showKeyboard(view.edit_launcher_edittext)
                        getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            val newName = view.edit_launcher_edittext.value
                            if (!newName.isEmpty()) {
                                if (activity.dbHelper.updateLauncherName(appLauncher.id, newName)) {
                                    callback()
                                    dismiss()
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
