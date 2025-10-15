package com.nfcalarmclock.timer.active

import android.Manifest
import android.annotation.SuppressLint
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
import java.util.Calendar
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
	 * Listener for when the service is stopped, such as when the stop button or auto
	 * dismiss handler are triggered.
	 */
	fun interface OnServiceStoppedListener
	{
		fun onServiceStopped()
	}

	/**
	 * Listener for when the countdown timer changes.
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

	///**
	// * Action of the intent that started the service.
	// */
	//private var intentAction: String = ""

	/**
	 * Timers.
	 */
	private var allTimers: MutableList<NacTimer> = arrayListOf()

	/**
	 * Wakelocks. One for each timer.
	 */
	private var allWakeLocks: HashMap<Long, WakeLock> = hashMapOf()

	/**
	 * Countdown timers. One for each timer.
	 */
	private var allCountdownTimers: HashMap<Long, CountDownTimer?> = hashMapOf()

	/**
	 * Wakeup processes that: plays music, vibrates the phone, etc. One for each timer.
	 */
	private var allWakeupProcesses: HashMap<Long, NacWakeupProcess> = hashMapOf()

	/**
	 * Automatically dismiss the timer in case it does not get dismissed. One for each timer.
	 */
	private var allAutoDismissHandlers: HashMap<Long, Handler> = hashMapOf()

	/**
	 * Time at which to show when the notification was shown. One for each timer.
	 */
	private var allNotificationShowWhenTime: HashMap<Long, Long> = hashMapOf()

	/**
	 * Notification ID of the foreground notification.
	 */
	private var foregroundNotificationId: Int = 0

	/**
	 * Listener for when the service is stopped, such as when the stop button or auto
	 * dismiss handler are triggered.
	 */
	var allOnServiceStoppedListeners: HashMap<Long, MutableList<OnServiceStoppedListener>> = hashMapOf()

	/**
	 * Listener for when the countdown timer changes.
	 */
	var allOnCountdownTimerChangedListeners: HashMap<Long, MutableList<OnCountdownTimerChangedListener>> = hashMapOf()

	/**
	 * Whether the service is bound or not.
	 */
	private var isBound : Boolean = false

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
	 * Add a listener for when the service is stopped, such as when the stop button or
	 * auto dismiss handler are triggered.
	 */
	fun addOnServiceStoppedListener(id: Long, listener: OnServiceStoppedListener)
	{
		// Create the list for any other listeners that may be added
		if (allOnServiceStoppedListeners[id] == null)
		{
			allOnServiceStoppedListeners.put(id, mutableListOf())
		}

		// Add the listener to the list
		allOnServiceStoppedListeners[id]!!.add(listener)
	}

	/**
	 * Add a listener for when the countdown timer changes.
	 */
	fun addOnCountdownTimerChangedListener(id: Long, listener: OnCountdownTimerChangedListener)
	{
		// Create the list for any other listeners that may be added
		if (allOnCountdownTimerChangedListeners[id] == null)
		{
			allOnCountdownTimerChangedListeners.put(id, mutableListOf())
		}

		// Add the listener to the list
		allOnCountdownTimerChangedListeners[id]!!.add(listener)
	}

	/**
	 * Add time to the countdown.
	 */
	fun addTimeToCountdown(timer: NacTimer, sec: Long)
	{
		// Get the countdown timer
		val countdownTimer = allCountdownTimers.get(timer.id)

		// Cancel the countdown
		countdownTimer?.cancel()

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
		startCountdownTimer(timer)
	}

	/**
	 * Add time to the countdown.
	 */
	fun cancelCountdownTimer(timer: NacTimer)
	{
		// Get the countdown timer
		val countdownTimer = allCountdownTimers.get(timer.id)

		// Cancel the countdown
		countdownTimer?.cancel()

		// Set it to null to indicate that the timer is not running
		allCountdownTimers[timer.id] = null
	}

	/**
	 * Run cleanup for a timer.
	 */
	@SuppressLint("MissingPermission")
	@UnstableApi
	private fun cleanup(timer: NacTimer)
	{
		// Clean the wakeup process
		allWakeupProcesses[timer.id]?.cleanup()

		// Cleanup the auto dismiss handler
		allAutoDismissHandlers[timer.id]?.removeCallbacksAndMessages(null)

		// Cleanup the wake lock
		allWakeLocks[timer.id]?.apply {
			if (isHeld)
			{
				release()
			}
		}

		// Remove the items from the hashmaps
		allWakeupProcesses.remove(timer.id)
		allAutoDismissHandlers.remove(timer.id)
		allWakeLocks.remove(timer.id)
		allTimers.removeIf { it.id == timer.id }

		// Stop the service if no more timers are active
		if (allTimers.isEmpty())
		{
			stopActiveTimerService()
		}
		// Call the stop listener
		else
		{
			println("Calling stop yo listener : ${timer.id}")
			allOnServiceStoppedListeners[timer.id]?.forEach { it.onServiceStopped() }

			// Close the notification
			val notification = NacActiveTimerNotification(this, timer)
			val notificationManagerCompat = NotificationManagerCompat.from(this)

			if (notification.id == foregroundNotificationId)
			{
				val newForegroundTimer = allTimers.first { it.id != timer.id }
				println("Closing the foreground jank notification : ${notification.id} | ${newForegroundTimer.id}")
				updateNotification(newForegroundTimer, foreground = true)
			}
			else
			{
				println("Normal closing the notification : ${notification.id}")
				notificationManagerCompat.cancel(notification.id)
			}
		}
	}

	/**
	 * Dismiss the timer.
	 *
	 * This will finish the service.
	 */
	@UnstableApi
	fun dismiss(timer: NacTimer, usedNfc: Boolean = false, wasMissed: Boolean = false)
	{
		// Update the timer
		lifecycleScope.launch {

			// Dismiss the timer
			timer.isActive = false
			println("Dismissing timer, so inactive.")

			// Delete the timer after it is dismissed
			if (timer.shouldDeleteAfterDismissed)
			{
				timerRepository.delete(timer)
			}
			// Update the timer in the database
			else
			{
				timerRepository.update(timer)
			}

			// TODO: Do I need this?
			//// Set flag to refresh the main activity
			//sharedPreferences.shouldRefreshMainActivity = true

			// Show toast that the timer was dismissed and stop the service
			withContext(Dispatchers.Main) {
				NacUtility.quickToast(this@NacActiveTimerService, R.string.message_timer_dismiss)
				println("Stopping active timer service")
				cleanup(timer)
				//stopActiveTimerService()
			}

		}
	}

	/**
	 * Called when the service is bound.
	 */
	override fun onBind(intent: Intent): IBinder
	{
		// Super
		super.onBind(intent)

		println("onBind()")
		// Set the bound flag
		isBound = true

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
		//wakeLock = null
		//timer = null
		//autoDismissHandler =

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
			allTimers.forEach { t ->

				println("OnDestroy, Makking timer inactive.")
				// Deactivate the timer
				t.isActive = false
				timerRepository.update(t)

				// Cleanup everything
				cleanup(t)
			}
		}

		// Cleanup everything
		//cleanup()
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
		//setupActiveTimerService(intent)
		// Attempt to get the timer from the intent
		val timer = intent?.getTimer()

		println("INTENT ACTION : ${intent?.action} | ${allTimers.forEach { it.id }}")
		// Set the new timer for this service
		if (timer != null)
		{
			println("Revised timer id : ${timer.id}")
			// Add the timer to the list
			if (allTimers.none { it.id == timer.id })
			{
				println("ADDING TIMER : ${timer.id}")
				allTimers.add(timer)
			}
			//timer = intentTimer
		}
		else
		{
			println("TIMER IS NULL STOPPING PREMATURELY")
			return START_NOT_STICKY
		}

		// Show active timer notification
		showActiveTimerNotification(timer)
		println("Final jank : ${intent.action}")

		// Check the intent action
		when (intent.action)
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
				startActiveTimerService(timer)
				return START_STICKY
			}

			// Dismiss
			ACTION_DISMISS_TIMER -> dismiss(timer)

			// Dismiss with NFC
			ACTION_DISMISS_TIMER_WITH_NFC -> dismiss(timer, usedNfc = true)

			// Pause
			ACTION_PAUSE_TIMER -> {
				println("PAUSE TIMER")
				cancelCountdownTimer(timer)
				updateNotification(timer)
				allOnCountdownTimerChangedListeners[timer.id]?.forEach { it.onCountdownPaused() }
				//allOnCountdownTimerChangedListeners[timer.id]?.onCountdownPaused()
				//onCountdownTimerChangedListener?.onCountdownPaused()
			}

			// Resume
			ACTION_RESUME_TIMER -> {
				println("RESUMIO TIMER")
				startCountdownTimer(timer)
				updateNotification(timer)
			}

			// The default case if things go wrong
			else -> stopActiveTimerService()

		}

		return START_NOT_STICKY
	}

	/**
	 * Remove all matching listeners for when the countdown timer changes.
	 */
	fun removeAllMatchingOnCountdownTimerChangedListener(listener: OnCountdownTimerChangedListener)
	{
		allOnCountdownTimerChangedListeners.values.forEach {
			it.remove(listener)
		}
	}

	/**
	 * Remove all matching listeners for when the service is stopped, such as when the stop
	 * button or auto dismiss handler are triggered.
	 */
	fun removeAllMatchingOnServiceStoppedListeners(listener: OnServiceStoppedListener)
	{
		allOnServiceStoppedListeners.values.forEach {
			it.remove(listener)
		}
	}

	/**
	 * Remove the listener for when the countdown timer changes.
	 */
	fun removeOnCountdownTimerChangedListener(id: Long, listener: OnCountdownTimerChangedListener)
	{
		allOnCountdownTimerChangedListeners[id]?.remove(listener)
	}

	/**
	 * Remove the listener for when the service is stopped, such as when the stop button
	 * or auto dismiss handler are triggered.
	 */
	fun removeOnServiceStoppedListener(id: Long, listener: OnServiceStoppedListener)
	{
		allOnServiceStoppedListeners[id]?.remove(listener)
	}

	/**
	 * Reset the timer.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	fun resetCountdownTimer(timer: NacTimer)
	{
		// Set the total duration and millis needed to finish
		totalDurationMillis = timer.duration*1000
		millisUntilFinished = totalDurationMillis

		// Cancel the countdown
		cancelCountdownTimer(timer)

		// Update the notification
		updateNotification(timer)

		// Call the listener
		println("Total : $totalDurationMillis | Millis : $millisUntilFinished | Sec : $secUntilFinished")
		allOnCountdownTimerChangedListeners[timer.id]?.forEach { it.onCountdownReset(secUntilFinished) }
		//allOnCountdownTimerChangedListeners[timer.id]?.onCountdownReset(secUntilFinished)
	}

	///**
	// * Setup the service by getting the action, setting up the timer, and showing the
	// * notification.
	// */
	//@UnstableApi
	//private fun setupActiveTimerService(intent: Intent?)
	//{
	//	//// Set the intent action
	//	//intentAction = intent?.action ?: ""

	//	// Attempt to get the timer from the intent
	//	val intentTimer = intent?.getTimer()

	//	println("INTENT ACTION : ${intent?.action} | ${allTimers.forEach { it.id }}")

	//	// Set the new timer for this service
	//	if (intentTimer != null)
	//	{
	//		println("Revised timer id : ${intentTimer.id}")
	//		// Add the timer to the list
	//		if (allTimers.none { it.id == intentTimer.id })
	//		{
	//			println("ADDING TIMER : ${intentTimer.id}")
	//			allTimers.add(intentTimer)
	//		}
	//		//timer = intentTimer
	//	}

	//	//// No timer found, so set the action to stop the service
	//	//if (timer == null)
	//	//{
	//	//	intentAction = ACTION_STOP_SERVICE
	//	//}
	//}

	/**
	 * Show the notification.
	 *
	 * Note: This can still be run with a null timer.
	 */
	@SuppressLint("MissingPermission")
	private fun showActiveTimerNotification(timer: NacTimer)
	{
		// Create the active timer notification
		val notification = NacActiveTimerNotification(this, timer)

		try
		{
			// Service is already running in the foreground. Create a new notification
			println("Starting active timer notification : ${notification.id} | ${timer.id}")
			if (isBound)
			{
				println("NOTIFICY")
				val notificationmanager = NotificationManagerCompat.from(this)
				notificationmanager.notify(notification.id, notification.build())
			}
			// Service barely beginning to run. Start the service in the foreground
			else
			{
				println("FOREGROUND")
				startForeground(notification.id, notification.build())
				foregroundNotificationId = notification.id
			}
		}
		catch (e: Exception)
		{
			// Check if not allowed to start foreground service
			if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) && (e is ForegroundServiceStartNotAllowedException))
			{
				NacUtility.toast(this, R.string.error_message_unable_to_start_foreground_service)
			}
		}

		// Save the show when time for the notification
		if (timer.id !in allNotificationShowWhenTime)
		{
			allNotificationShowWhenTime[timer.id] = Calendar.getInstance().timeInMillis
			println("Adding show when time : ${allNotificationShowWhenTime[timer.id]}")
		}
	}

	/**
	 * Start the service.
	 */
	@UnstableApi
	private fun startActiveTimerService(timer: NacTimer)
	{
		// Set the total duration and millis needed to finish
		// TODO: How do I want to use the isActive time? In relation to say counting down vs ringing and counting up
		totalDurationMillis = timer.duration*1000
		millisUntilFinished = totalDurationMillis

		// Set the active flag and update the timer in the database
		lifecycleScope.launch {
			timer.isActive = true
			timerRepository.update(timer)
			println("Timer is active. Updating the database")
		 }

		// Start the countdown timer
		startCountdownTimer(timer)
	}

	/**
	 * Start the countdown timer.
	 */
	fun startCountdownTimer(timer: NacTimer)
	{
		// Start the countdown timer since the fragment is no longer doing it
		val countdownTimer = object : CountDownTimer(millisUntilFinished, 1000)
		{

			/**
			 * Every tick of the countdown.
			 */
			@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
			override fun onTick(millisUntilFinished: Long)
			{
				// Set the milliseconds until finished
				this@NacActiveTimerService.millisUntilFinished = millisUntilFinished
				println("Service on tick : $millisUntilFinished | $secUntilFinished | Total : ${totalDurationMillis / 1000L} | $progress")

				// Update the notification
				updateNotification(timer)

				// Call the listener
				allOnCountdownTimerChangedListeners[timer.id]?.forEach { it.onCountdownTick(secUntilFinished, progress) }
				//allOnCountdownTimerChangedListeners[timer.id]?.onCountdownTick(secUntilFinished, progress)
				//onCountdownTimerChangedListener?.onCountdownTick(secUntilFinished, progress)

				// Change the first tick to false once the fragment has been connected
				// and has received the first tick
				//if ((onCountdownTimerChangedListener != null) && isFirstTick)
				if (allOnCountdownTimerChangedListeners.isNotEmpty() && isFirstTick)
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
				updateNotification(timer)

				// Get the power manager and timeout for the wakelock
				val powerManager = getSystemService(POWER_SERVICE) as PowerManager
				val timeout = timer.autoDismissTime * 1000L

				// Acquire the wakelock
				val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					WAKELOCK_TAG)
				wakeLock!!.acquire(timeout)

				// Add the wakelock to the hashmap
				allWakeLocks.put(timer.id, wakeLock)

				// Start the timer activity
				// TODO: How to start the fragment? Start activity and indicate fragment?
				//NacActiveTimerFragment.startAlarmActivity(this, timer!!)

				// Wait for auto dismiss
				waitForAutoDismiss(timer)

				// Start the wakeup process
				val wakeupProcess = NacWakeupProcess(this@NacActiveTimerService, timer)
				wakeupProcess.start()

				// Add the wakeup process to the hashmap
				allWakeupProcesses.put(timer.id, wakeupProcess)

				// Call the listener
				allOnCountdownTimerChangedListeners[timer.id]?.forEach { it.onCountdownFinished() }
				//allOnCountdownTimerChangedListeners[timer.id]?.onCountdownFinished()
				//onCountdownTimerChangedListener?.onCountdownFinished()
			}
		}

		// Start the countdown timer
		countdownTimer.start()

		// Add the countdown timer to the hashmap
		allCountdownTimers.put(timer.id, countdownTimer)
	}

	/**
	 * Stop the service.
	 */
	@Suppress("deprecation")
	fun stopActiveTimerService()
	{
		// Call the listener
		allTimers.forEach { it ->
			println("Calling stop active timer service : ${it.id}")
			allOnServiceStoppedListeners.values.forEach { it.forEach { listener -> listener.onServiceStopped() } }
		}

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
	fun updateNotification(timer: NacTimer, foreground: Boolean = false)
	{
		// Get the countdown timer
		val countdownTimer = allCountdownTimers.get(timer.id)

		// Get the new title
		val newTitle = NacCalendar.getFullTimeUntilTimer(this, secUntilFinished)

		// Get the notification manager and builder
		val notificationManagerCompat = NotificationManagerCompat.from(this)
		val notificationBuilder = NacActiveTimerNotification(this, timer)

		// Create the notification
		val notification = (notificationBuilder.setContentTitle(newTitle)
			.setWhen(allNotificationShowWhenTime[timer.id]!!) as NacActiveTimerNotification)
			.apply {
				// Timer is counting down
				if (millisUntilFinished > 0)
				{
					// Paused
					if (countdownTimer == null)
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
		if (foreground)
		{
			println("FOREGROUND JANK BRO")
			startForeground(notificationBuilder.id, notification)
			foregroundNotificationId = notificationBuilder.id
		}
		else
		{
			notificationManagerCompat.notify(notificationBuilder.id, notification)
		}
	}

	/**
	 * Wait in the background until the activity needs to auto dismiss the
	 * timer.
	 *
	 * Auto dismiss a bit early to avoid the race condition between a new timer
	 * starting at the same time that the timer will auto-dismiss.
	 */
	@UnstableApi
	fun waitForAutoDismiss(timer: NacTimer)
	{
		// Check if should not auto dismiss
		if (!timer.shouldAutoDismiss || (timer.autoDismissTime == 0))
		{
			return
		}

		// Amount of time until the timer is automatically dismissed
		//val delay = TimeUnit.SECONDS.toMillis(timer!!.autoDismissTime.toLong()) - 750
		val delay = TimeUnit.SECONDS.toMillis(30) - 750
		println("Auto dismiss delay : $delay")

		// Create the handler
		val handler = Handler(mainLooper)

		// Automatically dismiss the timer
		handler.postDelayed({

			// TODO: Should show missed timer notification?
			// Auto dismiss the timer. This will stop the service
			dismiss(timer, wasMissed = true)

		}, delay)

		// Save the handler
		allAutoDismissHandlers.put(timer.id, handler)
	}

	companion object
	{

		/**
		 * Action to start the service.
		 */
		const val ACTION_START_SERVICE = "com.nfcalarmclock.timer.active.ACTION_START_SERVICE"

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