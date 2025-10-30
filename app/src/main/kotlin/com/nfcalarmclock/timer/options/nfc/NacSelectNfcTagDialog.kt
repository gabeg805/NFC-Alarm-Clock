package com.nfcalarmclock.timer.options.nfc

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.nfc.NacSelectNfcTagDialog
import com.nfcalarmclock.system.getTimer
import dagger.hilt.android.AndroidEntryPoint

/**
 * Select NFC tag(s) to dismiss an alarm.
 */
@AndroidEntryPoint
class NacSelectNfcTagDialog
	: NacSelectNfcTagDialog()
{

	/**
	 * Get the alarm/timer argument from the fragment.
	 */
	override fun getFragmentArgument(): NacAlarm?
	{
		return arguments?.getTimer()
	}

	/**
	 * Called when the view has been created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the view
		val selectNfcTagDescription: TextView = view.findViewById(R.id.select_nfc_tag_description)

		// Change the description for the timer
		selectNfcTagDescription.setText(R.string.description_select_nfc_tag_timer)
	}

}