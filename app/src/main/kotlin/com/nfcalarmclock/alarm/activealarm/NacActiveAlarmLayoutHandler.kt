package com.nfcalarmclock.alarm.activealarm

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.nfc.NacNfc
import com.nfcalarmclock.alarm.options.nfc.db.NacNfcTag
import com.nfcalarmclock.shared.NacSharedPreferences

abstract class NacActiveAlarmLayoutHandler(

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

	/**
	 * Whether the alarm should use NFC or not.
	 */
	val shouldUseNfc: Boolean = NacNfc.exists(activity) && (alarm != null) && alarm.shouldUseNfc && sharedPreferences.shouldShowNfcButton

	/**
	 * Run any setup steps.
	 */
	open fun setup(context: Context) {}

	/**
	 * Setup an NFC tag, if necessary.
	 */
	open fun setupNfcTag(nfcTag: NacNfcTag?) {}

	/**
	 * Start the layout and run any setup that needs to run.
	 */
	abstract fun start(context: Context)

	/**
	 * Stop the layout handler.
	 */
	abstract fun stop(context: Context)

}