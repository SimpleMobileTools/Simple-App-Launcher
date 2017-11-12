package com.simplemobiletools.applauncher.models

data class AppLauncher(val id: Int, var name: String, val packageName: String) {
    override fun equals(other: Any?): Boolean {
        return packageName.equals((other as AppLauncher).packageName, true)
    }
}
