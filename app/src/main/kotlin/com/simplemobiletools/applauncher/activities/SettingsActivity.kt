package com.simplemobiletools.applauncher.activities

import android.os.Bundle
import com.simplemobiletools.applauncher.databinding.ActivitySettingsBinding
import com.simplemobiletools.applauncher.extensions.config
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.NavigationIcon
import com.simplemobiletools.commons.helpers.isTiramisuPlus
import java.util.Locale
import kotlin.system.exitProcess

class SettingsActivity : SimpleActivity() {
    private val binding by viewBinding(ActivitySettingsBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            updateMaterialActivityViews(settingsCoordinator, settingsHolder, useTransparentNavigation = true, useTopSearchMenu = false)
            setupMaterialScrollListener(settingsNestedScrollview, settingsToolbar)
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.settingsToolbar, NavigationIcon.Arrow)

        setupPurchaseThankYou()
        setupCustomizeColors()
        setupUseEnglish()
        setupLanguage()
        setupCloseApp()
        updateTextColors(binding.settingsHolder)

        binding.apply {
            arrayOf(settingsColorCustomizationSectionLabel, settingsGeneralSettingsLabel).forEach {
                it.setTextColor(getProperPrimaryColor())
            }
        }
    }

    private fun setupPurchaseThankYou() {
        binding.settingsPurchaseThankYouHolder.apply {
            beGoneIf(isOrWasThankYouInstalled())
            setOnClickListener {
                launchPurchaseThankYouIntent()
            }
        }
    }

    private fun setupCustomizeColors() {
        binding.apply {
            settingsColorCustomizationLabel.text = getCustomizeColorsString()
            settingsColorCustomizationHolder.setOnClickListener {
                handleCustomizeColorsClick()
            }
        }
    }

    private fun setupUseEnglish() {
        binding.apply {
            settingsUseEnglishHolder.beVisibleIf((config.wasUseEnglishToggled || Locale.getDefault().language != "en") && !isTiramisuPlus())
            settingsUseEnglish.isChecked = config.useEnglish
            settingsUseEnglishHolder.setOnClickListener {
                settingsUseEnglish.toggle()
                config.useEnglish = settingsUseEnglish.isChecked
                exitProcess(0)
            }
        }
    }

    private fun setupLanguage() {
        binding.apply {
            settingsLanguage.text = Locale.getDefault().displayLanguage
            settingsLanguageHolder.beVisibleIf(isTiramisuPlus())
            settingsLanguageHolder.setOnClickListener {
                launchChangeAppLanguageIntent()
            }
        }
    }

    private fun setupCloseApp() {
        binding.apply {
            settingsCloseApp.isChecked = config.closeApp
            settingsCloseAppHolder.setOnClickListener {
                settingsCloseApp.toggle()
                config.closeApp = settingsCloseApp.isChecked
            }
        }
    }
}
