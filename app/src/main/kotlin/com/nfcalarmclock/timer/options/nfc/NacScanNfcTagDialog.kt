package com.nfcalarmclock.timer.options.nfc

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.nfc.NacScanNfcTagDialog
import com.nfcalarmclock.system.getTimer
import dagger.hilt.android.AndroidEntryPoint

/**
 * Scan an NFC tag that will be used to dismiss the given timer when it goes
 * off.
 */
@AndroidEntryPoint
class NacScanNfcTagDialog
	: NacScanNfcTagDialog()
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

		// Change the description for the timer
		val scanNfcTagDescription: TextView = view.findViewById(R.id.scan_nfc_tag_description)

		scanNfcTagDescription.setText(R.string.description_scan_nfc_tag_timer)
	}

}