package com.simplemobiletools.applauncher.adapters

import android.app.Activity
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
import com.simplemobiletools.applauncher.models.AppLauncher
import kotlinx.android.synthetic.main.app_launcher_dialog_item.view.*
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
                R.id.cab_delete -> {
                    val positions = multiSelector.selectedPositions
                    val deleteIds = ArrayList<String>(positions.size)
                    for (i in positions) {
                        deleteIds.add(launchers[i].id.toString())
                    }
                    DbHelper(act).deleteLaunchers(deleteIds)
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
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(act, deleteMode, multiSelector, launchers[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.app_launcher_dialog_item, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun getItemCount(): Int {
        return launchers.count()
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
            } else {
                itemClick(appLauncher)
            }
        }
    }
}
