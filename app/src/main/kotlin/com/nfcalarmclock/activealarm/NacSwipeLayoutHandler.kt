package com.nfcalarmclock.activealarm

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.net.Uri
import android.text.format.DateFormat
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.media.NacMedia
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.util.createTimeTickReceiver
import com.nfcalarmclock.util.registerMyReceiver
import com.nfcalarmclock.util.unregisterMyReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Handler for the swipe layout.
 */
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
	 * Current meridian.
	 */
	private val currentMeridianView: TextView = activity.findViewById(R.id.current_meridian)

	/**
	 * Scan NFC container.
	 */
	private val scanNfcView: LinearLayout = activity.findViewById(R.id.scan_nfc_view)

	/**
	 * Snooze button.
	 */
	private val snoozeButton: RelativeLayout = activity.findViewById(R.id.snooze_view)

	/**
	 * Dismiss button.
	 */
	private val dismissButton: RelativeLayout = activity.findViewById(R.id.dismiss_view)

	/**
	 * View to capture the user's attention for the snooze button.
	 */
	private val snoozeAttentionView: RelativeLayout = activity.findViewById(R.id.snooze_attention_view)

	/**
	 * View to capture the user's attention for the dismiss button.
	 */
	private val dismissAttentionView: RelativeLayout = activity.findViewById(R.id.dismiss_attention_view)

	/**
	 * Slider path for the alarm action buttons (snooze and dismiss).
	 */
	private val sliderPath: RelativeLayout = activity.findViewById(R.id.alarm_action_slider_path)

	/**
	 * Instructions as to what sliding on the path will do.
	 */
	private val sliderInstructions: TextView = activity.findViewById(R.id.slider_instructions)

	/**
	 * Arrows in the slider path to show which direction to slide.
	 */
	private val sliderCenterArrow: ImageView = activity.findViewById(R.id.slider_center_arrow)
	private val sliderLeftArrow: ImageView = activity.findViewById(R.id.slider_left_arrow)
	private val sliderRightArrow: ImageView = activity.findViewById(R.id.slider_right_arrow)

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
	 * Starting X position on the alarm action row.
	 */
	private var startAlarmActionX: Float = -1f

	/**
	 * Center X position on the alarm action row.
	 */
	private var centerAlarmActionX: Float = -1f

	/**
	 * Ending X position on the alarm action row.
	 */
	private var endAlarmActionX: Float = -1f

	/**
	 * Receiver for the time tick intent. This is called when the time increments
	 * every minute.
	 */
	private val timeTickReceiver = createTimeTickReceiver { context, _ ->
		setupCurrentDateAndTime(context)
	}

	/**
	 * Listener for any changes to the shared preferences.
	 */
	private lateinit var onSharedPreferenceChangedListener: SharedPreferences.OnSharedPreferenceChangeListener

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
	 * Run any setup steps.
	 */
	override fun setup(context: Context)
	{
		// Setup the views based on user preference
		setupAlarmName()
		setupCurrentDateAndTime(context)
		setupMusicInformation(context)

		// Check if the dismiss button should be visible or not
		if (alarm!!.shouldUseNfc)
		{
			// Show the scan NFC view
			scanNfcView.visibility = View.VISIBLE

			// Set to INVISIBLE so that the end X position can still be
			// determined, and then it will be set to GONE later
			dismissButton.visibility = View.INVISIBLE
			dismissAttentionView.visibility = View.INVISIBLE
		}
	}

	/**
	 * Setup an alarm action button.
	 */
	@SuppressLint("ClickableViewAccessibility")
	private fun setupAlarmActionButton(button: View, x: Float = -1f)
	{
		// X position information
		val origX = if (x >= 0) x else button.x
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

					// Determine the text of the slider instructions
					val text = if (view.id == snoozeButton.id)
					{
						R.string.description_slide_to_snooze
					}
					else
					{
						R.string.description_slide_to_dismiss
					}

					sliderInstructions.setText(text)

					// Determine which arrow to show
					val arrow = if (view.id == snoozeButton.id)
					{
						R.drawable.arrow_right
					}
					else
					{
						R.drawable.arrow_left
					}

					sliderCenterArrow.setImageResource(arrow)
					sliderLeftArrow.setImageResource(arrow)
					sliderRightArrow.setImageResource(arrow)

					// Show the slider path
					swipeAnimation.showSliderPath(sliderPath, sliderInstructions)
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

					// Calculate the duration for the animation below
					val duration = if (view.x == origX) 0L else 300L

					// Animate back to the original X position
					view.animate()
						.x(origX)
						.setDuration(duration)
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
							swipeAnimation.hideSliderPath(sliderPath, sliderInstructions)

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
	private fun setupCurrentDateAndTime(context: Context)
	{
		// Get the user preference on whether the current date and time should
		// be shown
		val visibility = if (sharedPreferences.showCurrentDateAndTime) View.VISIBLE else View.INVISIBLE

		// Set the visibility
		currentDateTextView.visibility = visibility
		currentTimeTextView.visibility = visibility
		currentMeridianView.visibility = visibility

		// Get info for calculating current date and time
		val locale = Locale.getDefault()
		val cal = Calendar.getInstance()

		// Get the current date
		val skeleton = DateFormat.getBestDateTimePattern(locale, "E MMM d")
		val dateFormat = SimpleDateFormat(skeleton, locale)

		dateFormat.timeZone = TimeZone.getDefault()
		dateFormat.applyLocalizedPattern(skeleton)

		val date = dateFormat.format(cal.time)

		// Get the current time
		val hour = cal[Calendar.HOUR_OF_DAY]
		val minute = cal[Calendar.MINUTE]
		val time = NacCalendar.getClockTime(context, hour, minute)
		val meridian = NacCalendar.getMeridian(context, hour)

		// Set the text
		currentDateTextView.text = date
		currentTimeTextView.text = time
		currentMeridianView.text = if (DateFormat.is24HourFormat(context)) "" else meridian
	}

	/**
	 * Setup the dismiss button.
	 */
	private fun setupDismissButton()
	{
		// Check if the X position has already been set
		if (endAlarmActionX >= 0)
		{
			return
		}

		// Determine the right bound of where the snooze/dismiss button can go
		endAlarmActionX = dismissButton.x

		// Check if the dismiss button should be visible or not
		if (alarm!!.shouldUseNfc)
		{
			dismissButton.visibility = View.GONE
			dismissAttentionView.visibility = View.GONE
			return
		}

		// Setup the dismiss button
		setupAlarmActionButton(dismissButton)

		// Change color of the dismiss button
		dismissButton.backgroundTintList = ColorStateList.valueOf(sharedPreferences.themeColor)
	}

	/**
	 * Setup the music information.
	 */
	private fun setupMusicInformation(context: Context)
	{
		// Get the current media item
		val mediaPath = sharedPreferences.currentPlayingAlarmMedia

		// Get the user preference on whether the music info should be shown
		val visibility = if (sharedPreferences.showMusicInfo && mediaPath.isNotEmpty())
			View.VISIBLE else View.INVISIBLE

		// Set the visibility
		musicContainer.visibility = visibility

		// Check if the music container is not visible
		if (musicContainer.visibility == View.INVISIBLE)
		{
			// Do nothing else
			return
		}

		// Get the title and artist of the media
		val mediaUri = Uri.parse(mediaPath)
		val title = NacMedia.getTitle(context, mediaUri)
		val artist = NacMedia.getArtist(context, mediaUri)
		val unknown = context.getString(R.string.state_unknown)

		// Set the title and artist
		musicTitleTextView.text = title
		musicArtistTextView.text = artist

		// Set the visibility of the artist if it is unknown
		musicArtistTextView.visibility = if (artist != unknown) View.VISIBLE else View.GONE
	}

	/**
	 * Setup the snooze button.
	 */
	private fun setupSnoozeButton()
	{
		// Check if the X position has already been set
		if (startAlarmActionX >= 0)
		{
			return
		}

		// Determine the left bound of where the snooze/dismiss button can go
		startAlarmActionX = snoozeButton.x

		// Check if the snooze button should be in the center or not
		if (alarm!!.shouldUseNfc)
		{
			// Determine the center X position where the snooze button will go
			centerAlarmActionX = ((sliderPath.width - snoozeButton.width) / 2).toFloat()

			// Set the snooze button in the center
			val layoutParams = snoozeButton.layoutParams as RelativeLayout.LayoutParams

			layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_START)
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)

			snoozeButton.layoutParams = layoutParams
		}

		// Setup the snooze button
		setupAlarmActionButton(snoozeButton, x=centerAlarmActionX)
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
	 * Start the layout and run any setup that needs to run.
	 */
	override fun start(context: Context)
	{
		// Setup the snooze and dismiss buttons
		setupSnoozeButton()
		setupDismissButton()

		// Show the attention views and start their animations
		swipeAnimation.showAttentionViews(snoozeAttentionView, dismissAttentionView)

		// Register the time tick receiver
		registerMyReceiver(context, timeTickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))

		// Watch shared preferences listener
		onSharedPreferenceChangedListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->

			// Get the key for the currently playing media
			val currentPlayingMediaKey = context.getString(R.string.key_current_playing_alarm_media)

			// Check if the keys do not match. Only care about the currently
			// playing media key
			if (key != currentPlayingMediaKey)
			{
				return@OnSharedPreferenceChangeListener
			}

			// Setup the music information
			setupMusicInformation(context)
		}

		// Register the shared preference listener
		sharedPreferences.instance.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangedListener)
	}

	/**
	 * Stop the layout handler.
	 */
	override fun stop(context: Context)
	{
		// Hide the attention views and stop the animations
		swipeAnimation.hideAttentionViews(snoozeAttentionView, dismissAttentionView)

		// Unregister the time tick receiver
		unregisterMyReceiver(context, timeTickReceiver)

		// Unregister the shared preference listener
		sharedPreferences.instance.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangedListener)
	}

}