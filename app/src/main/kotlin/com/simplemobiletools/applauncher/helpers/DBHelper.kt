package com.simplemobiletools.applauncher.helpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.getIntValue
import com.simplemobiletools.commons.extensions.getStringValue
import java.util.*

class DBHelper(context: Context) : SQLiteOpenHelper(context, "launchers.db", null, 1) {
    private val resources = context.resources
    private val TABLE = "launchers"
    private val CREATE_DB = "CREATE TABLE $TABLE ($ID INTEGER PRIMARY KEY AUTOINCREMENT, $NAME TEXT, $PKG_NAME TEXT UNIQUE, $ICON_ID INTEGER, $POSITION INTEGER)"

    companion object {
        val ID: String = "_id"
        val NAME: String = "name"
        val PKG_NAME: String = "pkgName"
        val ICON_ID: String = "icon"
        val POSITION: String = "position"

        var dbInstance: DBHelper? = null

        fun newInstance(context: Context): DBHelper {
            if (dbInstance == null)
                dbInstance = DBHelper(context)

            return dbInstance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_DB)
        addInitialLaunchers(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    private fun addInitialLaunchers(db: SQLiteDatabase) {
        addLauncher(string(R.string.calculator), "com.simplemobiletools.calculator", R.drawable.ic_calculator, db)
        addLauncher(string(R.string.calendar), "com.simplemobiletools.calendar", R.drawable.ic_calendar, db)
        addLauncher(string(R.string.camera), "com.simplemobiletools.camera", R.drawable.ic_camera, db)
        addLauncher(string(R.string.draw), "com.simplemobiletools.draw", R.drawable.ic_draw, db)
        addLauncher(string(R.string.file_manager), "com.simplemobiletools.filemanager", R.drawable.ic_filemanager, db)
        addLauncher(string(R.string.flashlight), "com.simplemobiletools.flashlight", R.drawable.ic_flashlight, db)
        addLauncher(string(R.string.gallery), "com.simplemobiletools.gallery", R.drawable.ic_gallery, db)
        addLauncher(string(R.string.music_player), "com.simplemobiletools.musicplayer", R.drawable.ic_musicplayer, db)
        addLauncher(string(R.string.notes), "com.simplemobiletools.notes", R.drawable.ic_notes, db)
    }

    fun addLauncher(name: String, pkgName: String, iconId: Int = 0, db: SQLiteDatabase = writableDatabase) {
        val contentValues = ContentValues()
        contentValues.put(NAME, name)
        contentValues.put(PKG_NAME, pkgName)
        contentValues.put(ICON_ID, iconId)
        db.insert(TABLE, null, contentValues)
    }

    fun deleteLaunchers(ids: ArrayList<String>) {
        val args = TextUtils.join(", ", ids.toArray())
        writableDatabase.delete(TABLE, "$ID IN ($args)", null)
    }

    fun updateLauncherName(id: Int, newName: String): Int {
        val values = ContentValues()
        values.put(NAME, newName)
        val selection = ID + " = ?"
        val selectionArgs = Array(1) { id.toString() }
        return writableDatabase.update(TABLE, values, selection, selectionArgs)
    }

    fun getLaunchers(): ArrayList<AppLauncher> {
        val launchers = ArrayList<AppLauncher>()
        val cursor = readableDatabase.query(TABLE, arrayOf(ID, NAME, PKG_NAME, ICON_ID), null, null, null, null, NAME)
        cursor.use {
            while (cursor.moveToNext()) {
                val id = cursor.getIntValue(ID)
                val name = cursor.getStringValue(NAME)
                val pkgName = cursor.getStringValue(PKG_NAME)
                val icon = cursor.getIntValue(ICON_ID)
                val launcher = AppLauncher(id, name, pkgName, icon)
                launchers.add(launcher)
            }
        }
        return launchers
    }

    private fun string(id: Int) = resources.getString(id)
}
