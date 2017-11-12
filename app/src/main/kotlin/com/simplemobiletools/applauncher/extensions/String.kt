package com.simplemobiletools.applauncher.extensions

import com.simplemobiletools.applauncher.helpers.predefinedPackageNames

// treat apps like "com.simplemobiletools.notes.debug" as any third party app
fun String.isAPredefinedApp() = predefinedPackageNames.contains(this)
