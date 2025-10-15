package com.nfcalarmclock.timer.card

import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.card.NacBaseCardHolder
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.view.performHapticFeedback

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
	 * Listener for editing the timer.
	 */
	fun interface OnEditTimerClickedListener
	{
		fun onEditTimer(timer: NacTimer)
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
	 * Timer.
	 */
	var timer: NacTimer? = null

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
	private val secondsTextView: TextView = root.findViewById(R.id.timer_seconds)

	/**
	 * Hour units textview.
	 */
	private val hourUnits: TextView = root.findViewById(R.id.timer_hour_units)

	/**
	 * Minute units textview.
	 */
	private val minuteUnits: TextView = root.findViewById(R.id.timer_minute_units)

	/**
	 * Summary view containing the days to repeat.
	 */
	private val summaryName: TextView = root.findViewById(R.id.timer_name)

	/**
	 * Start button.
	 */
	private val startButton: MaterialButton = root.findViewById(R.id.timer_start_button)

	/**
	 * Pause button.
	 */
	private val pauseButton: MaterialButton = root.findViewById(R.id.timer_pause_button)

	/**
	 * Reset button.
	 */
	private val resetButton: MaterialButton = root.findViewById(R.id.timer_reset_button)

	/**
	 * Listener for editing the timer.
	 */
	var onEditTimerClickedListener: OnEditTimerClickedListener? = null

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
		summaryName.setTextColor(sharedPreferences.nameColor)
	}

	/**
	 * Initialize the listeners of the various views.
	 */
	override fun initListeners()
	{
		// List of all the views that need the same on click listener for the card
		val allCardViews = listOf(cardView)

		// Call the edit timer listener when one of the views that handles that is clicked
		for (view in allCardViews)
		{
			view.setOnClickListener {

				// Call the listener
				onEditTimerClickedListener?.onEditTimer(timer!!)

				// Haptic feedback
				view.performHapticFeedback()

			}
		}

		// Start clicked
		startButton.setOnClickListener {

			// Show the pause and reset buttons. Hide the start button
			startButton.visibility = View.INVISIBLE
			pauseButton.visibility = View.VISIBLE
			resetButton.visibility = View.VISIBLE

			// Call the listener
			onStartTimerClickedListener?.onStartTimer(timer!!)

		}

		// Pause clicked
		pauseButton.setOnClickListener {

			// Show the start and reset buttons. Hide the pause button
			startButton.visibility = View.VISIBLE
			pauseButton.visibility = View.INVISIBLE
			resetButton.visibility = View.VISIBLE

			// Call the listener
			onPauseTimerClickedListener?.onPauseTimer(timer!!)

		}

		// Reset clicked
		resetButton.setOnClickListener {

			// Show the start. Hide the pause and reset buttons
			startButton.visibility = View.VISIBLE
			pauseButton.visibility = View.INVISIBLE
			resetButton.visibility = View.GONE

			// Call the listener
			onResetTimerClickedListener?.onResetTimer(timer!!)

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

		// TODO: Start/pause/reset visibility
	}

}