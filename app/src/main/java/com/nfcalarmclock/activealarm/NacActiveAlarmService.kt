package com.nfcalarmclock.activealarm

import android.app.ActivityManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.missedalarm.NacMissedAlarmNotification
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacContext.autoDismissAlarmActivity
import com.nfcalarmclock.util.NacContext.startAlarmActivity
import com.nfcalarmclock.util.NacContext.startMainActivity
import com.nfcalarmclock.util.NacContext.stopAlarmActivity
import com.nfcalarmclock.util.NacIntent.createForegroundService
import com.nfcalarmclock.util.NacIntent.dismissForegroundService
import com.nfcalarmclock.util.NacIntent.dismissForegroundServiceWithNfc
import com.nfcalarmclock.util.NacIntent.getAlarm
import com.nfcalarmclock.util.NacIntent.snoozeForegroundService
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

/**
 * Service to allow an alarm to be run.
 */
@AndroidEntryPoint
class NacActiveAlarmService
	: Service()
{

	companion object
	{

		/**
		 * Action to start the service.
		 */
		const val ACTION_START_SERVICE = "com.nfcalarmclock.ACTION_START_SERVICE"

		/**
		 * Action to stop the service.
		 */
		const val ACTION_STOP_SERVICE = "com.nfcalarmclock.ACTION_STOP_SERVICE"

		/**
		 * Action to snooze the alarm.
		 */
		const val ACTION_SNOOZE_ALARM = "com.nfcalarmclock.ACTION_SNOOZE_ALARM"

		/**
		 * Action to dismiss the alarm.
		 */
		const val ACTION_DISMISS_ALARM = "com.nfcalarmclock.ACTION_DISMISS_ALARM"

		/**
		 * Action to dismiss the alarm with NFC.
		 */
		const val ACTION_DISMISS_ALARM_WITH_NFC = "com.nfcalarmclock.ACTION_DISMISS_ALARM_WITH_NFC"

		/**
		 * Tag for the wakelock.
		 */
		const val WAKELOCK_TAG = "NFC Alarm Clock:NacForegroundService"

		/**
		 * Dismiss the foreground service for the given alarm.
		 *
		 *
		 * If alarm is null, it will stop the currently active foreground service.
		 */
		fun dismissService(context: Context, alarm: NacAlarm?)
		{
			val intent = dismissForegroundService(context, alarm)
			context.startService(intent)
		}

		/**
		 * Dismiss the foreground service for the given alarm with NFC.
		 *
		 *
		 * If alarm is null, it will stop the currently active foreground service.
		 */
		fun dismissServiceWithNfc(context: Context, alarm: NacAlarm?)
		{
			val intent = dismissForegroundServiceWithNfc(context, alarm)
			context.startService(intent)
		}

		/**
		 * @return True if the alarm service is already running, and False otherwise.
		 */
		fun isRunning(context: Context): Boolean
		{
			// Get the name of the class of the service
			val className = NacActiveAlarmService::class.java.name

			// Get the list of all running services
			val activityManager = context.getSystemService(
				ACTIVITY_SERVICE) as ActivityManager
			val allRunningServices = activityManager.getRunningServices(Int.MAX_VALUE)

			// Iterate over each running service that was found
			for (serviceInfo in allRunningServices)
			{
				val serviceName = serviceInfo.service.className

				// Found an instance of the alarm service. The service name matches the
				// alarm service class name
				if (serviceName == className)
				{
					return true
				}
			}

			// Unable to find a running instance of the alarm service
			return false
		}

		/**
		 * Snooze the foreground service for the given alarm.
		 *
		 *
		 * The alarm cannot be null, unlike the dismissService() method.
		 */
		fun snoozeService(context: Context, alarm: NacAlarm?)
		{
			val intent = snoozeForegroundService(context, alarm)
			context.startService(intent)
		}

		/**
		 * Start the service.
		 */
		fun startService(context: Context, alarm: NacAlarm?)
		{
			// Unable to start the service because the alarm is null
			if (alarm == null)
			{
				return
			}

			// Create the intent
			val intent = createForegroundService(context, alarm)

			// Start the service
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			{
				context.startForegroundService(intent)
			}
			else
			{
				context.startService(intent)
			}
		}

	}

	/**
	 * Shared preferences.
	 */
	private var sharedPreferences: NacSharedPreferences? = null

	/**
	 * Wakelock.
	 */
	private var wakeLock: WakeLock? = null

	/**
	 * Alarm.
	 */
	private var alarm: NacAlarm? = null

	/**
	 * Wakeup process that: plays music, vibrates the phone, etc.
	 */
	private var wakeupProcess: NacWakeupProcess? = null

	/**
	 * Automatically dismiss the alarm in case it does not get dismissed.
	 */
	private var autoDismissHandler: Handler? = null

	/**
	 * Automatically dismiss the alarm.
	 *
	 * This will finish the service.
	 */
	@UnstableApi
	private fun autoDismiss()
	{
		// Finish the service
		finish(autoDismiss = true)
	}

	/**
	 * Run cleanup.
	 */
	@UnstableApi
	private fun cleanup()
	{
		// Clean the wakeup process
		wakeupProcess?.cleanup()

		// Cleanup the auto dismiss handler
		autoDismissHandler?.removeCallbacksAndMessages(null)

		// Cleanup the wake lock
		cleanupWakeLock()
	}

	/**
	 * Cleanup the wake lock.
	 */
	private fun cleanupWakeLock()
	{
		if ((wakeLock != null) && wakeLock!!.isHeld)
		{
			wakeLock!!.release()
		}

		this.wakeLock = null
	}

	/**
	 * Dismiss the alarm.
	 *
	 * This will finish the service.
	 */
	@UnstableApi
	private fun dismiss()
	{
		// Finish the service
		finish()
	}

	/**
	 * Dismiss the alarm with NFC.
	 *
	 * This will finish the service.
	 */
	@UnstableApi
	private fun dismissWithNfc()
	{
		// Finish the service
		finish()
	}

	/**
	 * Enable alias for the main activity so that tapping an NFC tag will open
	 * the main activity.
	 */
	private fun enableActivityAlias()
	{
		val aliasName = "$packageName.main.NacMainAliasActivity"
		val componentName = ComponentName(this, aliasName)

		packageManager.setComponentEnabledSetting(componentName,
			PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
			PackageManager.DONT_KILL_APP)
	}

	/**
	 * Finish the service.
	 */
	@UnstableApi
	fun finish(autoDismiss : Boolean = false)
	{
		// Cleanup everything
		cleanup()

		// Check if the alarm should be auto dismissed
		if (autoDismiss)
		{
			// Auto dismiss the alarm activity
			autoDismissAlarmActivity(this, alarm)
		}
		// The alarm should be stopped
		else
		{
			// Stop the alarm activity, regardless if the alarm is null or not
			stopAlarmActivity(this, alarm)
		}

		// Start the main activity
		// TODO: If I auto dismiss, should I be starting the main activity?
		startMainActivity(this)

		// Stop the service
		stopService()
	}

	/**
	 * Check if a new service, with a different alarm, was started.
	 *
	 * @param  intent An Intent.
	 *
	 * @return True if a new service was started, and False otherwise.
	 */
	private fun isNewServiceStarted(intent: Intent?): Boolean
	{
		// Get the alarm from the intent
		val intentAlarm = getAlarm(intent)

		// Check to make sure that a new intent was started with a different alarm
		return ((alarm != null)
			&& (intentAlarm != null)
			&& !intentAlarm.equals(alarm)
			&& (intent?.action == ACTION_START_SERVICE))
	}

	/**
	 * Called when the service is bound.
	 */
	override fun onBind(intent: Intent): IBinder?
	{
		return null
	}

	/**
	 * Called when the service is created.
	 */
	@UnstableApi
	override fun onCreate()
	{
		sharedPreferences = NacSharedPreferences(this)
		wakeLock = null
		alarm = null
		//wakeupProcess = NacWakeupProcess(this)
		autoDismissHandler = Handler(mainLooper)

		// Enable the activity alias so that tapping an NFC tag will open the main
		// activity
		enableActivityAlias()
	}

	/**
	 * Called when the service is destroyed.
	 */
	@UnstableApi
	override fun onDestroy()
	{
		// Disable the activity alias so that tapping an NFC tag will not do anything
		val aliasName = "$packageName.main.NacMainAliasActivity"
		val componentName = ComponentName(this, aliasName)

		packageManager.setComponentEnabledSetting(componentName,
			PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
			PackageManager.DONT_KILL_APP)

		// Cleanup everything
		cleanup()
	}

	/**
	 * Called when the service is started.
	 */
	@UnstableApi
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		// Setup the service
		// TODO: This was updating the previous alarm with the active time when a new intent came in. Did that do anything?
		setupService(intent)

		// Show the notification
		showActiveAlarmNotification()

		// The default case if things go wrong, or if the service should be
		// dismissed.
		//
		// TODO: Maybe this should just be the else? Oh but it checks alarm is null,
		// that is important.
		if ((alarm == null)
			|| intent?.action.isNullOrEmpty()
			|| (intent?.action == ACTION_STOP_SERVICE)
			|| (intent?.action == ACTION_DISMISS_ALARM))
		{
			dismiss()
		}
		// Dismiss with NFC
		else if (intent?.action == ACTION_DISMISS_ALARM_WITH_NFC)
		{
			dismissWithNfc()
		}
		// Snooze
		else if (intent?.action == ACTION_SNOOZE_ALARM)
		{
			snooze()
		}
		// Start the service
		else if (intent?.action == ACTION_START_SERVICE)
		{
			// Setup the wake lock
			setupWakeLock()

			// Start the wakeup process
			wakeupProcess = NacWakeupProcess(this, alarm!!)
			wakeupProcess!!.start()

			// Wait for auto dismiss
			waitForAutoDismiss()

			// Start the alarm activity
			startAlarmActivity(this, alarm)

			return START_STICKY
		}

		return START_NOT_STICKY
	}

	/**
	 * Setup the service.
	 */
	@UnstableApi
	private fun setupService(intent: Intent?)
	{
		// Attempt to get the alarm from the intent
		val intentAlarm = getAlarm(intent)

		// Prepare a new service
		if (isNewServiceStarted(intent))
		{
			println("NEW SERVICE STARTED")
			cleanup()
		}

		// Define the new alarm for this service
		// TODO: When does this happen if it is not a new service started?
		if (intentAlarm != null)
		{
			println("NEW ALARM SET FOR THE SERVICE : " + intentAlarm.equals(alarm))
			alarm = intentAlarm
		}
	}

	/**
	 * Setup the wake lock so that the screen remains on.
	 */
	private fun setupWakeLock()
	{
		// Cleanup the wakelock
		cleanupWakeLock()

		// Get the power manager and timeout for the wakelock
		val powerManager = getSystemService(POWER_SERVICE) as PowerManager
		val timeout = sharedPreferences!!.autoDismissTime * 60L * 1000L

		// Acquire the wakelock
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG)
		wakeLock!!.acquire(timeout)
	}

	/**
	 * Show the notification.
	 */
	private fun showActiveAlarmNotification()
	{
		// Create the active alarm notification
		val notification = NacActiveAlarmNotification(this)

		// Set the alarm to be part of the notification
		notification.alarm = alarm

		// Start the service in the foreground
		startForeground(NacActiveAlarmNotification.ID,
			notification.builder().build())
	}

	/**
	 * Snooze the alarm.
	 *
	 * This will finish the service.
	 */
	@UnstableApi
	fun snooze()
	{
		// Check if the alarm can be snoozed
		if (alarm!!.canSnooze(sharedPreferences!!))
		{
			// Finish the service
			finish()
		}
	}

	/**
	 * Stop the service.
	 */
	@Suppress("deprecation")
	private fun stopService()
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

	/**
	 * Wait in the background until the activity needs to auto dismiss the
	 * alarm.
	 *
	 * Auto dismiss a bit early to avoid the race condition between a new alarm
	 * starting at the same time that the alarm will auto-dismiss.
	 */
	@UnstableApi
	fun waitForAutoDismiss()
	{
		// Amount of time until the alarm is automatically dismissed
		val autoDismiss = sharedPreferences!!.autoDismissTime
		val delay = TimeUnit.MINUTES.toMillis(autoDismiss) - alarm!!.timeActive - 2000

		// There is an auto dismiss time set
		if (autoDismiss != 0L)
		{
			// Cleanup the auto dismiss handler, in case it is already set
			autoDismissHandler?.removeCallbacksAndMessages(null)

			// Automatically dismiss the alarm.
			autoDismissHandler!!.postDelayed({

				// Show the missed alarm notification
				if (sharedPreferences!!.missedAlarmNotification)
				{
					// Create the missed alarm notification
					val notification = NacMissedAlarmNotification(
						this@NacActiveAlarmService, alarm!!)

					// Show the notification
					notification.show()
				}

				// Auto dismiss
				autoDismiss()

			}, delay)
		}
	}

}