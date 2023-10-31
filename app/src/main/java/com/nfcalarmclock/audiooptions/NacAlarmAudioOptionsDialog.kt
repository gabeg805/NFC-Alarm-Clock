package com.nfcalarmclock.audiooptions

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment

/**
 * Show the audio options of the alarm.
 */
class NacAlarmAudioOptionsDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when an audio option is clicked.
	 */
	fun interface OnAudioOptionClickedListener
	{
		fun onAudioOptionClicked(alarmId: Long, which: Int)
	}

	/**
	 * Alarm ID.
	 */
	var alarmId: Long = 0

	/**
	 * Listener for when an audio option is clicked.
	 */
	var onAudioOptionClickedListener: OnAudioOptionClickedListener? = null

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(R.string.title_audio_option)
			.setItems(R.array.audio_options) { _, which: Int ->

				// Call the listener
				onAudioOptionClickedListener?.onAudioOptionClicked(alarmId, which)

			}
			.create()
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacAlarmAudioOptionsDialog"

	}

}