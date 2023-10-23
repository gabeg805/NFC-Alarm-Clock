package com.nfcalarmclock.timechange

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nfcalarmclock.scheduler.NacScheduler

/**
 * Receive this signal from AlarmManager and start the foreground service.
 */
class NacTimeChangeBroadcastReceiver : BroadcastReceiver()
{

	/**
	 * It is possible for another actor to send a spoofed intent with no
	 * action string or a different action string and cause undesired behavior.
	 * Ensure that the received Intent's action string matches the expected
	 * value before updating alarms.
	*/
	override fun onReceive(context: Context, intent: Intent)
	{
		val action = intent.action

		if (action == Intent.ACTION_DATE_CHANGED
			|| action == Intent.ACTION_TIME_CHANGED
			|| action == Intent.ACTION_TIMEZONE_CHANGED
			|| action == Intent.ACTION_LOCALE_CHANGED)
		{
			NacScheduler.updateAll(context)
		}
	}

}