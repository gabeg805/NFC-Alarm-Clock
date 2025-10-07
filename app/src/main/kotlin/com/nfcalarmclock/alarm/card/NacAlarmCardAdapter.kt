package com.nfcalarmclock.alarm.card

import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.card.NacBaseCardAdapter

/**
 * Alarm card adapter.
 */
class NacAlarmCardAdapter
	: NacBaseCardAdapter<NacAlarm, NacAlarmCardHolder>(::NacAlarmCardHolder)
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId: Int = R.layout.card_alarm_frame

}