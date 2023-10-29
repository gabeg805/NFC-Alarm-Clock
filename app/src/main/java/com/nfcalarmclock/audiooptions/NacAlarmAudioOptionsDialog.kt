package com.nfcalarmclock.audiooptions

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.nfcalarmclock.R

/**
 * Show the audio options of the alarm.
 */
class NacAlarmAudioOptionsDialog
	: DialogFragment()
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
	 * Call the OnAudioOptionClickedListener object, if it has been set.
	 *
	 * @param  which  Which item was clicked.
	 */
	private fun callOnAudioOptionClickedListener(which: Int)
	{
		onAudioOptionClickedListener?.onAudioOptionClicked(alarmId, which)
	}

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Get the title
		val title = getString(R.string.title_audio_option)

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(title)
			.setItems(R.array.audio_options) { _, which: Int ->
				callOnAudioOptionClickedListener(which)
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