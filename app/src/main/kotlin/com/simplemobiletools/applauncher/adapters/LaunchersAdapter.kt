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
import java.util.*

class LaunchersAdapter(activity: SimpleActivity, val launchers: MutableList<AppLauncher>, val listener: RefreshRecyclerViewListener?,
                       recyclerView: MyRecyclerView, fastScroller: FastScroller, itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, fastScroller, itemClick) {

    init {
        setupDragListener(true)
    }

    override fun getActionMenuId() = R.menu.cab

    override fun prepareItemSelection(viewHolder: ViewHolder) {
        viewHolder.itemView?.launcher_check?.background?.applyColorFilter(primaryColor)
    }

    override fun markViewHolderSelection(select: Boolean, viewHolder: ViewHolder?) {
        viewHolder?.itemView?.launcher_check?.beInvisibleIf(!select)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createViewHolder(R.layout.item_app_launcher, parent)

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val launcher = launchers[position]
        val view = holder.bindView(launcher, true, true) { itemView, adapterPosition ->
            setupView(itemView, launcher)
        }
        bindViewHolder(holder, position, view)
    }

    override fun getItemCount() = launchers.size

    override fun prepareActionMode(menu: Menu) {
        menu.apply {
            findItem(R.id.cab_edit).isVisible = isOneItemSelected()
        }
    }

    override fun actionItemPressed(id: Int) {
        when (id) {
            R.id.cab_edit -> showEditDialog()
            R.id.cab_remove -> tryRemoveLauncher()
        }
    }

    override fun getSelectableItemCount() = launchers.size

    override fun getIsItemSelectable(position: Int) = true

    private fun showEditDialog() {
        EditDialog(activity, launchers[selectedPositions.first()]) {
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
        val removeIds = ArrayList<String>(selectedPositions.size)
        val removeLaunchers = ArrayList<AppLauncher>(selectedPositions.size)
        selectedPositions.sortedDescending().forEach {
            val launcher = launchers[it]
            removeIds.add(launcher.id.toString())
            removeLaunchers.add(launcher)
        }

        launchers.removeAll(removeLaunchers)
        activity.dbHelper.deleteLaunchers(removeIds)
        removeSelectedItems()
    }

    private fun setupView(view: View, launcher: AppLauncher) {
        view.apply {
            launcher_label.text = launcher.name
            launcher_label.setTextColor(textColor)
            launcher_icon.setImageDrawable(launcher.drawable!!)
        }
    }
}
