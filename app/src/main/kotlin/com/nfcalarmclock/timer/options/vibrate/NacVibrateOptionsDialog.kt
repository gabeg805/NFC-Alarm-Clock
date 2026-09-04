package com.nfcalarmclock.timer.options.vibrate

import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.vibrate.NacVibrateOptionsDialog
import com.nfcalarmclock.system.getTimer
import com.nfcalarmclock.timer.db.NacTimer

/**
 * Vibrate options for a timer.
 */
class NacVibrateOptionsDialog
	: NacVibrateOptionsDialog()
{

	/**
	 * Get the alarm/timer argument from the fragment.
	 */
	override fun getFragmentArgument(): NacAlarm
	{
		return arguments?.getTimer() ?: NacTimer.build(sharedPreferences)
	}

}