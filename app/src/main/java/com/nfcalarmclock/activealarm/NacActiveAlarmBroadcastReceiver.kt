package com.nfcalarmclock.activealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nfcalarmclock.util.NacContext.startAlarm
import com.nfcalarmclock.util.NacIntent.getAlarmBundle

/**
 * Receive this signal from AlarmManager and start the foreground service.
 */
class NacActiveAlarmBroadcastReceiver
	: BroadcastReceiver()
{

	/**
	 * It is possible for another actor to send a spoofed intent with no
	 * action string or a different action string and cause undesired behavior.
	 * Ensure that the received Intent's action string matches the expected
	 * value before restoring alarms.
	 */
	override fun onReceive(context: Context, intent: Intent)
	{
		// Get the alarm bundle from the intent
		val bundle = getAlarmBundle(intent)

		// Start the alarm
		startAlarm(context, bundle)
	}

}