package com.simplemobiletools.applauncher.dialogs

import android.app.Activity
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.adapters.AddLaunchersAdapter
import com.simplemobiletools.applauncher.databinding.DialogAddLaunchersBinding
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.areSystemAnimationsEnabled
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff

class AddLaunchersDialog(
    val activity: Activity,
    val allLaunchers: ArrayList<AppLauncher>,
    val shownLaunchers: ArrayList<AppLauncher>,
    val callback: () -> Unit
) {
    private var adapter: AddLaunchersAdapter? = null

    init {
        val binding = DialogAddLaunchersBinding.inflate(activity.layoutInflater)

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { dialogInterface, i -> confirmSelection() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) {
                    adapter = AddLaunchersAdapter(activity, allLaunchers, shownLaunchers)
                    binding.addLaunchersHolder.adapter = adapter

                    if (activity.areSystemAnimationsEnabled) {
                        binding.addLaunchersHolder.scheduleLayoutAnimation()
                    }
                }
            }
    }

    private fun confirmSelection() {
        val selectedLaunchers = adapter?.getSelectedLaunchers() as ArrayList<AppLauncher> ?: return
        val selectedPackageNames = selectedLaunchers.map { it.packageName }
        val filtered = shownLaunchers.map { it.packageName }.filter { !selectedPackageNames.contains(it) }
        filtered.forEach {
            activity.dbHelper.deleteLauncher(it)
        }
        selectedLaunchers.forEach {
            activity.dbHelper.insertAppLauncher(it)
        }
        callback()
    }
}
