package com.nfcalarmclock.alarm.activealarm

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.BuildConfig
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.NacAlarmRepository
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.main.NacMainActivity.Companion.startMainActivity
import com.nfcalarmclock.alarm.options.missedalarm.NacMissedAlarmNotification
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.statistics.NacAlarmStatisticRepository
import com.nfcalarmclock.util.NacIntent
import com.nfcalarmclock.util.NacIntent.getAlarm
import com.nfcalarmclock.util.NacUtility
import com.nfcalarmclock.util.disableActivityAlias
import com.nfcalarmclock.util.enableActivityAlias
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Service to allow an alarm to be run.
 */
@AndroidEntryPoint
class NacActiveAlarmService
	: Service()
{

	/**
	 * Supervisor job for the service.
	 */
	private val job = SupervisorJob()

	/**
	 * Coroutine scope for the service.
	 */
	private val scope = CoroutineScope(Dispatchers.IO + job)

	/**
	 * Alarm repository.
	 */
	@Inject
	lateinit var alarmRepository: NacAlarmRepository

	/**
	 * Statistic view model.
	 */
	@Inject
	lateinit var statisticRepository: NacAlarmStatisticRepository

	/**
	 * Shared preferences.
	 */
	private lateinit var sharedPreferences: NacSharedPreferences

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
	 * Automatically snooze the alarm, if desired by the user.
	 */
	private var autoSnoozeHandler: Handler? = null

	/**
	 * Time that the service was started, in milliseconds.
	 */
	private var startTime: Long = 0

	/**
	 * Check if the alarm can be snoozed and show a toast if it cannot.
	 */
	private val canSnooze: Boolean
		get()
		{
			// Check if the alarm can be snoozed
			return if (alarm!!.canSnooze)
			{
				true
			}
			// Unable to snooze the alarm
			else
			{
				// Show a toast saying the alarm could not be snoozed
				NacUtility.quickToast(this, R.string.error_message_snooze)
				false
			}
		}

	/**
	 * Run cleanup.
	 */
	@UnstableApi
	private fun cleanup()
	{
		// Clean the wakeup process
		wakeupProcess?.cleanup()

		// Cleanup the auto dismiss and snooze handler
		autoDismissHandler?.removeCallbacksAndMessages(null)
		autoSnoozeHandler?.removeCallbacksAndMessages(null)

		// Check if a wake lock is held
		if (wakeLock?.isHeld == true)
		{
			// Cleanup the wake lock
			wakeLock?.release()
		}

		// Clear the currently playing media in the shared preference
		sharedPreferences.currentPlayingAlarmMedia = ""
	}

	/**
	 * Dismiss the alarm.
	 *
	 * This will finish the service.
	 */
	@UnstableApi
	fun dismiss(usedNfc: Boolean = false, wasMissed: Boolean = false)
	{
		// Update the alarm
		scope.launch {

			// Dismiss the alarm
			alarm!!.dismiss()

			// Update the alarm
			alarmRepository.update(alarm!!)

			// Check if the alarm missed and had to be dismissed via auto
			// dismiss
			if (wasMissed)
			{
				// Save the missed alarm into the statistics table
				statisticRepository.insertMissed(alarm)
			}
			// Alarm was dismissed normally
			else
			{
				// Save the dismissed alarm to the statistics table (used NFC)
				statisticRepository.insertDismissed(alarm!!, usedNfc)
			}

			// Reschedule the alarm
			NacScheduler.update(this@NacActiveAlarmService, alarm!!)

			// Set flag that the main activity needs to be refreshed
			sharedPreferences.shouldRefreshMainActivity = true

			// Check if there are any other active alarms that need to run
			if (hasAnyOtherActiveAlarms())
			{
				// Restart another active alarm
				restartOtherActiveAlarm()
			}
			// No other active alarms
			else
			{
				withContext(Dispatchers.Main) {

					// Show toast that the alarm was dismissed
					NacUtility.quickToast(this@NacActiveAlarmService, R.string.message_alarm_dismiss)

					// Stop the service
					stopActiveAlarmService()

				}
			}

		}
	}

	/**
	 * Check if there are any other active alarms.
	 */
	private suspend fun hasAnyOtherActiveAlarms(): Boolean
	{
		// Try and find any active alarms
		val activeAlarm = alarmRepository.getActiveAlarm()

		// Check if the alarm is not null
		return (activeAlarm != null)
	}

	/**
	 * Check if a new service was started.
	 *
	 * @param  intentAlarm An alarm from an intent.
	 * @param  action The action from an intent.
	 *
	 * @return True if a new service was started, and False otherwise.
	 */
	private fun isNewServiceStarted(
		intentAlarm: NacAlarm?,
		action: String?
	): Boolean
	{
		return ((alarm != null)
			&& (intentAlarm != null)
			&& (action == ACTION_START_SERVICE))
	}

	/**
	 * Check if the same service was started.
	 *
	 * @return True if the same service was started, and False otherwise.
	 */

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
		// Super
		super.onCreate()

		// Initialize member varirables
		sharedPreferences = NacSharedPreferences(this)
		wakeLock = null
		alarm = null
		autoDismissHandler = Handler(mainLooper)
		autoSnoozeHandler = Handler(mainLooper)

		// Enable the activity alias so that tapping an NFC tag will open the main
		// activity
		enableActivityAlias(this)
	}

	/**
	 * Setup the app version.
	 */
	private fun setupAppVersion()
	{
		// Set the previous app version as the current version
		sharedPreferences.previousAppVersion = BuildConfig.VERSION_NAME
	}

	/**
	 * Called when the service is destroyed.
	 */
	@UnstableApi
	override fun onDestroy()
	{
		// Super
		super.onDestroy()

		// Check if the app version is not set
		if (sharedPreferences.previousAppVersion.isEmpty())
		{
			// Setup the app version
			setupAppVersion()
		}

		// Stop the alarm activity
		NacActiveAlarmActivity.stopAlarmActivity(this)

		// Disable the activity alias so that tapping an NFC tag will not do anything
		disableActivityAlias(this)

		// Start the main activity so that if there are any other alarms that
		// need to run, this will kick them off
		startMainActivity(this)

		scope.launch {

			// Check if the alarm is not null
			if (alarm != null)
			{
				// Set the alarm as not active
				alarm!!.isActive = false

				// Update the alarm
				alarmRepository.update(alarm!!)
			}

		}

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
		setupActiveAlarmService(intent)

		// Show the notification
		showActiveAlarmNotification()

		// Check the intent action
		when (intent?.action)
		{

			// Alarms are equal. Start the alarm activity
			ACTION_EQUAL_ALARMS ->
			{
				NacActiveAlarmActivity.startAlarmActivity(this, alarm!!)
				return START_STICKY

			}

			// Start the service
			ACTION_START_SERVICE ->
			{
				startActiveAlarmService()
				return START_STICKY
			}

			// Stop the service
			ACTION_STOP_SERVICE -> stopActiveAlarmService()

			// Dismiss
			ACTION_DISMISS_ALARM -> dismiss()

			// Dismiss with NFC
			ACTION_DISMISS_ALARM_WITH_NFC -> dismiss(usedNfc = true)

			// Snooze
			ACTION_SNOOZE_ALARM ->
			{
				// Check if can snooze
				if (canSnooze)
				{
					snooze()
				}
			}

			// The default case if things go wrong
			else -> stopActiveAlarmService()

		}

		return START_NOT_STICKY
	}

	/**
	 * Restart any other active alarm.
	 */
	private suspend fun restartOtherActiveAlarm()
	{
		// Restart alarms
		// Get output from restart
		// startAlarmService
		// recreate notification

		// Try and find any active alarms
		val activeAlarm = alarmRepository.getActiveAlarm()

		// Start the alarm service for this alarm
		startAlarmService(this, activeAlarm)

		// Creaete the notification
		val notification = NacActiveAlarmNotification(this, activeAlarm)

		// Show the notification
		notification.show()
	}

	/**
	 * Setup the service.
	 */
	@UnstableApi
	private fun setupActiveAlarmService(intent: Intent?)
	{
		// Attempt to get the alarm from the intent
		val intentAlarm = getAlarm(intent)

		// Check if a new service was started
		if (isNewServiceStarted(intentAlarm, intent?.action))
		{
			// Check if the alarms are equal
			if (intentAlarm!!.equals(alarm))
			{
				// Set the action indicating that the alarms are equal
				intent!!.action = ACTION_EQUAL_ALARMS
				return
			}
			else
			{
				// Update the active time of the current alarm
				updateTimeActiveOfCurrentAlarm()
			}
		}

		// Check if the intent alarm is not null
		if (intentAlarm != null)
		{
			// Set the new alarm for this service
			alarm = intentAlarm
		}

		// Check if the service alarm is null
		if (alarm == null)
		{
			// Set the action indicating to stop the service
			intent?.action = ACTION_STOP_SERVICE
		}
	}

	/**
	 * Show the notification.
	 *
	 * Note: This can still be run with a null alarm.
	 */
	private fun showActiveAlarmNotification()
	{
		// Create the active alarm notification
		val notification = NacActiveAlarmNotification(this, alarm)

		try
		{
			// Start the service in the foreground
			startForeground(NacActiveAlarmNotification.ID,
				notification.builder().build())
		}
		catch (e: Exception)
		{
			// Check if not allowed to start foreground service
			if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) && (e is ForegroundServiceStartNotAllowedException))
			{
				NacUtility.toast(this, R.string.error_message_unable_to_start_foreground_service)
			}
		}
	}

	/**
	 * Snooze the alarm.
	 *
	 * This will finish the service.
	 */
	@UnstableApi
	fun snooze()
	{
		scope.launch {

			// Snooze the alarm and get the next time to run the alarm again
			val cal = alarm!!.snooze()

			// Update the time the alarm was active
			alarm!!.addToTimeActive(System.currentTimeMillis() - startTime)

			// Update the alarm
			alarmRepository.update(alarm!!)

			// Save this snooze duration to the statistics table
			statisticRepository.insertSnoozed(alarm!!, 60L * alarm!!.snoozeDuration)

			// Reschedule the alarm
			NacScheduler.update(this@NacActiveAlarmService, alarm!!, cal)

			// Set the flag that the main activity will need to be refreshed
			sharedPreferences.shouldRefreshMainActivity = true

			// Check if there are any other active alarms that need to run
			if (hasAnyOtherActiveAlarms())
			{
				// Restart another active alarm
				restartOtherActiveAlarm()
			}
			// No other active alarms
			else
			{
				withContext(Dispatchers.Main) {

					// Show a toast saying the alarm was snoozed
					NacUtility.quickToast(this@NacActiveAlarmService, R.string.message_alarm_snooze)

					// Stop the service
					stopActiveAlarmService()

				}
			}

		}
	}

	/**
	 * Start the service.
	 */
	@UnstableApi
	private fun startActiveAlarmService()
	{
		// Cleanup any resources
		cleanup()

		// Get the power manager and timeout for the wakelock
		val powerManager = getSystemService(POWER_SERVICE) as PowerManager
		val timeout = alarm!!.autoDismissTime * 60L * 1000L

		// Acquire the wakelock
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
			WAKELOCK_TAG)
		wakeLock!!.acquire(timeout)

		// Start the wakeup process
		wakeupProcess = NacWakeupProcess(this, alarm!!)
		wakeupProcess!!.start()

		// Wait for auto dismiss
		waitForAutoDismiss()

		// Wait for auto snooze
		waitForAutoSnooze()

		// Start the alarm activity
		NacActiveAlarmActivity.startAlarmActivity(this, alarm!!)

		scope.launch {

			 // Set the active flag
			 alarm!!.isActive = true

			 // Update the alarm
			 alarmRepository.update(alarm!!)

			 // Reschedule the alarm
			 NacScheduler.update(this@NacActiveAlarmService, alarm!!)

		}
	}

	/**
	 * Stop the service.
	 */
	@Suppress("deprecation")
	private fun stopActiveAlarmService()
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
	 * Update the active time of the current alarm.
	 */
	private fun updateTimeActiveOfCurrentAlarm()
	{
		val currentStartTime = startTime
		val currentAlarm = alarm!!

		scope.launch {

			// Check if the service has started
			if (currentStartTime != 0L)
			{
				// Set the time that the alarm was active. Do not set the
				// alarm as inactive though because then it will not go off
				// again. It should go off again because the alarm has not
				// been finished being interacted with
				currentAlarm.addToTimeActive(System.currentTimeMillis() - currentStartTime)

				// Update the alarm
				alarmRepository.update(currentAlarm)
			}

		}
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
		val autoDismiss = alarm!!.autoDismissTime
		val delay = TimeUnit.MINUTES.toMillis(autoDismiss.toLong()) - alarm!!.timeActive - 2000

		// There is an auto dismiss time set
		if (autoDismiss != 0)
		{
			// Automatically dismiss the alarm
			autoDismissHandler!!.postDelayed({

				// Show the missed alarm notification
				if (sharedPreferences.missedAlarmNotification)
				{
					// Create the missed alarm notification
					val notification = NacMissedAlarmNotification(
						this@NacActiveAlarmService, alarm!!)

					// Show the notification
					notification.show()
				}

				// Auto dismiss the alarm. This will stop the service
				dismiss(wasMissed = true)

			}, delay)
		}

		// Set the start time
		startTime = System.currentTimeMillis()
	}

	/**
	 * Wait in the background until the activity needs to auto snooze the
	 * alarm.
	 *
	 * Auto snooze a bit early to avoid the race condition between a new alarm
	 * starting at the same time that the alarm will auto-snooze.
	 */
	@UnstableApi
	fun waitForAutoSnooze()
	{
		// Amount of time until the alarm is automatically snoozed
		val autoSnooze = alarm!!.autoSnoozeTime
		val delay = TimeUnit.MINUTES.toMillis(autoSnooze.toLong()) - 1000

		// There is an auto snooze time set and the alarm is able to be snoozed
		if ((autoSnooze != 0) && canSnooze)
		{
			// Automatically snooze the alarm
			autoSnoozeHandler!!.postDelayed({ snooze() }, delay)
		}
	}

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
		 * Action to dismiss the alarm.
		 */
		const val ACTION_DISMISS_ALARM = "com.nfcalarmclock.ACTION_DISMISS_ALARM"

		/**
		 * Action to dismiss the alarm with NFC.
		 */
		const val ACTION_DISMISS_ALARM_WITH_NFC = "com.nfcalarmclock.ACTION_DISMISS_ALARM_WITH_NFC"

		/**
		 * Action to snooze the alarm.
		 */
		const val ACTION_SNOOZE_ALARM = "com.nfcalarmclock.ACTION_SNOOZE_ALARM"

		/**
		 * Action to do when alarms are equal. This is to say when the service
		 * is started with the same alarm.
		 */
		private const val ACTION_EQUAL_ALARMS = "com.nfcalarmclock.ACTION_EQUAL_ALARMS"

		/**
		 * Tag for the wakelock.
		 */
		const val WAKELOCK_TAG = "NFC Alarm Clock:NacForegroundService"

		/**
		 * Dismiss the alarm service for the given alarm.
		 */
		fun dismissAlarmService(context: Context, alarm: NacAlarm?)
		{
			// Create an intent with the alarm activity
			val intent = getDismissIntent(context, alarm)

			// Start the service. This will not be a foreground service so do
			// not need to call startForegroundService()
			context.startService(intent)
		}

		/**
		 * Dismiss the alarm activity for the given alarm with NFC.
		 */
		fun dismissAlarmServiceWithNfc(context: Context, alarm: NacAlarm?)
		{
			// Create the intent with the alarm activity
			val intent = getDismissIntentWithNfc(context, alarm)

			// Start the service. This will not be a foreground service so do
			// not need to call startForegroundService()
			context.startService(intent)
		}

		/**
		 * Get an intent that will be used to dismiss the foreground alarm
		 * service.
		 *
		 * @return An intent that will be used to dismiss the foreground alarm
		 *         service.
		 */
		fun getDismissIntent(context: Context, alarm: NacAlarm?): Intent
		{
			// Create the intent with the alarm service
			val intent = Intent(ACTION_DISMISS_ALARM, null, context,
				NacActiveAlarmService::class.java)

			// Add the alarm to the intent
			return NacIntent.addAlarm(intent, alarm)
		}

		/**
		 * Get an intent that will be used to dismiss the foreground alarm
		 * service witH NFC.
		 *
		 * @return An intent that will be used to dismiss the foreground alarm
		 *         service with NFC.
		 */
		private fun getDismissIntentWithNfc(context: Context, alarm: NacAlarm?): Intent
		{
			// Create the intent with the alarm service
			val intent = Intent(ACTION_DISMISS_ALARM_WITH_NFC, null, context,
				NacActiveAlarmService::class.java)

			// Add the alarm to the intent
			return NacIntent.addAlarm(intent, alarm)
		}

		/**
		 * Get an intent that will be used to snooze the foreground alarm
		 * service.
		 *
		 * @return An intent that will be used to snooze the foreground alarm
		 *         service.
		 */
		fun getSnoozeIntent(context: Context?, alarm: NacAlarm?): Intent
		{
			// Create the intent with the alarm service
			val intent = Intent(ACTION_SNOOZE_ALARM, null, context,
				NacActiveAlarmService::class.java)

			// Add the alarm to the intent
			return NacIntent.addAlarm(intent, alarm)
		}

		/**
		 * Create an intent that will be used to start the foreground alarm
		 * service.
		 *
		 * @param context A context.
		 * @param alarm   An alarm.
		 *
		 * @return The Foreground service intent.
		 */
		fun getStartIntent(context: Context, alarm: NacAlarm?): Intent
		{
			// Create an intent with the alarm service
			val intent = Intent(ACTION_START_SERVICE, null, context,
				NacActiveAlarmService::class.java)

			// Add the alarm to the intent
			return NacIntent.addAlarm(intent, alarm)
		}

		/**
		 * Snooze the alarm service for the given alarm.
		 */
		fun snoozeAlarmService(context: Context, alarm: NacAlarm?)
		{
			// Create an intent with the alarm activity
			val intent = getSnoozeIntent(context, alarm)

			// Start the service. This will not be a foreground service so do
			// not need to call startForegroundService()
			context.startService(intent)
		}

		/**
		 * Start the foreground service.
		 */
		fun startAlarmService(context: Context, alarm: NacAlarm?)
		{
			// Create the intent
			val intent = getStartIntent(context, alarm)

			// Check if the API >= 26
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			{
				// Start the foreground service
				context.startForegroundService(intent)
			}
			// API is < 26
			else
			{
				// Start the service
				context.startService(intent)
			}
		}

	}

}