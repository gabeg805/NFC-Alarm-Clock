package com.nfcalarmclock.activealarm

import androidx.appcompat.app.AppCompatActivity
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.shared.NacSharedPreferences

open class NacActiveAlarmLayoutHandler(

	/**
	 * Activity.
	 */
	activity: AppCompatActivity,

	/**
	 * Alarm.
	 */
	val alarm: NacAlarm?,

	/**
	 * Listener for an alarm action.
	 */
	val onAlarmActionListener: OnAlarmActionListener

)
{

	/**
	 * Listener for an alarm action, such as snooze or dismiss.
	 */
	interface OnAlarmActionListener
	{
		fun onSnooze(alarm: NacAlarm)
		fun onDismiss(alarm: NacAlarm)
	}

	/**
	 * Shared preferences.
	 */
	val sharedPreferences: NacSharedPreferences = NacSharedPreferences(activity)

}