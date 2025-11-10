package com.nfcalarmclock.timer.options.nfc

import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.navigation.NavDestination
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.nfc.NacScanNfcTagDialog
import com.nfcalarmclock.system.addTimer
import com.nfcalarmclock.system.getTimer
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.view.setupSwitchColor
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
	 * Start timer on NFC scan switch.
	 */
	private val startTimerOnScanSwitch: SwitchCompat by lazy {
		requireView().findViewById(R.id.start_timer_on_nfc_tag_scan_switch)
	}

	/**
	 * Add an alarm/timer argument to a Bundle.
	 *
	 * @return An alarm/timer argument added to a Bundle.
	 */
	override fun <T: NacAlarm> addFragmentArgument(item: T?): Bundle
	{
		return Bundle().addTimer(item as NacTimer?)
	}

	/**
	 * Get the alarm/timer argument from the fragment.
	 */
	override fun getFragmentArgument(): NacAlarm?
	{
		return arguments?.getTimer()
	}

	/**
	 * Get the navigation destination ID for the Save NFC Tag dialog.
	 *
	 * @return The navigation destination ID for the Save NFC Tag dialog.
	 */
	override fun getSaveNfcTagDialogId(currentDestination: NavDestination?): Int
	{
		// Normal option
		return if (currentDestination?.id == R.id.nacScanNfcTagDialog3)
		{
			R.id.nacSaveNfcTagDialog3
		}
		// TODO: Quick option
		else
		{
			R.id.nacSaveNfcTagDialog3
		}
	}

	/**
	 * Get the navigation destination ID for the Select NFC Tag dialog.
	 *
	 * @return The navigation destination ID for the Select NFC Tag dialog.
	 */
	override fun getSelectNfcTagDialogId(currentDestination: NavDestination?): Int
	{
		// Normal option
		return if (currentDestination?.id == R.id.nacScanNfcTagDialog3)
		{
			R.id.nacSelectNfcTagDialog3
		}
		// TODO: Quick option
		else
		{
			R.id.nacSelectNfcTagDialog3
		}
	}

	/**
	 * OK buton is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// Super
		super.onOkClicked(alarm)

		// Set the start timer on scan flag
		println("Start timer on scan? ${startTimerOnScanSwitch.isChecked}")
		(alarm as NacTimer?)?.shouldScanningNfcTagStartTimer = startTimerOnScanSwitch.isChecked
	}

	/**
	 * Use any NFC tag was clicked.
	 */
	override fun onUseAnyNfcTagClicked(alarm: NacAlarm?)
	{
		// Clear the start timer on NFC scan flag
		(alarm as NacTimer?)?.shouldScanningNfcTagStartTimer = false

		// Super
		super.onUseAnyNfcTagClicked(alarm)
	}

	/**
	 * View has been created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Change the description for the timer
		val scanNfcTagDescription: TextView = view.findViewById(R.id.scan_nfc_tag_description)
		scanNfcTagDescription.setText(R.string.description_scan_nfc_tag_timer)
	}

	/**
	 * Setup the start timer on scan.
	 */
	override fun setupStartTimerOnScan(alarm: NacAlarm?)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.start_timer_on_nfc_tag_scan_container)
		val description: TextView = dialog!!.findViewById(R.id.start_timer_on_nfc_tag_scan_description)
		val separator: Space = dialog!!.findViewById(R.id.start_timer_on_nfc_tag_scan_separator)

		// Set the visibility
		relativeLayout.visibility = View.VISIBLE
		separator.visibility = View.VISIBLE

		// Setup the switch
		startTimerOnScanSwitch.setupSwitchColor(sharedPreferences)
		startTimerOnScanSwitch.isChecked = (alarm as NacTimer?)?.shouldScanningNfcTagStartTimer == true

		// Set the relative layout listener
		relativeLayout.setOnClickListener {

			// Toggle the switch
			startTimerOnScanSwitch.toggle()

		}

		// Set the description depending on how many NFC tags are used
		if ((alarm != null) && (alarm.nfcTagId.isNotEmpty()))
		{
			// Determine the string to use
			val stringId = if (alarm.nfcTagIdList.size == 1)
			{
				// 1 NFC tag
				println("SINGLE NFC TAG")
				R.string.description_start_timer_on_single_nfc_tag_scan
			}
			else
			{
				// 2+ NFC tags
				println("MULTIPLE NFC TAGS")
				R.string.description_start_timer_on_multiple_nfc_tag_scan
			}

			// Set the description
			println("Setting description of start timer!")
			description.setText(stringId)
		}
	}

}