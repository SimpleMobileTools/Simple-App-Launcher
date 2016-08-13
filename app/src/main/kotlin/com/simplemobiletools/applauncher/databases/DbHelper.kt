package com.simplemobiletools.applauncher.databases

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(context, "launchers.db", null, 1) {
    val TABLE = "launchers"
    val CREATE_DB = "CREATE TABLE $TABLE (" +
            "$ID integer PRIMARY KEY autoincrement," +
            "$NAME TEXT," +
            "$PKG_NAME TEXT UNIQUE" +
            ")"

    companion object {
        val ID: String = "_id"
        val NAME: String = "name"
        val PKG_NAME: String = "pkgName"
    }

    fun addInitialLaunchers(db:SQLiteDatabase) {
        addLauncher("Simple Calculator", "com.simplemobiletools.calculator", db)
        addLauncher("Simple Calendar", "com.simplemobiletools.calendar", db)
        addLauncher("Simple Camera", "com.simplemobiletools.camera", db)
        addLauncher("Simple Draw", "com.simplemobiletools.draw", db)
        addLauncher("Simple File Manager", "com.simplemobiletools.filemanager", db)
        addLauncher("Simple Flashlight", "com.simplemobiletools.flashlight", db)
        addLauncher("Simple Gallery", "com.simplemobiletools.gallery", db)
        addLauncher("Simple Music Player", "com.simplemobiletools.musicplayer", db)
        addLauncher("Simple Notes", "com.simplemobiletools.notes", db)
    }

    fun addLauncher(name: String, pkgName: String, db: SQLiteDatabase = writableDatabase) {
        val contentValues = ContentValues()
        contentValues.put(NAME, name)
        contentValues.put(PKG_NAME, pkgName)
        db.insert(TABLE, null, contentValues)
    }

    fun getLaunchers(): Cursor {
        return readableDatabase.query(TABLE, arrayOf(NAME, PKG_NAME), null, null, null, null, NAME)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_DB)
        addInitialLaunchers(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}
