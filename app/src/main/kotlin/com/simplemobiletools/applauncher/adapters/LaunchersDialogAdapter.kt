package com.simplemobiletools.applauncher.adapters

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.extensions.config
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.beInvisibleIf
import com.simplemobiletools.commons.interfaces.MyAdapterListener
import kotlinx.android.synthetic.main.item_app_launcher.view.*
import java.util.*

class LaunchersDialogAdapter(activity: Activity, val launchers: ArrayList<AppLauncher>) : RecyclerView.Adapter<LaunchersDialogAdapter.ViewHolder>() {
    private val config = activity.config
    private var primaryColor = config.primaryColor
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

        itemViews[pos]?.launcher_check?.beInvisibleIf(!select)
    }

    fun getSelectedLaunchers(): ArrayList<AppLauncher> {
        val selectedLaunchers = ArrayList<AppLauncher>()
        selectedPositions.forEach {
            selectedLaunchers.add(launchers[it])
        }
        return selectedLaunchers
    }

    private val adapterListener = object : MyAdapterListener {
        override fun itemLongClicked(position: Int) {
        }

        override fun toggleItemSelectionAdapter(select: Boolean, position: Int) {
            toggleItemSelection(select, position)
        }

        override fun getSelectedPositions(): HashSet<Int> = selectedPositions
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        itemViews.put(position, holder.bindView(launchers[position], textColor))
        toggleItemSelection(selectedPositions.contains(position), position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_launcher, parent, false)
        return ViewHolder(view, adapterListener)
    }

    override fun getItemCount() = launchers.size

    class ViewHolder(view: View, val adapterListener: MyAdapterListener) : RecyclerView.ViewHolder(view) {
        fun bindView(launcher: AppLauncher, textColor: Int): View {
            itemView.apply {
                launcher_label.text = launcher.name
                launcher_label.setTextColor(textColor)
                launcher_icon.setImageDrawable(launcher.drawable!!)

                setOnClickListener { viewClicked() }
                setOnLongClickListener { viewClicked(); true }
            }
            return itemView
        }

        private fun viewClicked() {
            val isSelected = adapterListener.getSelectedPositions().contains(adapterPosition)
            adapterListener.toggleItemSelectionAdapter(!isSelected, adapterPosition)
        }
    }
}
