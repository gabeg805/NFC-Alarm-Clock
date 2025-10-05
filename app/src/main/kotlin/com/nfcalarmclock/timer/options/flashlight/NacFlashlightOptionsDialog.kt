package com.nfcalarmclock.timer.options.flashlight

import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.flashlight.NacFlashlightOptionsDialog
import com.nfcalarmclock.system.getTimer

/**
 * Flashlight options for a timer.
 */
class NacFlashlightOptionsDialog
	: NacFlashlightOptionsDialog()
{

	/**
	 * Get the alarm/timer argument from the fragment.
	 */
	override fun getFragmentArgument(): NacAlarm?
	{
		return arguments?.getTimer()
	}

}