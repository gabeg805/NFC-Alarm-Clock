package com.nfcalarmclock.system.triggers.shutdown

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nfcalarmclock.alarm.NacAlarmRepository
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.system.goAsync
import com.nfcalarmclock.timer.NacTimerRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Remove any active alarms on shutdown or reboot.
 *
 * Note: This needs to be registered in NacMainActivity because:
 *
 *       "As of Build.VERSION_CODES#P this broadcast is only sent to receivers
 *        registered through Context.registerReceiver."
 */
@AndroidEntryPoint
class NacShutdownBroadcastReceiver
	: BroadcastReceiver()
{

	/**
	 * Alarm repository.
	 */
	@Inject
	lateinit var alarmRepository: NacAlarmRepository

	/**
	 * Timer repository.
	 */
	@Inject
	lateinit var timerRepository: NacTimerRepository

	/**
	 * It is possible for another actor to send a spoofed intent with no
	 * action string or a different action string and cause undesired behavior.
	 * Ensure that the received Intent's action string matches the expected
	 * value before restoring alarms.
	 */
	override fun onReceive(context: Context, intent: Intent) = goAsync {

		// Check that the intent action is correct
		if ((intent.action == Intent.ACTION_SHUTDOWN)
			|| (intent.action == Intent.ACTION_REBOOT))
		{
			// Get the active alarms from the repository
			val alarms = alarmRepository.getActiveAlarms()
			val timers = timerRepository.getAllActiveTimers()

			// Iterate over each active alarm
			for (a in alarms)
			{
				// Dismiss the alarm
				a.dismiss()

				// Update the repo now that the alarm is no longer active
				alarmRepository.update(a)

				// Cancel the alarm
				NacScheduler.cancel(context, a)
			}

			// Iterate over each active timer
			for (t in timers)
			{
				// Set the timer to inactive and update the repo
				t.isActive = false
				timerRepository.update(t)
			}
		}

	}

}
