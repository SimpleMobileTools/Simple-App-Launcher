package com.simplemobiletools.applauncher.adapters

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.content.res.Resources
import android.support.v7.view.ActionMode
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.*
import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback
import com.bignerdranch.android.multiselector.MultiSelector
import com.bignerdranch.android.multiselector.SwappingHolder
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.activities.SimpleActivity
import com.simplemobiletools.applauncher.extensions.config
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.extensions.getLauncherDrawable
import com.simplemobiletools.applauncher.extensions.isAPredefinedApp
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.interfaces.MyAdapterListener
import kotlinx.android.synthetic.main.app_launcher_item.view.*
import kotlinx.android.synthetic.main.dialog_edit_launcher.view.*
import java.util.*

class RecyclerAdapter(val activity: SimpleActivity, val launchers: List<AppLauncher>, val listener: AppLaunchersListener?, val itemClick: (AppLauncher) -> Unit) :
        RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private val config = activity.config
    private var actMode: ActionMode? = null
    private var primaryColor = config.primaryColor

    private val multiSelector = MultiSelector()
    private var itemViews = SparseArray<View>()
    private val selectedPositions = HashSet<Int>()
    private var textColor = config.textColor
    private var resources = activity.resources
    private var packageManager = activity.packageManager

    fun toggleItemSelection(select: Boolean, pos: Int) {
        if (select) {
            if (itemViews[pos] != null) {
                itemViews[pos].launcher_check?.background?.applyColorFilter(primaryColor)
                selectedPositions.add(pos)
            }
        } else {
            selectedPositions.remove(pos)
        }

        itemViews[pos]?.launcher_check?.beVisibleIf(select)

        if (selectedPositions.isEmpty()) {
            actMode?.finish()
            return
        }

        updateTitle(selectedPositions.size)
    }

    private fun updateTitle(cnt: Int) {
        actMode?.title = "$cnt / ${launchers.size}"
        actMode?.invalidate()
    }

    private val adapterListener = object : MyAdapterListener {
        override fun toggleItemSelectionAdapter(select: Boolean, position: Int) {
            toggleItemSelection(select, position)
        }

        override fun getSelectedPositions(): HashSet<Int> = selectedPositions
    }

    private val multiSelectorMode = object : ModalMultiSelectorCallback(multiSelector) {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.cab_edit -> showEditDialog()
                R.id.cab_delete -> deleteSelectedItems()
                else -> return false
            }
            return true
        }

        override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
            super.onCreateActionMode(actionMode, menu)
            actMode = actionMode
            activity.menuInflater.inflate(R.menu.cab, menu)
            return true
        }

        override fun onPrepareActionMode(actionMode: ActionMode?, menu: Menu): Boolean {
            menu.findItem(R.id.cab_edit).isVisible = multiSelector.selectedPositions.size == 1
            return true
        }

        override fun onDestroyActionMode(actionMode: ActionMode?) {
            super.onDestroyActionMode(actionMode)
            selectedPositions.forEach {
                itemViews[it]?.launcher_check?.beGone()
            }
            selectedPositions.clear()
            actMode = null
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        itemViews.put(position, holder.bindView(launchers[position], textColor, resources, packageManager))
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.app_launcher_item, parent, false)
        return ViewHolder(view, adapterListener, activity, multiSelectorMode, multiSelector, listener, itemClick)
    }

    override fun getItemCount() = launchers.count()

    private fun showEditDialog() {
        val selectedLauncher = launchers[multiSelector.selectedPositions[0]]
        val editView = activity.layoutInflater.inflate(R.layout.dialog_edit_launcher, null)
        editView.edit_launcher_edittext.setText(selectedLauncher.name)

        AlertDialog.Builder(activity).apply {
            setTitle(activity.getString(R.string.rename))
            setView(editView)
            setPositiveButton(R.string.ok, null)
            setNegativeButton(R.string.cancel, null)
            create().apply {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                show()
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val newName = editView.edit_launcher_edittext.text.toString().trim()
                    if (!newName.isEmpty()) {
                        if (activity.dbHelper.updateLauncherName(selectedLauncher.id, newName) > 0) {
                            listener?.refreshLaunchers()
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

    private fun deleteSelectedItems() {
        val positions = multiSelector.selectedPositions
        val deleteIds = ArrayList<String>(positions.size)
        val deletedLaunchers = ArrayList<AppLauncher>(positions.size)
        for (i in positions) {
            val launcher = launchers[i]
            deleteIds.add(launcher.id.toString())

            launcher.name = getRealAppName(launcher)
            if (launcher.name.isNotEmpty()) {
                deletedLaunchers.add(launcher)
            }
        }

        activity.dbHelper.deleteLaunchers(deleteIds)
        actMode?.finish()
        listener?.refreshLaunchers()
    }

    private fun getRealAppName(launcher: AppLauncher): String {
        return try {
            val applicationInfo = activity.packageManager.getApplicationInfo(launcher.packageName, 0)
            activity.packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }

    class ViewHolder(view: View, val adapterListener: MyAdapterListener, val activity: SimpleActivity, val multiSelectorCallback: ModalMultiSelectorCallback,
                     val multiSelector: MultiSelector, val listener: AppLaunchersListener?, val itemClick: (AppLauncher) -> (Unit)) : SwappingHolder(view, MultiSelector()) {
        fun bindView(launcher: AppLauncher, textColor: Int, resources: Resources, packageManager: PackageManager): View {
            itemView.apply {
                launcher_label.text = launcher.name
                launcher_label.setTextColor(textColor)

                setOnClickListener { viewClicked(launcher) }
                setOnLongClickListener { viewLongClicked(); true }

                val drawable = if (launcher.packageName.isAPredefinedApp()) {
                    resources.getLauncherDrawable(launcher.packageName)
                } else {
                    packageManager.getApplicationIcon(launcher.packageName)
                }
                launcher_icon.setImageDrawable(drawable)
            }
            return itemView
        }

        private fun viewClicked(appLauncher: AppLauncher) {
            if (multiSelector.isSelectable) {
                val isSelected = adapterListener.getSelectedPositions().contains(adapterPosition)
                adapterListener.toggleItemSelectionAdapter(!isSelected, adapterPosition)
            } else {
                itemClick(appLauncher)
            }
        }

        private fun viewLongClicked() {
            if (listener != null) {
                if (!multiSelector.isSelectable) {
                    activity.startSupportActionMode(multiSelectorCallback)
                    adapterListener.toggleItemSelectionAdapter(true, adapterPosition)
                }
            }
        }
    }

    interface AppLaunchersListener {
        fun refreshLaunchers()
    }
}
