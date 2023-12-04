package com.nfcalarmclock.alarmoptions

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.children
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment

/**
 * Show the options for an alarm.
 */
class NacAlarmOptionsDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when an audio option is clicked.
	 */
	fun interface OnAlarmOptionClickedListener
	{
		fun onAlarmOptionClicked(alarmId: Long, id: Int)
	}

	/**
	 * Alarm ID.
	 */
	var alarmId: Long = 0

	/**
	 * Listener for when an alarm option is clicked.
	 */
	var onAlarmOptionClickedListener: OnAlarmOptionClickedListener? = null

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
			.setView(R.layout.dlg_alarm_options)
			.create()
	}

	/**
	 * Called when the fragment is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Get the container of the dialog and the text view
		val container = dialog!!.findViewById<LinearLayout>(R.id.all_alarm_options)

		// Iterate over each child in the container
		for (view in container.children)
		{
			// Check if this is the divider
			if (view.id == R.id.alarm_option_divider)
			{
				setupViewThemeColor(view)
			}
			// View is a textview
			else
			{
				// Set the listener
				view.setOnClickListener {

					// Call the listener
					onAlarmOptionClickedListener?.onAlarmOptionClicked(alarmId, it.id)

					// Dismiss the dialog
					dismiss()

				}
			}
		}
	}

	/**
	 * Setup the view with the theme color.
	 */
	private fun setupViewThemeColor(view: View)
	{
		view.backgroundTintList = ColorStateList.valueOf(sharedPreferences!!.themeColor)
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacAlarmOptionsDialog"

	}

}