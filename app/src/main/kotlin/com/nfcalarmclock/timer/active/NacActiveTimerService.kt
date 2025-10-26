package com.nfcalarmclock.timer.active

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager.WakeLock
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.activealarm.NacWakeupProcess
import com.nfcalarmclock.nfc.shouldUseNfc
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.system.NacLifecycleService
import com.nfcalarmclock.system.addTimer
import com.nfcalarmclock.system.disableActivityAlias
import com.nfcalarmclock.system.enableActivityAlias
import com.nfcalarmclock.system.getTimer
import com.nfcalarmclock.timer.NacTimerRepository
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.view.quickToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.ceil

/**
 * Service to allow a timer to be run.
 */
@UnstableApi
@AndroidEntryPoint
class NacActiveTimerService
	: NacLifecycleService()
{

	/**
	 * Listener for when the service is stopped, such as when the stop button or auto
	 * dismiss handler are triggered.
	 */
	fun interface OnServiceStoppedListener
	{
		fun onServiceStopped(timer: NacTimer)
	}

	/**
	 * Listener for when the countdown timer changes.
	 */
	interface OnCountdownTimerChangedListener
	{
		fun onCountdownFinished(timer: NacTimer)
		fun onCountdownPaused(timer: NacTimer)
		fun onCountdownReset(timer: NacTimer, secUntilFinished: Long)
		fun onCountdownTick(timer: NacTimer, secUntilFinished: Long, newProgress: Int)
	}

	/**
	 * Listener for when the countup handler ticks.
	 */
	fun interface OnCountupTickListener
	{
		fun onCountupTick(timer: NacTimer, secOfRinging: Long)
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
	 * Timer repository.
	 */
	@Inject
	lateinit var timerRepository: NacTimerRepository

	/**
	 * Shared preferences.
	 */
	private lateinit var sharedPreferences: NacSharedPreferences

	/**
	 * Binder given to clients.
	 */
	private val binder: NacLocalBinder = NacLocalBinder()

	/**
	 * Timers.
	 */
	private val allTimers: MutableList<NacTimer> = arrayListOf()

	/**
	 * Wakelocks. One for each timer.
	 */
	private val allWakeLocks: HashMap<Long, WakeLock> = hashMapOf()

	/**
	 * Countdown timers. One for each timer.
	 */
	private val allCountdownTimers: HashMap<Long, CountDownTimer?> = hashMapOf()

	/**
	 * Wakeup processes that: plays music, vibrates the phone, etc. One for each timer.
	 */
	private val allWakeupProcesses: HashMap<Long, NacWakeupProcess> = hashMapOf()

	/**
	 * Automatically dismiss the timer in case it does not get dismissed. One for each timer.
	 */
	private val allAutoDismissHandlers: HashMap<Long, Handler> = hashMapOf()

	/**
	 * Time at which to show when the notification was shown. One for each timer.
	 */
	private val allNotificationShowWhenTime: HashMap<Long, Long> = hashMapOf()

	/**
	 * Notification ID of the foreground notification.
	 */
	private var foregroundNotificationId: Int = 0

	/**
	 * Whether the service is bound or not.
	 */
	private var isBound : Boolean = false

	/**
	 * Total duration of the timer, in milliseconds. One for each timer.
	 */
	private var allTotalDurationMillis: HashMap<Long, Long> = hashMapOf()

	/**
	 * Milliseconds until the countdown finishes.
	 */
	private var allMillisUntilFinished: HashMap<Long, Long> = hashMapOf()

	/**
	 * Whether this is the first tick of the countdown timer, while being connected to
	 * the fragment, or not.
	 */
	val allIsFirstTick: HashMap<Long, Boolean> = hashMapOf()

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
	 * Listener for when the countup handler ticks.
	 */
	var allOnCountupTickListeners: HashMap<Long, MutableList<OnCountupTickListener>> = hashMapOf()

	/**
	 * The time that a timer started ringing, in milliseconds.
	 */
	val allTimerRingingCountupHandlers: HashMap<Long, Handler> = hashMapOf()

	/**
	 * The time that a timer started ringing, in milliseconds.
	 */
	val timerRingingStartTimeMillis: HashMap<Long, Long> = hashMapOf()

	/**
	 * Read only list of the timers being used by the service.
	 */
	val allTimersReadOnly: List<NacTimer>
		get() = allTimers

	/**
	 * Add a listener for when the service is stopped, such as when the stop button or
	 * auto dismiss handler are triggered.
	 */
	fun addOnServiceStoppedListener(id: Long, listener: OnServiceStoppedListener)
	{
		// Create the list for any other listeners that may be added
		if (allOnServiceStoppedListeners[id] == null)
		{
			allOnServiceStoppedListeners[id] = mutableListOf()
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
			allOnCountdownTimerChangedListeners[id] = mutableListOf()
		}

		// Add the listener to the list
		allOnCountdownTimerChangedListeners[id]!!.add(listener)
	}

	/**
	 * Add a listener for when the countup handler ticks.
	 */
	fun addOnCountupTickListener(id: Long, listener: OnCountupTickListener)
	{
		// Create the list for any other listeners that may be added
		if (allOnCountupTickListeners[id] == null)
		{
			allOnCountupTickListeners[id] = mutableListOf()
		}

		// Add the listener to the list
		allOnCountupTickListeners[id]!!.add(listener)
	}

	/**
	 * Add time to the countdown.
	 */
	fun addTimeToCountdown(timer: NacTimer, sec: Long)
	{
		// Get the countdown timer and times
		val countdownTimer = allCountdownTimers[timer.id]
		val totalDurationMillis = allTotalDurationMillis[timer.id]!!
		var millisUntilFinished = allMillisUntilFinished[timer.id]!!

		// Cancel the countdown
		countdownTimer?.cancel()

		// Add time to the milliseconds until finished
		millisUntilFinished += sec*1000
		allMillisUntilFinished[timer.id] = millisUntilFinished
		println("New millis : $millisUntilFinished")

		// Time until finished exceeds the total time. Update the total time to match the
		// new time until finished. Use seconds until finished because it rounds up, in
		// case the milliseconds are off by a little
		if (totalDurationMillis < millisUntilFinished)
		{
			allTotalDurationMillis[timer.id] = getSecUntilFinished(timer) * 1000
			println("Updated total duration to match millis : ${allTotalDurationMillis[timer.id]}")
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
		val countdownTimer = allCountdownTimers[timer.id]

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
	fun cleanup(timer: NacTimer)
	{
		// Clean the wakeup process
		allWakeupProcesses[timer.id]?.cleanup()

		// Cleanup the auto dismiss handler
		allAutoDismissHandlers[timer.id]?.removeCallbacksAndMessages(null)

		// Cleanup the timer ringing countup handlers
		allTimerRingingCountupHandlers[timer.id]?.removeCallbacksAndMessages(null)

		// Cleanup the wake lock
		allWakeLocks[timer.id]?.apply {
			if (isHeld)
			{
				release()
			}
		}

		// Remove the items from the hashmaps
		allWakeupProcesses.remove(timer.id)
		allTimerRingingCountupHandlers.remove(timer.id)
		allAutoDismissHandlers.remove(timer.id)
		allWakeLocks.remove(timer.id)
		allIsFirstTick.remove(timer.id)
		allTimers.removeIf { it.id == timer.id }


		// Stop the service if no more timers are active
		if (allTimers.isEmpty())
		{
			println("STOPPING ACTIVE YO VERY COOL")
			stopThisService()
		}
		// Close the notification and if this is the foreground notification, make
		// another timer the foreground notification
		else
		{
			val notification = NacActiveTimerNotification(this, timer)
			val notificationManagerCompat = NotificationManagerCompat.from(this)

			// Have another timer be the foreground notification. Since the current timer
			// is the foreground notification, making another timer take its place will
			// naturally close the notification (by replacing it)
			if (notification.id == foregroundNotificationId)
			{
				val newForegroundTimer = allTimers.first { it.id != timer.id }
				println("Closing the foreground jank notification : ${notification.id} | ${newForegroundTimer.id}")
				updateNotification(newForegroundTimer, foreground = true)
			}
			// Close the notification
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
	fun dismiss(timer: NacTimer)
	{
		// Update the timer
		lifecycleScope.launch {

			// Dismiss the timer
			timer.isActive = false

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

			println("Stopping active timer service")
			// Cleanup resources used by the timer
			cleanup(timer)

			// Call the stop listener(s)
			println("dismiss() Calling stop yo listener : ${timer.id}")
			allOnServiceStoppedListeners[timer.id]?.forEach { it.onServiceStopped(timer) }

			// Show toast that the timer was dismissed and stop the service
			withContext(Dispatchers.Main) {
				quickToast(this@NacActiveTimerService, R.string.message_timer_dismiss)
			}

		}
	}

	/**
	 * Get the current countdown progress as a percentage.
	 */
	fun getProgress(timer: NacTimer): Int
	{
		// Get the times
		val totalDurationMillis = allTotalDurationMillis[timer.id]!!
		val millisUntilFinished = allMillisUntilFinished[timer.id]!!

		// Calculate the progress
		return 100 - ((totalDurationMillis - millisUntilFinished) * 100 / totalDurationMillis).toInt()
	}

	/**
	 * Get the seconds the timer has been ringing for.
	 */
	fun getSecOfRinging(timer: NacTimer): Long
	{
		val now = System.currentTimeMillis()

		return (now - timerRingingStartTimeMillis[timer.id]!!) / 1000
	}

	/**
	 * Get the seconds until the countdown finishes.
	 */
	fun getSecUntilFinished(timer: NacTimer): Long
	{
		return ceil(allMillisUntilFinished[timer.id]!! / 1000f).toLong()
	}

	/**
	 * Check if the timer is active.
	 *
	 * @return True if the timer is active, and False otherwise.
	 */
	fun isTimerActive(timer: NacTimer): Boolean
	{
		return allTimers.find { it.id == timer.id }
			?.isActive == true
	}

	/**
	 * Check if the timer is paused.
	 *
	 * @return True if the timer is paused, and False otherwise.
	 */
	fun isTimerPaused(timer: NacTimer): Boolean
	{
		return isTimerActive(timer) && (allCountdownTimers[timer.id] == null)
	}

	/**
	 * Check if the timer is ringing.
	 *
	 * @return True if the timer is ringing, and False otherwise.
	 */
	fun isTimerRinging(timer: NacTimer): Boolean
	{
		return allMillisUntilFinished[timer.id] == 0L
	}

	/**
	 * Check if the service is using the timer.
	 *
	 * @return True if the timer is being used by the service, and False otherwise.
	 */
	fun isUsingTimer(timer: NacTimer): Boolean
	{
		return allTimers.find { it.id == timer.id } != null
	}

	/**
	 * Service is binding.
	 */
	override fun onBind(intent: Intent): IBinder
	{
		// Super
		super.onBind(intent)

		println("SERVICE onBind()")
		// Set the bound flag
		isBound = true

		return binder
	}

	/**
	 * Service is created.
	 */
	@UnstableApi
	override fun onCreate()
	{
		// Super
		super.onCreate()
		println("SERVICE onCreate()")

		// Initialize member varirables
		sharedPreferences = NacSharedPreferences(this)

		// Enable the activity alias so that tapping an NFC tag will open the main
		// activity
		enableActivityAlias(this)
	}

	/**
	 * Service is destroyed.
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

				// Deactivate the timer
				t.isActive = false
				timerRepository.update(t)

				// Cleanup everything
				cleanup(t)

				// Call the stop listener(s)
				println("onDestroy() Calling stop yo listener : ${t.id}")
				allOnServiceStoppedListeners[t.id]?.forEach { it.onServiceStopped(t) }

			}
		}
	}

	/**
	 * Service is started.
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
			// Add the timer to the list
			if (allTimers.none { it.id == timer.id })
			{
				println("ADDING TIMER : ${timer.id}")
				allTimers.add(timer)
				allIsFirstTick[timer.id] = true
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

		// Check the intent action
		when (intent.action)
		{

			// Start the service
			ACTION_START_SERVICE ->
			{
				startActiveTimerService(timer)
				return START_STICKY
			}

			// Dismiss
			ACTION_DISMISS_TIMER -> dismiss(timer)

			// Dismiss with NFC
			ACTION_DISMISS_TIMER_WITH_NFC -> dismiss(timer)

			// Pause
			ACTION_PAUSE_TIMER -> {
				cancelCountdownTimer(timer)
				updateNotification(timer)
				allOnCountdownTimerChangedListeners[timer.id]?.forEach { it.onCountdownPaused(timer) }
			}

			// Resume
			ACTION_RESUME_TIMER -> {
				startCountdownTimer(timer)
				updateNotification(timer)
			}

			// The default case if things go wrong
			else -> stopThisService()

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
	 * Remove all matching listeners for when the countup handler ticks.
	 */
	fun removeAllMatchingOnCountupTickListener(listener: OnCountupTickListener)
	{
		allOnCountupTickListeners.values.forEach {
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
	 * Remove the listener for when the countup handler ticks.
	 */
	fun removeOnCountupTickListener(id: Long, listener: OnCountupTickListener)
	{
		allOnCountupTickListeners[id]?.remove(listener)
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
		// Cancel the countdown
		cancelCountdownTimer(timer)

		// Reset the first tick flag, total duration, and millis until finished
		allIsFirstTick[timer.id] = true
		allTotalDurationMillis[timer.id] = timer.duration*1000
		allMillisUntilFinished[timer.id] = allTotalDurationMillis[timer.id]!!

		// Update the notification
		updateNotification(timer)

		// Deactivate the timer
		lifecycleScope.launch {
			timer.isActive = false
			timerRepository.update(timer)

			// Remove the old timer and replace it with the updated timer that is no
			// longer marked as active
			if (allTimers.removeIf { it.id == timer.id })
			{
				allTimers.add(timer)
			}
		}

		// Call the listener
		val secUntilFinished = getSecUntilFinished(timer)
		allOnCountdownTimerChangedListeners[timer.id]?.forEach { it.onCountdownReset(timer, secUntilFinished) }
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

		// Show the notification
		showForegroundNotification {

			// Service barely beginning to run. Start the service in the foreground
			if (foregroundNotificationId == 0)
			{
				println("FOREGROUND")
				startForeground(notification.id, notification.build())
				foregroundNotificationId = notification.id
			}
			// Service is already running in the foreground. Create a new notification
			else
			{
				println("NOTIFY")
				val notificationmanager = NotificationManagerCompat.from(this)
				notificationmanager.notify(notification.id, notification.build())
			}

		}

		// Save the show when time for the notification
		if (timer.id !in allNotificationShowWhenTime)
		{
			allNotificationShowWhenTime[timer.id] = Calendar.getInstance().timeInMillis
		}
	}

	/**
	 * Start the service.
	 */
	@UnstableApi
	private fun startActiveTimerService(timer: NacTimer)
	{
		println("startActiveTimerService()")
		// Set the total duration and millis needed to finish
		allTotalDurationMillis[timer.id] = timer.duration*1000
		allMillisUntilFinished[timer.id] = allTotalDurationMillis[timer.id]!!

		// Set the active flag and update the timer in the database
		lifecycleScope.launch {
			timer.isActive = true
			timerRepository.update(timer)
		 }

		// Start the countdown timer
		startCountdownTimer(timer)
	}

	/**
	 * Start the countdown timer.
	 */
	fun startCountdownTimer(timer: NacTimer)
	{
		// Get the times
		val totalDurationMillis = allTotalDurationMillis[timer.id]!!
		val millisUntilFinished = allMillisUntilFinished[timer.id]!!

		// Activate the timer as active if the timer was previously reset and thus marked
		// as deactivated
		if (timer.duration*1000 == allTotalDurationMillis[timer.id])
		{
			lifecycleScope.launch {
				timer.isActive = true
				timerRepository.update(timer)
			}
		}

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
				this@NacActiveTimerService.allMillisUntilFinished[timer.id] = millisUntilFinished
				val secUntilFinished = getSecUntilFinished(timer)
				val progress = getProgress(timer)
				println("Service on tick : $millisUntilFinished | $secUntilFinished | Total : ${totalDurationMillis / 1000L} | $progress | ${allIsFirstTick[timer.id]}")

				// Update the notification
				updateNotification(timer)

				// Call the listener
				allOnCountdownTimerChangedListeners[timer.id]?.forEach { it.onCountdownTick(timer, secUntilFinished, progress) }

				// Change the first tick to false once the fragment has been connected
				// and has received the first tick
				if (allOnCountdownTimerChangedListeners.isNotEmpty() && allIsFirstTick[timer.id]!!)
				{
					allIsFirstTick[timer.id] = false
				}
			}

			/**
			 * Countdown finished.
			 */
			@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
			override fun onFinish()
			{
				println("SERVICE DONE WITH COUNTDOWN : ${timer.id} | ${timer.isActive}")

				// Set the milliseconds until finished
				this@NacActiveTimerService.allMillisUntilFinished[timer.id] = 0

				// Change the seconds in the notification to 0
				updateNotification(timer)

				// Start counting up as the timer rings
				startCountupTimerRinging(timer)

				// Acquire the wakelock and add it to the hashmap
				val wakeLock = acquireWakeLock(timer.autoDismissTime, WAKELOCK_TAG)
				allWakeLocks[timer.id] = wakeLock

				// Wait for auto dismiss
				waitForAutoDismiss(timer)

				// Start the wakeup process and add it to the hashmap
				val wakeupProcess = NacWakeupProcess(this@NacActiveTimerService, timer)
				wakeupProcess.start()
				allWakeupProcesses[timer.id] = wakeupProcess

				// Call the listener
				allOnCountdownTimerChangedListeners[timer.id]?.forEach { it.onCountdownFinished(timer) }
			}
		}

		// Start the countdown timer
		countdownTimer.start()

		// Add the countdown timer to the hashmap
		allCountdownTimers[timer.id] = countdownTimer
	}

	/**
	 * Start counting up once the timer starts ringing.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	private fun startCountupTimerRinging(timer: NacTimer)
	{
		// Set the time at which the timer started ringing
		timerRingingStartTimeMillis[timer.id] = System.currentTimeMillis()
		println("Start count up time : ${timerRingingStartTimeMillis[timer.id]}")

		// Create a handler that will run every second and add it to the hashmap
		allTimerRingingCountupHandlers[timer.id] = Handler(mainLooper)

		// Start counting up
		countupEverySec(timer)

	}

	/**
	 * Count up every second, and update the notification.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	private fun countupEverySec(timer: NacTimer)
	{
		// Timer handler does not exist
		if (timer.id !in allTimerRingingCountupHandlers)
		{
			println("TIMER HANDLER DOES NOT EXIST")
			return
		}

		// Start the handler
		allTimerRingingCountupHandlers[timer.id]?.postDelayed({

			val secOfRinging = getSecOfRinging(timer)
			println("Elapsed time of ringing : $secOfRinging")

			// Update the time
			updateNotification(timer, timeInSec = secOfRinging)

			// Call the listener
			allOnCountupTickListeners[timer.id]?.forEach { it.onCountupTick(timer, secOfRinging) }

			// Recursively call this method
			countupEverySec(timer)

		}, 1000)
	}

	/**
	 * Update the notification.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	fun updateNotification(
		timer: NacTimer,
		timeInSec: Long = getSecUntilFinished(timer),
		foreground: Boolean = false)
	{
		// Get the new title
		val newTitle = NacCalendar.getTimerFullTime(this, timeInSec)

		// Get the notification manager and builder
		val notificationManagerCompat = NotificationManagerCompat.from(this)
		val notificationBuilder = NacActiveTimerNotification(this, timer)

		// Create the notification
		val notification = (notificationBuilder.setContentTitle(newTitle)
			.setWhen(allNotificationShowWhenTime[timer.id]!!) as NacActiveTimerNotification)
			.apply {
				// Timer is counting down
				if (allMillisUntilFinished[timer.id]!! > 0)
				{
					// Paused
					if (allCountdownTimers[timer.id] == null)
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
					// NFC does not need to be used to dismiss the timer
					// Note: This evaluates to False on the emulator because the emulator
					// is unable to use NFC
					if (!timer.shouldUseNfc(this@NacActiveTimerService, sharedPreferences))
					{
						addAction(R.drawable.stop_32, R.string.action_timer_stop, notificationBuilder.dismissPendingIntent)
					}
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
		val delay = if (timer.shouldUseNfc)
		{
			60000L
		}
		else
		{
			timer.autoDismissTime*1000L
		}
		//val delay = timer.autoDismissTime*1000L - 750
		println("Auto dismiss delay : $delay")

		// Create the handler
		val handler = Handler(mainLooper)

		// Automatically dismiss the timer
		handler.postDelayed({

			// TODO: Should show missed timer notification?
			// Auto dismiss the timer. This will stop the service
			dismiss(timer)

		}, delay)

		// Save the handler
		allAutoDismissHandlers[timer.id] = handler
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
		 * Tag for the wakelock.
		 */
		const val WAKELOCK_TAG = "NFC Alarm Clock:NacActiveTimerService"

		/**
		 * Bind to the service.
		 */
		fun bindToService(context: Context, cls: Class<*>, serviceConnection: ServiceConnection)
		{
			// Bind to the active timer service
			val intent = Intent(context, cls)

			context.bindService(intent, serviceConnection, 0)
		}

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