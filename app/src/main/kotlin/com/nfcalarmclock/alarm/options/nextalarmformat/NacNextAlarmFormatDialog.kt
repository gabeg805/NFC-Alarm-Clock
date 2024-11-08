package com.nfcalarmclock.alarm.options.nextalarmformat

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment

class NacNextAlarmFormatDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when a next alarm format is selected.
	 */
	fun interface OnNextAlarmFormatSelectedListener
	{
		fun onNextAlarmFormatSelected(which: Int)
	}

	/**
	 * Default next alarm format index.
	 *
	 * This will be changed externally.
	 */
	var defaultNextAlarmFormatIndex: Int = 0
		set(value) {
			// Set the current index
			currentSelectedNextAlarmFormatIndex = value

			// Set the backing field
			field = value
		}

	/**
	 * The current next alarm format index.
	 */
	private var currentSelectedNextAlarmFormatIndex: Int = 0

	/**
	 * Listener for when an audio option is clicked.
	 */
	var onNextAlarmFormatListener: OnNextAlarmFormatSelectedListener? = null

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Create list of items to display
		val timeIn = getString(R.string.next_alarm_format_time_in)
		val timeOn = getString(R.string.next_alarm_format_time_on)
		val timeFormats = arrayOf(timeIn, timeOn)

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(R.string.title_next_alarm_format)
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Call the listener
				onNextAlarmFormatListener?.onNextAlarmFormatSelected(currentSelectedNextAlarmFormatIndex)

			}
			.setNegativeButton(R.string.action_cancel, null)
			.setSingleChoiceItems(timeFormats, defaultNextAlarmFormatIndex) { _, which: Int ->

				// Set the current selected index
				currentSelectedNextAlarmFormatIndex = which

			}
			.create()
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacNextAlarmFormatDialog"

	}

}