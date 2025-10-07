package com.nfcalarmclock.timer.card

import com.nfcalarmclock.R
import com.nfcalarmclock.card.NacBaseCardAdapter
import com.nfcalarmclock.timer.db.NacTimer

/**
 * Timer card adapter.
 */
class NacTimerCardAdapter
	: NacBaseCardAdapter<NacTimer, NacTimerCardHolder>(::NacTimerCardHolder)
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId: Int = R.layout.card_timer_frame

}