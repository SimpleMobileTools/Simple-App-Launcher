package com.simplemobiletools.applauncher.dialogs

import android.app.Activity
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.*

class EditDialog(val activity: Activity, val appLauncher: AppLauncher, val callback: () -> Unit) {

    init {
        val layoutId = if (activity.baseConfig.isUsingSystemTheme) {
            R.layout.dialog_edit_launcher_material
        } else {
            R.layout.dialog_edit_launcher
        }

        val view = (activity.layoutInflater.inflate(layoutId, null) as ViewGroup)
        view.findViewById<AppCompatEditText>(R.id.edit_launcher_edittext).setText(appLauncher.title)

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.rename) { alertDialog ->
                    alertDialog.showKeyboard(view.findViewById(R.id.edit_launcher_edittext))
                    alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val newName = view.findViewById<AppCompatEditText>(R.id.edit_launcher_edittext).value
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
