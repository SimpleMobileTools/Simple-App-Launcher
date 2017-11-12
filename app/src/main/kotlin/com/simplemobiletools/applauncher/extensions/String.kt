package com.simplemobiletools.applauncher.extensions

import com.simplemobiletools.applauncher.helpers.predefinedPackageNames

fun String.isAPredefinedApp() = predefinedPackageNames.contains(this)
