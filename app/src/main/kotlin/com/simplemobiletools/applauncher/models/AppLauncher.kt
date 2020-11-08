package com.simplemobiletools.applauncher.models

import android.graphics.drawable.Drawable
import com.simplemobiletools.commons.helpers.SORT_BY_TITLE
import com.simplemobiletools.commons.helpers.SORT_DESCENDING

data class AppLauncher(val id: Int, var title: String, val packageName: String, var order: Int, val drawable: Drawable? = null) : Comparable<AppLauncher> {
    companion object {
        var sorting = 0
    }

    override fun equals(other: Any?) = packageName.equals((other as AppLauncher).packageName, true)

    fun getBubbleText() = title

    override fun compareTo(other: AppLauncher): Int {
        var result = when {
            sorting and SORT_BY_TITLE != 0 -> title.toLowerCase().compareTo(other.title.toLowerCase())
            else -> order.compareTo(other.order)
        }

        if (sorting and SORT_DESCENDING != 0) {
            result *= -1
        }

        return result
    }
}
