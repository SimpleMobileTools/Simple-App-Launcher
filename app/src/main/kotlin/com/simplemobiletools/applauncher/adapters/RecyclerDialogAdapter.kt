package com.simplemobiletools.applauncher.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.extensions.hide
import com.simplemobiletools.applauncher.extensions.show
import com.simplemobiletools.applauncher.models.AppLauncher
import kotlinx.android.synthetic.main.app_launcher_dialog_item.view.*

class RecyclerDialogAdapter(val cxt: Context, val launchers: List<AppLauncher>) : RecyclerView.Adapter<RecyclerDialogAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(cxt, launchers[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.app_launcher_dialog_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return launchers.count()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindView(context: Context, launcher: AppLauncher) {
            with(launcher) {
                itemView.launcher_label.text = launcher.name
                itemView.setOnClickListener {
                    launcher.isChecked = !launcher.isChecked
                    handleCheck(itemView.launcher_check, launcher)
                }

                handleCheck(itemView.launcher_check, launcher)
                if (launcher.iconId != 0) {
                    val icon = context.resources.getDrawable(launcher.iconId)
                    itemView.launcher_icon.setImageDrawable(icon)
                } else {
                    val icon = context.packageManager.getApplicationIcon(launcher.pkgName)
                    itemView.launcher_icon.setImageDrawable(icon)
                }
            }
        }

        fun handleCheck(check: View, launcher: AppLauncher) {
            if (launcher.isChecked) {
                check.show()
            } else {
                check.hide()
            }
        }
    }
}
