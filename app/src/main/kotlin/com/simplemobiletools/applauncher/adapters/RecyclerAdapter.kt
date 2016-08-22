package com.simplemobiletools.applauncher.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.RecyclerView
import android.view.*
import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback
import com.bignerdranch.android.multiselector.MultiSelector
import com.bignerdranch.android.multiselector.SwappingHolder
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.databases.DbHelper
import com.simplemobiletools.applauncher.extensions.hide
import com.simplemobiletools.applauncher.extensions.show
import com.simplemobiletools.applauncher.extensions.toast
import com.simplemobiletools.applauncher.models.AppLauncher
import kotlinx.android.synthetic.main.app_launcher_dialog_item.view.*
import kotlinx.android.synthetic.main.edit_launcher.view.*
import java.util.*

class RecyclerAdapter(val act: Activity, val launchers: List<AppLauncher>, val itemClick: (AppLauncher) -> Unit) :
        RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    val multiSelector = MultiSelector()

    companion object {
        var actMode: ActionMode? = null
    }

    val deleteMode = object : ModalMultiSelectorCallback(multiSelector) {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.cab_edit -> {
                    showEditDialog()
                    return true
                }
                R.id.cab_delete -> {
                    deleteSelectedItems()
                    return true
                }
            }
            return false
        }

        override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
            super.onCreateActionMode(actionMode, menu)
            actMode = actionMode
            act.menuInflater.inflate(R.menu.cab, menu)
            return true
        }

        override fun onPrepareActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
            val menuItem = menu?.findItem(R.id.cab_edit)
            menuItem?.isVisible = multiSelector.selectedPositions.size == 1
            return true
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(act, deleteMode, multiSelector, launchers[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.app_launcher_item, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun getItemCount(): Int {
        return launchers.count()
    }

    private fun showEditDialog() {
        val selectedLauncher = launchers[multiSelector.selectedPositions[0]]
        val editView = act.layoutInflater.inflate(R.layout.edit_launcher, null)
        editView.edit_launcher_edittext.setText(selectedLauncher.name)

        val builder = AlertDialog.Builder(act)
        builder.setTitle(act.getString(R.string.rename_launcher))
        builder.setView(editView)

        builder.setPositiveButton(R.string.ok, null)
        builder.setNegativeButton(R.string.cancel, null)

        val alertDialog = builder.create()
        alertDialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val newName = editView.edit_launcher_edittext.text.toString().trim()
            if (!newName.isEmpty()) {
                if (DbHelper(act).updateLauncherName(selectedLauncher.id, newName) > 0) {
                    (act as EditLaunchersInterface).launcherRenamed()
                    finishActionMode()
                    alertDialog.dismiss()
                } else {
                    act.toast(R.string.unknown_error)
                }
            } else {
                act.toast(R.string.enter_launcher_name)
            }
        }
    }

    fun finishActionMode() {
        actMode?.finish()
    }

    private fun deleteSelectedItems() {
        val positions = multiSelector.selectedPositions
        val deleteIds = ArrayList<String>(positions.size)
        val deletedLaunchers = ArrayList<AppLauncher>(positions.size)
        for (i in positions) {
            val launcher = launchers[i]
            deleteIds.add(launcher.id.toString())

            launcher.name = getRealAppName(launcher)
            if (launcher.name.isNotEmpty())
                deletedLaunchers.add(launcher)
        }
        DbHelper(act).deleteLaunchers(deleteIds)
        finishActionMode()
        (act as EditLaunchersInterface).launchersDeleted(positions, deletedLaunchers)
    }

    private fun getRealAppName(launcher: AppLauncher): String {
        try {
            val applicationInfo = act.packageManager.getApplicationInfo(launcher.pkgName, 0)
            return act.packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            return ""
        }
    }

    class ViewHolder(view: View, val itemClick: (AppLauncher) -> (Unit)) : SwappingHolder(view, MultiSelector()) {
        val viewHolder = this

        fun bindView(act: Activity, deleteMode: ModalMultiSelectorCallback, multiSelector: MultiSelector, launcher: AppLauncher) {
            with(launcher) {
                itemView.launcher_label.text = launcher.name
                itemView.setOnClickListener {
                    viewClicked(multiSelector, launcher)
                }

                itemView.setOnLongClickListener {
                    if (!multiSelector.isSelectable) {
                        (act as AppCompatActivity).startSupportActionMode(deleteMode)
                        multiSelector.setSelected(viewHolder, true)
                        actMode?.title = multiSelector.selectedPositions.size.toString()
                        itemView.launcher_check.show()
                    }
                    true
                }

                if (launcher.iconId != 0) {
                    val icon = act.resources.getDrawable(launcher.iconId)
                    itemView.launcher_icon.setImageDrawable(icon)
                } else {
                    val icon = act.packageManager.getApplicationIcon(launcher.pkgName)
                    itemView.launcher_icon.setImageDrawable(icon)
                }
            }
        }

        fun viewClicked(multiSelector: MultiSelector, appLauncher: AppLauncher) {
            if (multiSelector.isSelectable) {
                val isSelected = multiSelector.selectedPositions.contains(viewHolder.layoutPosition)
                multiSelector.setSelected(viewHolder, !isSelected)
                if (isSelected) {
                    itemView.launcher_check.hide()
                } else {
                    itemView.launcher_check.show()
                }

                val selectedCnt = multiSelector.selectedPositions.size
                if (selectedCnt == 0) {
                    actMode?.finish()
                } else {
                    actMode?.title = selectedCnt.toString()
                }
                actMode?.invalidate()
            } else {
                itemClick(appLauncher)
            }
        }
    }

    interface EditLaunchersInterface {
        fun launchersDeleted(indexes: List<Int>, deletedLaunchers: List<AppLauncher>)

        fun launcherRenamed()
    }
}
