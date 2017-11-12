package com.simplemobiletools.applauncher.models

data class AppLauncher(val id: Int, var name: String, val pkgName: String, val iconId: Int) {
    override fun equals(other: Any?): Boolean {
        return pkgName.equals((other as AppLauncher).pkgName, true)
    }
}
