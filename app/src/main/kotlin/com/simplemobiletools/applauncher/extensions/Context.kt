package com.simplemobiletools.applauncher.extensions

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import com.simplemobiletools.applauncher.helpers.Config
import com.simplemobiletools.applauncher.helpers.DBHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.helpers.isLollipopPlus

val Context.config: Config get() = Config.newInstance(applicationContext)

val Context.dbHelper: DBHelper get() = DBHelper.newInstance(applicationContext)

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun Context.getNotDisplayedLaunchers(displayedLaunchers: ArrayList<AppLauncher>): ArrayList<AppLauncher> {
    val allApps = ArrayList<AppLauncher>()
    val intent = Intent(Intent.ACTION_MAIN, null)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    val list = packageManager.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED)
    for (info in list) {
        val componentInfo = info.activityInfo.applicationInfo
        val label = componentInfo.loadLabel(packageManager).toString()
        val packageName = componentInfo.packageName

        var drawable: Drawable? = null
        if (isLollipopPlus()) {
            try {
                // try getting the properly colored launcher icons
                val launcher = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
                val activityList = launcher.getActivityList(packageName, android.os.Process.myUserHandle())[0]
                drawable = activityList.getBadgedIcon(0)
            } catch (e: Exception) {
            }
        }

        if (drawable == null) {
            drawable = if (packageName.isAPredefinedApp()) {
                resources.getLauncherDrawable(packageName)
            } else {
                packageManager.getApplicationIcon(packageName)
            }
        }

        allApps.add(AppLauncher(0, label, packageName, drawable))
    }

    val sorted = allApps.sortedWith(compareBy { it.name.toLowerCase() })
    val unique = sorted.distinctBy { it.packageName }
    return unique.filter { !displayedLaunchers.contains(it) && it.packageName != "com.simplemobiletools.applauncher" } as ArrayList<AppLauncher>
}
