package com.nfcalarmclock.timer.options.nfc

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.navigation.NavDestination
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.nfc.NacScanNfcTagDialog
import com.nfcalarmclock.system.addTimer
import com.nfcalarmclock.system.getTimer
import com.nfcalarmclock.timer.db.NacTimer
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
	 * View has been created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Change the description for the timer
		val scanNfcTagDescription: TextView = view.findViewById(R.id.scan_nfc_tag_description)

		scanNfcTagDescription.setText(R.string.description_scan_nfc_tag_timer)

		// Set the use any NFC tag clicked listener
		onUseAnyNfcTagClickedListener = OnUseAnyNfcTagClickedListener { alarm ->
			(alarm as NacTimer?)?.shouldScanningNfcTagStartTimer = false
		}
	}

}