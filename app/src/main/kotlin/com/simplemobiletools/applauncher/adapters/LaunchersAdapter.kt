package com.simplemobiletools.applauncher.adapters

import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller
import com.simplemobiletools.applauncher.LauncherAdapterUpdateListener
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.activities.SimpleActivity
import com.simplemobiletools.applauncher.databinding.ItemLauncherLabelBinding
import com.simplemobiletools.applauncher.databinding.ItemLauncherNoLabelBinding
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
import com.simplemobiletools.commons.interfaces.StartReorderDragListener
import com.simplemobiletools.commons.views.MyRecyclerView
import java.util.*

class LaunchersAdapter(
    activity: SimpleActivity,
    val launchers: ArrayList<AppLauncher>,
    val listener: LauncherAdapterUpdateListener,
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
        val binding = Binding.getByAppConfig(activity.config.showAppName).inflate(layoutInflater, parent, false)

        return createViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val launcher = launchers[position]
        holder.bindView(launcher, true, true) { itemView, adapterPosition ->
            setupView(Binding.getByAppConfig(activity.config.showAppName).bind(itemView), launcher, holder)
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
            listener.refreshItems()
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
            listener.refreshItems()
        } else {
            listener.refetchItems()
        }
    }

    private fun setupView(binding: ItemViewBinding, launcher: AppLauncher, holder: ViewHolder) {
        binding.apply {
            val isSelected = selectedKeys.contains(launcher.packageName.hashCode())
            launcherCheck?.beInvisibleIf(!isSelected)
            launcherLabel?.text = launcher.title
            launcherLabel?.setTextColor(textColor)
            launcherLabel?.beVisibleIf(activity.config.showAppName)
            launcherIcon.setImageDrawable(launcher.drawable!!)

            val bottomPadding = if (activity.config.showAppName) {
                0
            } else {
                iconPadding
            }

            launcherIcon.setPadding(iconPadding, iconPadding, iconPadding, bottomPadding)
            launcherDragHandle.beVisibleIf(isChangingOrder)
            if (isChangingOrder) {
                launcherDragHandle.applyColorFilter(textColor)
                launcherDragHandle.setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        startReorderDragListener.requestDrag(holder)
                    }
                    false
                }
            }

            if (isSelected) {
                launcherCheck?.background?.applyColorFilter(properPrimaryColor)
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

    private sealed interface Binding {
        companion object {
            fun getByAppConfig(showAppName: Boolean): Binding {
                return if (showAppName) {
                    ItemLauncherLabel
                } else {
                    ItemLauncherNoLabel
                }
            }
        }

        fun inflate(layoutInflater: LayoutInflater, viewGroup: ViewGroup, attachToRoot: Boolean): ItemViewBinding
        fun bind(view: View): ItemViewBinding

        data object ItemLauncherLabel : Binding {
            override fun inflate(layoutInflater: LayoutInflater, viewGroup: ViewGroup, attachToRoot: Boolean): ItemViewBinding {
                return ItemLauncherLabelBindingAdapter(ItemLauncherLabelBinding.inflate(layoutInflater, viewGroup, attachToRoot))
            }

            override fun bind(view: View): ItemViewBinding {
                return ItemLauncherLabelBindingAdapter(ItemLauncherLabelBinding.bind(view))
            }
        }

        data object ItemLauncherNoLabel : Binding {
            override fun inflate(layoutInflater: LayoutInflater, viewGroup: ViewGroup, attachToRoot: Boolean): ItemViewBinding {
                return ItemLauncherNoLabelBindingAdapter(ItemLauncherNoLabelBinding.inflate(layoutInflater, viewGroup, attachToRoot))
            }

            override fun bind(view: View): ItemViewBinding {
                return ItemLauncherNoLabelBindingAdapter(ItemLauncherNoLabelBinding.bind(view))
            }
        }
    }

    private interface ItemViewBinding : ViewBinding {
        val launcherCheck: ImageView
        val launcherIcon: ImageView
        val launcherDragHandle: ImageView
        val launcherLabel: TextView?
    }

    private class ItemLauncherLabelBindingAdapter(val binding: ItemLauncherLabelBinding) : ItemViewBinding {
        override val launcherCheck: ImageView = binding.launcherCheck
        override val launcherIcon: ImageView = binding.launcherIcon
        override val launcherDragHandle: ImageView = binding.launcherDragHandle
        override val launcherLabel: TextView = binding.launcherLabel

        override fun getRoot(): View = binding.root
    }

    private class ItemLauncherNoLabelBindingAdapter(val binding: ItemLauncherNoLabelBinding) : ItemViewBinding {
        override val launcherCheck: ImageView = binding.launcherCheck
        override val launcherIcon: ImageView = binding.launcherIcon
        override val launcherDragHandle: ImageView = binding.launcherDragHandle
        override val launcherLabel: TextView? = null

        override fun getRoot(): View = binding.root
    }
}
