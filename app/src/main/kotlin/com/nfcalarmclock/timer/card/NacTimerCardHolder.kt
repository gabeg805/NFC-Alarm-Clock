package com.nfcalarmclock.timer.card

import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.card.NacBaseCardHolder
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.view.performHapticFeedback
import com.nfcalarmclock.view.setupThemeColor

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
		fun onEditTimer()
	}

	/**
	 * Listener for when the start timer button is clicked.
	 */
	fun interface OnStartTimerClickedListener
	{
		fun onStartTimer()
	}

	/**
	 * Listener for when the pause timer button is clicked.
	 */
	fun interface OnPauseTimerClickedListener
	{
		fun onPauseTimer()
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
	private val summaryRepeatFrequency: TextView = root.findViewById(R.id.timer_repeat_frequency)

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
		summaryRepeatFrequency.setTextColor(sharedPreferences.daysColor)
		summaryName.setTextColor(sharedPreferences.nameColor)
		startButton.setupThemeColor(sharedPreferences)
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
				onEditTimerClickedListener?.onEditTimer()

				// Haptic feedback
				view.performHapticFeedback()

			}
		}

		// Start timer clicked
		startButton.setOnClickListener { onStartTimerClickedListener?.onStartTimer() }

		// Pause timer clicked
		pauseButton.setOnClickListener { onPauseTimerClickedListener?.onPauseTimer() }
	}

	/**
	 * Initialize the various views.
	 */
	override fun initViews()
	{
		// Get the hour, minutes, and seconds to display
		val (hour, minute, seconds) = NacCalendar.getTimerHourMinuteSecondsZeroPadded(timer!!.duration)

		// Set the hours
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

		// Set the minutes
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

		// TODO: Set the repeat frequency
		summaryRepeatFrequency.visibility = View.INVISIBLE

		// Set the name
		if (timer!!.name.isNotEmpty())
		{
			summaryName.text = timer!!.name
			summaryName.visibility = View.VISIBLE
		}
		else
		{
			summaryName.visibility = View.INVISIBLE
		}
	}

}