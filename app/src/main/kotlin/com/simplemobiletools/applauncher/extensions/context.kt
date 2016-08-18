package com.simplemobiletools.applauncher.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun Context.viewIntent(url: String): Intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

fun Context.toast(msgId: Int, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, resources.getString(msgId), duration).show()
