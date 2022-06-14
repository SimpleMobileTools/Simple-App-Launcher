package com.simplemobiletools.applauncher.adapters

import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.activities.SimpleActivity
import com.simplemobiletools.applauncher.dialogs.EditDialog
import com.simplemobiletools.applauncher.extensions.config
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.SORT_BY_CUSTOM
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.interfaces.ItemMoveCallback
import com.simplemobiletools.commons.interfaces.ItemTouchHelperContract
import com.simplemobiletools.commons.interfaces.RefreshRecyclerViewListener
import com.simplemobiletools.commons.interfaces.StartReorderDragListener
import com.simplemobiletools.commons.views.MyRecyclerView
import kotlinx.android.synthetic.main.item_launcher_label.view.*
import java.util.*

class LaunchersAdapter(
    activity: SimpleActivity,
    val launchers: ArrayList<AppLauncher>,
    val listener: RefreshRecyclerViewListener?,
    recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, itemClick), ItemTouchHelperContract, RecyclerViewFastScroller.OnPopupTextUpdate {

    private var isChangingOrder = false
    private var iconPadding = 0
    private var startReorderDragListener: StartReorderDragListener

    init {
        setupDragListener(true)
        calculateIconWidth()

        val touchHelper = ItemTouchHelper(ItemMoveCallback(this, true))
        touchHelper.attachToRecyclerView(recyclerView)

        startReorderDragListener = object : StartReorderDragListener {
            override fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
                touchHelper.startDrag(viewHolder)
            }
        }
    }

    override fun getActionMenuId() = R.menu.cab

    override fun prepareActionMode(menu: Menu) {
        menu.findItem(R.id.cab_edit).isVisible = isOneItemSelected()
    }

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_change_order -> changeOrder()
            R.id.cab_edit -> showEditDialog()
            R.id.cab_remove -> tryRemoveLauncher()
            R.id.cab_select_all -> selectAll()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = if (activity.config.showAppName) {
            R.layout.item_launcher_label
        } else {
            R.layout.item_launcher_no_label
        }

        return createViewHolder(layoutId, parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val launcher = launchers[position]
        holder.bindView(launcher, true, true) { itemView, adapterPosition ->
            setupView(itemView, launcher, holder)
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

    override fun onActionModeDestroyed() {
        if (isChangingOrder) {
            notifyDataSetChanged()
            launchers.forEachIndexed { index, appLauncher ->
                appLauncher.order = index + 1
            }

            ensureBackgroundThread {
                launchers.forEach {
                    activity.dbHelper.updateLauncherOrder(it.id, it.order)
                }
            }
        }

        isChangingOrder = false
    }

    private fun changeOrder() {
        isChangingOrder = true
        notifyDataSetChanged()
    }

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

    fun calculateIconWidth() {
        val currentColumnCount = if (activity.portrait) {
            activity.config.portraitColumnCnt
        } else {
            activity.config.landscapeColumnCnt
        }

        val iconWidth = activity.realScreenSize.x / currentColumnCount
        iconPadding = (iconWidth * 0.1f).toInt()
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

        launchers.removeAll(removeLaunchers.toSet())
        activity.dbHelper.deleteLaunchers(removeIds)
        positions.sortDescending()
        removeSelectedItems(positions)
        if (launchers.isEmpty()) {
            listener?.refreshItems()
        }
    }

    private fun setupView(view: View, launcher: AppLauncher, holder: ViewHolder) {
        view.apply {
            val isSelected = selectedKeys.contains(launcher.packageName.hashCode())
            launcher_check?.beInvisibleIf(!isSelected)
            launcher_label?.text = launcher.title
            launcher_label?.setTextColor(textColor)
            launcher_label?.beVisibleIf(activity.config.showAppName)
            launcher_icon.setImageDrawable(launcher.drawable!!)

            val bottomPadding = if (activity.config.showAppName) {
                0
            } else {
                iconPadding
            }

            launcher_icon.setPadding(iconPadding, iconPadding, iconPadding, bottomPadding)
            launcher_drag_handle.beVisibleIf(isChangingOrder)
            if (isChangingOrder) {
                launcher_drag_handle.applyColorFilter(textColor)
                launcher_drag_handle.setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        startReorderDragListener.requestDrag(holder)
                    }
                    false
                }
            }

            if (isSelected) {
                launcher_check?.background?.applyColorFilter(properPrimaryColor)
            }
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(launchers, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(launchers, i, i - 1)
            }
        }

        notifyItemMoved(fromPosition, toPosition)
        activity.config.sorting = SORT_BY_CUSTOM
    }

    override fun onRowClear(myViewHolder: ViewHolder?) {}

    override fun onRowSelected(myViewHolder: ViewHolder?) {}

    override fun onChange(position: Int) = launchers.getOrNull(position)?.getBubbleText() ?: ""
}
