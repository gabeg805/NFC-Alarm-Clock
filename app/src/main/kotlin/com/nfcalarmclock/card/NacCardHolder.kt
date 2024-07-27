package com.nfcalarmclock.card

import android.animation.Animator
import android.animation.AnimatorInflater
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.format.DateFormat
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.nfcalarmclock.R
import com.nfcalarmclock.activealarm.NacActiveAlarmService.Companion.dismissAlarmService
import com.nfcalarmclock.activealarm.NacActiveAlarmService.Companion.startAlarmService
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.media.NacMedia
import com.nfcalarmclock.name.NacNameDialog
import com.nfcalarmclock.name.NacNameDialog.OnNameEnteredListener
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacCalendar.Day
import com.nfcalarmclock.util.NacUtility.getHeight
import com.nfcalarmclock.util.NacUtility.quickToast
import com.nfcalarmclock.view.dayofweek.NacDayOfWeek
import com.nfcalarmclock.view.dayofweek.NacDayOfWeek.OnWeekChangedListener

//import com.google.android.material.timepicker.MaterialTimePicker;

/**
 * Card view holder.
 */
class NacCardHolder(

	/**
	 * Root view.
	 */
	val root: View

	// Constructor
) : RecyclerView.ViewHolder(root)
{

	/**
	 * Listener for when the alarm options button is clicked.
	 */
	fun interface OnCardAlarmOptionsClickedListener
	{
		fun onCardAlarmOptionsClicked(holder: NacCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when a card is collapsed.
	 */
	fun interface OnCardCollapsedListener
	{
		fun onCardCollapsed(holder: NacCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when the delete button is clicked.
	 */
	fun interface OnCardDeleteClickedListener
	{
		fun onCardDeleteClicked(holder: NacCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when a card is expanded.
	 */
	interface OnCardExpandedListener
	{
		fun onCardExpanded(holder: NacCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when the media button is clicked.
	 */
	fun interface OnCardMediaClickedListener
	{
		fun onCardMediaClicked(holder: NacCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when a card is updated.
	 */
	fun interface OnCardUpdatedListener
	{
		fun onCardUpdated(holder: NacCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when a card will use the flashlight or not is changed.
	 */
	fun interface OnCardUseFlashlightChangedListener
	{
		fun onCardUseFlashlightChanged(holder: NacCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when a card will use NFC or not is changed.
	 */
	fun interface OnCardUseNfcChangedListener
	{
		fun onCardUseNfcChanged(holder: NacCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when a card will vibrate or not is changed.
	 */
	fun interface OnCardUseVibrateChangedListener
	{
		fun onCardUseVibrateChanged(holder: NacCardHolder, alarm: NacAlarm)
	}

	/**
	 * Shared preferences.
	 */
	private val sharedPreferences: NacSharedPreferences = NacSharedPreferences(context)

	/**
	 * Alarm.
	 */
	var alarm: NacAlarm? = null

	/**
	 * Card view.
	 */
	val cardView: CardView = root.findViewById(R.id.nac_card)

	/**
	 * Copy swipe view.
	 */
	val copySwipeView: RelativeLayout = root.findViewById(R.id.nac_swipe_copy)

	/**
	 * Delete swipe view.
	 */
	val deleteSwipeView: RelativeLayout = root.findViewById(R.id.nac_swipe_delete)

	/**
	 * Header view.
	 */
	private val headerView: LinearLayout = root.findViewById(R.id.nac_header)

	/**
	 * Summary view.
	 */
	private val summaryView: LinearLayout = root.findViewById(R.id.nac_summary)

	/**
	 * Dismiss snoozed alarm parent view.
	 */
	private val dismissParentView: LinearLayout = root.findViewById(R.id.nac_dismiss_parent)

	/**
	 * Dismiss snoozed alarm button.
	 */
	private val dismissButton: MaterialButton = root.findViewById(R.id.nac_dismiss)

	/**
	 * Dismiss early alarm button.
	 */
	private val dismissEarlyButton: MaterialButton = root.findViewById(R.id.nac_dismiss_early)

	/**
	 * Extra view.
	 */
	private val extraView: LinearLayout = root.findViewById(R.id.nac_extra)

	/**
	 * On/off switch for an alarm.
	 */
	private val switch: SwitchCompat = root.findViewById(R.id.nac_switch)

	/**
	 * Time parent view.
	 */
	private val timeParentView: LinearLayout = root.findViewById(R.id.nac_time_parent)

	/**
	 * Time text.
	 */
	private val timeView: TextView = root.findViewById(R.id.nac_time)

	/**
	 * Meridian text (AM/PM).
	 */
	private val meridianView: TextView = root.findViewById(R.id.nac_meridian)

	/**
	 * Summary view containing the days to repeat.
	 */
	private val summaryDaysView: TextView = root.findViewById(R.id.nac_summary_days)

	/**
	 * Summary view containing the name of the alarm.
	 */
	private val summaryNameView: TextView = root.findViewById(R.id.nac_summary_name)

	/**
	 * Day of week.
	 */
	private val dayOfWeek: NacDayOfWeek = NacDayOfWeek(root.findViewById(R.id.nac_days))

	/**
	 * Repeat button.
	 */
	private val repeatButton: MaterialButton = root.findViewById(R.id.nac_repeat)

	/**
	 * Vibrate button.
	 */
	private val vibrateButton: MaterialButton = root.findViewById(R.id.nac_vibrate)

	/**
	 * NFC button.
	 */
	val nfcButton: MaterialButton = root.findViewById(R.id.nac_nfc)

	/**
	 * Flashlight button.
	 */
	private val flashlightButton: MaterialButton = root.findViewById(R.id.nac_flashlight)

	/**
	 * Media button.
	 */
	private val mediaButton: MaterialButton = root.findViewById(R.id.nac_media)

	/**
	 * Volume image view.
	 */
	private val volumeImageView: ImageView = root.findViewById(R.id.nac_volume_icon)

	/**
	 * Volume seekbar.
	 */
	private val volumeSeekBar: SeekBar = root.findViewById(R.id.nac_volume_slider)

	/**
	 * Alarm options button.
	 */
	private val alarmOptionsButton: MaterialButton = root.findViewById(R.id.nac_alarm_options)

	/**
	 * Name button.
	 */
	private val nameButton: MaterialButton = root.findViewById(R.id.nac_name)

	/**
	 * Delete button.
	 */
	private val deleteButton: MaterialButton = root.findViewById(R.id.nac_delete)

	/**
	 * Card animator for collapsing and expanding.
	 */
	private val cardAnimator: NacHeightAnimator = NacHeightAnimator(cardView)

	/**
	 * Color animator for animating the background color of the card.
	 */
	private var backgroundColorAnimator: Animator? = null

	/**
	 * Color animator for highlighting the card.
	 */
	private var highlightAnimator: Animator? = null

	///**
	// * Time picker dialog.
	// */
	//private MaterialTimePicker mTimePicker;
	//this.mTimePicker = null;

	/**
	 * Listener for when the alarm options button is clicked.
	 */
	var onCardAlarmOptionsClickedListener: OnCardAlarmOptionsClickedListener? = null

	/**
	 * Listener for when the alarm card is collapsed.
	 */
	var onCardCollapsedListener: OnCardCollapsedListener? = null

	/**
	 * Listener for when the delete button is clicked.
	 */
	var onCardDeleteClickedListener: OnCardDeleteClickedListener? = null

	/**
	 * Listener for when the alarm card is expanded.
	 */
	var onCardExpandedListener: OnCardExpandedListener? = null

	/**
	 * Listener for when the media button is clicked.
	 */
	var onCardMediaClickedListener: OnCardMediaClickedListener? = null

	/**
	 * Listener for when the alarm card is updated.
	 */
	var onCardUpdatedListener: OnCardUpdatedListener? = null

	/**
	 * Listener for when a card will use flashlight or not is changed.
	 */
	var onCardUseFlashlightChangedListener: OnCardUseFlashlightChangedListener? = null

	/**
	 * Listener for when a card will use NFC or not is changed.
	 */
	var onCardUseNfcChangedListener: OnCardUseNfcChangedListener? = null

	/**
	 * Listener for when a card will vibrate or not is changed.
	 */
	var onCardUseVibrateChangedListener: OnCardUseVibrateChangedListener? = null

	/**
	 * The context.
	 */
	val context: Context
		get() = root.context

	/**
	 * The button to collapse the alarm card.
	 */
	private val collapseButton: MaterialButton
		get() = root.findViewById(R.id.nac_collapse)

	/**
	 * The parent view that contains the collapse button.
	 */
	private val collapseParentView: LinearLayout
		get() = root.findViewById(R.id.nac_collapse_parent)

	/**
	 * The button to expand the alarm card.
	 */
	private val expandButton: MaterialButton
		get() = root.findViewById(R.id.nac_expand)

	/**
	 * The other button to expand the alarm card (there are 2).
	 */
	private val expandOtherButton: MaterialButton
		get() = root.findViewById(R.id.nac_expand_other)

	/**
	 * The height when the card is collapsed.
	 */
	private val heightCollapsed: Int
		// Check if the alarm is snoozed or will alarm soon
		get() = if (alarm!!.isSnoozed || alarm!!.willAlarmSoon())
		{
			// Show a little extra space for buttons right beneath the time
			sharedPreferences.cardHeightCollapsedDismiss
		}
		else
		{
			// Normal collapsed height
			sharedPreferences.cardHeightCollapsed
		}

	/**
	 * The height when the card is expanded.
	 */
	private val heightExpanded: Int
		get() = sharedPreferences.cardHeightExpanded

	/**
	 * Check if the alarm is in use.
	 */
	val isAlarmInUse: Boolean
		get() = alarm!!.isInUse

	/**
	 * Check if the alarm card is collapsed.
	 */
	val isCollapsed: Boolean
		get() = (extraView.visibility == View.GONE)
			|| (cardView.measuredHeight == sharedPreferences.cardHeightCollapsed)
			|| (cardView.measuredHeight == sharedPreferences.cardHeightCollapsedDismiss)

	/**
	 * Check if the alarm card is expanded.
	 */
	val isExpanded: Boolean
		get() = (extraView.visibility == View.VISIBLE)
			|| (cardView.measuredHeight == sharedPreferences.cardHeightExpanded)

	/**
	 * Flag indicating that the card view is being bound to an alarm.
	 */
	private var isBinding: Boolean = false

	/**
	 * Constructor.
	 */
	init
	{
		// Set the interpolator for the animator
		cardAnimator.interpolator = AccelerateInterpolator()

		// Initialize the listeners
		initListeners()
	}

	/**
	 * Animate the background color of the card changing to the collapsed color.
	 */
	private fun animateCollapsedBackgroundColor()
	{
		// Set the background color animator
		backgroundColorAnimator = AnimatorInflater.loadAnimator(context,
			R.animator.card_color_collapse)

		// Setup the animator
		backgroundColorAnimator!!.setTarget(cardView)

		// Start the animator
		backgroundColorAnimator!!.start()
	}

	/**
	 * Animate the background color of the card changing to the expanded color.
	 */
	private fun animateExpandedBackgroundColor()
	{
		// Set the background color animator
		backgroundColorAnimator = AnimatorInflater.loadAnimator(context,
			R.animator.card_color_expand)

		// Setup the animator
		backgroundColorAnimator!!.setTarget(cardView)

		// Start the animator
		backgroundColorAnimator!!.start()
	}

	/**
	 * Bind the alarm to the card view.
	 */
	fun bind(alarm: NacAlarm)
	{
		// Set the alarm and binding flag
		this.alarm = alarm
		isBinding = true

		// Hide the swipe views
		hideSwipeViews()
		hideAppearanceSettingViews()

		// Setup the views and colors
		initViews()
		initColors()

		// Unset the binding flag
		isBinding = false
	}

	/**
	 * Call the card collapsed listener.
	 *
	 *
	 * This listener will not get called if the card has not been measured yet.
	 */
	private fun callOnCardCollapsedListener()
	{
		// Check if the card has been measured and is collapsed
		if (sharedPreferences.cardIsMeasured && isCollapsed)
		{
			// Call the listener
			onCardCollapsedListener?.onCardCollapsed(this, alarm!!)
		}
	}

	/**
	 * Call the card expanded listener.
	 *
	 *
	 * This listener will not get called if the card has not been measured yet.
	 */
	private fun callOnCardExpandedListener()
	{
		// Check if the card has been measured and is expanded
		if (sharedPreferences.cardIsMeasured && isExpanded)
		{
			// Call the listener
			onCardExpandedListener?.onCardExpanded(this, alarm!!)
		}
	}

	/**
	 * Call the card updated listener.
	 */
	private fun callOnCardUpdatedListener()
	{
		// Check if the card view is not being bound
		if (!isBinding)
		{
			// Call the listener
			onCardUpdatedListener?.onCardUpdated(this, alarm!!)
		}
	}

	/**
	 * Reset the animator that changes the background color of the alarm card.
	 */
	private fun cancelBackgroundColor()
	{
		// Check if the animator is running
		if (backgroundColorAnimator?.isRunning == true)
		{
			// Cancel the animator
			backgroundColorAnimator!!.cancel()
		}

		// Reset the animator
		backgroundColorAnimator = null
	}

	/**
	 * Cancel the animator that highlights the alarm card.
	 */
	private fun cancelHighlight()
	{
		// Check if the animator is running
		if (highlightAnimator?.isRunning == true)
		{
			// Cancel the animator
			highlightAnimator!!.cancel()
		}

		// Reset the animator
		highlightAnimator = null
	}

	/**
	 * Check if the alarm can be deleted.
	 */
	private fun checkCanDeleteAlarm(): Boolean
	{
		// Alarm is active
		if (alarm!!.isActive)
		{
			// Show toast that unable to delete an active alarm
			quickToast(context, R.string.error_message_active_delete)
		}
		// Alarm is snoozed
		else if (alarm!!.isSnoozed)
		{
			// Show a toast that unable to delete a snoozed alarm
			quickToast(context, R.string.error_message_snoozed_delete)
		}
		// Alarm can be deleted
		else
		{
			return true
		}

		// Unable to delete the alarm
		return false
	}

	/**
	 * Check if the alarm can be modified, and if it cannot, display toasts to the
	 * user indicating as such.
	 *
	 * @return True if the check passed successfully, and the alarm can be
	 *         modified, and False otherwise.
	 */
	private fun checkCanModifyAlarm(): Boolean
	{
		// Card holder is binding
		if (isBinding)
		{
			return true
		}
		// Alarm is active
		else if (alarm!!.isActive)
		{
			// Show a toast that unable to modify an active alarm
			quickToast(context, R.string.error_message_active_modify)
		}
		// Alarm is snoozed
		else if (alarm!!.isSnoozed)
		{
			// Show a toast that unable to modify a snoozed alarm
			quickToast(context, R.string.error_message_snoozed_modify)
		}
		// Alarm can be modified
		else
		{
			return true
		}

		// Unable to modify the alarm
		return false
	}

	/**
	 * Collapse the alarm card.
	 */
	private fun collapse()
	{
		// Cancel the highlight
		cancelHighlight()

		// Setup the animator
		cardAnimator.animationType = NacHeightAnimator.AnimationType.COLLAPSE
		cardAnimator.setHeights(heightExpanded, heightCollapsed)
		cardAnimator.duration = COLLAPSE_DURATION.toLong()

		// Start the animator
		cardAnimator.start()
	}

	/**
	 * Collapse the alarm card after a refresh.
	 */
	private fun collapseRefresh()
	{
		// Card is not collapsed
		if (!isCollapsed)
		{
			return
		}

		// Define the to/from heights
		val fromHeight: Int
		val toHeight: Int

		// Set the from/to heights that the collapse will act on
		if (dismissParentView.visibility == View.VISIBLE)
		{
			fromHeight = sharedPreferences.cardHeightCollapsed
			toHeight = sharedPreferences.cardHeightCollapsedDismiss
		}
		else
		{
			fromHeight = sharedPreferences.cardHeightCollapsedDismiss
			toHeight = sharedPreferences.cardHeightCollapsed
		}

		// Cancel the highlight
		cancelHighlight()

		// Setup the animator
		cardAnimator.animationType = NacHeightAnimator.AnimationType.COLLAPSE
		cardAnimator.setHeights(fromHeight, toHeight)
		cardAnimator.duration = COLLAPSE_DURATION.toLong()

		// Start the animator to animate the collapse
		cardAnimator.start()
	}

	/**
	 * Compare the default color of two ColorStateList objects.
	 *
	 * @return True if the default color is the same, and False otherwise.
	 */
	private fun compareColorStateList(
		oldColor: ColorStateList?,
		newColor: ColorStateList?
	): Boolean
	{
		// Check if the colors are defined
		return if (oldColor == null || newColor == null)
		{
			false
		}
		// Compare the old and new colors
		else
		{
			oldColor.defaultColor == newColor.defaultColor
		}
	}

	/**
	 * Create a ColorStateList object that is blended with the theme color.
	 */
	private fun createBlendedThemeColorStateList(): ColorStateList
	{
		// Blend the theme color
		val themeColor = sharedPreferences.themeColor
		val blendedColor = ColorUtils.blendARGB(themeColor, Color.TRANSPARENT, 0.6f)

		// Return the blended color
		return ColorStateList.valueOf(blendedColor)
	}

	/**
	 * Delete the alarm card.
	 */
	fun delete()
	{
		// Call the listener
		onCardDeleteClickedListener?.onCardDeleteClicked(this, alarm!!)
	}

	/**
	 * Act as if the alarm options button was clicked.
	 */
	private fun doAlarmOptionsButtonClick()
	{
		onCardAlarmOptionsClickedListener?.onCardAlarmOptionsClicked(this, alarm!!)
	}

	/**
	 * Act as if the alarm card was clicked.
	 */
	private fun doCardClick()
	{
		// Collapsed
		if (isCollapsed)
		{
			expand()
		}
		// Expanded
		else if (isExpanded)
		{
			collapse()
		}
		// Unknown
		else
		{
			collapse()
		}
	}

	/**
	 * Collapse the alarm card without any animations.
	 */
	private fun doCollapse()
	{
		// Setup the summary
		summaryView.visibility = View.VISIBLE
		summaryView.isEnabled = true

		// Setup the extra
		extraView.visibility = View.GONE
		extraView.isEnabled = false

		// Refresh dismiss buttons
		refreshDismissAndDismissEarlyButtons()
	}

	/**
	 * Changes the color of the card, in addition to collapsing it.
	 *
	 * @see .doCollapse
	 */
	fun doCollapseWithColor()
	{
		doCollapse()
		setCollapsedBackgroundColor()

		// Reset the height because it wasa showing up as expanded for some
		// reason
		if (sharedPreferences.cardIsMeasured && (heightCollapsed > 0))
		{
			cardView.layoutParams.height = heightCollapsed
		}
	}

	/**
	 * Act as if the day button was clicked.
	 */
	private fun doDayButtonClick(day: Day)
	{
		// Toggle the day
		alarm!!.toggleDay(day)

		// Check if no days are selected
		if (!alarm!!.areDaysSelected)
		{
			// Disable repeat
			alarm!!.repeat = false
		}

		// Setup the views
		setRepeatButton()
		setSummaryDaysView()

		// Call the listener
		callOnCardUpdatedListener()
	}

	/**
	 * Act as if the delete button was clicked.
	 */
	private fun doDeleteButtonClick()
	{
		delete()
	}

	/**
	 * Act as if the dismiss button was clicked.
	 */
	private fun doDismissButtonClick()
	{
		// Check if alarm uses NFC
		if (alarm!!.shouldUseNfc)
		{
			// Start the alarm service
			startAlarmService(context, alarm)
		}
		// Alarm does not require NFC to dismiss the alarm
		else
		{
			// Dismiss the alarm service
			dismissAlarmService(context, alarm)
		}
	}

	/**
	 * Act as if the dismiss early button was clicked.
	 */
	private fun doDismissEarlyButtonClick()
	{
		// Dismiss the alarm early
		alarm!!.dismissEarly()

		// Refresh the dismiss button
		refreshDismissAndDismissEarlyButtons()
		collapseRefresh()

		// Call the listener
		callOnCardUpdatedListener()
	}

	/**
	 * Expand the alarm card without any animations.
	 */
	private fun doExpand()
	{
		// Setup the summary
		summaryView.visibility = View.GONE
		summaryView.isEnabled = false

		// Setup the extra
		extraView.visibility = View.VISIBLE
		extraView.isEnabled = true
	}

	/**
	 * Changes the color of the card, in addition to expanding it.
	 *
	 * @see .doExpand
	 */
	fun doExpandWithColor()
	{
		doExpand()
		setExpandedBackgroundColor()

		// Reset the height because it wasa showing up as expanded for some
		// reason
		if (sharedPreferences.cardIsMeasured && (heightExpanded > 0))
		{
			cardView.layoutParams.height = heightExpanded
		}
	}

	/**
	 * Act as if the flashlight button was clicked.
	 */
	private fun doFlashlightButtonClick()
	{
		// Toggle the flashlight button
		alarm!!.toggleUseFlashlight()

		// Call the listener
		onCardUseFlashlightChangedListener?.onCardUseFlashlightChanged(this, alarm!!)
	}

	/**
	 * Act as if the media button was clicked.
	 */
	private fun doMediaButtonClick()
	{
		onCardMediaClickedListener?.onCardMediaClicked(this, alarm!!)
	}

	/**
	 * Act as if the name was clicked.
	 */
	private fun doNameClick()
	{
		showNameDialog()
	}

	/**
	 * Act as if the NFC button was clicked.
	 */
	fun doNfcButtonClick()
	{
		// Toggle the NFC button
		alarm!!.toggleUseNfc()

		// Check if NFC should not be used
		if (!alarm!!.shouldUseNfc)
		{
			// Clear the NFC tag ID
			alarm!!.nfcTagId = ""
		}

		// Call the listener
		onCardUseNfcChangedListener?.onCardUseNfcChanged(this, alarm!!)
	}

	/**
	 * Act as if the repeat button was clicked.
	 */
	private fun doRepeatButtonClick()
	{
		// Toggle the repeat button
		alarm!!.toggleRepeat()

		// Call the listener
		callOnCardUpdatedListener()

		// Determine which message to show
		val messageId = if (alarm!!.shouldRepeat)
		{
			R.string.message_repeat_enabled
		}
		else
		{
			R.string.message_repeat_disabled
		}

		// Toast the repeat message
		quickToast(context, messageId)
	}

	/**
	 * Act as if the repeat button was long clicked.
	 */
	private fun doRepeatButtonLongClick()
	{
		// Disable the repeat button
		alarm!!.repeat = false

		// Clear out the selected days
		alarm!!.setDays(0)

		// Setup the views
		setDayOfWeek()
		setRepeatButton()
		setSummaryDaysView()

		// Call the listener
		callOnCardUpdatedListener()
	}

	/**
	 * Act as if the switch was changed.
	 */
	private fun doSwitchCheckedChanged(state: Boolean)
	{
		// Reset the snooze counter
		if (!state)
		{
			alarm!!.snoozeCount = 0
		}

		// Set the alarm enabled state
		alarm!!.isEnabled = state

		// Setup the views
		setSummaryDaysView()

		// Call the listener
		callOnCardUpdatedListener()
	}

	/**
	 * Act as if the time was clicked.
	 */
	private fun doTimeClick()
	{
		showTimeDialog()
	}

	/**
	 * Act as if the vibrate button was clicked.
	 */
	private fun doVibrateButtonClick()
	{
		// Toggle the vibrate button
		alarm!!.toggleVibrate()

		// Call the listener
		onCardUseVibrateChangedListener?.onCardUseVibrateChanged(this, alarm!!)
	}

	/**
	 * Expand the alarm card.
	 */
	private fun expand()
	{
		// Cancel the highlight
		cancelHighlight()

		// Setup the animator
		cardAnimator.animationType = NacHeightAnimator.AnimationType.EXPAND
		cardAnimator.setHeights(heightCollapsed, heightExpanded)
		cardAnimator.duration = EXPAND_DURATION.toLong()

		// Start the animator
		cardAnimator.start()
	}

	/**
	 * @return The meridian color.
	 */
	private fun getMeridianColor(meridian: String): Int
	{
		// Get the AM and PM strings
		val am = context.getString(R.string.am)
		val pm = context.getString(R.string.pm)

		// AM color
		return when (meridian)
		{
			am -> sharedPreferences.amColor
			pm -> sharedPreferences.pmColor
			else -> context.resources.getInteger(R.integer.default_color)
		}
	}

	///**
	// * @return The time picker.
	// */
	//public MaterialTimePicker getTimePicker()
	//{
	//	return this.mTimePicker;
	//}

	/**
	 * Hide the views in the Appearance settings.
	 */
	private fun hideAppearanceSettingViews()
	{
		// Vibrate
		if (!sharedPreferences.shouldShowVibrateButton)
		{
			vibrateButton.visibility = View.GONE
		}

		// NFC
		if (!sharedPreferences.shouldShowNfcButton)
		{
			nfcButton.visibility = View.GONE
		}

		// Flashlight
		if (!sharedPreferences.shouldShowFlashlightButton)
		{
			flashlightButton.visibility = View.GONE
		}
	}

	/**
	 * Hide the swipe views.
	 */
	private fun hideSwipeViews()
	{
		copySwipeView.visibility = View.GONE
		deleteSwipeView.visibility = View.GONE
	}

	/**
	 * Highlight the alarm card.
	 */
	fun highlight()
	{
		// Cancel the background color
		cancelBackgroundColor()

		// Set the animator
		highlightAnimator = AnimatorInflater.loadAnimator(context,
			R.animator.card_color_highlight)

		// Setup the animator
		highlightAnimator!!.setTarget(cardView)

		// Start the animator
		highlightAnimator!!.start()
	}

	/**
	 * Initialize the colors of the various views.
	 */
	private fun initColors()
	{
		setDividerColor()
		setTextViewColor(timeView, sharedPreferences.timeColor)
		setMeridianColor()
		setSwitchColor()
		setTextViewColor(summaryDaysView, sharedPreferences.daysColor)
		setTextViewColor(summaryNameView, sharedPreferences.nameColor)
		this.setMaterialButtonColor(dismissButton)
		setDayOfWeekRippleColor()
		this.setMaterialButtonColor(repeatButton)
		this.setMaterialButtonColor(vibrateButton)
		this.setMaterialButtonColor(nfcButton)
		this.setMaterialButtonColor(flashlightButton)
		this.setMaterialButtonColor(mediaButton)
		setVolumeSeekBarColor()
		this.setMaterialButtonColor(alarmOptionsButton)
		this.setMaterialButtonColor(nameButton)
		this.setMaterialButtonColor(deleteButton)
		this.setMaterialButtonColor(collapseButton)
		this.setMaterialButtonColor(expandButton)
	}

	/**
	 * Initialize the listeners of the various views.
	 */
	private fun initListeners()
	{
		setupCardExpandCollapseClickListeners()
		setupCardAnimatorListener()
		setupTimeClickListener()
		setupSwitchChangedListener()
		setupDismissButtonListener()
		setupDismissEarlyButtonListener()
		setupDayOfWeekChangedListener()
		setupRepeatButtonListener()
		setupRepeatButtonLongClickListener()
		setupVibrateButtonListener()
		setupNfcButtonListener()
		setupFlashlightButtonListener()
		setupMediaButtonListener()
		setupAlarmOptionsListener()
		setupVolumeSeekBarListener()
		setupNameListener()
		setupDeleteButtonListener()
	}

	/**
	 * Initialize the various views.
	 */
	private fun initViews()
	{
		refreshDismissAndDismissEarlyButtons()
		setTimeView()
		setMeridianView()
		setSwitchView()
		setSummaryDaysView()
		setSummaryNameView()
		setDayOfWeek()
		dayOfWeek.setStartWeekOn(sharedPreferences.startWeekOn)
		setRepeatButton()
		setVibrateButton()
		setNfcButton()
		setFlashlightButton()
		setMediaButton()
		setVolumeSeekBar()
		setVolumeImageView()
		setNameButton()
	}

	/**
	 * Interact with an alarm.
	 *
	 * Should be called when an alarm has been newly added.
	 */
	fun interact()
	{
		// Check if the alarm should be expanded
		if (sharedPreferences.expandNewAlarm)
		{
			// Show the time dialog
			showTimeDialog()

			// Expand the alarm
			expand()
		}
	}

	///**
	// * @return True if showing the time picker, and false otherwise.
	// */
	//public boolean isShowingTimePicker()
	//{
	//	MaterialTimePicker timepicker = this.getTimePicker();
	//	return (timepicker != null);
	//}

	/**
	 * Measure the different alarm card heights.
	 *
	 * This will populate the array that is passed in with the corresponding
	 * heights:
	 *
	 * i=0: Collapsed height.
	 * i=1: Collapsed height with the dismiss button shown.
	 * i=2: Expanded height.
	 *
	 * @param  heights  An integer array of 3 elements.
	 */
	fun measureCard(heights: IntArray?)
	{
		// Check if heights is the right length
		if (heights == null || heights.size != 3)
		{
			return
		}

		// Expand the alarm
		doExpand()

		// Save the height
		heights[2] = getHeight(cardView)

		// Collapse the card
		doCollapse()

		// Hide the dismiss button
		dismissParentView.visibility = View.GONE

		// Save the height
		heights[0] = getHeight(cardView)

		// Show the dismiss button
		dismissParentView.visibility = View.VISIBLE

		// Save the height
		heights[1] = getHeight(cardView)

		// Refresh the dismiss and dismiss early buttons
		refreshDismissAndDismissEarlyButtons()
	}

	/**
	 * Perform haptic feedback on a view.
	 */
	private fun performHapticFeedback(view: View)
	{
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
	}

	/**
	 * Check if the dismiss view should be refreshed or not.
	 *
	 * @return True if the dismiss view should be refreshed, and False otherwise.
	 */
	fun shouldRefreshDismissView(): Boolean
	{
		// Alarm is in use, or will alarm soon so the "Dismiss" or "Dismiss early"
		// button should be shown
		val dismissVisibility = if (alarm!!.isInUse || alarm!!.willAlarmSoon())
		{
			View.VISIBLE
		}
		else
		{
			View.GONE
		}

		// The dismiss view is being shown so do not show this view
		val expandVisibility = if (dismissVisibility == View.GONE)
		{
			View.VISIBLE
		}
		else
		{
			View.INVISIBLE
		}

		// The "Dismiss"/"Dismiss early" button OR the "Expand" down-arrow button
		// are NOT the correct and expected visibilities
		return (dismissParentView.visibility != dismissVisibility)
			|| (expandButton.visibility != expandVisibility)
	}

	/**
	 * Set the dismiss view parent, which contains both "Dismiss" and
	 * "Dismiss early" to its proper setting.
	 */
	private fun refreshDismissAndDismissEarlyButtons()
	{

		// Alarm is in use, or will alarm soon so the "Dismiss" or "Dismiss early"
		// button should be shown
		val dismissVis = if (alarm!!.isInUse || alarm!!.willAlarmSoon())
		{
			View.VISIBLE
		}
		else
		{
			View.GONE
		}

		// The dismiss view is being shown so do not show this view
		val expandVis = if (dismissVis == View.GONE)
		{
			View.VISIBLE
		}
		else
		{
			View.INVISIBLE
		}

		// Set the "Dismiss" button visibility
		if (dismissParentView.visibility != dismissVis)
		{
			dismissParentView.visibility = dismissVis
		}

		// Set the "Expand" down-arrow button visibility
		if (expandButton.visibility != expandVis)
		{
			expandButton.visibility = expandVis
		}

		// Set the "Dismiss" and "Dismiss early" visibilities
		if (dismissVis == View.VISIBLE)
		{
			// Alarm is in use
			if (alarm!!.isInUse)
			{
				// Show the "Dismiss" button
				dismissButton.visibility = View.VISIBLE
				dismissEarlyButton.visibility = View.GONE
			}
			// Alarm will alarm soon
			else if (alarm!!.willAlarmSoon())
			{
				// Show the "Dismiss early" button
				dismissButton.visibility = View.GONE
				dismissEarlyButton.visibility = View.VISIBLE
			}
		}
	}

	/**
	 * Set the dismiss view parent, which contains both "Dismiss" and
	 * "Dismiss early" to its proper setting and collapse refresh as well
	 * if it needs to be done.
	 */
	private fun refreshDismissAndDismissEarlyButtonsWithCollapse()
	{
		// Get the visiblity before refreshing the dismiss buttons
		val beforeVisibility = dismissParentView.visibility

		// Refresh dismiss buttons
		refreshDismissAndDismissEarlyButtons()

		// Get the visiblity after refreshing the dismiss buttons
		val afterVisibility = dismissParentView.visibility

		// Determine if the card is already collapsed and the visibility of the
		// dismiss buttons has changed after the new time was set. If so, there
		// is or should be new space because of the dismiss buttons, so do a collapse
		// due to the refresh
		if (isCollapsed && beforeVisibility != afterVisibility)
		{
			collapseRefresh()
		}
	}

	/**
	 * Set the background tint list of a view.
	 */
	private fun setBackgroundTintList(view: View, newColorStateList: ColorStateList)
	{
		// Get the current color
		val colorStateList = view.backgroundTintList

		// Check if the current color and the new color are different
		if (!this.compareColorStateList(colorStateList, newColorStateList))
		{
			// Set the new color
			view.backgroundTintList = newColorStateList
		}
	}

	/**
	 * Set the background color for when the card is collapsed.
	 */
	private fun setCollapsedBackgroundColor()
	{
		// Get the colors
		val grayDark = ContextCompat.getColor(context, R.color.gray_dark)
		val color = MaterialColors.getColor(context, R.attr.colorCard, grayDark)

		// Set the color
		cardView.setBackgroundColor(color)
	}

	/**
	 * Set the day of week to its proper setting.
	 */
	private fun setDayOfWeek()
	{
		// Check if the days are not equal
		if (dayOfWeek.days != alarm!!.days)
		{
			// Set the days
			dayOfWeek.setDays(alarm!!.days)
		}
	}

	/**
	 * Set the ripple color for each day in the day of week view.
	 */
	private fun setDayOfWeekRippleColor()
	{
		// Create the new color
		val newColor = createBlendedThemeColorStateList()

		// Iterate over each day button
		for (day in dayOfWeek.dayButtons)
		{
			// Set the new color
			this.setMaterialButtonColor(day.button, newColor)
		}
	}

	/**
	 * Set the divider color.
	 */
	private fun setDividerColor()
	{
		// Get the dividers
		val headerDivider = root.findViewById<ViewGroup>(R.id.nac_divider_header)
		val deleteDivider = root.findViewById<View>(R.id.nac_divider_delete)

		// Get the color
		val themeColor = ColorStateList.valueOf(sharedPreferences.themeColor)

		// Iterate over each header divider (the 3 dots)
		for (i in 0 until headerDivider.childCount)
		{
			// Get one of the dots
			val view = headerDivider.getChildAt(i)

			// Set the color
			this.setBackgroundTintList(view, themeColor)
		}

		// Set the delete divider color
		this.setBackgroundTintList(deleteDivider, themeColor)
	}

	/**
	 * Set the background color for when the card is expanded.
	 */
	private fun setExpandedBackgroundColor()
	{
		// Get the colors
		val gray = ContextCompat.getColor(context, R.color.gray)
		val color = MaterialColors.getColor(context, R.attr.colorCardExpanded, gray)

		// Set the color
		cardView.setBackgroundColor(color)
	}

	/**
	 * Set the flashlight button to its proper settings.
	 */
	private fun setFlashlightButton()
	{
		// Get the flashlight state from the alarm
		val shouldUseFlashlight = alarm!!.useFlashlight

		// Check if the state of the button and the alarm are different
		if (flashlightButton.isChecked != shouldUseFlashlight)
		{
			// Set the new state of the button
			flashlightButton.isChecked = shouldUseFlashlight
		}
	}

	/**
	 * Set the color of a MaterialButton.
	 *
	 * @param  button  The button to color.
	 * @param  newColor  The new color of the button.
	 */
	private fun setMaterialButtonColor(button: MaterialButton?, newColor: ColorStateList?)
	{
		// Do nothing if the button is null
		if (button == null)
		{
			return
		}

		// Change color if not equal
		if (!this.compareColorStateList(button.rippleColor, newColor))
		{
			button.rippleColor = newColor
		}
	}

	/**
	 * @see .setMaterialButtonColor
	 */
	private fun setMaterialButtonColor(button: MaterialButton?)
	{
		// Do nothing if the button is null
		if (button == null)
		{
			return
		}

		// Create the new color
		val newColor = createBlendedThemeColorStateList()

		// Set the new color
		this.setMaterialButtonColor(button, newColor)
	}

	/**
	 * Set the media button to its proper setting.
	 */
	private fun setMediaButton()
	{
		val mediaPath = alarm!!.mediaPath
		val message : String
		val alpha : Float

		// Check if the media path is not empty
		if (mediaPath.isNotEmpty())
		{
			// Get the message and alpha of the alarm
			message = NacMedia.getTitle(context, mediaPath)
			alpha = 1.0f
		}
		else
		{
			// Get the message and alpha of the alarm
			message = context.resources.getString(R.string.description_media)
			alpha = 0.3f
		}

		// Check if the media button text and the alarm message are different
		if (mediaButton.text != message)
		{
			// Set the new message in the view
			mediaButton.text = message
		}

		// Check if the alpha of the media button is different that what it should be
		if (mediaButton.alpha.compareTo(alpha) != 0)
		{
			// Set the new alpha
			mediaButton.alpha = alpha
		}
	}

	/**
	 * Set the meridian color.
	 */
	private fun setMeridianColor()
	{
		// Get the meridian (AM/PM) of the alarm
		val meridian = alarm!!.getMeridian(context)

		// Get the color based on the meridian
		val color = getMeridianColor(meridian)

		// Set the color
		setTextViewColor(meridianView, color)
	}

	/**
	 * Set the meridian view to its proper setting.
	 */
	private fun setMeridianView()
	{
		// Get the meridian of the alarm
		val meridian = alarm!!.getMeridian(context)

		// Check if the meridian in the view and the alarm are different
		if (meridianView.text != meridian)
		{
			// Set the new meridian in the view
			meridianView.text = meridian
		}
	}

	/**
	 * Set the name button to its proper settings.
	 */
	private fun setNameButton()
	{
		// Get the name message
		val message = alarm!!.nameNormalized.ifEmpty {
			context.resources.getString(R.string.alarm_name)
		}

		// Get the alpha that the view should be
		val alpha = if (alarm!!.nameNormalized.isNotEmpty()) 1.0f else 0.3f

		// Check if the text in the view and alarm are different
		if (nameButton.text != message)
		{
			// Set the new message in the view
			nameButton.text = message
		}

		// Check if the alpha of the view is different than what it should be
		if (nameButton.alpha.compareTo(alpha) != 0)
		{
			// Set the new alpha
			nameButton.alpha = alpha
		}
	}

	/**
	 * Set the NFC button to its proper settings.
	 */
	private fun setNfcButton()
	{
		// Get the NFC state from the alarm
		val shouldUseNfc = alarm!!.shouldUseNfc

		// Check if the state of the button and the alarm are different
		if (nfcButton.isChecked != shouldUseNfc)
		{
			// Set the new state of the button
			nfcButton.isChecked = shouldUseNfc
		}
	}

	/**
	 * Set listener for when a menu item is clicked.
	 */
	fun setOnCreateContextMenuListener(listener: OnCreateContextMenuListener)
	{
		headerView.setOnCreateContextMenuListener(listener)
		summaryView.setOnCreateContextMenuListener(listener)
		timeParentView.setOnCreateContextMenuListener(listener)
	}

	/**
	 * Set the repeat button to its proper setting.
	 */
	private fun setRepeatButton()
	{
		// Get the is enabled and should repeat states from the alarm
		val isEnabled = alarm!!.areDaysSelected
		val shouldRepeat = alarm!!.areDaysSelected && alarm!!.shouldRepeat

		// Check if the repeat button status and the alarm should repeat status are
		// different
		if (repeatButton.isChecked != shouldRepeat)
		{
			// Set the new status in the button
			repeatButton.isChecked = shouldRepeat
		}

		// Check if the repeat button is enabled status and the alarm is enabled status
		// are different
		if (repeatButton.isEnabled != isEnabled)
		{
			// Set the new is enabled status in the button
			repeatButton.isEnabled = isEnabled
		}
	}

	/**
	 * Set the summary days view to its proper setting.
	 */
	private fun setSummaryDaysView()
	{
		// Get the string from the alarm
		val string = Day.alarmToDayString(context, alarm!!, sharedPreferences.startWeekOn)

		// Check if the alarm string and the string in the view are different
		if (summaryDaysView.text != string)
		{
			// Set the new value
			summaryDaysView.text = string
		}
	}

	/**
	 * Set the summary name view to its proper setting.
	 */
	private fun setSummaryNameView()
	{
		// Get the name of the alarm
		val name = alarm!!.nameNormalized

		// Check if the alarm name and the name in the view are different
		if (summaryNameView.text != name)
		{
			// Set the new value
			summaryNameView.text = name
		}
	}

	/**
	 * Set the color of the switch.
	 */
	private fun setSwitchColor()
	{
		// Get the IDs of the two states
		val states = arrayOf(intArrayOf(android.R.attr.state_checked),
			intArrayOf(-android.R.attr.state_checked))

		// Get the colors of the two states
		val themeDark = ColorUtils.blendARGB(sharedPreferences.themeColor, Color.BLACK, 0.6f)
		val thumbColors = intArrayOf(sharedPreferences.themeColor, Color.GRAY)
		val trackColors = intArrayOf(themeDark, Color.DKGRAY)

		// Create the color state lists
		val thumbStateList = ColorStateList(states, thumbColors)
		val trackStateList = ColorStateList(states, trackColors)

		// Set the color
		this.setSwitchColor(switch, thumbStateList, trackStateList)
	}

	/**
	 * Set the thumb and track color of a switch.
	 *
	 * @param  switchView  The switch.
	 * @param  thumbColor  The color of the thumb.
	 * @param  trackColor  The color of the track.
	 */
	private fun setSwitchColor(switchView: SwitchCompat?,
		thumbColor: ColorStateList?, trackColor: ColorStateList?)
	{
		// Check if the view is null
		if (switchView == null)
		{
			return
		}

		// Check if the current color and the new color are different
		if (!this.compareColorStateList(switchView.thumbTintList, thumbColor))
		{
			// Set the new thumb color
			switchView.thumbTintList = thumbColor
		}

		// Check if the current color and the new color are different
		if (!this.compareColorStateList(switchView.trackTintList, trackColor))
		{
			// Set the new track color
			switchView.trackTintList = trackColor
		}
	}

	/**
	 * Set the switch to its proper setting.
	 */
	private fun setSwitchView()
	{
		// Get the state
		val enabled = alarm!!.isEnabled

		// Check if the state of the switch and the alarm are different
		if (switch.isChecked != enabled)
		{
			// Set the new state of the switch
			switch.isChecked = enabled
		}
	}

	/**
	 * Set the color of a TextView.
	 *
	 * @param  textView  The text view to color.
	 * @param  newColor  The new color of the text view.
	 */
	private fun setTextViewColor(textView: TextView?, newColor: Int)
	{
		// Check if the view is null
		if (textView == null)
		{
			return
		}

		// Check if the current color of the textview and the new color are different
		if (textView.currentTextColor != newColor)
		{
			// Set the new color
			textView.setTextColor(newColor)
		}
	}

	///**
	// * Set the time.
	// */
	//public void setTime()
	//{
	//	//if (!this.isShowingTimePicker())
	//	//{
	//	//	return;
	//	//}
	//	MaterialTimePicker timepicker = this.getTimePicker();
	//	NacAlarm alarm = this.getAlarm();
	//	int hr = timepicker.getHour();
	//	int min = timepicker.getMinute();
	//	alarm.setHour(hr);
	//	alarm.setMinute(min);
	//	alarm.setIsEnabled(true);
	//	//alarm.changed();
	//	this.setTimeView();
	//	this.setMeridianView();
	//	this.setMeridianColor();
	//	this.setSwitchView();
	//	this.setSummaryDaysView();
	//	this.callOnCardUpdatedListener();
	//}

	/**
	 * Set the time view to its proper setting.
	 */
	private fun setTimeView()
	{
		// Get the current time
		val time = alarm!!.getClockTime(context)

		// Check if the time in the view and the alarm time are different
		if (timeView.text != time)
		{
			// Set the new time
			timeView.text = time
		}
	}

	/**
	 * Set the vibrate button to its proper setting.
	 */
	private fun setVibrateButton()
	{
		// Get the vibrate status
		val shouldVibrate = alarm!!.shouldVibrate

		// Check if the vibrate button and the alarm vibrate status are different
		if (vibrateButton.isChecked != shouldVibrate)
		{
			// Set the new status in the button
			vibrateButton.isChecked = shouldVibrate
		}
	}

	/**
	 * Set the volume image view.
	 */
	private fun setVolumeImageView()
	{
		// Set the resource ID depending on the volume level
		val resId: Int = when (alarm!!.volume)
		{

			0 ->
			{
				R.drawable.volume_off
			}

			in 1..33 ->
			{
				R.drawable.volume_low
			}

			in 34..66 ->
			{
				R.drawable.volume_med
			}

			else ->
			{
				R.drawable.volume_high
			}

		}

		// Check if the view tag is not set or not equal to the resource ID
		if (volumeImageView.tag == null || volumeImageView.tag as Int != resId)
		{
			// Set the new resource ID and tag
			volumeImageView.setImageResource(resId)
			volumeImageView.tag = resId
		}
	}

	/**
	 * Set the volume seekbar.
	 */
	private fun setVolumeSeekBar()
	{
		// Get the current volume level
		val volume = alarm!!.volume

		// Check if the volume seekbar and the alarm volume are different
		if (volumeSeekBar.progress != volume)
		{
			// Set the new volume level in the view
			volumeSeekBar.progress = volume
		}
	}

	/**
	 * Set the volume seekbar color.
	 */
	private fun setVolumeSeekBarColor()
	{
		// Create the new color
		val newColor = ColorStateList.valueOf(sharedPreferences.themeColor)

		// Progress bar
		if (!this.compareColorStateList(volumeSeekBar.progressTintList, newColor))
		{
			// Set the color
			volumeSeekBar.progressTintList = newColor
		}

		// Thumb
		if (!this.compareColorStateList(volumeSeekBar.thumbTintList, newColor))
		{
			// Set the color
			volumeSeekBar.thumbTintList = newColor
		}
	}

	/**
	 * Setup the alarm options button listener.
	 */
	private fun setupAlarmOptionsListener()
	{
		// Set the listener
		alarmOptionsButton.setOnClickListener { view ->

			// Check if the alarm can be modified
			if (checkCanModifyAlarm())
			{
				// Do the alarm options button click
				doAlarmOptionsButtonClick()
			}

			// Haptic feedback
			performHapticFeedback(view)

		}
	}

	/**
	 * Setup the card animator listener for when the height of the card is being
	 * animated.
	 */
	private fun setupCardAnimatorListener()
	{
		// Collapse listener
		cardAnimator.onAnimateCollapseListener = NacHeightAnimator.OnAnimateCollapseListener { animator ->

			// Check if this is the last update of the animation
			if (animator.isLastUpdate)
			{
				// Quickly change view visibility, no animations
				doCollapse()

				// Check if the card was already collapsed, in which this would not need
				// to be run
				if ((animator.fromHeight != sharedPreferences.cardHeightCollapsed)
					&& (animator.fromHeight != sharedPreferences.cardHeightCollapsedDismiss))
				{
					// Card was not already collapsed. Animate the background color
					animateCollapsedBackgroundColor()
				}

				// Call the listener
				callOnCardCollapsedListener()
			}

		}

		// Expand listener
		cardAnimator.onAnimateExpandListener = NacHeightAnimator.OnAnimateExpandListener { animator ->

			// This is the first update of the animator
			if (animator.isFirstUpdate)
			{
				// Expand the card
				doExpand()

				// Animate the background color
				animateExpandedBackgroundColor()

				// Call the listener
				callOnCardExpandedListener()
			}

		}
	}

	/**
	 * Setup main card view on click listeners that will expand and collapse the card.
	 */
	private fun setupCardExpandCollapseClickListeners()
	{
		// id == R.id.nac_header
		// id == R.id.nac_summary
		// id == R.id.nac_dismiss_parent
		// id == R.id.nac_expand
		// id == R.id.nac_expand_other
		// id == R.id.nac_collapse
		// id == R.id.nac_collapse_parent

		// List of all the views that need the same on click listener for the card
		val allCardViews = listOf(headerView, summaryView, dismissParentView,
			expandButton, expandOtherButton, collapseButton, collapseParentView)

		// Iterate over all the main card views that will control expanding and
		// collapsing the alarm
		for (view in allCardViews)
		{

			// Expand/collapse the alarm
			view.setOnClickListener {

				// Do the card click
				doCardClick()

				// Haptic feedback
				performHapticFeedback(view)
			}

		}
	}

	/**
	 * Setup the listener for when the day of week is changed.
	 */
	private fun setupDayOfWeekChangedListener()
	{
		// Set the listener
		dayOfWeek.onWeekChangedListener = OnWeekChangedListener { button, day ->

			// Check if the alarm can be modified
			if (checkCanModifyAlarm())
			{
				// Do the button click
				doDayButtonClick(day)
			}
			// Alarm cannot be modified
			else
			{
				// Revert the button press
				button.toggle()
			}

			// Haptic feedback
			performHapticFeedback(button)

		}
	}

	/**
	 * Setup the delete button listener.
	 */
	private fun setupDeleteButtonListener()
	{
		// Set the listener
		deleteButton.setOnClickListener { view ->

			// Check if the alarm can be deleted
			if (checkCanDeleteAlarm())
			{
				// Do the button click
				doDeleteButtonClick()
			}

			// Haptic feedback
			performHapticFeedback(view)

		}
	}

	/**
	 * Setup the dismiss button listener.
	 */
	private fun setupDismissButtonListener()
	{
		// Set the listener
		dismissButton.setOnClickListener { view ->

			// Do the button click
			doDismissButtonClick()

			// Haptic feedback
			performHapticFeedback(view)

		}
	}

	/**
	 * Setup the dismiss early button listener.
	 */
	private fun setupDismissEarlyButtonListener()
	{
		// Set the listener
		dismissEarlyButton.setOnClickListener { view ->

			// Do the button click
			doDismissEarlyButtonClick()

			// Haptic feedback
			performHapticFeedback(view)

		}
	}

	/**
	 * Setup the listener for the flashlight button.
	 *
	 * TODO: Can I change this to addOnCheckedChangeListener?
	 */
	private fun setupFlashlightButtonListener()
	{
		// Set the listener
		flashlightButton.setOnClickListener { view ->

			// Check if the alarm can be modified
			if (checkCanModifyAlarm())
			{
				// Do the button click
				doFlashlightButtonClick()
			}
			// Alarm cannot be modified
			else
			{
				// Revert the button press
				val flashlightButton = view as MaterialButton
				flashlightButton.isChecked = !flashlightButton.isChecked
			}

			// Haptic feedback
			performHapticFeedback(view)

		}
	}

	/**
	 * Setup the listener for the media button.
	 */
	private fun setupMediaButtonListener()
	{
		// Set the listener
		mediaButton.setOnClickListener { view ->

			// Check if the alarm can be modified
			if (checkCanModifyAlarm())
			{
				// Do the button click
				doMediaButtonClick()
			}

			// Haptic feedback
			performHapticFeedback(view)

		}
	}

	/**
	 * Setup the name button listener.
	 */
	private fun setupNameListener()
	{
		// Set the listener
		nameButton.setOnClickListener { view ->

			// Check if the alarm can be modified
			if (checkCanModifyAlarm())
			{
				// Do the name click
				doNameClick()
			}

			// Haptic feedback
			performHapticFeedback(view)

		}
	}

	/**
	 * Setup the listener for the NFC button.
	 *
	 * TODO: Can I change this to addOnCheckedChangeListener?
	 */
	private fun setupNfcButtonListener()
	{
		// Set the listener
		nfcButton.setOnClickListener { view ->

			// Check if the alarm can be modified
			if (checkCanModifyAlarm())
			{
				// Do the button click
				doNfcButtonClick()
			}
			// Alarm cannot be modified
			else
			{
				// Revert the button press
				val nfcButton = view as MaterialButton
				nfcButton.isChecked = !nfcButton.isChecked
			}

			// Haptic feedback
			performHapticFeedback(view)

		}
	}

	/**
	 * Setup the listener for when the repeat button is clicked.
	 */
	private fun setupRepeatButtonListener()
	{
		// Set the listener
		repeatButton.setOnClickListener { view ->

			// Check if the alarm can be modified
			if (checkCanModifyAlarm())
			{
				// Do the button click
				doRepeatButtonClick()
			}
			// Alarm cannot be modified
			else
			{
				// Revert the button press
				val repeatButton = view as MaterialButton
				repeatButton.isChecked = !repeatButton.isChecked
			}

			// Haptic feedback
			performHapticFeedback(view)

		}
	}

	/**
	 * Setup the listener for when the repeat button is long clicked.
	 */
	private fun setupRepeatButtonLongClickListener()
	{
		// Set the listener
		repeatButton.setOnLongClickListener {

			// Check if the alarm can be modified
			if (checkCanModifyAlarm())
			{
				// Do the button long click
				doRepeatButtonLongClick()
			}

			true
		}
	}

	/**
	 * Setup the listener for when the switch is changed.
	 */
	private fun setupSwitchChangedListener()
	{
		// Set the listener
		switch.setOnCheckedChangeListener { button, state ->

			// Check if the alarm can be modified
			if (checkCanModifyAlarm())
			{
				// Do the switch state change
				doSwitchCheckedChanged(state)

				// Refresh dismiss buttons and collapse refresh if need be
				refreshDismissAndDismissEarlyButtonsWithCollapse()
			}
			// Alarm cannot be modified
			else
			{
				// Change the state back
				button.isChecked = !state
			}

			// Haptic feedback
			performHapticFeedback(button)

		}
	}

	/**
	 * Setup the listener for when the time is clicked.
	 */
	private fun setupTimeClickListener()
	{
		// Set the listener
		timeParentView.setOnClickListener { view ->

			// Check if the alarm can be modified
			if (checkCanModifyAlarm())
			{
				// Do the time click
				doTimeClick()
			}
			// Unable to modify the alarm
			else
			{
				// Haptic feedback
				performHapticFeedback(view)
			}

		}
	}

	/**
	 * Setup the listener for the vibrate button.
	 */
	private fun setupVibrateButtonListener()
	{
		// Set the listener
		vibrateButton.setOnClickListener { view ->

			// Check if the alarm can be modified
			if (checkCanModifyAlarm())
			{
				// Do the button click
				doVibrateButtonClick()
			}
			// Unable to modify the alarm
			else
			{
				// Change the state back
				val vibrateButton = view as MaterialButton
				vibrateButton.isChecked = !vibrateButton.isChecked
			}

			// Haptic feedback
			performHapticFeedback(view)

		}
	}

	/**
	 * Setup the listener for when the volume slider is changed.
	 */
	private fun setupVolumeSeekBarListener()
	{
		// Set the listener
		volumeSeekBar.setOnSeekBarChangeListener(object: OnSeekBarChangeListener {

			/**
			 * Called when the progress is changed.
			 */
			override fun onProgressChanged(seekBar: SeekBar, progress: Int,
				fromUser: Boolean)
			{
				// Do nothing if the volumes are already the same
				if (alarm!!.volume == progress)
				{
					return
				}

				// Alarm can be changed
				if (checkCanModifyAlarm())
				{
					// Set the new volume
					alarm!!.volume = progress

					// Change the volume icon, if needed
					setVolumeImageView()
				}
				// Alarm cannot be changed
				else
				{
					// Revert the volume back to what it was before
					seekBar.progress = alarm!!.volume
				}
			}

			/**
			 * Called when the seekbar is touched.
			 */
			override fun onStartTrackingTouch(seekBar: SeekBar)
			{
			}

			/**
			 * Called when the seekbar is no longer touched.
			 */
			override fun onStopTrackingTouch(seekBar: SeekBar)
			{
				// Unable to update the alarm. It is currently in use (active or snoozed)
				if (alarm!!.isInUse)
				{
					return
				}

				// Update the card
				callOnCardUpdatedListener()
			}

		})
	}

	/**
	 * Show the name dialog.
	 */
	private fun showNameDialog()
	{
		// Get the fragment manager
		val manager = (context as AppCompatActivity).supportFragmentManager

		// Create the dialog
		val dialog = NacNameDialog()

		// Setup the default name
		dialog.defaultName = alarm!!.name

		// Setup the listener
		dialog.onNameEnteredListener = OnNameEnteredListener { name ->
			// Set the alarm name
			alarm!!.name = name

			// Setup the views
			setNameButton()
			setSummaryNameView()

			// Call the listener
			callOnCardUpdatedListener()
		}

		// Show the dialog
		dialog.show(manager, NacNameDialog.TAG)
	}

	/**
	 * Show the time picker dialog.
	 */
	private fun showTimeDialog()
	{
		// Create the listener
		val listener = OnTimeSetListener { _, hr, min ->

			// Set the alarm attributes
			alarm!!.hour = hr
			alarm!!.minute = min
			alarm!!.isEnabled = true

			// Setup the views
			setTimeView()
			setMeridianView()
			setMeridianColor()
			setSwitchView()
			setSummaryDaysView()

			// Refresh dismiss buttons and collapse refresh if need be
			refreshDismissAndDismissEarlyButtonsWithCollapse()

			// Call the card updated listener
			callOnCardUpdatedListener()

		}

		// Get whether 24 hour format should be used
		val is24HourFormat = DateFormat.is24HourFormat(context)

		// Create the dialog
		val dialog = TimePickerDialog(context, listener, alarm!!.hour, alarm!!.minute,
			is24HourFormat)

		// Show the dialog
		dialog.show()

		//FragmentManager fragmentManager = ((AppCompatActivity)context)
		//	.getSupportFragmentManager();
		//MaterialTimePicker timepicker = new MaterialTimePicker.Builder()
		//	.setHour(hour)
		//	.setMinute(minute)
		//	.setTimeFormat(is24HourFormat ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H)
		//	.build();

		//timepicker.addOnPositiveButtonClickListener(this);
		//timepicker.show(fragmentManager, "TimePicker");

		//this.mTimePicker = timepicker;
	}

	companion object
	{

		/**
		 * Collapse duration.
		 */
		private const val COLLAPSE_DURATION = 250

		/**
		 * Expand duration.
		 */
		private const val EXPAND_DURATION = 250

	}

}