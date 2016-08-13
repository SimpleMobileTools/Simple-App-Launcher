package com.simplemobiletools.applauncher.adapters

import android.content.Context
import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.databases.DbHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import kotlinx.android.synthetic.main.app_launcher_item.view.*

class MyCursorAdapter(cxt: Context, dataCursor: Cursor, val itemClick: (AppLauncher) -> Unit) : RecyclerView.Adapter<MyCursorAdapter.ViewHolder>() {
    val cursor = dataCursor
    val context = cxt

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.app_launcher_item, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        cursor.moveToPosition(position)
        val name = cursor.getString(cursor.getColumnIndex(DbHelper.NAME))
        val pkgName = cursor.getString(cursor.getColumnIndex(DbHelper.PKG_NAME))
        val icon = cursor.getInt(cursor.getColumnIndex(DbHelper.ICON_ID))
        val launcher = AppLauncher(name, pkgName, icon)
        holder.bindView(context, launcher)
    }

    override fun getItemCount(): Int {
        return cursor.count
    }

    class ViewHolder(view: View, val itemClick: (AppLauncher) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bindView(context: Context, launcher: AppLauncher) {
            with(launcher) {
                itemView.launcher_label.text = launcher.name
                itemView.launcher_icon.setImageDrawable(context.resources.getDrawable(launcher.iconId))
                itemView.setOnClickListener { itemClick(this) }
            }
        }
    }
}
