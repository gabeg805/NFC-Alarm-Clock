package com.nfcalarmclock.system.broadcasts.airplanemode

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.NacAlarmRepository
import com.nfcalarmclock.log.NacLog
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.goAsync
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.view.quickToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Disable alarms when in airplane mode.
 */
@AndroidEntryPoint
class NacAirplaneModeBroadcastReceiver
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

		// Shared preferences
		val shared = NacSharedPreferences(context)

		// State of airplane mode
		val state = intent.getBooleanExtra("state", false)

		// Intent action is NOT correct
		// Shared preference is not set
		if ((intent.action != Intent.ACTION_AIRPLANE_MODE_CHANGED)
			|| !shared.shouldToggleAlarmsWithAirplaneMode)
		{
			return@goAsync
		}

		NacLog.i("Airplane mode broadcast received. ${if (state) "Disabling" else "Enabling"} all alarms")

		// Disable each alarm that is not active
		alarmRepository.getAllAlarms().forEach { a ->

			// Skip active alarms
			if (a.isActive)
			{
				return@forEach
			}

			// Disable/enable the alarm based on opposite of airplane mode state
			a.isEnabled = !state

			// Update the alarm in the database and scheduler
			alarmRepository.update(a)
			NacScheduler.update(context, a)

		}

		// Show toast based on the airplane mode state
		withContext(Dispatchers.Main)
		{
			// Choose the correct message based on the airplane mode state
			val message = if (state)
			{
				R.string.description_toggle_alarms_with_airplane_mode_on
			}
			else
			{
				R.string.description_toggle_alarms_with_airplane_mode_off
			}

			// Show toast
			quickToast(context, message)
		}

	}

}