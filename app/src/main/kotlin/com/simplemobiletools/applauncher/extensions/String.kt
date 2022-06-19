package com.simplemobiletools.applauncher.extensions

import com.simplemobiletools.applauncher.helpers.getPredefinedPackageNames

fun String.isAPredefinedApp() = getPredefinedPackageNames().contains(this)
