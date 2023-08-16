package com.simplemobiletools.applauncher.dialogs

import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.databinding.DialogChangeSortingBinding
import com.simplemobiletools.applauncher.extensions.config
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.beGoneIf
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.helpers.SORT_BY_CUSTOM
import com.simplemobiletools.commons.helpers.SORT_BY_TITLE
import com.simplemobiletools.commons.helpers.SORT_DESCENDING

class ChangeSortingDialog(val activity: BaseSimpleActivity, private val callback: () -> Unit) {
    private var currSorting = 0
    private var config = activity.config
    private val binding = DialogChangeSortingBinding.inflate(activity.layoutInflater)

    init {
        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.sort_by)
            }

        currSorting = config.sorting
        setupSortRadio()
        setupOrderRadio()
    }

    private fun setupSortRadio() {
        binding.apply {
            sortingDialogRadioSorting.setOnCheckedChangeListener { group, checkedId ->
                val isCustomSorting = checkedId == sortingDialogRadioCustom.id
                sortingDialogRadioOrder.beGoneIf(isCustomSorting)
                sortingDialogDivider.beGoneIf(isCustomSorting)
            }
        }

        val sortBtn = when {
            currSorting and SORT_BY_TITLE != 0 -> binding.sortingDialogRadioTitle
            else -> binding.sortingDialogRadioCustom
        }
        sortBtn.isChecked = true
    }

    private fun setupOrderRadio() {
        var orderBtn = binding.sortingDialogRadioAscending

        if (currSorting and SORT_DESCENDING != 0) {
            orderBtn = binding.sortingDialogRadioDescending
        }
        orderBtn.isChecked = true
    }

    private fun dialogConfirmed() {
        val sortingRadio = binding.sortingDialogRadioSorting
        var sorting = when (sortingRadio.checkedRadioButtonId) {
            R.id.sorting_dialog_radio_title -> SORT_BY_TITLE
            else -> SORT_BY_CUSTOM
        }

        if (sortingRadio.checkedRadioButtonId != R.id.sorting_dialog_radio_custom && binding.sortingDialogRadioOrder.checkedRadioButtonId == R.id.sorting_dialog_radio_descending) {
            sorting = sorting or SORT_DESCENDING
        }

        config.sorting = sorting
        callback()
    }
}
