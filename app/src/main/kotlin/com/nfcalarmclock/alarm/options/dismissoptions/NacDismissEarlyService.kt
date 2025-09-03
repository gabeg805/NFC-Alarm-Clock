package com.nfcalarmclock.alarm.options.dismissoptions

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.util.addAlarm
import com.nfcalarmclock.util.getAlarm

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
	@UnstableApi
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		println("Starting dismiss early service!")
		// Attempt to get the alarm from the intent
		val alarm = intent?.getAlarm()

		// Create the reminder notification
		val notification = NacDismissEarlyNotification(this, alarm)

		// Start the service in the foreground
		startForeground(notification.id,
			notification.builder().build())

		return START_NOT_STICKY
	}

	companion object
	{

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

	}

}