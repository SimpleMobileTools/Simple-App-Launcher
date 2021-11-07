package com.simplemobiletools.applauncher.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.extensions.config
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import kotlinx.android.synthetic.main.item_add_app_launcher.view.*
import kotlinx.android.synthetic.main.item_app_launcher.view.launcher_icon
import java.util.*

class LaunchersDialogAdapter(activity: Activity, val allLaunchers: ArrayList<AppLauncher>, val shownLaunchers: ArrayList<AppLauncher>) :
    RecyclerView.Adapter<LaunchersDialogAdapter.ViewHolder>() {
    private val config = activity.config
    private var textColor = config.textColor
    private var adjustedPrimaryColor = activity.getAdjustedPrimaryColor()
    private var selectedKeys = HashSet<Int>()

    init {
        shownLaunchers.forEach {
            selectedKeys.add(it.packageName.hashCode())
        }
    }

    fun toggleItemSelection(select: Boolean, pos: Int) {
        val itemKey = allLaunchers.getOrNull(pos)?.packageName?.hashCode() ?: return

        if (select) {
            selectedKeys.add(itemKey)
        } else {
            selectedKeys.remove(itemKey)
        }

        notifyItemChanged(pos)
    }

    fun getSelectedLaunchers() = allLaunchers.filter { selectedKeys.contains(it.packageName.hashCode()) } as ArrayList<AppLauncher>

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(allLaunchers[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_app_launcher, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = allLaunchers.size

    private fun isKeySelected(key: Int) = selectedKeys.contains(key)

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindView(launcher: AppLauncher): View {
            val isSelected = isKeySelected(launcher.packageName.hashCode())
            itemView.apply {
                launcher_checkbox.apply {
                    isChecked = isSelected
                    text = launcher.title
                    setColors(textColor, adjustedPrimaryColor, 0)
                }

                launcher_icon.setImageDrawable(launcher.drawable!!)
                setOnClickListener { viewClicked(launcher) }
                setOnLongClickListener { viewClicked(launcher); true }
            }

            return itemView
        }

        private fun viewClicked(launcher: AppLauncher) {
            val isSelected = selectedKeys.contains(launcher.packageName.hashCode())
            toggleItemSelection(!isSelected, adapterPosition)
        }
    }
}
