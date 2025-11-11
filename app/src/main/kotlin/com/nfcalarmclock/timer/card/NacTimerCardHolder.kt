package com.nfcalarmclock.timer.card

import android.content.Context
import android.os.SystemClock
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.nfcalarmclock.R
import com.nfcalarmclock.card.NacBaseCardHolder
import com.nfcalarmclock.nfc.shouldUseNfc
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.view.performHapticFeedback
import com.nfcalarmclock.view.resetTimerRingingAnimation
import com.nfcalarmclock.view.startTimerRingingAnimation
import com.nfcalarmclock.view.updateHourMinuteSecondsTextViews

/**
 * Timer ViewHolder for a CardView.
 *
 * @param root Root view.
 */
class NacTimerCardHolder(
	root: View
) : NacBaseCardHolder<NacTimer>(root)
{

	/**
	 * Listener for when the timer is clicked.
	 */
	fun interface OnTimerClickedListener
	{
		fun onTimerClicked(timer: NacTimer)
	}

	/**
	 * Listener for when the start timer button is clicked.
	 */
	fun interface OnStartTimerClickedListener
	{
		fun onStartTimer(timer: NacTimer)
	}

	/**
	 * Listener for when the pause timer button is clicked.
	 */
	fun interface OnPauseTimerClickedListener
	{
		fun onPauseTimer(timer: NacTimer)
	}

	/**
	 * Listener for when the reset timer button is clicked.
	 */
	fun interface OnResetTimerClickedListener
	{
		fun onResetTimer(timer: NacTimer)
	}

	/**
	 * Listener for when the stop timer button is clicked.
	 */
	fun interface OnStopTimerClickedListener
	{
		fun onStopTimer(timer: NacTimer)
	}

	/**
	 * Timer.
	 */
	var timer: NacTimer? = null

	/**
	 * Circular progress indicator.
	 */
	val progressIndicator: CircularProgressIndicator = root.findViewById(R.id.timer_progress)

	/**
	 * Hour textview.
	 */
	private val hourTextView: TextView = root.findViewById(R.id.timer_hour)

	/**
	 * Minute textview.
	 */
	private val minuteTextView: TextView = root.findViewById(R.id.timer_minute)

	/**
	 * Second textview.
	 */
	val secondsTextView: TextView = root.findViewById(R.id.timer_seconds)

	/**
	 * Hour units textview.
	 */
	private val hourUnits: TextView = root.findViewById(R.id.timer_hour_units)

	/**
	 * Minute units textview.
	 */
	private val minuteUnits: TextView = root.findViewById(R.id.timer_minute_units)

	/**
	 * Seconds units textview.
	 */
	private val secondsUnits: TextView = root.findViewById(R.id.timer_seconds_units)

	/**
	 * Summary view containing the days to repeat.
	 */
	private val summaryName: TextView = root.findViewById(R.id.timer_name)

	/**
	 * Start button.
	 */
	val startButton: MaterialButton = root.findViewById(R.id.timer_start_button)

	/**
	 * Pause button.
	 */
	val pauseButton: MaterialButton = root.findViewById(R.id.timer_pause_button)

	/**
	 * Reset button.
	 */
	val resetButton: MaterialButton = root.findViewById(R.id.timer_reset_button)

	/**
	 * Stop button.
	 */
	val stopButton: MaterialButton = root.findViewById(R.id.timer_stop_button)

	/**
	 * Scan NFC tag icon.
	 */
	val scanNfcTagIcon: ImageView = root.findViewById(R.id.timer_scan_nfc_icon)

	/**
	 * Listener for editing the timer.
	 */
	var onTimerClickedListener: OnTimerClickedListener? = null

	/**
	 * Listener for when the start timer button is clicked.
	 */
	var onStartTimerClickedListener: OnStartTimerClickedListener? = null

	/**
	 * Listener for when the pause timer button is clicked.
	 */
	var onPauseTimerClickedListener: OnPauseTimerClickedListener? = null

	/**
	 * Listener for when the reset timer button is clicked.
	 */
	var onResetTimerClickedListener: OnResetTimerClickedListener? = null

	/**
	 * Listener for when the stop timer button is clicked.
	 */
	var onStopTimerClickedListener: OnStopTimerClickedListener? = null

	/**
	 * The last time the start button was clicked.
	 */
	private var lastClickTimeStartButton: Long = 0

	/**
	 * The last time the reset button was clicked.
	 */
	private var lastClickTimeResetButton: Long = 0

	/**
	 * Constructor.
	 */
	init
	{
		// Initialize the colors and listeners
		initColors()
		initListeners()
	}

	/**
	 * Bind the alarm to the card view.
	 */
	override fun bind(item: NacTimer)
	{
		// Set the timer
		timer = item

		// Super
		super.bind(item)
	}

	/**
	 * Initialize the colors of the various views.
	 *
	 * Do not initialize the meridian color because when this is called, then alarm still
	 * has not been bound to the card holder.
	 */
	override fun initColors()
	{
		progressIndicator.setIndicatorColor(sharedPreferences.themeColor)
		summaryName.setTextColor(sharedPreferences.nameColor)
	}

	/**
	 * Initialize the listeners of the various views.
	 */
	override fun initListeners()
	{
		// List of all the views that need the same on click listener for the card
		val allCardViews = listOf(cardView)

		// Call the timer clicked listener when one of the views is clicked
		for (view in allCardViews)
		{
			view.setOnClickListener {
				view.performHapticFeedback()
				onTimerClickedListener?.onTimerClicked(timer!!)
			}
		}

		// Start clicked
		startButton.setOnClickListener {

			// Button was clicked too recently
			if ((SystemClock.elapsedRealtime() - lastClickTimeStartButton) < 500)
			{
				return@setOnClickListener
			}

			it.performHapticFeedback()
			setResumeVisibility()
			onStartTimerClickedListener?.onStartTimer(timer!!)

			// Set the last click time
			lastClickTimeStartButton = SystemClock.elapsedRealtime()

		}

		// Pause clicked
		pauseButton.setOnClickListener {

			// Button was clicked too recently
			if ((SystemClock.elapsedRealtime() - lastClickTimeStartButton) < 500)
			{
				return@setOnClickListener
			}

			it.performHapticFeedback()
			setPauseVisibility()
			onPauseTimerClickedListener?.onPauseTimer(timer!!)
		}

		// Reset clicked
		resetButton.setOnClickListener {

			// Button was clicked too recently
			if ((SystemClock.elapsedRealtime() - lastClickTimeResetButton) < 500)
			{
				return@setOnClickListener
			}

			it.performHapticFeedback()
			setResetVisibility()
			onResetTimerClickedListener?.onResetTimer(timer!!)

			// Set the last click time
			lastClickTimeResetButton = SystemClock.elapsedRealtime()

		}

		// Stop clicked
		stopButton.setOnClickListener {
			it.performHapticFeedback()
			setStopVisibility()
			onStopTimerClickedListener?.onStopTimer(timer!!)
		}
	}

	/**
	 * Initialize the various views.
	 */
	override fun initViews()
	{
		// Get the hour, minutes, and seconds to display
		val (hour, minute, seconds) = NacCalendar.getTimerHourMinuteSecondsZeroPadded(timer!!.duration)

		// Show the hours
		if (hour.isNotEmpty())
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

		// Show the minutes
		if (minute.isNotEmpty())
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

		// Set the seconds. These are always visible
		secondsTextView.text = seconds

		// Name
		summaryName.text = timer!!.name
		summaryName.visibility = if (timer!!.name.isNotEmpty()) View.VISIBLE else View.GONE

		// Set the button visibility
		setResetVisibility()

		// Set the progress
		progressIndicator.progress = 0
	}

	/**
	 * Reset the timer ringing animation.
	 */
	fun resetTimerRingingAnimation(context: Context)
	{
		resetTimerRingingAnimation(
			context, progressIndicator,
			hourTextView, hourUnits,
			minuteTextView, minuteUnits,
			secondsTextView, secondsUnits)
	}

	/**
	 * Set the visibility of views when timer is paused.
	 */
	fun setPauseVisibility()
	{
		// Show the start button
		if (startButton.visibility != View.VISIBLE)
		{
			startButton.visibility = View.VISIBLE
		}

		// Hide the pause button
		if (pauseButton.visibility != View.INVISIBLE)
		{
			pauseButton.visibility = View.INVISIBLE
		}

		// Show the reset button
		if (resetButton.visibility != View.VISIBLE)
		{
			resetButton.visibility = View.VISIBLE
		}

		// Hide the scan NFC tag icon
		if (scanNfcTagIcon.visibility != View.INVISIBLE)
		{
			scanNfcTagIcon.visibility = View.INVISIBLE
		}

		// Hide the stop button
		if (stopButton.visibility != View.INVISIBLE)
		{
			stopButton.visibility = View.INVISIBLE
		}
	}

	/**
	 * Set the visibility of views when timer is reset.
	 */
	fun setResetVisibility()
	{
		// Show the start button
		if (startButton.visibility != View.VISIBLE)
		{
			startButton.visibility = View.VISIBLE
		}

		// Hide the pause button
		if (pauseButton.visibility != View.INVISIBLE)
		{
			pauseButton.visibility = View.INVISIBLE
		}

		// Hide the reset button
		if (resetButton.visibility != View.GONE)
		{
			resetButton.visibility = View.GONE
		}

		// Hide the scan NFC tag icon
		if (scanNfcTagIcon.visibility != View.INVISIBLE)
		{
			scanNfcTagIcon.visibility = View.INVISIBLE
		}

		// Hide the stop button
		if (stopButton.visibility != View.INVISIBLE)
		{
			stopButton.visibility = View.INVISIBLE
		}
	}

	/**
	 * Set the visibility of views when timer is started or resumed.
	 */
	fun setResumeVisibility()
	{
		// Hide the start button
		if (startButton.visibility != View.INVISIBLE)
		{
			startButton.visibility = View.INVISIBLE
		}

		// Show the pause button
		if (pauseButton.visibility != View.VISIBLE)
		{
			pauseButton.visibility = View.VISIBLE
		}

		// Show the reset button
		if (resetButton.visibility != View.VISIBLE)
		{
			resetButton.visibility = View.VISIBLE
		}

		// Hide the scan NFC tag icon
		if (scanNfcTagIcon.visibility != View.INVISIBLE)
		{
			scanNfcTagIcon.visibility = View.INVISIBLE
		}

		// Hide the stop button
		if (stopButton.visibility != View.INVISIBLE)
		{
			stopButton.visibility = View.INVISIBLE
		}
	}

	/**
	 * Set the visibility of views when timer is going off and should be stopped.
	 */
	fun setStopVisibility()
	{
		// Hide the start button
		if (startButton.visibility != View.INVISIBLE)
		{
			startButton.visibility = View.INVISIBLE
		}

		// Hide the pause button
		if (pauseButton.visibility != View.INVISIBLE)
		{
			pauseButton.visibility = View.INVISIBLE
		}

		// Hide the reset button
		if (resetButton.visibility != View.INVISIBLE)
		{
			resetButton.visibility = View.INVISIBLE
		}

		// Timer requires NFC
		if (timer!!.shouldUseNfc(root.context))
		{
			// Show the scan NFC tag icon
			if (scanNfcTagIcon.visibility != View.VISIBLE)
			{
				scanNfcTagIcon.visibility = View.VISIBLE
			}
		}
		// No NFC needed
		else
		{
			// Show the stop button
			if (stopButton.visibility != View.VISIBLE)
			{
				stopButton.visibility = View.VISIBLE
			}
		}
	}

	/**
	 * Start the timer ringing animation.
	 */
	fun startTimerRingingAnimation(context: Context)
	{
		startTimerRingingAnimation(
			context, progressIndicator,
			hourTextView, hourUnits,
			minuteTextView, minuteUnits,
			secondsTextView, secondsUnits)
	}

	/**
	 * Update the hour, minute, and seconds textviews based on the milliseconds until
	 * finished.
	 */
	fun updateHourMinuteSecondsTextViews(secUntilFinished: Long)
	{
		updateHourMinuteSecondsTextViews(
			hourTextView, hourUnits,
			minuteTextView, minuteUnits,
			secondsTextView,
			secUntilFinished
		)
	}

}