package com.nfcalarmclock.timer.options.dismissoptions

import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.dismissoptions.NacDismissOptionsDialog
import com.nfcalarmclock.system.getTimer
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.timer.db.NacTimer

/**
 * Dismiss options.
 */
class NacDismissOptionsDialog
	: NacDismissOptionsDialog()
{

	/**
	 * Get the alarm/timer argument from the fragment.
	 */
	override fun getFragmentArgument(): NacAlarm?
	{
		return arguments?.getTimer()
	}

	/**
	 * View has been created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the views
		val autoDismissDescription: TextView = view.findViewById(R.id.auto_dismiss_description)
		val dismissEarlyContainer: RelativeLayout = dialog!!.findViewById(R.id.dismiss_early_container)
		val dismissEarlyInputLayout: TextInputLayout = dialog!!.findViewById(R.id.dismiss_early_input_layout)
		val dismissEarlyNotificationContainer: RelativeLayout = dialog!!.findViewById(R.id.dismiss_early_notification_container)
		val dismissEarlyNotificationSeparator: Space = dialog!!.findViewById(R.id.dismiss_early_notification_separator)
		val deleteAfterDismissedDescription: TextView = view.findViewById(R.id.delete_after_dismissed_description)

		// Change the description for the timer
		autoDismissDescription.setText(R.string.description_dismiss_options_auto_dismiss_timer)
		deleteAfterDismissedDescription.setText(R.string.description_dismiss_options_delete_after_dismissed_timer)

		// Hide the views
		dismissEarlyContainer.visibility = View.GONE
		dismissEarlyInputLayout.visibility = View.GONE
		dismissEarlyNotificationContainer.visibility = View.GONE
		dismissEarlyNotificationSeparator.visibility = View.GONE
	}

	companion object
	{

		/**
		 * Dialog name.
		 */
		const val TAG = "NacDismissOptionsDialog"

		/**
		 * Create a dialog that can be shown easily.
		 */
		fun create(timer: NacTimer): NacDismissOptionsDialog
		{
			// Create the dialog
			val dialog = com.nfcalarmclock.timer.options.dismissoptions.NacDismissOptionsDialog()

			// Add the timer to the dialog
			dialog.arguments = timer.toBundle()

			return dialog
		}

	}

}