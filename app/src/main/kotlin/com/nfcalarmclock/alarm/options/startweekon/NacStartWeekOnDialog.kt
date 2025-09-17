package com.nfcalarmclock.alarm.options.startweekon

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment

/**
 * Show a dialog asking the user which day they want to start the week on.
 */
class NacStartWeekOnDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when a start week is selected.
	 */
	fun interface OnStartWeekSelectedListener
	{
		fun onStartWeekSelected(which: Int)
	}

	/**
	 * Default start week on index.
	 *
	 * This will be changed externally.
	 */
	var defaultStartWeekOnIndex: Int = 0

	/**
	 * The current start week on index.
	 */
	private var currentSelectedStartWeekOnIndex: Int = defaultStartWeekOnIndex

	/**
	 * Listener for when an audio option is clicked.
	 */
	var onStartWeekSelectedListener: OnStartWeekSelectedListener? = null

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(R.string.title_start_week_on)
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Call the listener
				onStartWeekSelectedListener?.onStartWeekSelected(currentSelectedStartWeekOnIndex)

			}
			.setNegativeButton(R.string.action_cancel, null)
			.setSingleChoiceItems(R.array.start_week_on, defaultStartWeekOnIndex) { _, which: Int ->

				// Set the current selected index
				currentSelectedStartWeekOnIndex = which

			}
			.create()
	}

	/**
	 * Called when the fragment is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Set the dialog background color
		dialog!!.window?.setBackgroundDrawableResource(R.color.gray)
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacStartWeekOnDialog"

	}

}