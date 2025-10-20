package com.nfcalarmclock.timer.addedit

import com.nfcalarmclock.timer.db.NacTimer
import dagger.hilt.android.AndroidEntryPoint

/**
 * Add a timer.
 */
@AndroidEntryPoint
class NacAddTimerFragment
	: NacBaseAddEditTimer()
{

	/**
	 * Initialize the timer that will be used in the fragment.
	 */
	override fun initTimer()
	{
		timer = NacTimer.build(sharedPreferences)
	}

}