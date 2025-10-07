package com.nfcalarmclock.timer.active

import android.Manifest
import android.animation.ObjectAnimator
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.os.Bundle
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
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.system.getTimer
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.view.calcContrastColor
import com.nfcalarmclock.view.setupBackgroundColor
import com.nfcalarmclock.view.setupProgressAndIndicatorColor
import dagger.hilt.android.AndroidEntryPoint

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
	 * Whether the timer is running or not.
	 */
	private var isRunning: Boolean = true

	/**
	 * Active timer service.
	 */
	private var service: NacActiveTimerService? = null

	/**
	 * Connection to the active timer service.
	 */
	private val serviceConnection = object : ServiceConnection
	{
		override fun onServiceConnected(className: ComponentName, serviceBinder: IBinder)
		{
			// Set the active timer service
			val binder = serviceBinder as NacActiveTimerService.NacLocalBinder
			service = binder.getService()
			println("SERVICE IS NOW CONNECTED : ${service!!.progress}")

			// Update the hour, minute, and seconds textviews
			updateHourMinuteSecondsTextViews(service!!.secUntilFinished)

			// This is the first tick of the countdown timer, while the service is
			// connected to this fragment
			if (service!!.isFirstTick)
			{
				// Do a little circular animation from 100% to 0% on the first tick
				progressIndicator.animateProgress(100, 0, 500)
			}
			else
			{
				progressIndicator.progress = service!!.progress
			}

			// Set the listener for when the active timer service is stopped
			service!!.onActiveTimerServiceStoppedListener = NacActiveTimerService.OnActiveTimerServiceStoppedListener {
				println("THIS JANK IS GETTING STOPPED RIGHT NOW. NEED TO DO SOMETHING")

				//// Reset the timer
				//resetTimer()

				// Navigate back to show alarms
				// TODO: Change so that it goes to show timers instead of add timer...or maybe should just stay in this fragment?
				// TODO: If I do this, need to think about restarting the service and replaying music and whatnot
				//findNavController().popBackStack()
				findNavController().popBackStack(R.id.nacAddTimerFragment, false)

			}

			// Set yo
			service!!.onCountdownTimerChangedListener = object : NacActiveTimerService.OnCountdownTimerChangedListener {

				override fun onCountdownFinished()
				{
					println("DONE WITH THE COUNTDOWN")
					// Set the running flag
					isRunning = false

					// Change the seconds text and progress to indicate done
					secondsTextView.text = resources.getString(R.string.number0)
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

				override fun onCountdownPaused()
				{
					// Set the running flag
					isRunning = false

					// Update the pause/resume button drawable
					updatePauseResumeDrawable()
				}

				override fun onCountdownReset(secUntilFinished: Long)
				{
					println("COUNTDOWN RESET : $secUntilFinished")
					// Set the running flag
					isRunning = false

					// Update the views
					updateHourMinuteSecondsTextViews(secUntilFinished)
					updatePauseResumeDrawable()

					// Show the resume button again and hide the stop button
					pauseResumeButton.visibility = View.VISIBLE
					stopButton.visibility = View.INVISIBLE

					// Show the add time buttons as the timer can be run again, if the user wants to
					add5sButton.visibility = View.VISIBLE
					add30sButton.visibility = View.VISIBLE
					add1mButton.visibility = View.VISIBLE
				}

				override fun onCountdownTick(secUntilFinished: Long, newProgress: Int)
				{
					println("COUNTDOWN Tick")
					// Set the running flag
					isRunning = true

					// Update the views
					updateHourMinuteSecondsTextViews(secUntilFinished)
					updatePauseResumeDrawable()

					// Animate the progress to that point
					progressIndicator.animateProgress(progressIndicator.progress, newProgress, 250)
				}

			}
		}

		override fun onServiceDisconnected(className: ComponentName) {}
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
		println("onStop(). Canceling timer")

		// Unbind from the active timer service
		requireContext().unbindService(serviceConnection)

		// Clear the service listeners
		service?.onActiveTimerServiceStoppedListener = null
		service?.onCountdownTimerChangedListener = null
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

		println("onViewCreated()!!!!!!!!!!!!!!!!!!!!!!!")

		// Setup views
		setupHourAndMinuteVisibility()
		setupResetButton(resetButton)
		setupPauseResumeButton(sharedPreferences)
		setupStopButton(sharedPreferences)
		setupAddTimeButtons()
		setupProgressIndicator(sharedPreferences)
		println("Done with onViewCreated()")
	}

	/**
	 * Setup the add time buttons.
	 */
	private fun setupAddTimeButtons()
	{
		add5sButton.setOnClickListener { service?.addTimeToCountdown(5) }
		add30sButton.setOnClickListener { service?.addTimeToCountdown(30) }
		add1mButton.setOnClickListener { service?.addTimeToCountdown(60) }
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
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	private fun setupPauseResumeButton(sharedPreferences: NacSharedPreferences)
	{
		// Setup color
		val contrastColor = calcContrastColor(sharedPreferences.themeColor)

		pauseResumeButton.setupBackgroundColor(sharedPreferences)
		pauseResumeButton.iconTint = ColorStateList.valueOf(contrastColor)

		// Setup the click listener
		pauseResumeButton.setOnClickListener {

			println("Pause/resume TIMER : $isRunning")
			// Pause the timer
			if (isRunning)
			{
				service?.cancelCountdownTimer()
			}
			// Resume the timer
			else
			{
				service?.startCountdownTimer()
			}

			// Update the notification
			service?.updateNotification()

			// Toggle the running flag
			isRunning = !isRunning

			// Change the button drawable
			updatePauseResumeDrawable()

		}
	}

	/**
	 * Setup the progress indicator.
	 */
	private fun setupProgressIndicator(sharedPreferences: NacSharedPreferences)
	{
		// Setup color
		progressIndicator.setupProgressAndIndicatorColor(sharedPreferences)
	}

	/**
	 * Setup the reset button.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	private fun setupResetButton(button: MaterialButton)
	{
		// Setup the click listener
		button.setOnClickListener {

			// Reset progress back to 0
			progressIndicator.animateProgress(progressIndicator.progress, 0, 250)

			// Reset the timer
			service?.resetCountdownTimer()

			// TODO: Figure out logic for when timer is reset, but the fragment is stil visible
			// TODO: Should the service be stopped or not?
			//// Clear the service stop listener
			//service?.onActiveTimerServiceStoppedListener = null

			//// Stop the service so the notification gets cleared
			//service?.stopActiveTimerService()

		}
	}

	/**
	 * Setup the stop button.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	private fun setupStopButton(sharedPreferences: NacSharedPreferences)
	{
		// Setup color
		val contrastColor = calcContrastColor(sharedPreferences.themeColor)

		stopButton.setupBackgroundColor(sharedPreferences)
		stopButton.iconTint = ColorStateList.valueOf(contrastColor)

		// Setup the click listener
		// TODO: Figure out what logic should be for NFC
		stopButton.setOnClickListener {

			//// Reset the timer
			//service?.resetCountdownTimer()

			// Clear the service stop listener
			service?.onActiveTimerServiceStoppedListener = null

			// Dismiss the timer
			NacActiveTimerService.dismissTimerService(requireContext(), timer)

			// Navigate back to show alarms
			// TODO: Change so that it goes to show timers instead of add timer...or maybe should just stay in this fragment?
			// TODO: If I do this, need to think about restarting the service and replaying music and whatnot
			findNavController().popBackStack(R.id.nacAddTimerFragment, false)

		}
	}

	/**
	 * Update the hour, minute, and seconds textviews based on the milliseconds until
	 * finished.
	 */
	private fun updateHourMinuteSecondsTextViews(secUntilFunished: Long)
	{
		// Get the hour, minutes, and seconds to display
		val (hour, minute, seconds) = NacCalendar.getTimerHourMinuteSecondsZeroPadded(secUntilFunished)

		// Update the hours
		if (hour.isEmpty())
		{
			hourTextView.text = hour
			hourTextView.visibility = View.VISIBLE
			hourUnits.visibility = View.VISIBLE
		}
		// Hide the hours
		else
		{
			hourTextView.visibility = View.GONE
			hourUnits.visibility = View.GONE
		}

		// Update the minutes
		if (minute.isEmpty())
		{
			minuteTextView.text = minute
			minuteTextView.visibility = View.VISIBLE
			minuteUnits.visibility = View.VISIBLE
		}
		// Hide the minutes
		else
		{
			minuteTextView.visibility = View.GONE
			minuteUnits.visibility = View.GONE
		}

		// Update the seconds. These are always visible
		secondsTextView.text = seconds
	}

	/**
	 * Update the drawable of the pause/resume button.
	 */
	private fun updatePauseResumeDrawable()
	{
		// Get the drawable
		val context = requireContext()
		val drawableRes = if (isRunning) R.drawable.pause_32 else R.drawable.play

		// Set the drawable
		pauseResumeButton.icon = ContextCompat.getDrawable(context, drawableRes)
	}

}