package com.simplemobiletools.applauncher.activities

import android.os.Bundle
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.extensions.config
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.NavigationIcon
import com.simplemobiletools.commons.helpers.isTiramisuPlus
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*
import kotlin.system.exitProcess

class SettingsActivity : SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        updateMaterialActivityViews(settings_coordinator, settings_holder, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(settings_nested_scrollview, settings_toolbar)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(settings_toolbar, NavigationIcon.Arrow)

        setupPurchaseThankYou()
        setupCustomizeColors()
        setupUseEnglish()
        setupLanguage()
        setupCloseApp()
        updateTextColors(settings_holder)

        arrayOf(settings_color_customization_section_label, settings_general_settings_label).forEach {
            it.setTextColor(getProperPrimaryColor())
        }
    }

    private fun setupPurchaseThankYou() {
        settings_purchase_thank_you_holder.beGoneIf(isOrWasThankYouInstalled())
        settings_purchase_thank_you_holder.setOnClickListener {
            launchPurchaseThankYouIntent()
        }
    }

    private fun setupCustomizeColors() {
        settings_color_customization_label.text = getCustomizeColorsString()
        settings_color_customization_holder.setOnClickListener {
            handleCustomizeColorsClick()
        }
    }

    private fun setupUseEnglish() {
        settings_use_english_holder.beVisibleIf((config.wasUseEnglishToggled || Locale.getDefault().language != "en") && !isTiramisuPlus())
        settings_use_english.isChecked = config.useEnglish
        settings_use_english_holder.setOnClickListener {
            settings_use_english.toggle()
            config.useEnglish = settings_use_english.isChecked
            exitProcess(0)
        }
    }

    private fun setupLanguage() {
        settings_language.text = Locale.getDefault().displayLanguage
        settings_language_holder.beVisibleIf(isTiramisuPlus())
        settings_language_holder.setOnClickListener {
            launchChangeAppLanguageIntent()
        }
    }

    private fun setupCloseApp() {
        settings_close_app.isChecked = config.closeApp
        settings_close_app_holder.setOnClickListener {
            settings_close_app.toggle()
            config.closeApp = settings_close_app.isChecked
        }
    }
}
