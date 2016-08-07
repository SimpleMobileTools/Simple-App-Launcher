package com.simplemobiletools.applauncher.extensions

import android.content.Intent
import android.net.Uri

fun viewIntent(url: String): Intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
