package com.nfcalarmclock.alarm.activealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.util.getAlarm

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
	@OptIn(UnstableApi::class)
	override fun onReceive(context: Context, intent: Intent)
	{
		// Get the alarm
		val alarm = intent.getAlarm()

		// Start the alarm activity and service
		NacActiveAlarmActivity.startAlarmActivity(context, alarm)
		NacActiveAlarmService.startAlarmService(context, alarm)
	}

}