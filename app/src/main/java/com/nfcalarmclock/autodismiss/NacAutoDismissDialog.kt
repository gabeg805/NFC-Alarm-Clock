package com.nfcalarmclock.autodismiss

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacScrollablePickerDialogFragment

/**
 * Select an amount of time to auto dismiss an alarm.
 */
class NacAutoDismissDialog
	: NacScrollablePickerDialogFragment()
{

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacAutoDismissDialog"

	}

	/**
	 * The list of values for the scrollable picker.
	 */
	override val scrollablePickerValues: List<String>
		get()
		{
			val array = resources.getStringArray(R.array.auto_dismiss_summaries)

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
			.setTitle(R.string.auto_dismiss)
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Call the listener
				callOnScrollablePickerOptionSelectedListener()

			}
			.setNegativeButton(R.string.action_cancel, null)
			.setView(R.layout.dlg_scrollable_picker)
			.create()
	}

}