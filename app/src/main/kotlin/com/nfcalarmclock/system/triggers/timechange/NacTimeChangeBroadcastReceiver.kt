package com.nfcalarmclock.system.triggers.timechange

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nfcalarmclock.alarm.NacAlarmRepository
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.util.goAsync
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Receive this signal from AlarmManager and start the foreground service.
 */
@AndroidEntryPoint
class NacTimeChangeBroadcastReceiver
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
	 * value before updating alarms.
	*/
	override fun onReceive(context: Context, intent: Intent) = goAsync {

		// Check that the intent action is correct
		if ((intent.action == Intent.ACTION_DATE_CHANGED)
			|| (intent.action == Intent.ACTION_TIME_CHANGED)
			|| (intent.action == Intent.ACTION_TIMEZONE_CHANGED)
			|| (intent.action == Intent.ACTION_LOCALE_CHANGED))
		{
			// Get all the alarms
			val sharedPreferences = NacSharedPreferences(context)
			val allAlarms = alarmRepository.getAllAlarms()

			// Update all the alarms
			NacScheduler.updateAll(context, allAlarms)

			// Save the next alarm
			sharedPreferences.saveNextAlarm(allAlarms)
		}

	}

}