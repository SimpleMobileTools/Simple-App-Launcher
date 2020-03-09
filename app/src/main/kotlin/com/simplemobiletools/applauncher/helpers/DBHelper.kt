package com.simplemobiletools.applauncher.helpers

import android.content.ContentValues
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.drawable.Drawable
import android.text.TextUtils
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.extensions.getLauncherDrawable
import com.simplemobiletools.applauncher.extensions.isAPredefinedApp
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.extensions.getIntValue
import com.simplemobiletools.commons.extensions.getStringValue
import java.util.*

class DBHelper private constructor(val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    private val MAIN_TABLE_NAME = "launchers"
    private val COL_ID = "id"
    private val COL_NAME = "name"
    private val COL_PKG_NAME = "package_name"
    private val COL_POSITION = "position"
    private val COL_WAS_RENAMED = "was_renamed"
    private val COL_APP_ORDER = "app_order"

    private val mDb = writableDatabase

    companion object {
        private const val DB_VERSION = 6
        val DB_NAME = "applaunchers.db"
        var dbInstance: DBHelper? = null

        fun newInstance(context: Context): DBHelper {
            if (dbInstance == null) {
                dbInstance = DBHelper(context)
            }

            return dbInstance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $MAIN_TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_NAME TEXT, $COL_PKG_NAME TEXT UNIQUE, $COL_POSITION INTEGER," +
                "$COL_WAS_RENAMED INTEGER, $COL_APP_ORDER INTEGER)")
        addInitialLaunchers(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            val contacts = AppLauncher(0, context.getString(R.string.contacts), "com.simplemobiletools.contacts")
            addAppLauncher(contacts, db)
        }

        if (oldVersion < 4) {
            val clock = AppLauncher(0, context.getString(R.string.clock), "com.simplemobiletools.clock")
            addAppLauncher(clock, db)
        }

        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE $MAIN_TABLE_NAME ADD COLUMN $COL_WAS_RENAMED INTEGER NOT NULL DEFAULT 0")
        }

        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE $MAIN_TABLE_NAME ADD COLUMN $COL_APP_ORDER INTEGER NOT NULL DEFAULT 0")
        }
    }

    private fun addInitialLaunchers(db: SQLiteDatabase) {
        val titles = arrayListOf(
                R.string.calculator,
                R.string.calendar,
                R.string.camera,
                R.string.clock,
                R.string.contacts,
                R.string.draw,
                R.string.file_manager,
                R.string.flashlight,
                R.string.gallery,
                R.string.music_player,
                R.string.notes,
                R.string.thank_you
        )

        val cnt = titles.size
        val resources = context.resources
        val packages = predefinedPackageNames
        for (i in 0 until cnt) {
            val appLauncher = AppLauncher(0, resources.getString(titles[i]), packages[i])
            addAppLauncher(appLauncher, db)
        }
    }

    private fun addAppLauncher(appLauncher: AppLauncher, db: SQLiteDatabase) {
        insertAppLauncher(appLauncher, db)
    }

    fun insertAppLauncher(appLauncher: AppLauncher, db: SQLiteDatabase = mDb): Int {
        val values = fillAppLauncherValues(appLauncher)
        return db.insert(MAIN_TABLE_NAME, null, values).toInt()
    }

    private fun fillAppLauncherValues(appLauncher: AppLauncher): ContentValues {
        return ContentValues().apply {
            put(COL_NAME, appLauncher.name)
            put(COL_PKG_NAME, appLauncher.packageName)
        }
    }

    fun deleteLaunchers(ids: ArrayList<String>) {
        val args = TextUtils.join(", ", ids.toArray())
        val selection = "$COL_ID IN ($args)"
        mDb.delete(MAIN_TABLE_NAME, selection, null)
    }

    fun updateLauncherName(id: Int, newName: String): Boolean {
        val values = ContentValues().apply {
            put(COL_NAME, newName)
            put(COL_WAS_RENAMED, 1)
        }

        val selection = "$COL_ID = ?"
        val selectionArgs = Array(1) { id.toString() }
        return mDb.update(MAIN_TABLE_NAME, values, selection, selectionArgs) == 1
    }

    fun getLaunchers(): ArrayList<AppLauncher> {
        val resources = context.resources
        val packageManager = context.packageManager
        val launchers = ArrayList<AppLauncher>()
        val cols = arrayOf(COL_ID, COL_NAME, COL_PKG_NAME, COL_WAS_RENAMED)
        val cursor = mDb.query(MAIN_TABLE_NAME, cols, null, null, null, null, "$COL_NAME COLLATE NOCASE")
        val IDsToDelete = ArrayList<String>()
        cursor.use {
            while (cursor.moveToNext()) {
                val id = cursor.getIntValue(COL_ID)
                var name = cursor.getStringValue(COL_NAME)
                val packageName = cursor.getStringValue(COL_PKG_NAME)
                val wasRenamed = cursor.getIntValue(COL_WAS_RENAMED) == 1

                var drawable: Drawable? = null
                try {
                    // try getting the properly colored launcher icons
                    val launcher = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
                    val activityList = launcher.getActivityList(packageName, android.os.Process.myUserHandle())[0]
                    drawable = activityList.getBadgedIcon(0)

                    if (!wasRenamed) {
                        name = activityList.label.toString()
                    }
                } catch (e: Exception) {
                }

                if (drawable == null) {
                    drawable = if (packageName.isAPredefinedApp()) {
                        try {
                            packageManager.getApplicationIcon(packageName)
                        } catch (e: PackageManager.NameNotFoundException) {
                            resources.getLauncherDrawable(packageName)
                        }
                    } else {
                        try {
                            packageManager.getApplicationIcon(packageName)
                        } catch (e: PackageManager.NameNotFoundException) {
                            IDsToDelete.add(id.toString())
                            null
                        }
                    }
                }

                if (drawable != null) {
                    val launcher = AppLauncher(id, name, packageName, drawable)
                    launchers.add(launcher)
                }
            }
        }

        deleteLaunchers(IDsToDelete)
        return launchers
    }
}
