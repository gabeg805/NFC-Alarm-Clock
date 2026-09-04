package com.nfcalarmclock.timer.options.flashlight

import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.flashlight.NacFlashlightOptionsDialog
import com.nfcalarmclock.system.getTimer
import com.nfcalarmclock.timer.db.NacTimer

/**
 * Flashlight options for a timer.
 */
class NacFlashlightOptionsDialog
	: NacFlashlightOptionsDialog()
{

	/**
	 * Get the alarm/timer argument from the fragment.
	 */
	override fun getFragmentArgument(): NacAlarm
	{
		return arguments?.getTimer() ?: NacTimer.build(sharedPreferences)
	}

}