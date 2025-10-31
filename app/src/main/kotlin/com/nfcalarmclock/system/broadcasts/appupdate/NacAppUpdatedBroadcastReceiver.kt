package com.nfcalarmclock.system.broadcasts.appupdate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nfcalarmclock.db.NacAlarmDatabase
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.system.goAsync

/**
 * After the app is updated, reapply the alarms.
 *
 * When the app is updated, any alarms that were set are lost. This will attempt to restore those
 * alarms.
 */
class NacAppUpdatedBroadcastReceiver
	: BroadcastReceiver()
{

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
			// Move shared preferences to device protected storage
			NacSharedPreferences.moveToDeviceProtectedStorage(context)

			// Get the database. Before opening it, a check will run to move the database
			// to device protected storage
			val db = NacAlarmDatabase.getInstance(context)
			val sharedPreferences = NacSharedPreferences(context)

			// Get all the alarms
			val alarmDao = db.alarmDao()
			val allAlarms = alarmDao.getAllAlarms()

			// Reschedule all the alarms
			NacScheduler.updateAll(context, allAlarms)

			// Check if should fix any auto dismiss, auto snooze, or snooze duration values
			// that are set to 0 in alarms.
			if (!sharedPreferences.eventFixZeroAutoDismissAndSnooze)
			{
				sharedPreferences.runEventFixZeroAutoDismissAndSnooze(
					allAlarms,
					onAlarmChanged = { alarm ->

						// Update the database and reschedule the alarm
						alarmDao.update(alarm)
						NacScheduler.update(context, alarm)

					})
			}

			// Save the next alarm
			sharedPreferences.saveNextAlarm(allAlarms)
		}

	}

}