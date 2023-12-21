package com.nfcalarmclock.activealarm

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.util.NacCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class NacSwipeLayoutHandler(

	/**
	 * Activity.
	 */
	activity: AppCompatActivity,

	/**
	 * Alarm.
	 */
	alarm: NacAlarm?,

	/**
	 * Listener for an alarm action.
	 */
	onAlarmActionListener: OnAlarmActionListener

	// Constructor
) : NacActiveAlarmLayoutHandler(activity, alarm, onAlarmActionListener)
{

	/**
	 * Exception when unable to calculate new X position.
	 */
	class UnableToCalculateNewXposition
		: Exception("Unable to calculate new X position.")

	/**
	 * Animator for the guide on how the user should swipe.
	 */
	private val swipeAnimation: NacSwipeAnimationHandler = NacSwipeAnimationHandler(activity)

	/**
	 * Alarm name.
	 */
	private val alarmNameTextView: TextView = activity.findViewById(R.id.alarm_name)

	/**
	 * Current date.
	 */
	private val currentDateTextView: TextView = activity.findViewById(R.id.current_date)

	/**
	 * Current time.
	 */
	private val currentTimeTextView: TextView = activity.findViewById(R.id.current_time)

	/**
	 * Music information.
	 */
	private val musicContainer: RelativeLayout = activity.findViewById(R.id.music_container)

	/**
	 * Music title.
	 */
	private val musicTitleTextView: TextView = activity.findViewById(R.id.music_title)

	/**
	 * Music artist.
	 */
	private val musicArtistTextView: TextView = activity.findViewById(R.id.music_artist)

	/**
	 * Snooze button.
	 */
	private val snoozeButton: RelativeLayout = activity.findViewById(R.id.round_snooze_button)

	/**
	 * Dismiss button.
	 */
	private val dismissButton: RelativeLayout = activity.findViewById(R.id.round_dismiss_button)

	/**
	 * Slider path for the alarm action buttons (snooze and dismiss).
	 */
	private val sliderPath: RelativeLayout = activity.findViewById(R.id.alarm_action_slider_path)

	/**
	 * View to capture the user's attention for the snooze button.
	 */
	private val snoozeAttentionView: RelativeLayout = activity.findViewById(R.id.snooze_attention_view)

	/**
	 * View to capture the user's attention for the dismiss button.
	 */
	private val dismissAttentionView: RelativeLayout = activity.findViewById(R.id.dismiss_attention_view)

	/**
	 * Starting X position on the alarm action row.
	 */
	private var startAlarmActionX: Float = -1f

	/**
	 * Ending X position on the alarm action row.
	 */
	private var endAlarmActionX: Float = -1f

	/**
	 * Whether 24 hour format should be used or not.
	 */
	private val is24HourFormat = DateFormat.is24HourFormat(activity)

	/**
	 * AM string.
	 */
	private val am = activity.getString(R.string.am)

	/**
	 * PM string.
	 */
	private val pm = activity.getString(R.string.pm)

	/**
	 * Calculate the new X position.
	 */
	private fun calculateNewXposition(
		view: View,
		motionEvent: MotionEvent,
		dx: Float
	): Float
	{
		// Calculate the new X position
		var newX = motionEvent.rawX + dx

		// Check if the new X position is less than the start position
		if (newX < startAlarmActionX)
		{
			// Check if the view's position is equal to the start position
			if (view.x == startAlarmActionX)
			{
				// Do not move the view
				throw UnableToCalculateNewXposition()
			}

			// Set the new X position to the start position
			newX = startAlarmActionX
		}
		// Check if the new X position is greater than the end position
		else if (newX > endAlarmActionX)
		{
			// Check if the view's position is equal to the end position
			if (view.x == endAlarmActionX)
			{
				// Do not move the view
				throw UnableToCalculateNewXposition()
			}

			// Set the new X position to the end position
			newX = endAlarmActionX
		}

		return newX
	}

	/**
	 * Calculate X offset position.
	 */
	private fun calculateXoffsetPosition(view: View, motionEvent: MotionEvent): Float
	{
		return view.x - motionEvent.rawX
	}

	/**
	 * Get the inactive view by comparing the active view against the snooze
	 * and dismiss button views.
	 */
	private fun getInactiveView(
		activeView: View,
		snoozeButton: View,
		dismissButton: View
	): View
	{
		// Get the inactive view by checking the ID of the view actively being
		// moved and using the other view
		return when (activeView.id)
		{
			// Snooze button is active
			snoozeButton.id ->
			{
				// Dismiss button is inactive
				dismissButton
			}

			// Dismiss button is active
			dismissButton.id ->
			{
				// Snooze button is inactive
				snoozeButton
			}

			// Return the active view because unable to determine which view is
			// inactive. This should never happen
			else -> activeView
		}
	}

	/**
	 * Setup an alarm action button.
	 */
	@SuppressLint("ClickableViewAccessibility")
	private fun setupAlarmActionButton(button: View)
	{
		// X position information
		val origX = button.x
		var dx = 0f

		// Set the listener
		button.setOnTouchListener { view, motionEvent ->

			// Check action of motion event
			when (motionEvent.action)
			{

				// Finger DOWN on button
				MotionEvent.ACTION_DOWN ->
				{
					// Compute the offset X position
					dx = calculateXoffsetPosition(view, motionEvent)

					// Get the inactive view
					val inactiveView = getInactiveView(view, snoozeButton, dismissButton)

					// Hide the inactive view (either the snooze or dismiss button)
					swipeAnimation.hideInactiveView(inactiveView)

					// Hide the attention views and stop the animations
					swipeAnimation.hideAttentionViews(snoozeAttentionView,
						dismissAttentionView)

					// Show the slider path
					swipeAnimation.showSliderPath(sliderPath)
				}

				// Finger UP on button
				MotionEvent.ACTION_UP ->
				{
					// Check if the alarm should be snoozed
					if (shouldSnooze(view))
					{
						// Call the snooze listener
						onAlarmActionListener.onSnooze(alarm!!)
						return@setOnTouchListener true
					}
					// Check if the alarm should be dismissed
					else if (shouldDismiss(view))
					{
						// Call the dismiss listener
						onAlarmActionListener.onDismiss(alarm!!)
						return@setOnTouchListener true
					}

					// Animate back to the original X position
					view.animate()
						.x(origX)
						.setDuration(300)
						.withEndAction {

							// Get the inactive view
							val inactiveView = getInactiveView(view, snoozeButton,
								dismissButton)

							// Show the inactive view (either the snooze or dismiss button)
							swipeAnimation.showInactiveView(inactiveView,
								onEnd = {
									// Show the attention views and start their animations
									swipeAnimation.showAttentionViews(
										snoozeAttentionView, dismissAttentionView)
								})

							// Hide the slider path
							swipeAnimation.hideSliderPath(sliderPath)

						}
						.start()
				}

				// Moving finger
				MotionEvent.ACTION_MOVE ->
				{
					try
					{
						// Set the new X position
						view.x = calculateNewXposition(view, motionEvent, dx)
					}
					catch (_: UnableToCalculateNewXposition)
					{
					}
				}

			}

			// Return
			true
		}
	}

	/**
	 * Setup the dismiss button.
	 */
	private fun setupDismissButton()
	{
		// Determine the right bound of where the snooze/dismiss button can go
		endAlarmActionX = dismissButton.x

		// Setup the dismiss button
		setupAlarmActionButton(dismissButton)
	}

	/**
	 * Setup the snooze button.
	 */
	private fun setupSnoozeButton()
	{
		// Determine the left bound of where the snooze/dismiss button can go
		startAlarmActionX = snoozeButton.x

		// Setup the snooze button
		setupAlarmActionButton(snoozeButton)
	}

	/**
	 * Check if the alarm should be dismissed .
	 *
	 * @return True if the alarm should be dismissed because the view is the
	 *         dismiss button and it is at the start of the alarm action row,
	 *         and False otherwise.
	 */
	private fun shouldDismiss(view: View): Boolean
	{
		return when (view.id)
		{
			// Dismiss button
			dismissButton.id ->
			{
				// Check if the view position is at the start of the alarm action row
				return (view.x == startAlarmActionX)
			}

			// Snooze button or some other view which should never happen
			else -> false
		}
	}

	/**
	 * Check if the alarm should be snoozed.
	 *
	 * @return True if the alarm should be snoozed because the view is the
	 *         snooze button and it is at the end of the alarm action row, and
	 *         False otherwise.
	 */
	private fun shouldSnooze(view: View): Boolean
	{
		return when (view.id)
		{
			// Snooze button
			snoozeButton.id ->
			{
				// Check if the view position is at the end of the alarm action row
				return (view.x == endAlarmActionX)
			}

			// Dismiss button or some other view which should never happen
			else -> false
		}
	}

	/**
	 * Setup the alarm name.
	 */
	private fun setupAlarmName()
	{
		// Get the user preference on whether the alarm name should be shown
		val visibility = if (sharedPreferences.showAlarmName) View.VISIBLE else View.INVISIBLE

		// Set the visibility
		alarmNameTextView.visibility = visibility

		// Check if the alarm is not null and the user wants to see alarm name
		if ((alarm != null) && sharedPreferences.showAlarmName)
		{
			// Show the alarm name
			alarmNameTextView.text = alarm.nameNormalized
			alarmNameTextView.isSelected = true
		}
	}

	/**
	 * Setup the current date and time.
	 */
	private fun setupCurrentDateAndTime()
	{
		// Get the user preference on whether the current date and time should
		// be shown
		val visibility = if (sharedPreferences.showCurrentDateAndTime) View.VISIBLE else View.INVISIBLE

		// Set the visibility
		currentDateTextView.visibility = visibility
		currentTimeTextView.visibility = visibility

		// Get info for calculating current date and time
		val locale = Locale.getDefault()
		val cal = Calendar.getInstance()

		// Get the current date
		val skeleton = DateFormat.getBestDateTimePattern(locale, "E MMM d")
		val dateFormat = SimpleDateFormat(skeleton, locale)

		dateFormat.timeZone = TimeZone.getDefault()
		dateFormat.applyLocalizedPattern(skeleton)

		val date = dateFormat.format(cal.time)
		println(dateFormat)
		println(date)

		// Get the current time
		val hour = cal[Calendar.HOUR]
		val minute = cal[Calendar.MINUTE]
		var time = NacCalendar.getClockTime(hour, minute, is24HourFormat)
		val meridian = NacCalendar.getMeridian(hour, is24HourFormat, am, pm)

		time += " $meridian"

		// Set the text
		currentDateTextView.text = date
		currentTimeTextView.text = time
	}

	/**
	 * Setup the music information.
	 */
	private fun setupMusicInformation()
	{
		// Get the user preference on whether the music info should be shown
		val visibility = if (sharedPreferences.showMusicInfo) View.VISIBLE else View.INVISIBLE

		// Set the visibility
		// TODO: Get the music event system working
		musicContainer.visibility = View.INVISIBLE
	}


	/**
	 * Start the layout and run any setup that needs to run.
	 */
	override fun start()
	{
		// Setup the views based on user preference
		setupAlarmName()
		setupCurrentDateAndTime()
		setupMusicInformation()

		// Show the attention views and start their animations
		swipeAnimation.showAttentionViews(snoozeAttentionView, dismissAttentionView)

		// Setup the snooze and dismiss buttons
		setupSnoozeButton()
		setupDismissButton()
	}

	/**
	 * Stop the layout handler.
	 */
	override fun stop()
	{
		// Hide the attention views and stop the animations
		swipeAnimation.hideAttentionViews(snoozeAttentionView, dismissAttentionView)
	}

}