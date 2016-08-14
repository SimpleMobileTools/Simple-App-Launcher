package com.simplemobiletools.applauncher.models

data class AppLauncher(val name: String, val pkgName: String, val iconId: Int, var isChecked: Boolean = false) {
    override fun equals(other: Any?): Boolean {
        return pkgName.equals((other as AppLauncher).pkgName)
    }
}
