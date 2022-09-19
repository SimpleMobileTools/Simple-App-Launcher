package com.simplemobiletools.applauncher.extensions

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.simplemobiletools.applauncher.helpers.Config
import com.simplemobiletools.applauncher.helpers.DBHelper
import com.simplemobiletools.applauncher.models.AppLauncher
import com.simplemobiletools.commons.helpers.SORT_BY_CUSTOM

val Context.config: Config get() = Config.newInstance(applicationContext)

val Context.dbHelper: DBHelper get() = DBHelper.newInstance(applicationContext)

fun Context.getAllLaunchers(): ArrayList<AppLauncher> {
    val allApps = ArrayList<AppLauncher>()
    val allPackageNames = ArrayList<String>()
    val intent = Intent(Intent.ACTION_MAIN, null)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)

    val list = packageManager.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED)
    for (info in list) {
        val componentInfo = info.activityInfo.applicationInfo
        val label = info.loadLabel(packageManager).toString()
        val packageName = componentInfo.packageName

        var drawable: Drawable? = null
        try {
            // try getting the properly colored launcher icons
            val launcher = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val activityList = launcher.getActivityList(packageName, android.os.Process.myUserHandle())[0]
            drawable = activityList.getBadgedIcon(0)
        } catch (e: Exception) {
        } catch (e: Error) {
        }

        if (drawable == null) {
            drawable = if (packageName.isAPredefinedApp()) {
                resources.getLauncherDrawable(packageName)
            } else {
                try {
                    packageManager.getApplicationIcon(packageName)
                } catch (ignored: Exception) {
                    continue
                }
            }
        }

        allPackageNames.add(packageName)
        allApps.add(AppLauncher(0, label, packageName, 0, drawable))
    }

    dbHelper.getLaunchers().forEach { launcher ->
        if (!allPackageNames.contains(launcher.packageName)) {
            allApps.add(launcher)
        }
    }

    if (config.sorting and SORT_BY_CUSTOM != 0) {
        allApps.sortBy { it.title.toLowerCase() }
    } else {
        AppLauncher.sorting = config.sorting
        allApps.sort()
    }

    val unique = allApps.distinctBy { it.packageName }
    return unique.filter { it.packageName != "com.simplemobiletools.applauncher" } as ArrayList<AppLauncher>
}
