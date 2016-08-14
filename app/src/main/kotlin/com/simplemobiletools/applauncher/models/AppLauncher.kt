package com.simplemobiletools.applauncher.models

data class AppLauncher(val name: String, val pkgName: String, val iconId: Int) {
    override fun equals(other: Any?): Boolean {
        return pkgName.equals((other as AppLauncher).pkgName)
    }
}
