package com.nfcalarmclock.alarm.activealarm

import android.content.Context
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.alarm.NacAlarmRepository
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.system.NacLifecycleService
import com.nfcalarmclock.system.addAlarm
import com.nfcalarmclock.system.getAlarm
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Disable the active flag of an erroneously active alarm.
 *
 * I am not sure how this happens, but an alarm remains active when it should not, so the app
 * (previously) attempted to start the alarm activity. Normally, this would be fine if the
 * service was running, but in this case, the service would not be running.
 *
 * Now, a check is run to ensure the service is running, and if a connection is not made, then
 * this service is started to disable the active flag of the alarm.
 */
@AndroidEntryPoint
class NacDisableErroneousActiveAlarmService
	: NacLifecycleService()
{

	/**
	 * Alarm repository.
	 */
	@Inject
	lateinit var alarmRepository: NacAlarmRepository

	/**
	 * Called when the service is started.
	 */
	@UnstableApi
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		// Super
		super.onStartCommand(intent, flags, startId)

		// Attempt to get the alarm from the intent
		val alarm = intent?.getAlarm()

		lifecycleScope.launch {

			// Disable the active flag of the alarm
			if (alarm != null)
			{
				println("DISABLING ALARM")
				alarm.isActive = false
				alarmRepository.update(alarm)
			}

			// Stop the service
			super.stopSelf()

		}

		return START_NOT_STICKY
	}

	companion object
	{

		/**
		 * Create an intent that will be used to start the service.
		 *
		 * @param context A context.
		 * @param alarm   An alarm.
		 *
		 * @return The service intent.
		 */
		fun getStartIntent(context: Context, alarm: NacAlarm?): Intent
		{
			return Intent(Intent.ACTION_DEFAULT, null, context, NacDisableErroneousActiveAlarmService::class.java)
				.addAlarm(alarm)
		}
		/**
		 * Start the foreground service.
		 */
		fun startService(context: Context, alarm: NacAlarm?)
		{
			val intent = getStartIntent(context, alarm)

			context.startService(intent)
		}

	}

}