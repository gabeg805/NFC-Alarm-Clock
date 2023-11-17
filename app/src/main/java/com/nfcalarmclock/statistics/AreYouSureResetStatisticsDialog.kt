package com.nfcalarmclock.statistics

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment

class AreYouSureResetStatisticsDialog
	: NacDialogFragment()
{

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "AreYouSureDialog"

	}

	/**
	 * Listener for when the user has indicated they would like to reset
	 * statistics.
	 */
	fun interface OnResetStatisticsListener
	{
		fun onResetStatistics()
	}

	/**
	 * Listener for when the user has indicated they would like to reset
	 * statistics.
	 */
	var onResetStatisticsListener: OnResetStatisticsListener? = null

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Build the dialog
		return AlertDialog.Builder(requireContext())
			.setPositiveButton(R.string.action_yes) { _, _ ->

				// Call the listener
				onResetStatisticsListener?.onResetStatistics()

			}
			.setNegativeButton(R.string.action_no, null)
			.setTitle(R.string.title_reset_statistics)
			.setMessage(R.string.message_reset_statistics)
			.create()
	}

}