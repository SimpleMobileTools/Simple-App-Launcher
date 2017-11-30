package com.simplemobiletools.applauncher.adapters

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.activities.SimpleActivity
import com.simplemobiletools.applauncher.dialogs.EditDialog
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.interfaces.RefreshRecyclerViewListener
import com.simplemobiletools.commons.views.MyRecyclerView
import kotlinx.android.synthetic.main.app_launcher_item.view.*
import java.util.*

class LaunchersAdapter(activity: SimpleActivity, val launchers: MutableList<AppLauncher>, val listener: RefreshRecyclerViewListener?,
                       recyclerView: MyRecyclerView, itemClick: (Any) -> Unit) : MyRecyclerViewAdapter(activity, recyclerView, itemClick) {

    override fun getActionMenuId() = R.menu.cab

    override fun prepareItemSelection(view: View) {
        view.launcher_check?.background?.applyColorFilter(primaryColor)
    }

    override fun markItemSelection(select: Boolean, view: View?) {
        view?.launcher_check?.beVisibleIf(select)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) = createViewHolder(R.layout.app_launcher_item, parent)

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val launcher = launchers[position]
        val view = holder.bindView(launcher) { itemView, layoutPosition ->
            setupView(itemView, launcher)
        }
        bindViewHolder(holder, position, view)
    }

    override fun getItemCount() = launchers.size

    override fun prepareActionMode(menu: Menu) {
        menu.apply {
            findItem(R.id.cab_edit).isVisible = selectedPositions.size == 1
        }
    }

    override fun actionItemPressed(id: Int) {
        when (id) {
            R.id.cab_edit -> showEditDialog()
            R.id.cab_delete -> askConfirmDelete()
        }
    }

    override fun getSelectableItemCount() = launchers.size

    private fun showEditDialog() {
        EditDialog(activity, launchers[selectedPositions.first()]) {
            finishActMode()
            listener?.refreshItems()
        }
    }

    private fun askConfirmDelete() {
        ConfirmationDialog(activity, "", R.string.delete_explanation, R.string.ok, R.string.cancel) {
            deleteItems()
        }
    }

    private fun deleteItems() {
        val deleteIds = ArrayList<String>(selectedPositions.size)
        val removeLaunchers = ArrayList<AppLauncher>(selectedPositions.size)
        selectedPositions.sortedDescending().forEach {
            val launcher = launchers[it]
            deleteIds.add(launcher.id.toString())
            removeLaunchers.add(launcher)
        }

        launchers.removeAll(removeLaunchers)
        activity.dbHelper.deleteLaunchers(deleteIds)
        removeSelectedItems()
    }

    fun updatePrimaryColor(primaryColor: Int) {
        this.primaryColor = primaryColor
        notifyDataSetChanged()
    }

    private fun setupView(view: View, launcher: AppLauncher) {
        view.apply {
            launcher_label.text = launcher.name
            launcher_label.setTextColor(textColor)
            launcher_icon.setImageDrawable(launcher.drawable!!)
        }
    }
}
