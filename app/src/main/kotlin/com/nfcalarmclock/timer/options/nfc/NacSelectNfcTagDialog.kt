package com.nfcalarmclock.timer.options.nfc

import android.view.View
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.nfc.NacSelectNfcTagDialog
import com.nfcalarmclock.system.getTimer
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.view.setupSwitchColor
import dagger.hilt.android.AndroidEntryPoint

/**
 * Select NFC tag(s) to dismiss an alarm.
 */
@AndroidEntryPoint
class NacSelectNfcTagDialog
	: NacSelectNfcTagDialog()
{

	/**
	 * Start timer on NFC tag scan switch.
	 */
	private lateinit var startTimerOnScanSwitch: SwitchCompat

	/**
	 * Get the alarm/timer argument from the fragment.
	 */
	override fun getFragmentArgument(): NacAlarm?
	{
		return arguments?.getTimer()
	}

	/**
	 * Update the timer with selected options.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// Super
		super.onOkClicked(alarm)

		// Update the timer
		(alarm as NacTimer?)?.shouldScanningNfcTagStartTimer = startTimerOnScanSwitch.isChecked
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		// Super
		super.setupAlarmOptions(alarm)

		// Get the views
		val view = requireView()
		val selectNfcTagDescription: TextView = view.findViewById(R.id.select_nfc_tag_description)
		val startTimerOnScanContainer: RelativeLayout = view.findViewById(R.id.start_timer_on_nfc_tag_scan_container)
		val startTimerOnScanSeparator: Space = view.findViewById(R.id.start_timer_on_nfc_tag_scan_separator)
		startTimerOnScanSwitch = view.findViewById(R.id.start_timer_on_nfc_tag_scan_switch)

		// Change the description for the timer
		selectNfcTagDescription.setText(R.string.description_select_nfc_tag_timer)

		// Set the visibility
		startTimerOnScanContainer.visibility = View.VISIBLE
		startTimerOnScanSeparator.visibility = View.VISIBLE

		// Setup the start timer on scan switch
		startTimerOnScanSwitch.setupSwitchColor(sharedPreferences)

		// Click listener for start timer
		startTimerOnScanContainer.setOnClickListener {
			startTimerOnScanSwitch.toggle()
		}

		// Set the start timer on scan switch
		startTimerOnScanSwitch.isChecked = (alarm as NacTimer?)?.shouldScanningNfcTagStartTimer ?: false
	}

}