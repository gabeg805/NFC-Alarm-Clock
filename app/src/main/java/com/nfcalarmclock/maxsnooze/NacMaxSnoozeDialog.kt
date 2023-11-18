package com.nfcalarmclock.maxsnooze

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacScrollablePickerDialogFragment

/**
 * Select the max number of snoozes allowed for an alarm.
 */
class NacMaxSnoozeDialog
	: NacScrollablePickerDialogFragment()
{

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacMaxSnoozeDialog"

	}

	/**
	 * The list of values for the scrollable picker.
	 */
	override val scrollablePickerValues: List<String>
		get()
		{
			val array = resources.getStringArray(R.array.max_snooze_summaries)

			return array.asList()
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
			.setTitle(R.string.max_snooze)
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Call the listener
				callOnScrollablePickerOptionSelectedListener()

			}
			.setNegativeButton(R.string.action_cancel, null)
			.setView(R.layout.dlg_scrollable_picker)
			.create()
	}

}