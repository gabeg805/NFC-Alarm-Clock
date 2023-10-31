package com.nfcalarmclock.view.dialog

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import com.nfcalarmclock.shared.NacSharedConstants
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Helper class to create dialogs.
 */
abstract class NacDialogFragment
	: DialogFragment()
{

	/**
	 * Shared preferences.
	 */
	protected var sharedPreferences: NacSharedPreferences? = null
		private set

	/**
	 * Shared constants.
	 */
	protected val sharedConstants: NacSharedConstants
		get() = sharedPreferences!!.constants

	/**
	 * Called when the fragment is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Setup the color
		setupDialogColor()
	}

	/**
	 * Setup the dialog color.
	 */
	protected fun setupDialogColor()
	{
		// Get the buttons
		val alertDialog = dialog as AlertDialog?
		val okButton = alertDialog!!.getButton(DialogInterface.BUTTON_POSITIVE)
		val cancelButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
		val neutralButton = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL)

		// Get the theme color
		val themeColor = sharedPreferences!!.themeColor

		// Set the colors
		okButton.setTextColor(themeColor)
		cancelButton?.setTextColor(themeColor)
		neutralButton?.setTextColor(themeColor)
	}

	/**
	 * Setup the shared preferences.
	 */
	protected fun setupSharedPreferences()
	{
		sharedPreferences = NacSharedPreferences(context)
	}

}