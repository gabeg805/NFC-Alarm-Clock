package com.nfcalarmclock.maxsnooze

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacScrollablePickerDialogFragment

/**
 * Select the max number of snoozes allowed for an alarm.
 */
class NacMaxSnoozeDialog : NacScrollablePickerDialogFragment()
{

	/**
	 * Get the list of values for the scrollable picker.
	 *
	 * @return The list of values for the scrollable picker for the scrollable
	 * picker.
	 */
	override fun getScrollablePickerValues(): List<String>
	{
		return sharedConstants.maxSnoozeSummaries
	}

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(sharedConstants.maxSnooze)
			.setPositiveButton(sharedConstants.actionOk) { _, _ ->

				// Call the listener
				callOnScrollablePickerOptionSelectedListener()

			}
			.setNegativeButton(sharedConstants.actionCancel) { _, _ ->
			}
			.setView(R.layout.dlg_scrollable_picker)
			.create()
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacMaxSnoozeDialog"

	}

}