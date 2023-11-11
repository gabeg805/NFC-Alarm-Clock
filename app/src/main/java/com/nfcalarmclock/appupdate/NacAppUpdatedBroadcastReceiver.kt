package com.nfcalarmclock.appupdate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nfcalarmclock.alarm.NacAlarmRepository
import com.nfcalarmclock.scheduler.NacScheduler
import com.nfcalarmclock.util.goAsync
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * After the app is updated, reapply the alarms.
 *
 * When the app is updated, any alarms that were set are lost. This will attempt to restore those
 * alarms.
 */
@AndroidEntryPoint
class NacAppUpdatedBroadcastReceiver
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

		// Check that the action is correct
		if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED)
		{
			// Get all the alarms
			val alarms = alarmRepository.getAllAlarms()

			// Update all the alarms
			NacScheduler.updateAll(context, alarms)
		}

	}

}