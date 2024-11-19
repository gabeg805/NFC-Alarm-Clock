package com.nfcalarmclock.system.triggers.startup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nfcalarmclock.alarm.NacAlarmRepository
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.util.goAsync
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Restore alarms on startup. This should support direct boot mode as well.
 */
@AndroidEntryPoint
class NacStartupBroadcastReceiver
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
	override fun onReceive(context: Context, intent: Intent) = goAsync {

		println("RECEIVED THE BROADCAST : ${intent.action}")
		// Check that the intent action is correct
		if ((intent.action == Intent.ACTION_BOOT_COMPLETED)
			|| (intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED))
		{
			println("READING ALARMS")
			// Get all the alarms
			val alarms = alarmRepository.getAllAlarms()
			println("FOUND ALARMS : ${alarms.size}")

			// Update all the alarms
			println("UPDATING SCHEDULE OF ALARMS")
			NacScheduler.updateAll(context, alarms)
		}

	}

}