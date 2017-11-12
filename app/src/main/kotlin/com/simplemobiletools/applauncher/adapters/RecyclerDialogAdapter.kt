package com.simplemobiletools.applauncher.adapters

import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.extensions.config
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.interfaces.MyAdapterListener
import kotlinx.android.synthetic.main.app_launcher_item.view.*
import java.util.*

class RecyclerDialogAdapter(activity: Activity, val launchers: List<AppLauncher>) : RecyclerView.Adapter<RecyclerDialogAdapter.ViewHolder>() {
    private val config = activity.config
    private var primaryColor = config.primaryColor
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
    }

    private val adapterListener = object : MyAdapterListener {
        override fun toggleItemSelectionAdapter(select: Boolean, position: Int) {
            toggleItemSelection(select, position)
        }

        override fun getSelectedPositions(): HashSet<Int> = selectedPositions
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        itemViews.put(position, holder.bindView(launchers[position], textColor, resources, packageManager))
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.app_launcher_item, parent, false)
        return ViewHolder(view, adapterListener)
    }

    override fun getItemCount() = launchers.count()

    class ViewHolder(view: View, val adapterListener: MyAdapterListener) : RecyclerView.ViewHolder(view) {
        fun bindView(launcher: AppLauncher, textColor: Int, resources: Resources, packageManager: PackageManager): View {
            itemView.apply {
                launcher_label.text = launcher.name
                launcher_label.setTextColor(textColor)

                setOnClickListener { viewClicked() }
                setOnLongClickListener { viewClicked(); true }

                val drawable = if (launcher.iconId != 0) {
                    resources.getDrawable(launcher.iconId)
                } else {
                    packageManager.getApplicationIcon(launcher.pkgName)
                }
                launcher_icon.setImageDrawable(drawable)
            }
            return itemView
        }

        private fun viewClicked() {
            val isSelected = adapterListener.getSelectedPositions().contains(adapterPosition)
            adapterListener.toggleItemSelectionAdapter(!isSelected, adapterPosition)
        }
    }
}
