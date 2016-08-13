package com.simplemobiletools.applauncher.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.models.AppLauncher
import kotlinx.android.synthetic.main.app_launcher_dialog_item.view.*

class RecyclerAdapter(val launchers: List<AppLauncher>, val itemClick: (AppLauncher) -> Unit) :
        RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(launchers[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.app_launcher_dialog_item, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun getItemCount(): Int {
        return launchers.count()
    }

    class ViewHolder(view: View, val itemClick: (AppLauncher) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bindView(launcher: AppLauncher) {
            with(launcher) {
                itemView.launcher_label.text = launcher.name
                itemView.launcher_icon.setImageDrawable(launcher.drawable)
                itemView.setOnClickListener { itemClick(this) }
            }
        }
    }
}
