package com.nfcalarmclock.alarm.activealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nfcalarmclock.alarm.NacAlarmRepository
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.util.NacIntent
import com.nfcalarmclock.util.NacUtility
import com.nfcalarmclock.util.goAsync
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Receive this signal from AlarmManager and start the foreground service.
 */
@AndroidEntryPoint
class NacActiveAlarmBroadcastReceiver
	: BroadcastReceiver()
{

	/**
	 * Alarm repository.
	 */
	@Inject
	lateinit var alarmRepository: NacAlarmRepository

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

		// Check if the alarm should be skipped
		if (alarm?.shouldSkipNextAlarm == true)
		{
			goAsync()
			{

				// The alarm has been skipped so reset the flag
				alarm.shouldSkipNextAlarm = false

				// Dismiss the alarm, really just to toggle the alarm if repeat
				// is not enabled
				alarm.dismiss()

				// Update the database and reschedule the alarm
				alarmRepository.update(alarm)
				NacScheduler.update(context, alarm)
				NacUtility.quickToast(context, "Alarm was skipped")
			}
			return
		}

		// Start the alarm activity and service
		NacActiveAlarmActivity.startAlarmActivity(context, alarm)
		NacActiveAlarmService.startAlarmService(context, alarm)
	}

}