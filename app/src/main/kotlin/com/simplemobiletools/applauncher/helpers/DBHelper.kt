package com.simplemobiletools.applauncher.helpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import android.util.Log
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.getIntValue
import com.simplemobiletools.commons.extensions.getStringValue
import java.util.*

class DBHelper private constructor(val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    private val MAIN_TABLE_NAME = "launchers"
    private val COL_ID = "id"
    private val COL_NAME = "name"
    private val COL_PKG_NAME = "pkgName"
    private val COL_ICON_ID = "icon"
    private val COL_POSITION = "position"

    private val mDb = writableDatabase

    companion object {
        private val DB_VERSION = 2
        val DB_NAME = "applaunchers.db"
        var dbInstance: DBHelper? = null

        fun newInstance(context: Context): DBHelper {
            if (dbInstance == null)
                dbInstance = DBHelper(context)

            return dbInstance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.e("DEBUG", "create")
        db.execSQL("CREATE TABLE $MAIN_TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_NAME TEXT, $COL_PKG_NAME TEXT UNIQUE, " +
                "$COL_ICON_ID INTEGER, $COL_POSITION INTEGER)")
        addInitialLaunchers(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.e("DEBUG", "upgrade")
    }

    private fun addInitialLaunchers(db: SQLiteDatabase) {
        val titles = arrayListOf(
                R.string.calculator,
                R.string.calendar,
                R.string.camera,
                R.string.draw,
                R.string.file_manager,
                R.string.flashlight,
                R.string.gallery,
                R.string.music_player,
                R.string.notes
        )

        val packages = arrayListOf(
                "calculator",
                "calendar",
                "camera",
                "draw",
                "filemanager",
                "flashlight",
                "gallery",
                "musicplayer",
                "notes"
        )

        val icons = arrayListOf(
                R.drawable.ic_calculator,
                R.drawable.ic_calendar,
                R.drawable.ic_camera,
                R.drawable.ic_draw,
                R.drawable.ic_filemanager,
                R.drawable.ic_flashlight,
                R.drawable.ic_gallery,
                R.drawable.ic_musicplayer,
                R.drawable.ic_notes
        )

        val cnt = titles.size
        val resources = context.resources
        for (i in 0 until cnt) {
            val appLauncher = AppLauncher(0, resources.getString(titles[i]), "com.simplemobiletools.${packages[i]}", icons[i])
            addAppLauncher(appLauncher, db)
        }
    }

    fun addAppLauncher(appLauncher: AppLauncher, db: SQLiteDatabase) {
        insertAppLauncher(appLauncher, db)
    }

    private fun insertAppLauncher(appLauncher: AppLauncher, db: SQLiteDatabase = mDb): Int {
        val values = fillAppLauncherValues(appLauncher)
        return db.insert(MAIN_TABLE_NAME, null, values).toInt()
    }

    private fun fillAppLauncherValues(appLauncher: AppLauncher): ContentValues {
        return ContentValues().apply {
            put(COL_NAME, appLauncher.name)
            put(COL_PKG_NAME, appLauncher.pkgName)
            put(COL_ICON_ID, appLauncher.iconId)
        }
    }

    fun deleteLaunchers(ids: ArrayList<String>) {
        val args = TextUtils.join(", ", ids.toArray())
        val selection = "$COL_ID IN ($args)"
        mDb.delete(MAIN_TABLE_NAME, selection, null)
    }

    fun updateLauncherName(id: Int, newName: String): Int {
        val values = ContentValues()
        values.put(COL_NAME, newName)
        val selection = "$COL_ID = ?"
        val selectionArgs = Array(1) { id.toString() }
        return mDb.update(MAIN_TABLE_NAME, values, selection, selectionArgs)
    }

    fun getLaunchers(): ArrayList<AppLauncher> {
        val launchers = ArrayList<AppLauncher>()
        val cols = arrayOf(COL_ID, COL_NAME, COL_PKG_NAME, COL_ICON_ID)
        val cursor = mDb.query(MAIN_TABLE_NAME, cols, null, null, null, null, COL_NAME)
        cursor.use {
            while (cursor.moveToNext()) {
                val id = cursor.getIntValue(COL_ID)
                val name = cursor.getStringValue(COL_NAME)
                val pkgName = cursor.getStringValue(COL_PKG_NAME)
                val icon = cursor.getIntValue(COL_ICON_ID)
                val launcher = AppLauncher(id, name, pkgName, icon)
                launchers.add(launcher)
            }
        }
        return launchers
    }
}
