package com.simplemobiletools.applauncher.adapters

import android.support.v7.view.ActionMode
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.*
import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback
import com.bignerdranch.android.multiselector.MultiSelector
import com.bignerdranch.android.multiselector.SwappingHolder
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.activities.SimpleActivity
import com.simplemobiletools.applauncher.dialogs.EditDialog
import com.simplemobiletools.applauncher.extensions.config
import com.simplemobiletools.applauncher.extensions.dbHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.interfaces.MyAdapterListener
import kotlinx.android.synthetic.main.app_launcher_item.view.*
import java.util.*

class RecyclerAdapter(val activity: SimpleActivity, val launchers: MutableList<AppLauncher>, val listener: AppLaunchersListener?, val itemClick: (AppLauncher) -> Unit) :
        RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private val config = activity.config
    private var actMode: ActionMode? = null
    private var primaryColor = config.primaryColor

    private val multiSelector = MultiSelector()
    private var itemViews = SparseArray<View>()
    private val selectedPositions = HashSet<Int>()
    private var textColor = config.textColor

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
        override fun itemLongClicked(position: Int) {
        }

        override fun toggleItemSelectionAdapter(select: Boolean, position: Int) {
            toggleItemSelection(select, position)
        }

        override fun getSelectedPositions(): HashSet<Int> = selectedPositions
    }

    private val multiSelectorMode = object : ModalMultiSelectorCallback(multiSelector) {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.cab_edit -> showEditDialog()
                R.id.cab_delete -> askConfirmDelete()
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
            menu.findItem(R.id.cab_edit).isVisible = selectedPositions.size == 1
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
        itemViews.put(position, holder.bindView(launchers[position], textColor))
        toggleItemSelection(selectedPositions.contains(position), position)
        holder.itemView.tag = holder
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.app_launcher_item, parent, false)
        return ViewHolder(view, adapterListener, activity, multiSelectorMode, multiSelector, listener, itemClick)
    }

    override fun getItemCount() = launchers.count()

    private fun showEditDialog() {
        EditDialog(activity, launchers[selectedPositions.first()]) {
            actMode?.finish()
            listener?.refreshLaunchers()
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
            notifyItemRemoved(it)
            itemViews.put(it, null)
        }

        launchers.removeAll(removeLaunchers)
        activity.dbHelper.deleteLaunchers(deleteIds)

        val newItems = SparseArray<View>()
        (0 until itemViews.size())
                .filter { itemViews[it] != null }
                .forEachIndexed { curIndex, i -> newItems.put(curIndex, itemViews[i]) }

        itemViews = newItems
        actMode?.finish()
    }

    fun updateTextColor(textColor: Int) {
        this.textColor = textColor
        notifyDataSetChanged()
    }

    fun updatePrimaryColor(primaryColor: Int) {
        this.primaryColor = primaryColor
        notifyDataSetChanged()
    }

    fun selectItem(pos: Int) {
        toggleItemSelection(true, pos)
    }

    fun selectRange(from: Int, to: Int, min: Int, max: Int) {
        if (from == to) {
            (min..max).filter { it != from }
                    .forEach { toggleItemSelection(false, it) }
            return
        }

        if (to < from) {
            for (i in to..from)
                toggleItemSelection(true, i)

            if (min > -1 && min < to) {
                (min until to).filter { it != from }
                        .forEach { toggleItemSelection(false, it) }
            }
            if (max > -1) {
                for (i in from + 1..max)
                    toggleItemSelection(false, i)
            }
        } else {
            for (i in from..to)
                toggleItemSelection(true, i)

            if (max > -1 && max > to) {
                (to + 1..max).filter { it != from }
                        .forEach { toggleItemSelection(false, it) }
            }

            if (min > -1) {
                for (i in min until from)
                    toggleItemSelection(false, i)
            }
        }
    }

    class ViewHolder(view: View, val adapterListener: MyAdapterListener, val activity: SimpleActivity, val multiSelectorCallback: ModalMultiSelectorCallback,
                     val multiSelector: MultiSelector, val listener: AppLaunchersListener?, val itemClick: (AppLauncher) -> (Unit)) : SwappingHolder(view, MultiSelector()) {
        fun bindView(launcher: AppLauncher, textColor: Int): View {
            itemView.apply {
                launcher_label.text = launcher.name
                launcher_label.setTextColor(textColor)
                launcher_icon.setImageDrawable(launcher.drawable!!)

                setOnClickListener { viewClicked(launcher) }
                setOnLongClickListener { viewLongClicked(); true }
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

                listener.itemLongClicked(adapterPosition)
            }
        }
    }

    interface AppLaunchersListener {
        fun refreshLaunchers()

        fun itemLongClicked(position: Int)
    }
}
