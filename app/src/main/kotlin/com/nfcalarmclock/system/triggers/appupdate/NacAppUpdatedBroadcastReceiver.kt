package com.nfcalarmclock.system.triggers.appupdate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nfcalarmclock.db.NacAlarmDatabase
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.util.goAsync

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

		println("APP UPDATE BROADCAST : ${intent.action}")
		// Check that the action is correct
		if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED)
		{
			println("IN THE JANK")
			//// Define the context that should be used
			//var deviceContext = context

			//// Check if device can use direct boot
			//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			//{
			println("BROADCAST MOVING SHARED PREFS")
			NacSharedPreferences.moveToDeviceProtectedStorage(context)
			//	// Get direct boot context and default shared preferences file name
			//	deviceContext = context.createDeviceProtectedStorageContext()
			//	val sharedPrefsFileName = "${context.packageName}_preferences"

			//	// Move database and shared preferences to device encrypted storage
			//	val x = deviceContext.moveDatabaseFrom(context, NacAlarmDatabase.DB_NAME)
			//	println("Move database? $x")
			//	val y = deviceContext.moveSharedPreferencesFrom(context, sharedPrefsFileName)
			//	println("Move Shared pref? $y")
			//}

			println("GETTING ALL ALARMS")
			// Get all the alarms
			val db = NacAlarmDatabase.getInstance(context)
			val alarmDao = db.alarmDao()
			val alarms = alarmDao.getAllAlarms()
			println("SCHEDULING ALARMS : ${alarms.size}")

			// Update all the alarms
			NacScheduler.updateAll(context, alarms)
		}

	}

}