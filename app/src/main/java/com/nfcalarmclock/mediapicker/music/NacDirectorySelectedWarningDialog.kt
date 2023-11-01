package com.nfcalarmclock.mediapicker.music

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
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
		fun onDirectoryConfirmed(view: View)
	}

	/**
	 * Selected view.
	 */
	var selectedView: View? = null

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
				onDirectoryConfirmedListener?.onDirectoryConfirmed(selectedView!!)

			}
			.setNegativeButton(R.string.action_cancel, null)
			.setView(R.layout.dlg_media_playlist)
			.create()
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacDirectorySelectedWarningDialog"

	}

}