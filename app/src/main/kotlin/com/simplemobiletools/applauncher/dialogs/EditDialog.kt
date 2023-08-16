package com.simplemobiletools.applauncher.dialogs

import android.app.Activity
import android.app.AlertDialog
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.databinding.DialogEditLauncherBinding
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.*

class EditDialog(val activity: Activity, val appLauncher: AppLauncher, val callback: () -> Unit) {

    init {
        val binding = DialogEditLauncherBinding.inflate(activity.layoutInflater)

        binding.editLauncherEdittext.setText(appLauncher.title)
        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.rename) { alertDialog ->
                    alertDialog.showKeyboard(binding.editLauncherEdittext)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val newName = binding.editLauncherEdittext.value
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
