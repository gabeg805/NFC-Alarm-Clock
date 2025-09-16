package com.nfcalarmclock.alarm.options.dismissoptions

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.system.addAlarm
import com.nfcalarmclock.system.getAlarm

/**
 * Service to show the dismiss early notification.
 */
class NacDismissEarlyService
	: Service()
{

	/**
	 * Called when the service is bound.
	 */
	override fun onBind(intent: Intent): IBinder?
	{
		return null
	}

	/**
	 * Called when the service is started.
	 */
	@Suppress("deprecation")
	@UnstableApi
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		// Attempt to get the alarm from the intent
		val alarm = intent?.getAlarm()

		// Check the intent action
		when (intent?.action)
		{

			// Clear the notification by stopping the service
			ACTION_STOP_SERVICE ->
			{
				// Stop the foreground service using the updated form of
				// stopForeground() for API >= 33
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
				{
					super.stopForeground(STOP_FOREGROUND_REMOVE)
				}
				else
				{
					super.stopForeground(true)
				}

				// Stop the service
				super.stopSelf()
			}

			// Normal. Show the notification
			else ->
			{
				// Create the reminder notification
				val notification = NacDismissEarlyNotification(this, alarm)

				// Start the service in the foreground
				startForeground(notification.id,
					notification.builder().build())
			}

		}

		return START_NOT_STICKY
	}

	companion object
	{

		/**
		 * Action to clear the notification and stop the service.
		 */
		private const val ACTION_STOP_SERVICE = "com.nfcalarmclock.alarm.options.dismissearly.ACTION_STOP_SERVICE"

		/**
		 * Create an intent that will be used to start the foreground upcoming
		 * reminder service.
		 *
		 * @param context A context.
		 * @param alarm   An alarm.
		 *
		 * @return The Foreground service intent.
		 */
		fun getStartIntent(context: Context, alarm: NacAlarm?): Intent
		{
			// Create an intent with the alarm service
			return Intent(context, NacDismissEarlyService::class.java)
				.addAlarm(alarm)
		}

		/**
		 * Get an intent that will be used to clear the notification and stop the service.
		 *
		 * @return An intent that will be used to clear the notification and stop the service.
		 */
		fun getStopIntent(context: Context, alarm: NacAlarm?): Intent
		{
			// Create the intent with the alarm service
			return Intent(ACTION_STOP_SERVICE, null, context, NacDismissEarlyService::class.java)
				.addAlarm(alarm)
		}

		/**
		 * Stop the service.
		 */
		fun stopService(context: Context, alarm: NacAlarm?)
		{
			// Create the stop intent
			val intent = getStopIntent(context, alarm)

			// Start the intent to stop the service
			context.startService(intent)
		}

	}

}