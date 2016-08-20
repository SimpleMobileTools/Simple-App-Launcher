package com.simplemobiletools.applauncher.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import com.simplemobiletools.applauncher.BuildConfig
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.extensions.isFirstRun
import com.simplemobiletools.applauncher.extensions.preferences
import com.simplemobiletools.applauncher.extensions.viewIntent
import kotlinx.android.synthetic.main.activity_about.*
import java.util.*

class AboutActivity : SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setupEmail()
        setupInvite()
        setupRateUs()
        setupLicense()
        setupSocial()
        setupCopyright()
    }

    private fun setupEmail() {
        val email = getString(R.string.email)
        val appName = getString(R.string.app_name)
        val href = "<a href=\"mailto:$email?subject=$appName\">$email</a>"
        about_email.text = Html.fromHtml(href)
        about_email.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupInvite() {
        val intent = Intent()
        val text = String.format(getString(R.string.share_text), getString(R.string.app_name), getStoreUrl())
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.type = "text/plain"

        about_invite.setOnClickListener {
            startActivity(Intent.createChooser(intent, getString(R.string.invite_via)))
        }
    }

    private fun setupRateUs() {
        if (preferences.isFirstRun) {
            about_rate_us.visibility = View.GONE
        }

        about_rate_us.setOnClickListener {
            startActivity(viewIntent(getRateUsUrl()))
        }
    }

    private fun setupLicense() {
        about_license.setOnClickListener {
            startActivity(Intent(applicationContext, LicenseActivity::class.java))
        }
    }

    private fun setupSocial() {
        about_facebook.setOnClickListener {
            startActivity(viewIntent(getFacebookUrl()))
        }

        about_gplus.setOnClickListener {
            val link = "https://plus.google.com/communities/104880861558693868382"
            startActivity(viewIntent(link))
        }
    }

    private fun setupCopyright() {
        val versionName = BuildConfig.VERSION_NAME
        val year = Calendar.getInstance().get(Calendar.YEAR)
        about_copyright.text = String.format(getString(R.string.copyright), versionName, year)
    }

    private fun getFacebookUrl(): String {
        try {
            packageManager.getPackageInfo("com.facebook.katana", 0)
            return "fb://page/150270895341774"
        } catch (ignored: Exception) {
            return "https://www.facebook.com/simplemobiletools"
        }
    }

    private fun getRateUsUrl(): String {
        try {
            return "market://details?id=" + packageName
        } catch (ignored: ActivityNotFoundException) {
            return getStoreUrl()
        }
    }

    private fun getStoreUrl(): String {
        return "https://play.google.com/store/apps/details?id=" + packageName
    }
}
