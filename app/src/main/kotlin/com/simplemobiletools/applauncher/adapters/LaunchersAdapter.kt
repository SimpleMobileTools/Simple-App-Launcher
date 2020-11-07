package com.simplemobiletools.applauncher.adapters

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.activities.SimpleActivity
import com.simplemobiletools.applauncher.dialogs.EditDialog
import com.simplemobiletools.applauncher.extensions.config
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.beInvisibleIf
import com.simplemobiletools.commons.interfaces.RefreshRecyclerViewListener
import com.simplemobiletools.commons.views.FastScroller
import com.simplemobiletools.commons.views.MyRecyclerView
import kotlinx.android.synthetic.main.item_app_launcher.view.*

class LaunchersAdapter(activity: SimpleActivity, val launchers: ArrayList<AppLauncher>, val listener: RefreshRecyclerViewListener?,
                       recyclerView: MyRecyclerView, fastScroller: FastScroller, itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, fastScroller, itemClick) {

    init {
        setupDragListener(true)
    }

    override fun getActionMenuId() = R.menu.cab

    override fun prepareActionMode(menu: Menu) {
        menu.apply {
            findItem(R.id.cab_edit).isVisible = isOneItemSelected()
        }
    }

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_edit -> showEditDialog()
            R.id.cab_remove -> tryRemoveLauncher()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createViewHolder(R.layout.item_app_launcher, parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val launcher = launchers[position]
        holder.bindView(launcher, true, true) { itemView, adapterPosition ->
            setupView(itemView, launcher, selectedKeys.contains(launcher.packageName.hashCode()))
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = launchers.size

    private fun getItemWithKey(key: Int): AppLauncher? = launchers.firstOrNull { it.packageName.hashCode() == key }

    override fun getSelectableItemCount() = launchers.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = launchers.getOrNull(position)?.packageName?.hashCode()

    override fun getItemKeyPosition(key: Int) = launchers.indexOfFirst { it.packageName.hashCode() == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    private fun showEditDialog() {
        EditDialog(activity, getItemWithKey(selectedKeys.first())!!) {
            finishActMode()
            listener?.refreshItems()
        }
    }

    private fun tryRemoveLauncher() {
        if (activity.config.wasRemoveInfoShown) {
            removeItems()
        } else {
            askConfirmRemove()
        }
    }

    private fun askConfirmRemove() {
        ConfirmationDialog(activity, "", R.string.remove_explanation, R.string.ok, R.string.cancel) {
            activity.config.wasRemoveInfoShown = true
            removeItems()
        }
    }

    private fun removeItems() {
        val removeIds = ArrayList<String>(selectedKeys.size)
        val removeLaunchers = ArrayList<AppLauncher>(selectedKeys.size)
        val positions = ArrayList<Int>(selectedKeys.size)

        for (key in selectedKeys) {
            val launcher = getItemWithKey(key) ?: continue
            removeIds.add(launcher.id.toString())
            removeLaunchers.add(launcher)

            val position = launchers.indexOfFirst { it.packageName.hashCode() == key }
            if (position != -1) {
                positions.add(position)
            }
        }

        launchers.removeAll(removeLaunchers)
        activity.dbHelper.deleteLaunchers(removeIds)
        positions.sortDescending()
        removeSelectedItems(positions)
    }

    private fun setupView(view: View, launcher: AppLauncher, isSelected: Boolean) {
        view.apply {
            launcher_check?.beInvisibleIf(!isSelected)
            launcher_label.text = launcher.title
            launcher_label.setTextColor(textColor)
            launcher_icon.setImageDrawable(launcher.drawable!!)

            if (isSelected) {
                launcher_check?.background?.applyColorFilter(primaryColor)
            }
        }
    }
}
