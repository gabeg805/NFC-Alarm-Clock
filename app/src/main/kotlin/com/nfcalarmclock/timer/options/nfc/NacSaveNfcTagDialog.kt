package com.nfcalarmclock.timer.options.nfc

import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.nfc.NacSaveNfcTagDialog
import com.nfcalarmclock.system.getTimer
import dagger.hilt.android.AndroidEntryPoint

/**
 * Save an NFC tag that was scanned.
 */
@AndroidEntryPoint
class NacSaveNfcTagDialog
	: NacSaveNfcTagDialog()
{

	/**
	 * Get the alarm/timer argument from the fragment.
	 */
	override fun getFragmentArgument(): NacAlarm?
	{
		return arguments?.getTimer()
	}

}