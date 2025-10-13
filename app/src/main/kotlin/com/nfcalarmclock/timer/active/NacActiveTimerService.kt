package com.nfcalarmclock.timer.active

import android.Manifest
import android.app.ForegroundServiceStartNotAllowedException
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.activealarm.NacWakeupProcess
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.system.addTimer
import com.nfcalarmclock.system.disableActivityAlias
import com.nfcalarmclock.system.enableActivityAlias
import com.nfcalarmclock.system.getTimer
import com.nfcalarmclock.timer.NacTimerRepository
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.util.NacUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.ceil

/**
 * Service to allow a timer to be run.
 */
@UnstableApi
@AndroidEntryPoint
class NacActiveTimerService
	: LifecycleService()
{

	/**
	 * Listener for when the active timer service is stopped.
	 */
	fun interface OnActiveTimerServiceStoppedListener
	{
		fun onActiveTimerServiceStopped()
	}

	/**
	 * Interface for when the countdown timer changes.
	 */
	interface OnCountdownTimerChangedListener
	{
		fun onCountdownFinished()
		fun onCountdownPaused()
		fun onCountdownReset(secUntilFinished: Long)
		fun onCountdownTick(secUntilFinished: Long, newProgress: Int)
	}

	/**
	 * Local binder class that can return the service.
	 */
	inner class NacLocalBinder
		: Binder()
	{

		/**
		 * Return this instance of this service so clients can call public methods.
		 */
		fun getService(): NacActiveTimerService = this@NacActiveTimerService

	}

	/**
	 * Binder given to clients.
	 */
	private val binder: NacLocalBinder = NacLocalBinder()

	/**
	 * Timer repository.
	 */
	@Inject
	lateinit var timerRepository: NacTimerRepository

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
	 * Timer.
	 */
	private var timer: NacTimer? = null

	/**
	 * Wakeup process that: plays music, vibrates the phone, etc.
	 */
	private var wakeupProcess: NacWakeupProcess? = null

	/**
	 * Automatically dismiss the timer in case it does not get dismissed.
	 */
	private var autoDismissHandler: Handler? = null

	/**
	 * Countdown timer.
	 */
	var countDownTimer: CountDownTimer? = null

	/**
	 * Listener for when the service is stopped.
	 */
	var onActiveTimerServiceStoppedListener: OnActiveTimerServiceStoppedListener? = null

	/**
	 * Listener for when the countdown timer changes.
	 */
	var onCountdownTimerChangedListener: OnCountdownTimerChangedListener? = null

	/**
	 * Total duration of the timer, in milliseconds.
	 */
	var totalDurationMillis: Long = 0

	/**
	 * Milliseconds until the countdown finishes.
	 */
	var millisUntilFinished: Long = 0

	/**
	 * Seconds until the countdown finishes.
	 */
	val secUntilFinished: Long
		get() = ceil(millisUntilFinished / 1000f).toLong()

	/**
	 * Current countdown progress as a percentage.
	 */
	val progress: Int
		get() = ((totalDurationMillis - millisUntilFinished) * 100 / totalDurationMillis).toInt()

	/**
	 * Whether this is the first tick of the countdown timer, while being connected to
	 * the fragment, or not.
	 */
	var isFirstTick: Boolean = true

	/**
	 * Add time to the countdown.
	 */
	fun addTimeToCountdown(sec: Long)
	{
		// Cancel the countdown
		countDownTimer?.cancel()

		// Add time to the time until finished
		millisUntilFinished += sec*1000
		println("New millis : $millisUntilFinished")

		// Time until finished exceeds the total time. Update the total time to match the
		// new time until finished. Use seconds until finished because it rounds up, in
		// case the milliseconds are off by a little
		if (totalDurationMillis < millisUntilFinished)
		{
			totalDurationMillis = secUntilFinished * 1000
			println("Updated total duration to match millis : $totalDurationMillis")
		}

		// Start the countdown
		startCountdownTimer()
	}

	/**
	 * Add time to the countdown.
	 */
	fun cancelCountdownTimer()
	{
		// Cancel the countdown
		countDownTimer?.cancel()

		// Set it to null to indicate that the timer is not running
		countDownTimer = null
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

		// Check if a wake lock is held
		if (wakeLock?.isHeld == true)
		{
			// Cleanup the wake lock
			wakeLock?.release()
		}
	}

	/**
	 * Dismiss the timer.
	 *
	 * This will finish the service.
	 */
	@UnstableApi
	fun dismiss(usedNfc: Boolean = false, wasMissed: Boolean = false)
	{
		// Update the timer
		lifecycleScope.launch {

			// Dismiss the timer
			timer!!.isActive = false
			println("Dismissing timer, so inactive.")

			// TODO: Do I even need to schedule anything because it will either be started or not?

			// Delete the timer after it is dismissed
			if (timer!!.shouldDeleteAfterDismissed)
			{
				timerRepository.delete(timer!!)
				//NacScheduler.cancel(this@NacActiveTimerService, timer!!)
			}
			// Update the timer in the database
			else
			{
				timerRepository.update(timer!!)
				//NacScheduler.update(this@NacActiveTimerService, timer!!)
			}

			// TODO: Do I need this?
			//// Set flag to refresh the main activity
			//sharedPreferences.shouldRefreshMainActivity = true

			// Restart any other active timer or stop the service
			restartOtherActiveTimerOrStop(R.string.message_timer_dismiss)

		}
	}

	/**
	 * Check if a new service was started.
	 *
	 * @param intentTimer A timer from an intent.
	 * @param action The action from an intent.
	 *
	 * @return True if a new service was started, and False otherwise.
	 */
	private fun isNewServiceStarted(
		intentTimer: NacTimer?,
		action: String
	): Boolean
	{
		return ((timer != null)
			&& (intentTimer != null)
			&& (action == ACTION_START_SERVICE))
	}

	/**
	 * Called when the service is bound.
	 */
	override fun onBind(intent: Intent): IBinder
	{
		// Super
		super.onBind(intent)
		println("onBind()")

		return binder
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
		timer = null
		autoDismissHandler = Handler(mainLooper)

		// Enable the activity alias so that tapping an NFC tag will open the main
		// activity
		enableActivityAlias(this)
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

		// Update the timer so it is no longer marked as active in the database
		lifecycleScope.launch {
			if (timer != null)
			{
				println("OnDestroy, Makking timer inactive.")
				timer!!.isActive = false
				timerRepository.update(timer!!)
			}
		}

		// Cleanup everything
		cleanup()
	}

	/**
	 * Called when the service is started.
	 */
	@UnstableApi
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		// Super
		super.onStartCommand(intent, flags, startId)

		// Setup the service
		setupActiveTimerService(intent)

		// Show active timer notification
		showActiveTimerNotification()
		println("Final jank : $intentAction")

		// Check the intent action
		when (intentAction)
		{

			// Timers are equal. TODO: What should be done here? Could this be equivalent of adding time?
			ACTION_EQUAL_ALARMS ->
			{
				println("DO NOTHING HERE? EQUAL ALARMS IN TIMER SERVICE")
				// TODO
				//NacActiveAlarmActivity.startAlarmActivity(this, alarm!!)
				return START_STICKY

			}

			// Start the service
			ACTION_START_SERVICE ->
			{
				println("START AS NORMAL")
				startActiveTimerService()
				return START_STICKY
			}

			// Stop the service
			ACTION_STOP_SERVICE -> stopActiveTimerService()

			// Dismiss
			ACTION_DISMISS_TIMER -> dismiss()

			// Dismiss with NFC
			ACTION_DISMISS_TIMER_WITH_NFC -> dismiss(usedNfc = true)

			// Pause
			ACTION_PAUSE_TIMER -> {
				println("PAUSE TIMER")
				cancelCountdownTimer()
				updateNotification()
				onCountdownTimerChangedListener?.onCountdownPaused()
			}

			// Resume
			ACTION_RESUME_TIMER -> {
				println("RESUMIO TIMER")
				startCountdownTimer()
				updateNotification()
			}

			// The default case if things go wrong
			else -> stopActiveTimerService()

		}

		return START_NOT_STICKY
	}

	/**
	 * Reset the timer.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	fun resetCountdownTimer()
	{
		// Set the total duration and millis needed to finish
		totalDurationMillis = timer!!.duration*1000
		millisUntilFinished = totalDurationMillis

		// Cancel the countdown
		cancelCountdownTimer()

		// Update the notification
		updateNotification()

		// Call the listener
		println("Total : $totalDurationMillis | Millis : $millisUntilFinished | Sec : $secUntilFinished")
		onCountdownTimerChangedListener?.onCountdownReset(secUntilFinished)
	}

	/**
	 * Restart any other active timer that may be set, or show a toast and stop the
	 * service.
	 */
	private suspend fun restartOtherActiveTimerOrStop(messageId: Int)
	{
		// Try and find an active timer
		val activeTimer = timerRepository.getActiveTimer()

		// Active timer was found
		if (activeTimer != null)
		{
			println("Starting timer again?")
			activeTimer.print()
			// Start the timer service for the active timer
			startTimerService(this, activeTimer)
		}
		// No other active timer
		else
		{
			// Show toast that the timer was snoozed/dismissed and stop the service
			withContext(Dispatchers.Main) {
				NacUtility.quickToast(this@NacActiveTimerService, messageId)
				println("Stopping active timer service")
				stopActiveTimerService()
			}
		}
	}

	/**
	 * Setup the service by getting the action, setting up the timer, and showing the
	 * notification.
	 */
	@UnstableApi
	private fun setupActiveTimerService(intent: Intent?)
	{
		// Set the intent action
		intentAction = intent?.action ?: ""

		// Attempt to get the timer from the intent
		val intentTimer = intent?.getTimer()

		println("INTENT ACTION : $intentAction")
		// New service was started
		if (isNewServiceStarted(intentTimer, intentAction))
		{
			println("NEW SERVICE STARTED")
			// Timers are equal. Set the action to indicate this
			if (intentTimer!!.equals(timer))
			{
				println("TIMER EQUALS STUFFS")
				intentAction = ACTION_EQUAL_ALARMS
				return
			}
		}

		// Set the new timer for this service
		if (intentTimer != null)
		{
			timer = intentTimer
		}

		// No timer found, so set the action to stop the service
		if (timer == null)
		{
			intentAction = ACTION_STOP_SERVICE
		}
	}

	/**
	 * Show the notification.
	 *
	 * Note: This can still be run with a null timer.
	 */
	private fun showActiveTimerNotification()
	{
		// Create the active timer notification
		val notification = NacActiveTimerNotification(this, timer)

		try
		{
			// Start the service in the foreground
			startForeground(notification.id, notification.build())
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
	 * Start the service.
	 */
	@UnstableApi
	private fun startActiveTimerService()
	{
		// Cleanup any resources
		cleanup()

		// Set the total duration and millis needed to finish
		// TODO: How do I want to use the isActive time? In relation to say counting down vs ringing and counting up
		totalDurationMillis = timer!!.duration*1000
		millisUntilFinished = totalDurationMillis

		// Set the active flag and update the timer in the database
		lifecycleScope.launch {
			timer!!.isActive = true
			timerRepository.update(timer!!)
			println("Timer is active. Updating the database")
		 }

		// Start the countdown timer
		startCountdownTimer()
	}

	/**
	 * Start the countdown timer.
	 */
	fun startCountdownTimer()
	{
		// Start the countdown timer since the fragment is no longer doing it
		countDownTimer = object : CountDownTimer(millisUntilFinished, 1000)
		{

			/**
			 * Every tick of the countdown.
			 */
			@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
			override fun onTick(millisUntilFinished: Long)
			{
				// Set the milliseconds until finished
				this@NacActiveTimerService.millisUntilFinished = millisUntilFinished
				println("SERVICE ON TICK : $millisUntilFinished | $secUntilFinished | Total : ${totalDurationMillis / 1000L} | $progress")

				// Update the notification
				updateNotification()

				// Call the listener
				onCountdownTimerChangedListener?.onCountdownTick(secUntilFinished, progress)

				// Change the first tick to false once the fragment has been connected
				// and has received the first tick
				if ((onCountdownTimerChangedListener != null) && isFirstTick)
				{
					isFirstTick = false
				}
			}

			/**
			 * Countdown finished.
			 */
			@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
			override fun onFinish()
			{
				println("SERVICE DONE WITH COUNTDOWN")

				// Set the milliseconds until finished
				this@NacActiveTimerService.millisUntilFinished = 0

				// Change the seconds in the notification to 0
				updateNotification()

				// Get the power manager and timeout for the wakelock
				val powerManager = getSystemService(POWER_SERVICE) as PowerManager
				val timeout = timer!!.autoDismissTime * 1000L

				// Acquire the wakelock
				wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					WAKELOCK_TAG)
				wakeLock!!.acquire(timeout)

				// Start the timer activity
				// TODO: How to start the fragment? Start activity and indicate fragment?
				//NacActiveTimerFragment.startAlarmActivity(this, timer!!)

				// Wait for auto dismiss
				waitForAutoDismiss()

				// Start the wakeup process
				wakeupProcess = NacWakeupProcess(this@NacActiveTimerService, timer!!)
				wakeupProcess!!.start()

				// Call the listener
				onCountdownTimerChangedListener?.onCountdownFinished()
			}
		}

		// Start the timer
		countDownTimer!!.start()
	}

	/**
	 * Stop the service.
	 */
	@Suppress("deprecation")
	fun stopActiveTimerService()
	{
		// Call the listener
		onActiveTimerServiceStoppedListener?.onActiveTimerServiceStopped()

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
	 * Update the notification.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	fun updateNotification()
	{
		// Get the new title
		val newTitle = NacCalendar.getFullTimeUntilTimer(this, secUntilFinished)
		println("UPDATE NOTIFICATION : New text? $newTitle")

		// Get the notification manager and builder
		val notificationManagerCompat = NotificationManagerCompat.from(this)
		val notificationBuilder = NacActiveTimerNotification(this, timer)

		// Create the notification
		val notification = (notificationBuilder.setContentTitle(newTitle) as NacActiveTimerNotification)
			.apply {
				// Timer is counting down
				if (millisUntilFinished > 0)
				{
					// Paused
					if (countDownTimer == null)
					{
						addAction(R.drawable.play, R.string.action_timer_resume, notificationBuilder.resumePendingIntent)
					}
					// Resumed and counting down
					else
					{
						addAction(R.drawable.pause_32, R.string.action_timer_pause, notificationBuilder.pausePendingIntent)
					}
				}
				// Timer has reached 0 and the sound and everything is going off
				else
				{
					addAction(R.drawable.stop_32, R.string.action_timer_stop, notificationBuilder.dismissPendingIntent)
				}
			}
			.build()

		// Update the notification
		notificationManagerCompat.notify(notificationBuilder.id, notification)
	}

	/**
	 * Wait in the background until the activity needs to auto dismiss the
	 * timer.
	 *
	 * Auto dismiss a bit early to avoid the race condition between a new timer
	 * starting at the same time that the timer will auto-dismiss.
	 */
	@UnstableApi
	fun waitForAutoDismiss()
	{
		// Check if should not auto dismiss
		if (!timer!!.shouldAutoDismiss || (timer!!.autoDismissTime == 0))
		{
			return
		}

		// Amount of time until the timer is automatically dismissed
		//val delay = TimeUnit.SECONDS.toMillis(timer!!.autoDismissTime.toLong()) - 750
		val delay = TimeUnit.SECONDS.toMillis(30) - 750
		println("Auto dismiss delay : $delay")

		// Automatically dismiss the timer
		autoDismissHandler!!.postDelayed({

			// TODO: Should show missed timer notification?
			// Auto dismiss the timer. This will stop the service
			dismiss(wasMissed = true)

		}, delay)
	}

	companion object
	{

		/**
		 * Action to start the service.
		 */
		const val ACTION_START_SERVICE = "com.nfcalarmclock.timer.active.ACTION_START_SERVICE"

		/**
		 * Action to stop the service.
		 */
		const val ACTION_STOP_SERVICE = "com.nfcalarmclock.timer.active.ACTION_STOP_SERVICE"

		/**
		 * Action to dismiss the timer.
		 */
		const val ACTION_DISMISS_TIMER = "com.nfcalarmclock.timer.active.ACTION_DISMISS_TIMER"

		/**
		 * Action to dismiss the timer with NFC.
		 */
		const val ACTION_DISMISS_TIMER_WITH_NFC = "com.nfcalarmclock.timer.active.ACTION_DISMISS_TIMER_WITH_NFC"

		/**
		 * Action to pause the timer.
		 */
		const val ACTION_PAUSE_TIMER = "com.nfcalarmclock.timer.active.ACTION_PAUSE_TIMER"

		/**
		 * Action to resume the timer.
		 */
		const val ACTION_RESUME_TIMER = "com.nfcalarmclock.timer.active.ACTION_RESUME_TIMER"

		/**
		 * Action to do when alarms are equal. This is to say when the service
		 * is started with the same alarm.
		 */
		private const val ACTION_EQUAL_ALARMS = "com.nfcalarmclock.ACTION_EQUAL_ALARMS"

		/**
		 * Tag for the wakelock.
		 */
		const val WAKELOCK_TAG = "NFC Alarm Clock:NacActiveTimerService"

		/**
		 * Dismiss the service for the given timer.
		 */
		fun dismissTimerService(context: Context, timer: NacTimer?)
		{
			// Create an intent with the timer service
			val intent = getDismissIntent(context, timer)

			// Start the service. This will not be a foreground service so do
			// not need to call startForegroundService()
			context.startService(intent)
		}

		/**
		 * Dismiss the service for the given timer with NFC.
		 */
		fun dismissTimerServiceWithNfc(context: Context, timer: NacTimer?)
		{
			// Create the intent with the timer service
			val intent = getDismissIntentWithNfc(context, timer)

			// Start the service. This will not be a foreground service so do
			// not need to call startForegroundService()
			context.startService(intent)
		}

		/**
		 * Get an intent that will be used to dismiss the foreground timer
		 * service.
		 *
		 * @return An intent that will be used to dismiss the foreground timer
		 *         service.
		 */
		fun getDismissIntent(context: Context, timer: NacTimer?): Intent
		{
			return Intent(ACTION_DISMISS_TIMER, null, context, NacActiveTimerService::class.java)
				.addTimer(timer)
		}

		/**
		 * Get an intent that will be used to dismiss the foreground timer
		 * service with NFC.
		 *
		 * @return An intent that will be used to dismiss the foreground timer
		 *         service with NFC.
		 */
		private fun getDismissIntentWithNfc(context: Context, timer: NacTimer?): Intent
		{
			return Intent(ACTION_DISMISS_TIMER_WITH_NFC, null, context, NacActiveTimerService::class.java)
				.addTimer(timer)
		}

		/**
		 * Get an intent to pause the active timer service.
		 *
		 * @return An intent to pause the active timer service.
		 */
		fun getPauseIntent(context: Context, timer: NacTimer?): Intent
		{
			return Intent(ACTION_PAUSE_TIMER, null, context, NacActiveTimerService::class.java)
				.addTimer(timer)
		}

		/**
		 * Get an intent to resume the active timer service.
		 *
		 * @return An intent to resume the active timer service.
		 */
		fun getResumeIntent(context: Context, timer: NacTimer?): Intent
		{
			return Intent(ACTION_RESUME_TIMER, null, context, NacActiveTimerService::class.java)
				.addTimer(timer)
		}

		/**
		 * Create an intent that will be used to start the foreground timer
		 * service.
		 *
		 * @param context A context.
		 * @param timer A timer.
		 *
		 * @return The Foreground service intent.
		 */
		fun getStartIntent(context: Context, timer: NacTimer?): Intent
		{
			return Intent(ACTION_START_SERVICE, null, context, NacActiveTimerService::class.java)
				.addTimer(timer)
		}

		/**
		 * Start the foreground service.
		 */
		fun startTimerService(context: Context, timer: NacTimer?)
		{
			// Create the intent
			val intent = getStartIntent(context, timer)

			// Start the foreground service
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			{
				context.startForegroundService(intent)
			}
			// Start the service
			else
			{
				context.startService(intent)
			}
		}

	}

}