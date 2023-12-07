package com.nfcalarmclock.snoozeduration

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacScrollablePickerDialogFragment

/**
 * Select how long snoozing an alarm should be.
 */
class NacSnoozeDurationDialog
	: NacScrollablePickerDialogFragment()
{

	/**
	 * The list of values for the scrollable picker.
	 */
	override val scrollablePickerValues: List<String>
		get()
		{
			val array = resources.getStringArray(R.array.snooze_duration_summaries)

			return array.asList()
		}

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences.
		setupSharedPreferences()

		// Created the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(R.string.snooze_duration)
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Call the listener
				callOnScrollablePickerOptionSelectedListener()

			}
			.setNegativeButton(R.string.action_cancel, null)
			.setView(R.layout.dlg_scrollable_picker)
			.create()
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacSnoozeDurationDialog"

	}

}