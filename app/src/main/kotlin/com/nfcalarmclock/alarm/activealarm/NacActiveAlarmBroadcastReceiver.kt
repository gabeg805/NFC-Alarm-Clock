package com.nfcalarmclock.alarm.activealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nfcalarmclock.util.NacIntent

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
		// Get the alarm
		val alarm = NacIntent.getAlarm(intent)

		// Start the alarm activity and service
		NacActiveAlarmActivity.startAlarmActivity(context, alarm)
		NacActiveAlarmService.startAlarmService(context, alarm)
	}

}