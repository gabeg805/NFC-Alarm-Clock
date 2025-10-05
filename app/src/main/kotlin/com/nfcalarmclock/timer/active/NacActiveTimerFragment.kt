package com.nfcalarmclock.timer.active

import android.Manifest
import android.animation.ObjectAnimator
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.getTimer
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.view.calcContrastColor
import com.nfcalarmclock.view.setupBackgroundColor
import com.nfcalarmclock.view.setupProgressAndIndicatorColor
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.ceil

/**
 * Animate a change in progress.
 */
fun CircularProgressIndicator.animateProgress(from: Int, to: Int, millis: Long)
{
	ObjectAnimator.ofInt(this, "progress", from, to)
		.apply {
			duration = millis
			start()
		}
}

/**
 * Active timer.
 */
@UnstableApi
@AndroidEntryPoint
class NacActiveTimerFragment
	: Fragment()
{

	/**
	 * Timer.
	 */
	private lateinit var timer: NacTimer

	/**
	 * Circular progress indicator.
	 */
	private lateinit var progressIndicator: CircularProgressIndicator

	/**
	 * Hour textview.
	 */
	private lateinit var hourTextView: TextView

	/**
	 * Minute textview.
	 */
	private lateinit var minuteTextView: TextView

	/**
	 * Second textview.
	 */
	private lateinit var secondsTextView: TextView

	/**
	 * Hour units textview.
	 */
	private lateinit var hourUnits: TextView

	/**
	 * Minute units textview.
	 */
	private lateinit var minuteUnits: TextView

	/**
	 * Pause/resume button.
	 */
	private lateinit var pauseResumeButton: MaterialButton

	/**
	 * Stop button.
	 */
	private lateinit var stopButton: MaterialButton

	/**
	 * Add 5 seconds button.
	 */
	private lateinit var add5sButton: MaterialButton

	/**
	 * Add 30 seconds button.
	 */
	private lateinit var add30sButton: MaterialButton

	/**
	 * Add 1 minute button.
	 */
	private lateinit var add1mButton: MaterialButton

	/**
	 * Countdown timer.
	 */
	private lateinit var countDownTimer: CountDownTimer

	/**
	 * Total duration in milliseconds.
	 */
	private var totalDurationMillis: Long = 0

	/**
	 * Milliseconds until the countdown finishes.
	 */
	private var millisUntilFinished: Long = 0

	/**
	 * Seconds until the countdown finishes.
	 */
	private val secUntilFunished: Long
		get() = ceil(millisUntilFinished / 1000f).toLong()

	/**
	 * Whether the timer is running or not.
	 */
	private var isRunning: Boolean = true

	/**
	 * Whether this is the first tick of the countdown timer or not.
	 */
	private var isFirstTick: Boolean = true

	/**
	 * Active timer service.
	 */
	private var service: NacActiveTimerService? = null

	/**
	 * Connection to the active timer service.
	 */
	private val serviceConnection = object : ServiceConnection
	{
		override fun onServiceConnected(className: ComponentName, service: IBinder)
		{
			// Set the active timer service
			val binder = service as NacActiveTimerService.NacLocalBinder
			this@NacActiveTimerFragment.service = binder.getService()
		}

		override fun onServiceDisconnected(p0: ComponentName?) {}
	}

	/**
	 * Add time to the countdown.
	 */
	private fun addTimeToTimer(sec: Long)
	{
		// Cancel the countdown
		countDownTimer.cancel()

		// Add time to the time until finished
		millisUntilFinished += sec*1000
		println("New millis : $millisUntilFinished")

		// Time until finished exceeds the total time. Update the total time to match the
		// new time until finished. Use seconds until finished because it rounds up, in
		// case the milliseconds are off by a little
		if (totalDurationMillis < millisUntilFinished)
		{
			totalDurationMillis = secUntilFunished * 1000
			println("Updated total duration to match millis : $totalDurationMillis")
		}

		// Start the countdown
		startCountdownTimer()
	}

	/**
	 * Create the root view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.frg_active_timer, container, false)
	}

	override fun onSaveInstanceState(outState: Bundle)
	{
		super.onSaveInstanceState(outState)
		println("onSaveInstanceState()")
		// TODO: If the user clears the app from memory, will state really be restored?
		// TODO: Need to be able to get communication from service saying that it is stopping. Scope service binder overview again
	}

	override fun onViewStateRestored(savedInstanceState: Bundle?)
	{
		super.onViewStateRestored(savedInstanceState)
		println("onViewStateRestored()")
	}

	/**
	 */
	override fun onStart()
	{
		// Super
		super.onStart()
		println("onStart()")

		// Bind to the active timer service
		val context = requireContext()
		val intent = Intent(context, NacActiveTimerService::class.java)

		context.bindService(intent, serviceConnection, 0)
	}

	/**
	 */
	override fun onStop()
	{
		// Super
		super.onStop()
		println("onStop()")

		// Unbind from the active timer service
		requireContext().unbindService(serviceConnection)
	}

	/**
	 * View is created.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the timer
		val t = arguments?.getTimer()
		println("Timer : $t")

		// Set the timer
		if (t != null)
		{
			timer = t
		}
		// Stop the fragment if there is no timer
		else
		{
			println("POPPING BACK STACK")
			findNavController().popBackStack()
			return
		}

		// Get the context and shared preferences
		val context = requireContext()
		val sharedPreferences = NacSharedPreferences(context)

		// Get the views
		progressIndicator = view.findViewById(R.id.timer_progress)
		hourTextView = view.findViewById(R.id.timer_hour)
		minuteTextView = view.findViewById(R.id.timer_minute)
		secondsTextView = view.findViewById(R.id.timer_seconds)
		hourUnits = view.findViewById(R.id.timer_hour_units)
		minuteUnits = view.findViewById(R.id.timer_minute_units)
		pauseResumeButton = view.findViewById(R.id.timer_pause_resume)
		stopButton = view.findViewById(R.id.timer_stop)
		add5sButton = view.findViewById(R.id.timer_add_5s)
		add30sButton = view.findViewById(R.id.timer_add_30s)
		add1mButton = view.findViewById(R.id.timer_add_1m)
		val resetButton: MaterialButton = view.findViewById(R.id.timer_reset)

		// Setup views
		setupHourAndMinuteVisibility()
		setupResetButton(resetButton)
		setupPauseResumeButton(sharedPreferences)
		setupStopButton(sharedPreferences)
		setupAddTimeButtons()
		setupProgressIndicator(sharedPreferences)
	}

	/**
	 * Reset timer.
	 */
	private fun resetTimer()
	{
		// Change time back to max
		updateHourMinuteSecondsTextViews()

		// Stop the service so the notification gets cleared
		service?.stopActiveTimerService()

		// Show the resume button again and hide the stop button
		pauseResumeButton.visibility = View.VISIBLE
		stopButton.visibility = View.INVISIBLE

		// Show the add time buttons as the timer can be run again, if the user wants to
		add5sButton.visibility = View.VISIBLE
		add30sButton.visibility = View.VISIBLE
		add1mButton.visibility = View.VISIBLE

	}

	/**
	 * Setup the add time buttons.
	 */
	private fun setupAddTimeButtons()
	{
		add5sButton.setOnClickListener { addTimeToTimer(5) }
		add30sButton.setOnClickListener { addTimeToTimer(30) }
		add1mButton.setOnClickListener { addTimeToTimer(60) }
	}

	/**
	 * Setup the visibility of the hour and minute textviews. The seconds should always
	 * be visible.
	 */
	private fun setupHourAndMinuteVisibility()
	{
		// Hide hours
		if (timer.duration < 3600)
		{
			hourTextView.visibility = View.GONE
			hourUnits.visibility = View.GONE
		}

		// Hide minutes
		if (timer.duration < 60)
		{
			minuteTextView.visibility = View.GONE
			minuteUnits.visibility = View.GONE
		}
	}

	/**
	 * Setup the pause/resume button.
	 */
	private fun setupPauseResumeButton(sharedPreferences: NacSharedPreferences)
	{
		// Setup color
		val contrastColor = calcContrastColor(sharedPreferences.themeColor)

		pauseResumeButton.setupBackgroundColor(sharedPreferences)
		pauseResumeButton.iconTint = ColorStateList.valueOf(contrastColor)

		// Setup the click listener
		pauseResumeButton.setOnClickListener {

			println("Pause/resume TIMER : $isRunning")
			// Get the correct drawable to show
			val drawable = if (isRunning) R.drawable.pause_32 else R.drawable.play

			// Stop the timer
			if (isRunning)
			{
				countDownTimer.cancel()
			}
			// Start the timer
			else
			{
				startCountdownTimer()
			}

			// Set the drawable
			val context = requireContext()
			pauseResumeButton.icon = ContextCompat.getDrawable(context, drawable)

			// Toggle the running flag
			isRunning = !isRunning

		}
	}

	/**
	 * Setup the progress indicator.
	 */
	private fun setupProgressIndicator(sharedPreferences: NacSharedPreferences)
	{
		// Setup color
		progressIndicator.setupProgressAndIndicatorColor(sharedPreferences)

		// Set the total duration and millis needed to finish
		totalDurationMillis = timer.duration*1000
		millisUntilFinished = totalDurationMillis

		// Start the countdown
		startCountdownTimer()
	}

	/**
	 * Setup the reset button.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	private fun setupResetButton(button: MaterialButton)
	{
		// Setup the click listener
		button.setOnClickListener {

			// Cancel the countdown
			countDownTimer.cancel()

			// Reset progress back to 0
			progressIndicator.animateProgress(progressIndicator.progress, 0, 250)

			// Set the total duration and millis needed to finish
			totalDurationMillis = timer.duration*1000
			millisUntilFinished = totalDurationMillis

			// Reset the timer
			resetTimer()

		}
	}

	/**
	 * Setup the stop button.
	 */
	private fun setupStopButton(sharedPreferences: NacSharedPreferences)
	{
		// Setup color
		val contrastColor = calcContrastColor(sharedPreferences.themeColor)

		stopButton.setupBackgroundColor(sharedPreferences)
		stopButton.iconTint = ColorStateList.valueOf(contrastColor)

		// Setup the click listener
		// TODO: Figure out what logic should be for NFC
		stopButton.setOnClickListener {

			println("STOP TIMER : $isRunning")

			// Stop the timer
			// TODO: Do I need to these two? Maybe not but I'll keep it here for now
			//countDownTimer.cancel()

			// Reset the timer
			resetTimer()

			// Navigate back to show alarms
			// TODO: Change so that it goes to show timers instead of add timer...or maybe should just stay in this fragment?
			// TODO: If I do this, need to think about restarting the service and replaying music and whatnot
			findNavController().popBackStack()

		}
	}

	/**
	 * Start the countdown timer.
	 */
	private fun startCountdownTimer()
	{
		// Create and set the countdown timer
		countDownTimer = object : CountDownTimer(millisUntilFinished, 1000)
		{

			/**
			 * Every tick of the countdown.
			 */
			@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
			override fun onTick(millisUntilFinished: Long)
			{
				// TODO: Should keep track of elapsed here and in onPause? No it'd have to be in the notification service
				// Set the milliseconds until finished
				this@NacActiveTimerFragment.millisUntilFinished = millisUntilFinished

				// Update the hour, minute, and seconds textviews
				updateHourMinuteSecondsTextViews()
				service?.updateNotification(secUntilFunished)

				// This is the first tick of the countdown timer
				if (isFirstTick)
				{
					// Do a little circular animation from 100% to 0% on the first tick
					progressIndicator.animateProgress(100, 0, 500)
					isFirstTick = false
				}
				// Some other run that is not the first
				else
				{
					// Calculate the new progress
					val newProgress = ((totalDurationMillis - millisUntilFinished) * 100 / totalDurationMillis).toInt()

					// Animate the progress to that point
					progressIndicator.animateProgress(progressIndicator.progress, newProgress, 250)
				}

				println("Counting down... ${progressIndicator.progress} | Millis : $totalDurationMillis | $millisUntilFinished | ${timer.duration}")
			}

			/**
			 * Countdown finished.
			 */
			@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
			override fun onFinish()
			{
				println("DONE WITH THE COUNTDOWN")

				// Toggle the running flag
				isRunning = false

				// Change the seconds text and progress to indicate done
				secondsTextView.text = resources.getString(R.string.number0)
				service?.updateNotification(0)
				progressIndicator.animateProgress(progressIndicator.progress, 100, 100)

				// TODO: Change text and progress to red to indicate the time past the set timer

				// Show the stop button and hide the pause/resume button
				stopButton.visibility = View.VISIBLE
				pauseResumeButton.visibility = View.INVISIBLE

				// Hide the add time buttons as the timer is no longer running
				add5sButton.visibility = View.INVISIBLE
				add30sButton.visibility = View.INVISIBLE
				add1mButton.visibility = View.INVISIBLE

			}
		}

		// Start the timer
		countDownTimer.start()
	}

	/**
	 * Update the hour, minute, and seconds textviews based on the milliseconds until
	 * finished.
	 */
	private fun updateHourMinuteSecondsTextViews()
	{
		// Get the hour, minutes, and seconds to display
		val hour = secUntilFunished / 3600
		val minute = secUntilFunished / 60
		val seconds = secUntilFunished % 60
		println("Sec until finished : $secUntilFunished | $hour:$minute:$seconds")

		// Hide the hours
		if (hour == 0L)
		{
			hourTextView.visibility = View.GONE
			hourUnits.visibility = View.GONE
		}
		// Update the hours
		else
		{
			hourTextView.text = hour.toString()
		}

		// Hide the minutes
		if (minute == 0L)
		{
			minuteTextView.visibility = View.GONE
			minuteUnits.visibility = View.GONE
		}
		// Update the minutes
		else
		{
			minuteTextView.text = minute.toString()
		}

		// Update the seconds. These are always visible
		secondsTextView.text = seconds.toString()
	}

}