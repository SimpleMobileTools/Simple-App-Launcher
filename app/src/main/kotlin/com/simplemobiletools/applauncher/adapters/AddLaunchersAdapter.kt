package com.simplemobiletools.applauncher.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.applauncher.databinding.ItemAddLauncherBinding
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.getProperPrimaryColor
import com.simplemobiletools.commons.extensions.getProperTextColor

class AddLaunchersAdapter(activity: Activity, val allLaunchers: ArrayList<AppLauncher>, val shownLaunchers: ArrayList<AppLauncher>) :
    RecyclerView.Adapter<AddLaunchersAdapter.ViewHolder>() {
    private var layoutInflater = activity.layoutInflater
    private var textColor = activity.getProperTextColor()
    private var adjustedPrimaryColor = activity.getProperPrimaryColor()
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
        return ViewHolder(ItemAddLauncherBinding.inflate(layoutInflater, parent, false).root)
    }

    override fun getItemCount() = allLaunchers.size

    private fun isKeySelected(key: Int) = selectedKeys.contains(key)

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindView(launcher: AppLauncher): View {
            val isSelected = isKeySelected(launcher.packageName.hashCode())
            ItemAddLauncherBinding.bind(itemView).apply {
                launcherCheckbox.apply {
                    isChecked = isSelected
                    text = launcher.title
                    setColors(textColor, adjustedPrimaryColor, 0)
                }

                launcherIcon.setImageDrawable(launcher.drawable!!)
                root.setOnClickListener { viewClicked(launcher) }
                root.setOnLongClickListener { viewClicked(launcher); true }
            }

            return itemView
        }

        private fun viewClicked(launcher: AppLauncher) {
            val isSelected = selectedKeys.contains(launcher.packageName.hashCode())
            toggleItemSelection(!isSelected, adapterPosition)
        }
    }
}
