package com.nfcalarmclock.mediapicker.music

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.google.android.material.checkbox.MaterialCheckBox
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment

class NacDirectorySelectedWarningDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when a user confirms they want to select a directory.
	 */
	fun interface OnDirectoryConfirmedListener
	{
		fun onDirectoryConfirmed(shuffleMedia: Boolean, recusivelyPlayMedia: Boolean)
	}

	/**
	 * Default whether media should be shuffled.
	 *
	 * This will be set externally before the dialog is shown.
	 */
	var defaultShouldShuffleMedia = false

	/**
	 * Default whether media should be recursively played.
	 *
	 * This will be set externally before the dialog is shown.
	 */
	var defaultShouldRecursivelyPlayMedia = false

	/**
	 * Check box for whether the media should be shuffled.
	 */
	private var shuffleMediaCheckBox: MaterialCheckBox? = null

	/**
	 * Check box for whether the media should be recursively played.
	 */
	private var recursivelyPlayMediaCheckBox: MaterialCheckBox? = null

	/**
	 * Listener for when the user confirms they want to select a directory.
	 */
	var onDirectoryConfirmedListener: OnDirectoryConfirmedListener? = null

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(R.string.title_folder_selected)
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Call the listener
				onDirectoryConfirmedListener?.onDirectoryConfirmed(
					shuffleMediaCheckBox!!.isChecked,
					recursivelyPlayMediaCheckBox!!.isChecked)

			}
			.setNegativeButton(R.string.action_cancel, null)
			.setView(R.layout.dlg_media_playlist)
			.create()
	}

	/**
	 * Called when the fragment is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Set the member variable
		shuffleMediaCheckBox = dialog!!.findViewById(R.id.shuffle_media_checkbox) as MaterialCheckBox
		recursivelyPlayMediaCheckBox = dialog!!.findViewById(R.id.recursively_play_media_checkbox) as MaterialCheckBox

		// Set the status of the checkbox
		shuffleMediaCheckBox!!.isChecked = defaultShouldShuffleMedia
		recursivelyPlayMediaCheckBox!!.isChecked = defaultShouldRecursivelyPlayMedia
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacDirectorySelectedWarningDialog"

	}

}