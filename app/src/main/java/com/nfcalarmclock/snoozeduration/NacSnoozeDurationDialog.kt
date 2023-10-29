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

		// Get the name of the title
		val title = getString(R.string.snooze_duration)

		// Get the name of the actions
		val ok = getString(R.string.action_ok)
		val cancel = getString(R.string.action_cancel)

		// Created the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(title)
			.setPositiveButton(ok) { _, _ ->

				// Call the listener
				callOnScrollablePickerOptionSelectedListener()

			}
			.setNegativeButton(cancel) { _, _ ->
			}
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