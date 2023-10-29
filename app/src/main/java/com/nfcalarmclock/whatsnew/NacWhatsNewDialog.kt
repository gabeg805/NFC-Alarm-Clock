package com.nfcalarmclock.whatsnew

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import com.nfcalarmclock.BuildConfig
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment

/**
 * Show what is new with the app after an update.
 */
class NacWhatsNewDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when what's new dialog has been read.
	 */
	fun interface OnReadWhatsNewListener
	{
		fun onReadWhatsNew()
	}

	/**
	 * Listener for when a text-to-speech option and frequency is selected.
	 */
	var onReadWhatsNewListener: OnReadWhatsNewListener? = null

	/**
	 * Called when the dialog is canceled.
	 */
	override fun onCancel(dialog: DialogInterface)
	{
		// Call the listener
		onReadWhatsNewListener?.onReadWhatsNew()
	}

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Get the title
		val title = getString(R.string.title_whats_new)

		// Get the action button
		val ok = getString(R.string.action_ok)

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(title)
			.setPositiveButton(ok) { _, _ ->

				// Call the listener
				onReadWhatsNewListener?.onReadWhatsNew()

			}
			.setView(R.layout.dlg_whats_new)
			.create()
	}

	/**
	 * Called when the fragment is resumed
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Get the text view
		val textView = dialog!!.findViewById<TextView>(R.id.whats_new_version)

		// Setup the version
		setupVersion(textView)
	}

	/**
	 * Setup the version.
	 */
	private fun setupVersion(textView: TextView)
	{
		// Prepare the strings
		val versionWord = textView.text.toString()
		val versionName = BuildConfig.VERSION_NAME
		val versionNameAndNum = String.format("%s %s", versionWord, versionName)

		// Set the version
		textView.text = versionNameAndNum
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacWhatsNewDialog"
	}

}