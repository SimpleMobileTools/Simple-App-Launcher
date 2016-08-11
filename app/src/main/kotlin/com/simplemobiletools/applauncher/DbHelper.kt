package com.simplemobiletools.applauncher

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(context, "launchers.db", null, 1) {
    val TABLE = "launchers"
    val CREATE_DB = "CREATE TABLE $TABLE (" +
            "$ID integer PRIMARY KEY autoincrement," +
            "$NAME TEXT," +
            "$PKG_NAME TEXT" +
            ")"

    companion object {
        val ID: String = "_id"
        val NAME: String = "name"
        val PKG_NAME: String = "pkgName"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_DB)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}
