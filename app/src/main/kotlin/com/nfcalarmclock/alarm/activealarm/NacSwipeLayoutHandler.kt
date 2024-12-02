package com.nfcalarmclock.alarm.activealarm

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.net.Uri
import android.text.format.DateFormat
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.util.media.NacMedia
import com.nfcalarmclock.alarm.options.nfc.db.NacNfcTag
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.util.createTimeTickReceiver
import com.nfcalarmclock.util.registerMyReceiver
import com.nfcalarmclock.util.unregisterMyReceiver
import java.lang.Float.max
import java.lang.Float.min
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.absoluteValue

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
	 * TextView for the name of the NFC tag.
	 */
	private val nfcNameTextView: TextView = activity.findViewById(R.id.ismiss_text3)

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
	 * View property animator for the previous view that was being moved.
	 */
	private var viewPropertyAnimator: ViewPropertyAnimator? = null

	/**
	 * Velocity tracker for when a view is swiped.
	 */
	private var velocityTracker: VelocityTracker? = null

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
	 * Check if the handler was stopped via stop().
	 */
	private var wasStopped: Boolean = false

	/**
	 * Animate the button back to its original X position
	 */
	private fun animateButtonBackToOriginalXposition(view: View, origX: Float = -1f)
	{
		// Get the original X position of the view
		val newOrigX = if (origX >= 0) origX else getOriginalXposition(view)

		// Calculate the duration for the animation below
		var duration = if (view.x == newOrigX) 0L else 300L

		// Check if the alarm should be snoozed
		if (shouldSnooze(view))
		{
			// Increase the duration so that if it snoozes, the
			// user does not see the button go back, and if it is
			// unable to snooze, then the slow animation back is ok
			duration = 1000L

			// Call the snooze listener
			onAlarmActionListener.onSnooze(alarm!!)
		}
		// Check if the alarm should be dismissed
		else if (shouldDismiss(view))
		{
			// Call the dismiss listener
			onAlarmActionListener.onDismiss(alarm!!)
			return
		}

		// Set the animator to animate view the back to its original X position
		viewPropertyAnimator = view.animate()
			.x(newOrigX)
			.setDuration(duration)
			.withEndAction {

				// Check if the handler was stopped
				if (wasStopped)
				{
					// Do nothing else as the activity was stopped
					return@withEndAction
				}

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

		// Start the animator
		viewPropertyAnimator?.start()

		return
	}

	/**
	 * Calculate the final velocity.
	 */
	private fun calculateFinalVelocity(
		motionEvent: MotionEvent,
		minValue: Float,
		maxValue: Float)
		: Float
	{
		// Add movement to the velocity tracker
		velocityTracker?.addMovement(motionEvent)

		// Compute current velocity
		velocityTracker?.computeCurrentVelocity(1000)

		// Get velocity
		val pointerId: Int = motionEvent.getPointerId(motionEvent.actionIndex)
		var finalVelocity: Float = velocityTracker?.getXVelocity(pointerId)?.times(
			FLING_SCALE_FACTOR)
			?: return 0f

		// Calibrate the final velocity based on its current calculated value
		if (finalVelocity.absoluteValue < FLING_DEFAULT_VELOCITY)
		{
			finalVelocity = FLING_DEFAULT_VELOCITY
		}

		// Check if final velocity is the wrong sign when snoozing
		if ((minValue == 0f) && (finalVelocity < 0))
		{
			finalVelocity *= -1f
		}
		// Check if final velocity is the wrong sign when dismissing
		else if ((maxValue == 0f) && (finalVelocity > 0))
		{
			finalVelocity *= -1f
		}

		// Make sure the final velocity is within the min and max values
		return max(minValue, min(maxValue, finalVelocity))
	}

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
	 * Cleanup the velocity tracker.
	 */
	private fun cleanupVelocityTracker()
	{
		velocityTracker?.recycle()
		velocityTracker = null
	}

	/**
	 * Fling the view.
	 */
	private fun flingView(
		view: View,
		velocity: Float,
		minValue: Float,
		maxValue: Float,
		onEnd: () -> Unit = {})
	{
		FlingAnimation(view, DynamicAnimation.TRANSLATION_X).apply {

			// Listener for as the animation is updated
			addUpdateListener { _, _, _ ->

				// Make sure the view's position does not go
				// outside of the allowable start and end X
				// positions
				if (view.x >= endAlarmActionX)
				{
					// Set the view to the end X position
					view.x = endAlarmActionX

					// Cancel the animation
					cancel()
				}
				else if (view.x <= startAlarmActionX)
				{
					// Set the view to the start X position
					view.x = startAlarmActionX

					// Cancel the animation
					cancel()
				}

			}

			// Listener for when the animation ends
			addEndListener { _, _, _, _ -> onEnd() }

			// Setup
			setStartVelocity(velocity)
			setMinValue(minValue)
			setMaxValue(maxValue)
			friction = FLING_FRICTION_COEFFICIENT

			// Start the animation
			try
			{
				start()
			}
			catch (e: IllegalArgumentException)
			{
				// Starting value need to be in between min value and max value
				//
				// I think this happens when swiping the view outside of its
				// desired range. For instance, if the view starts in the
				// center, its desired range is from the center to the right.
				// However, if a user tries to swipe it to the left, this is
				// outside of its desired range and thus the exception will
				// occur
				onEnd()
			}
		}
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
	 * Get the original X position of the view.
	 *
	 * @return The original X position of the view.
	 */
	private fun getOriginalXposition(view: View): Float
	{
		// Determine the X position of the view that is being animated
		return when (view.id)
		{
			// Snooze button
			snoozeButton.id -> startAlarmActionX

			// Dismiss button
			dismissButton.id -> endAlarmActionX

			// Default X position
			else -> startAlarmActionX
		}
	}

	/**
	 * Move the view.
	 */
	private fun moveView(view: View, motionEvent: MotionEvent, dx: Float)
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

	/**
	 * Run any setup steps.
	 */
	override fun setup(context: Context)
	{
		// Setup the views based on user preference
		setupAlarmName()
		setupCurrentDateAndTime(context)
		setupMusicInformation(context)
		musicContainer.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

		// Check if the dismiss button should be visible or not
		if (shouldUseNfc)
		{
			// Show the scan NFC view
			scanNfcView.visibility = View.VISIBLE

			// Setup the color of the name of the NFC tag
			nfcNameTextView.setTextColor(sharedPreferences.themeColor)

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
	private fun setupAlarmActionButton(button: View, origX: Float = -1f)
	{
		// Change in X position
		var dx = 0f

		// Set the listener
		button.setOnTouchListener { view, motionEvent ->

			// Check action of motion event
			when (motionEvent.action)
			{

				// Finger DOWN on button
				MotionEvent.ACTION_DOWN ->
				{
					// Clear any previous animations on the view
					viewPropertyAnimator?.cancel()

					// Compute the offset X position
					dx = calculateXoffsetPosition(view, motionEvent)

					// Setup the velocity tracker
					setupVelocityTracker(motionEvent)

					// Get the inactive view
					val inactiveView = getInactiveView(view, snoozeButton, dismissButton)

					// Hide the inactive view (either the snooze or dismiss button)
					swipeAnimation.hideInactiveView(inactiveView)

					// Hide the attention views and stop the animations
					swipeAnimation.hideAttentionViews(snoozeAttentionView,
						dismissAttentionView)

					// Setup the slider instructions
					setupSliderInstructions(view.id)

					// Setup the slider arrows
					setupSliderArrows(view.id)

					// Show the slider path
					swipeAnimation.showSliderPath(sliderPath, sliderInstructions)
				}

				// Finger UP on button
				MotionEvent.ACTION_UP ->
				{
					// Calculate the min and max fling value
					val minValue = if (view.id == snoozeButton.id) FLING_MIN_VALUE else -FLING_MAX_VALUE
					val maxValue = if (view.id == snoozeButton.id) FLING_MAX_VALUE else FLING_MIN_VALUE

					// Calculate the final velocity
					val finalVelocity = calculateFinalVelocity(motionEvent, minValue, maxValue)

					// Cleanup the velocity tracker
					cleanupVelocityTracker()

					// Check if the view should be flung with the additional
					// velocity the user imparted on the view
					if (shouldFlingView(finalVelocity))
					{
						// Fling the view
						flingView(view, finalVelocity, minValue, maxValue,
							onEnd = {
								// Animate the view back when fling ends
								animateButtonBackToOriginalXposition(view, origX=origX)
							})
					}
					else
					{
						// Animate the view back to its original X position
						animateButtonBackToOriginalXposition(view, origX=origX)
					}
				}

				// Moving finger
				MotionEvent.ACTION_MOVE ->
				{
					// Move the view
					moveView(view, motionEvent, dx)

					// Add movement to the velocity tracker
					velocityTracker?.addMovement(motionEvent)
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
		val visibility = if (sharedPreferences.shouldShowAlarmName) View.VISIBLE else View.INVISIBLE

		// Set the visibility
		alarmNameTextView.visibility = visibility

		// Check if the alarm is not null and the user wants to see alarm name
		if ((alarm != null) && sharedPreferences.shouldShowAlarmName)
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
		val visibility = if (sharedPreferences.shouldShowCurrentDateAndTime) View.VISIBLE else View.INVISIBLE

		// Set the visibility
		currentDateTextView.visibility = visibility
		currentTimeTextView.visibility = visibility
		currentMeridianView.visibility = visibility

		// Get info for calculating current date and time
		val cal = Calendar.getInstance()

		// Get the current date
		val date = NacCalendar.getDate(cal)

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
		if (shouldUseNfc)
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
		val visibility = if (sharedPreferences.shouldShowMusicInfo && mediaPath.isNotEmpty())
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
	 * Setup an NFC tag, if necessary.
	 */
	override fun setupNfcTag(nfcTag: NacNfcTag?)
	{
		// Set the name of the NFC tag
		nfcNameTextView.text = nfcTag?.name
	}

	/**
	 * Setup the slider arrows.
	 */
	private fun setupSliderArrows(viewId: Int)
	{
		// Determine which arrow to show
		val arrow = if (viewId == snoozeButton.id)
		{
			R.drawable.arrow_right
		}
		else
		{
			R.drawable.arrow_left
		}

		// Set the slider arrows
		sliderCenterArrow.setImageResource(arrow)
		sliderLeftArrow.setImageResource(arrow)
		sliderRightArrow.setImageResource(arrow)
	}

	/**
	 * Setup the slider instructions.
	 */
	private fun setupSliderInstructions(viewId: Int)
	{
		// Determine the text of the slider instructions
		val text = if (viewId == snoozeButton.id)
		{
			R.string.description_slide_to_snooze
		}
		else
		{
			R.string.description_slide_to_dismiss
		}

		// Set the instructions
		sliderInstructions.setText(text)
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
		if (shouldUseNfc)
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
		setupAlarmActionButton(snoozeButton, origX=centerAlarmActionX)
	}

	/**
	 * Setup the velocity tracker.
	 */
	@SuppressLint("Recycle")
	private fun setupVelocityTracker(motionEvent: MotionEvent)
	{
		// Reset the velocity tracker
		velocityTracker?.clear()

		// Obtain the current or a new velocity tracker
		velocityTracker = velocityTracker ?: VelocityTracker.obtain()

		// Add movement to the velocity tracker
		velocityTracker?.addMovement(motionEvent)
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
	 * Check if should fling the view with some velocity.
	 */
	private fun shouldFlingView(velocity: Float): Boolean
	{
		return (velocity != 0f)
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
		// Reset the stopped flag
		wasStopped = false

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
		// Set the stopped flag
		wasStopped = true

		// Hide the attention views and stop the animations
		swipeAnimation.hideAttentionViews(snoozeAttentionView, dismissAttentionView)

		// Unregister the time tick receiver
		unregisterMyReceiver(context, timeTickReceiver)

		// Check if the lateinit shared preference listener has been initialized
		if (this::onSharedPreferenceChangedListener.isInitialized)
		{
			// Unregister the shared preference listener
			sharedPreferences.instance.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangedListener)
		}
	}

	companion object
	{

		/**
		 * Minimum fling value.
		 */
		const val FLING_MIN_VALUE = 0f

		/**
		 * Maximum fling value.
		 */
		const val FLING_MAX_VALUE = 4000f

		/**
		 * Scaling factor to make fling easier.
		 */
		const val FLING_SCALE_FACTOR = 2.75f

		/**
		 * Default velocity for a fling if the calculated velocity is too small.
		 */
		const val FLING_DEFAULT_VELOCITY = 750f

		/**
		 * Friction coefficient to slow down fling speed.
		 */
		const val FLING_FRICTION_COEFFICIENT = 1f

	}

}