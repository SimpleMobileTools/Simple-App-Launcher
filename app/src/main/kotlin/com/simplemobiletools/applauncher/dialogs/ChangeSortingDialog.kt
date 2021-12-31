package com.simplemobiletools.applauncher.dialogs

import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.applauncher.R
import com.simplemobiletools.applauncher.extensions.config
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.beGoneIf
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.helpers.SORT_BY_CUSTOM
import com.simplemobiletools.commons.helpers.SORT_BY_TITLE
import com.simplemobiletools.commons.helpers.SORT_DESCENDING
import kotlinx.android.synthetic.main.dialog_change_sorting.view.*

class ChangeSortingDialog(val activity: BaseSimpleActivity, private val callback: () -> Unit) {
    private var currSorting = 0
    private var config = activity.config
    private var view = activity.layoutInflater.inflate(R.layout.dialog_change_sorting, null)

    init {
        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.sort_by)
            }

        currSorting = config.sorting
        setupSortRadio()
        setupOrderRadio()
    }

    private fun setupSortRadio() {
        val sortingRadio = view.sorting_dialog_radio_sorting
        sortingRadio.setOnCheckedChangeListener { group, checkedId ->
            val isCustomSorting = checkedId == sortingRadio.sorting_dialog_radio_custom.id
            view.sorting_dialog_radio_order.beGoneIf(isCustomSorting)
            view.sorting_dialog_divider.beGoneIf(isCustomSorting)
        }

        val sortBtn = when {
            currSorting and SORT_BY_TITLE != 0 -> sortingRadio.sorting_dialog_radio_title
            else -> sortingRadio.sorting_dialog_radio_custom
        }
        sortBtn.isChecked = true
    }

    private fun setupOrderRadio() {
        val orderRadio = view.sorting_dialog_radio_order
        var orderBtn = orderRadio.sorting_dialog_radio_ascending

        if (currSorting and SORT_DESCENDING != 0) {
            orderBtn = orderRadio.sorting_dialog_radio_descending
        }
        orderBtn.isChecked = true
    }

    private fun dialogConfirmed() {
        val sortingRadio = view.sorting_dialog_radio_sorting
        var sorting = when (sortingRadio.checkedRadioButtonId) {
            R.id.sorting_dialog_radio_title -> SORT_BY_TITLE
            else -> SORT_BY_CUSTOM
        }

        if (sortingRadio.checkedRadioButtonId != R.id.sorting_dialog_radio_custom && view.sorting_dialog_radio_order.checkedRadioButtonId == R.id.sorting_dialog_radio_descending) {
            sorting = sorting or SORT_DESCENDING
        }

        config.sorting = sorting
        callback()
    }
}
