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
        private const val DB_VERSION = 7
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
            val contacts = AppLauncher(0, context.getString(R.string.contacts_short), "com.simplemobiletools.contacts", 0)
            addAppLauncher(contacts, db)
        }

        if (oldVersion < 4) {
            val clock = AppLauncher(0, context.getString(R.string.clock_short), "com.simplemobiletools.clock", 0)
            addAppLauncher(clock, db)
        }

        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE $MAIN_TABLE_NAME ADD COLUMN $COL_WAS_RENAMED INTEGER NOT NULL DEFAULT 0")
        }

        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE $MAIN_TABLE_NAME ADD COLUMN $COL_APP_ORDER INTEGER NOT NULL DEFAULT 0")
        }

        if (oldVersion < 7) {
            val dialer = AppLauncher(0, context.getString(R.string.dialer_short), "com.simplemobiletools.dialer", 0)
            val smsMessenger = AppLauncher(0, context.getString(R.string.sms_messenger_short), "com.simplemobiletools.smsmessenger", 0)
            val voiceRecorder = AppLauncher(0, context.getString(R.string.voice_recorder_short), "com.simplemobiletools.voicerecorder", 0)
            addAppLauncher(dialer, db)
            addAppLauncher(smsMessenger, db)
            addAppLauncher(voiceRecorder, db)
        }
    }

    private fun addInitialLaunchers(db: SQLiteDatabase) {
        val titles = arrayListOf(
            R.string.calculator_short,
            R.string.calendar_short,
            R.string.camera_short,
            R.string.clock_short,
            R.string.contacts_short,
            R.string.dialer_short,
            R.string.draw_short,
            R.string.file_manager_short,
            R.string.flashlight_short,
            R.string.gallery_short,
            R.string.music_player_short,
            R.string.notes_short,
            R.string.sms_messenger_short,
            R.string.thank_you_short,
            R.string.voice_recorder_short
        )

        val cnt = titles.size
        val resources = context.resources
        val packages = predefinedPackageNames
        for (i in 0 until cnt) {
            val appLauncher = AppLauncher(0, resources.getString(titles[i]), packages[i], 0)
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
            put(COL_NAME, appLauncher.title)
            put(COL_PKG_NAME, appLauncher.packageName)
            put(COL_APP_ORDER, appLauncher.order)
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

    fun updateLauncherOrder(id: Int, order: Int) {
        val values = ContentValues().apply {
            put(COL_APP_ORDER, order)
        }

        val selection = "$COL_ID = ?"
        val selectionArgs = Array(1) { id.toString() }
        mDb.update(MAIN_TABLE_NAME, values, selection, selectionArgs)
    }

    fun getLaunchers(): ArrayList<AppLauncher> {
        val resources = context.resources
        val packageManager = context.packageManager
        val launchers = ArrayList<AppLauncher>()
        val cols = arrayOf(COL_ID, COL_NAME, COL_PKG_NAME, COL_WAS_RENAMED, COL_APP_ORDER)
        val cursor = mDb.query(MAIN_TABLE_NAME, cols, null, null, null, null, "$COL_NAME COLLATE NOCASE")
        val IDsToDelete = ArrayList<String>()
        cursor.use {
            while (cursor.moveToNext()) {
                val id = cursor.getIntValue(COL_ID)
                var name = cursor.getStringValue(COL_NAME)
                val packageName = cursor.getStringValue(COL_PKG_NAME)
                val wasRenamed = cursor.getIntValue(COL_WAS_RENAMED) == 1
                val order = cursor.getIntValue(COL_APP_ORDER)

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
                    val launcher = AppLauncher(id, name, packageName, order, drawable)
                    launchers.add(launcher)
                }
            }
        }

        deleteLaunchers(IDsToDelete)
        return launchers
    }
}
