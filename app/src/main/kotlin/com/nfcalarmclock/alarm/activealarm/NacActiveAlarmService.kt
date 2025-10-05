package com.nfcalarmclock.alarm.activealarm

import android.app.ForegroundServiceStartNotAllowedException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.BuildConfig
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.NacAlarmRepository
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.dismissoptions.NacDismissEarlyService
import com.nfcalarmclock.alarm.options.missedalarm.NacMissedAlarmNotification
import com.nfcalarmclock.alarm.options.upcomingreminder.NacUpcomingReminderService
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.statistics.NacAlarmStatisticRepository
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.util.NacUtility
import com.nfcalarmclock.system.addAlarm
import com.nfcalarmclock.system.disableActivityAlias
import com.nfcalarmclock.system.enableActivityAlias
import com.nfcalarmclock.system.getAlarm
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Service to allow an alarm to be run.
 */
@UnstableApi
@AndroidEntryPoint
class NacActiveAlarmService
	: LifecycleService()
{

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
	 * Action of the intent that started the service.
	 */
	private var intentAction: String = ""

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
	 * Delay the wakeup process to ensure that the activity is shown before playing
	 * audio.
	 */
	private var wakeupProcessDelayHandler: Handler? = null

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
	 * Run cleanup.
	 */
	@UnstableApi
	private fun cleanup()
	{
		// Clean the wakeup process
		wakeupProcess?.cleanup()

		// Cleanup the auto dismiss and snooze handler
		wakeupProcessDelayHandler?.removeCallbacksAndMessages(null)
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
		sharedPreferences.isSelectedMediaForAlarmNotAvailable = false
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
		lifecycleScope.launch {

			// Dismiss the alarm
			alarm!!.dismiss()

			// Check if the alarm missed and had to be dismissed via auto
			// dismiss
			if (wasMissed)
			{
				// Write the missed alarm to the statistics table
				statisticRepository.insertMissed(alarm)
			}
			// Alarm was dismissed normally
			else
			{
				// Write the dismissed alarm to the statistics table (used NFC)
				statisticRepository.insertDismissed(alarm, usedNfc)
			}

			// Delete the alarm after it is dismissed and cancel any subsequent alarms.
			// This will also write to the stats table
			if (alarm!!.shouldDeleteAfterDismissed)
			{
				alarmRepository.delete(alarm!!)
				statisticRepository.insertDeleted(alarm)
				NacScheduler.cancel(this@NacActiveAlarmService, alarm!!)
			}
			// Update and reschedule the alarm
			else
			{
				alarmRepository.update(alarm!!)
				NacScheduler.update(this@NacActiveAlarmService, alarm!!)
			}

			// Save the next alarm
			saveNextAlarm()

			// Set flag to refresh the main activity
			sharedPreferences.shouldRefreshMainActivity = true

			// Clear the dismiss early notification
			NacDismissEarlyService.stopService(this@NacActiveAlarmService, alarm)

			// Restart any other active alarm or stop the service
			restartOtherActiveAlarmOrStop(R.string.message_alarm_dismiss)

		}
	}

	/**
	 * Check if a new service was started.
	 *
	 * @param intentAlarm An alarm from an intent.
	 * @param action The action from an intent.
	 *
	 * @return True if a new service was started, and False otherwise.
	 */
	private fun isNewServiceStarted(
		intentAlarm: NacAlarm?,
		action: String
	): Boolean
	{
		return ((alarm != null)
			&& (intentAlarm != null)
			&& (action == ACTION_START_SERVICE))
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
		wakeupProcessDelayHandler = Handler(mainLooper)
		autoDismissHandler = Handler(mainLooper)
		autoSnoozeHandler = Handler(mainLooper)

		// Enable the activity alias so that tapping an NFC tag will open the main
		// activity
		enableActivityAlias(this)

		// Set the previous app version if it has not been set yet
		if (sharedPreferences.previousAppVersion.isEmpty())
		{
			setupAppVersion()
		}
	}

	/**
	 * Called when the service is destroyed.
	 */
	@UnstableApi
	override fun onDestroy()
	{
		// Super
		super.onDestroy()

		// Disable the activity alias so that tapping an NFC tag will not do anything
		disableActivityAlias(this)

		// Skip the service, so just run the cleanup and return
		if (intentAction == ACTION_SKIP_SERVICE)
		{
			cleanup()
			return
		}

		// Stop the alarm activity
		NacActiveAlarmActivity.stopAlarmActivity(this)

		// Set the alarm as not active and update the alarm
		lifecycleScope.launch {
			if (alarm != null)
			{
				alarm!!.isActive = false
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
		// Super
		super.onStartCommand(intent, flags, startId)

		// TODO: Try to have activity bind and use this flag BIND_ALLOW_ACTIVITY_STARTS, BIND_IMPORTANT

		// Setup the service
		setupActiveAlarmService(intent)

		// Setup the service and disable any reminder notification that may be present
		// when NOT skipping this alarm
		if (intentAction != ACTION_SKIP_SERVICE)
		{
			// Show active alarm notification
			showActiveAlarmNotification()

			// Clear the upcoming reminder notification
			NacUpcomingReminderService.stopService(this, alarm)
		}

		// Check the intent action
		when (intentAction)
		{

			// Alarms are equal. Start the alarm activity
			ACTION_EQUAL_ALARMS ->
			{
				NacActiveAlarmActivity.startAlarmActivity(this, alarm!!)
				return START_STICKY

			}

			// Legacy check to see if the alarm should be skipped
			ACTION_SKIP_SERVICE ->
			{
				// Show the skip notification
				showSkipAlarmNotification()

				lifecycleScope.launch {

					// Skip the alarm
					alarm!!.skipAlarm()

					// Update the database
					alarmRepository.update(alarm!!)

					// Reschedule the next alarm
					NacScheduler.update(this@NacActiveAlarmService, alarm!!)

					// Stop the service
					super.stopSelf()

				}

				return START_NOT_STICKY
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
				if (alarm!!.canSnooze)
				{
					snooze()
				}
				// Unable to snooze the alarm
				else
				{
					// Show a toast saying the alarm could not be snoozed
					lifecycleScope.launch {
						withContext(Dispatchers.Main)
						{
							NacUtility.quickToast(this@NacActiveAlarmService, R.string.error_message_snooze)
						}
					}
				}
			}

			// The default case if things go wrong
			else -> stopActiveAlarmService()

		}

		return START_NOT_STICKY
	}

	/**
	 * Restart any other active alarm that may be set, or show a toast and stop the
	 * service.
	 */
	private suspend fun restartOtherActiveAlarmOrStop(messageId: Int)
	{
		// Try and find an active alarm, ordered by the one that has been active the
		// longest
		val activeAlarm = alarmRepository.getActiveAlarm()

		// Active alarm was found
		if (activeAlarm != null)
		{
			// Start the alarm service for the active alarm
			startAlarmService(this, activeAlarm)
		}
		// No other active alarm
		else
		{
			// Show toast that the alarm was snoozed/dismissed and stop the service
			withContext(Dispatchers.Main) {
				NacUtility.quickToast(this@NacActiveAlarmService, messageId)
				stopActiveAlarmService()
			}
		}
	}

	/**
	 * Save the next alarm.
	 *
	 * This will only do something if the shared preference to do so is set.
	 */
	private suspend fun saveNextAlarm(snoozeCal: Calendar? = null)
	{
		// Check if should save the app's next alarm or if any system alarm is used
		if (sharedPreferences.appShouldSaveNextAlarm)
		{
			// Save the next alarm information
			val allAlarms = alarmRepository.getAllAlarms()
			sharedPreferences.saveNextAlarm(allAlarms, snoozeCal = snoozeCal)
		}
	}

	/**
	 * Setup the service by getting the action, setting up the alarm, and showing the
	 * notification.
	 */
	@UnstableApi
	private fun setupActiveAlarmService(intent: Intent?)
	{
		// Set the intent action
		intentAction = intent?.action ?: ""

		// Attempt to get the alarm from the intent
		val intentAlarm = intent?.getAlarm()

		// New service was started
		if (isNewServiceStarted(intentAlarm, intentAction))
		{
			// Alarms are equal. Set the action to indicate this
			if (intentAlarm!!.equals(alarm))
			{
				intentAction = ACTION_EQUAL_ALARMS
				return
			}
			// Update the active time of the current alarm
			else
			{
				updateTimeActiveOfCurrentAlarm()
			}
		}

		// Set the new alarms for this service
		if (intentAlarm != null)
		{
			alarm = intentAlarm
		}

		// No alarm found, so set the action to stop the service
		if (alarm == null)
		{
			intentAction = ACTION_STOP_SERVICE
		}
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
	 * Show the notification when an alarm is skipped.
	 */
	private fun showSkipAlarmNotification()
	{
		// Create the active alarm notification
		val notification = NacSkipAlarmNotification(this, alarm)

		try
		{
			// Start the service in the foreground
			startForeground(NacSkipAlarmNotification.ID,
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
		lifecycleScope.launch {

			// Snooze the alarm and get the next time to run the alarm again
			val cal = alarm!!.snooze()

			// Update the time the alarm was active
			alarm!!.timeActive += System.currentTimeMillis() - startTime

			// Update the alarm, write to the stats table, and reschedule the alarm
			alarmRepository.update(alarm!!)
			statisticRepository.insertSnoozed(alarm, alarm!!.snoozeDuration.toLong())
			NacScheduler.update(this@NacActiveAlarmService, alarm!!, cal)

			// Save the next alarm
			saveNextAlarm(snoozeCal = cal)

			// Set flag to refresh the main activity
			sharedPreferences.shouldRefreshMainActivity = true

			// Restart any other active alarm or stop the service
			restartOtherActiveAlarmOrStop(R.string.message_alarm_snooze)

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
		val timeout = alarm!!.autoDismissTime * 1000L

		// Acquire the wakelock
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
			WAKELOCK_TAG)
		wakeLock!!.acquire(timeout)

		// Start the alarm activity
		NacActiveAlarmActivity.startAlarmActivity(this, alarm!!)

		// Wait for auto dismiss and auto snooze
		waitForAutoDismiss()
		waitForAutoSnooze()

		// Start the wakeup process after a delay of 1 sec. With Android 15, possibly
		// earlier versions as well, need to ensure that the activity is in focus before
		// trying to request audio focus, otherwise any media or TTS will fail
		wakeupProcess = NacWakeupProcess(this, alarm!!)

		//Handler(mainLooper).postDelayed({
		wakeupProcessDelayHandler?.postDelayed({
			wakeupProcess!!.start()
		}, 1000)

		// Set the active flag and reset the skip flag, then update the alarm
		lifecycleScope.launch {
			alarm!!.isActive = true
			alarm!!.shouldSkipNextAlarm = false

			alarmRepository.update(alarm!!)
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

		lifecycleScope.launch {

			// Check if the service has started
			if (currentStartTime != 0L)
			{
				// Set the time that the alarm was active. Do not set the
				// alarm as inactive though because then it will not go off
				// again. It should go off again because the alarm has not
				// been finished being interacted with
				currentAlarm.timeActive += System.currentTimeMillis() - currentStartTime

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
		// Set the start time
		startTime = System.currentTimeMillis()

		// Check if should not auto dismiss
		if (!alarm!!.shouldAutoDismiss || (alarm!!.autoDismissTime == 0))
		{
			return
		}

		// Amount of time until the alarm is automatically dismissed
		val delay = TimeUnit.SECONDS.toMillis(alarm!!.autoDismissTime.toLong()) - alarm!!.timeActive - 750

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
		// Check if should not auto snooze or cannot snooze
		if (!alarm!!.shouldAutoSnooze || !alarm!!.canSnooze || (alarm!!.autoSnoozeTime == 0))
		{
			return
		}

		// Amount of time until the alarm is automatically snoozed
		val delay = TimeUnit.SECONDS.toMillis(alarm!!.autoSnoozeTime.toLong()) - 750

		// Automatically snooze the alarm
		autoSnoozeHandler!!.postDelayed({ snooze() }, delay)
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
		 * Action to skip the service.
		 */
		const val ACTION_SKIP_SERVICE = "com.nfcalarmclock.ACTION_SKIP_SERVICE"

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
		const val WAKELOCK_TAG = "NFC Alarm Clock:NacActiveAlarmService"

		/**
		 * Dismiss the service for the given alarm.
		 */
		fun dismissAlarmService(context: Context, alarm: NacAlarm?)
		{
			// Create an intent with the alarm service
			val intent = getDismissIntent(context, alarm)

			// Start the service. This will not be a foreground service so do
			// not need to call startForegroundService()
			context.startService(intent)
		}

		/**
		 * Dismiss the service for the given alarm with NFC.
		 */
		fun dismissAlarmServiceWithNfc(context: Context, alarm: NacAlarm?)
		{
			// Create the intent with the alarm service
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
			return Intent(ACTION_DISMISS_ALARM, null, context, NacActiveAlarmService::class.java)
				.addAlarm(alarm)
		}

		/**
		 * Get an intent that will be used to dismiss the foreground alarm
		 * service with NFC.
		 *
		 * @return An intent that will be used to dismiss the foreground alarm
		 *         service with NFC.
		 */
		private fun getDismissIntentWithNfc(context: Context, alarm: NacAlarm?): Intent
		{
			// Create the intent with the alarm service
			return Intent(ACTION_DISMISS_ALARM_WITH_NFC, null, context, NacActiveAlarmService::class.java)
				.addAlarm(alarm)
		}

		/**
		 * Create an intent that will be used to skip the alarm service.
		 *
		 * @param context A context.
		 * @param alarm   An alarm.
		 *
		 * @return The service intent.
		 */
		fun getSkipIntent(context: Context, alarm: NacAlarm?): Intent
		{
			// Create an intent with the alarm service
			return Intent(ACTION_SKIP_SERVICE, null, context, NacActiveAlarmService::class.java)
				.addAlarm(alarm)
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
			return Intent(ACTION_SNOOZE_ALARM, null, context, NacActiveAlarmService::class.java)
				.addAlarm(alarm)
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
			return Intent(ACTION_START_SERVICE, null, context, NacActiveAlarmService::class.java)
				.addAlarm(alarm)
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
			//
			// Note: Skipped alarms will use the normal startService() since they will stop the
			// service immediately and won't need to be in the foreground
			if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) && (alarm?.shouldSkipNextAlarm != true))
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