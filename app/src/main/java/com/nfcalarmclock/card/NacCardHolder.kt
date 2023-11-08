package com.nfcalarmclock.card

import android.animation.Animator
import android.animation.AnimatorInflater
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.nfcalarmclock.R
import com.nfcalarmclock.activealarm.NacActiveAlarmService
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.card.NacHeightAnimator.OnAnimateHeightListener
import com.nfcalarmclock.name.NacNameDialog
import com.nfcalarmclock.name.NacNameDialog.OnNameEnteredListener
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.util.NacCalendar.Day
import com.nfcalarmclock.util.NacContext.dismissAlarmActivity
import com.nfcalarmclock.util.NacUtility.getHeight
import com.nfcalarmclock.util.NacUtility.quickToast
import com.nfcalarmclock.view.dayofweek.NacDayButton
import com.nfcalarmclock.view.dayofweek.NacDayOfWeek
import com.nfcalarmclock.view.dayofweek.NacDayOfWeek.OnWeekChangedListener
//import com.google.android.material.timepicker.MaterialTimePicker;

/**
 * Card view holder.
 */
class NacCardHolder(

	private val root: View

	// Constructor
) : RecyclerView.ViewHolder(root),

	// Interfaces
	View.OnClickListener,
	OnLongClickListener,
	CompoundButton.OnCheckedChangeListener,
	OnTimeSetListener,
	OnNameEnteredListener,
	OnWeekChangedListener,
	OnSeekBarChangeListener,
	OnAnimateHeightListener
{

	/**
	 * Listener for when the audio options button is clicked.
	 */
	interface OnCardAudioOptionsClickedListener
	{
		fun onCardAudioOptionsClicked(holder: NacCardHolder?, alarm: NacAlarm?)
	}

	/**
	 * Listener for when a card is collapsed.
	 */
	interface OnCardCollapsedListener
	{
		fun onCardCollapsed(holder: NacCardHolder?, alarm: NacAlarm?)
	}

	/**
	 * Listener for when the delete button is clicked.
	 */
	interface OnCardDeleteClickedListener
	{
		fun onCardDeleteClicked(holder: NacCardHolder?, alarm: NacAlarm?)
	}

	/**
	 * Listener for when a card is expanded.
	 */
	interface OnCardExpandedListener
	{
		fun onCardExpanded(holder: NacCardHolder?, alarm: NacAlarm?)
	}

	/**
	 * Listener for when the media button is clicked.
	 */
	interface OnCardMediaClickedListener
	{
		fun onCardMediaClicked(holder: NacCardHolder?, alarm: NacAlarm?)
	}

	/**
	 * Listener for when a card is updated.
	 */
	interface OnCardUpdatedListener
	{
		fun onCardUpdated(holder: NacCardHolder?, alarm: NacAlarm?)
	}

	/**
	 * Listener for when a card will use NFC or not is changed.
	 */
	interface OnCardUseNfcChangedListener
	{
		fun onCardUseNfcChanged(holder: NacCardHolder?, alarm: NacAlarm?)
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
	val dismissParentView: LinearLayout = root.findViewById(R.id.nac_dismiss_parent)

	/**
	 * Dismiss snoozed alarm button.
	 */
	val dismissButton: MaterialButton = root.findViewById(R.id.nac_dismiss)

	/**
	 * Dismiss early alarm button.
	 */
	val dismissEarlyButton: MaterialButton = root.findViewById(R.id.nac_dismiss_early)

	/**
	 * Extra view.
	 */
	val extraView: LinearLayout = root.findViewById(R.id.nac_extra)

	/**
	 * On/off switch for an alarm.
	 */
	val switch: SwitchCompat = root.findViewById(R.id.nac_switch)

	/**
	 * Time parent view.
	 */
	val timeParentView: LinearLayout = root.findViewById(R.id.nac_time_parent)

	/**
	 * Time text.
	 */
	val timeView: TextView = root.findViewById(R.id.nac_time)

	/**
	 * Meridian text (AM/PM).
	 */
	val meridianView: TextView = root.findViewById(R.id.nac_meridian)

	/**
	 * Summary view containing the days to repeat.
	 */
	val summaryDaysView: TextView = root.findViewById(R.id.nac_summary_days)

	/**
	 * Summary view containing the name of the alarm.
	 */
	val summaryNameView: TextView = root.findViewById(R.id.nac_summary_name)

	/**
	 * Day of week.
	 */
	private val dayOfWeek: NacDayOfWeek = NacDayOfWeek(root.findViewById(R.id.nac_days))

	/**
	 * Repeat button.
	 */
	val repeatButton: MaterialButton = root.findViewById(R.id.nac_repeat)

	/**
	 * Vibrate button.
	 */
	private val vibrateButton: MaterialButton = root.findViewById(R.id.nac_vibrate)

	/**
	 * NFC button.
	 */
	val nfcButton: MaterialButton = root.findViewById(R.id.nac_nfc)

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
	 * Audio options button.
	 */
	val audioOptionsButton: MaterialButton = root.findViewById(R.id.nac_audio_options)

	/**
	 * Name button.
	 */
	val nameButton: MaterialButton = root.findViewById(R.id.nac_name)

	/**
	 * Delete button.
	 */
	val deleteButton: MaterialButton = root.findViewById(R.id.nac_delete)

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
	 * Listener for when the audio options button is clicked.
	 */
	var onCardAudioOptionsClickedListener: OnCardAudioOptionsClickedListener? = null

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
	 * Listener for when a card will use NFC or not is changed.
	 */
	var onCardUseNfcChangedListener: OnCardUseNfcChangedListener? = null

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
	 * Constructor.
	 */
	init
	{
		// Set the interpolator for the animator
		cardAnimator.interpolator = AccelerateInterpolator()
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
			onCardCollapsedListener?.onCardCollapsed(this, alarm)
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
			onCardExpandedListener?.onCardExpanded(this, alarm)
		}
	}

	/**
	 * Call the card updated listener.
	 */
	private fun callOnCardUpdatedListener()
	{
		onCardUpdatedListener?.onCardUpdated(this, alarm)
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
	 * @see .checkCanModifyAlarm
	 */
	fun checkCanDeleteAlarm(): Boolean
	{
		// Alarm is active
		if (alarm!!.isActive)
		{
			toastDeleteActiveAlarmError()
		}
		// Alarm is snoozed
		else if (alarm!!.isSnoozed)
		{
			toastDeleteSnoozedAlarmError()
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
	fun checkCanModifyAlarm(): Boolean
	{
		// Alarm is active
		if (alarm!!.isActive)
		{
			toastModifyActiveAlarmError()
		}
		// Alarm is snoozed
		else if (alarm!!.isSnoozed)
		{
			toastModifySnoozedAlarmError()
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
	fun collapse()
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
	fun collapseRefresh()
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
	fun compareColorStateList(oldColor: ColorStateList?,
		newColor: ColorStateList?): Boolean
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
	 * Create a ColorStateList object from the theme color.
	 */
	private fun createThemeColorStateList(): ColorStateList
	{
		// Get the theme color
		val themeColor = sharedPreferences.themeColor

		// Return the theme color
		return ColorStateList.valueOf(themeColor)
	}

	/**
	 * Delete the alarm card.
	 */
	fun delete()
	{
		// Call the listener
		onCardDeleteClickedListener?.onCardDeleteClicked(this, alarm)
	}

	/**
	 * Act as if the audio options button was clicked.
	 */
	private fun doAudioOptionsButtonClick()
	{
		onCardAudioOptionsClickedListener?.onCardAudioOptionsClicked(this, alarm)
	}

	/**
	 * Act as if the alarm card was clicked.
	 */
	fun doCardClick()
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
	fun doCollapse()
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
	 * Act as if the day button was clicked.
	 */
	fun doDayButtonClick(day: Day?)
	{
		// Toggle the day
		alarm!!.toggleDay(day!!)

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
	fun doDeleteButtonClick()
	{
		delete()
	}

	/**
	 * Act as if the dismiss button was clicked.
	 */
	fun doDismissButtonClick()
	{
		// Dismiss the alarm activity
		dismissAlarmActivity(context, alarm)
	}

	/**
	 * Act as if the dismiss early button was clicked.
	 */
	fun doDismissEarlyButtonClick()
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
	fun doExpand()
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
	}

	/**
	 * Act as if the media button was clicked.
	 */
	fun doMediaButtonClick()
	{
		onCardMediaClickedListener?.onCardMediaClicked(this, alarm)
	}

	/**
	 * Act as if the name was clicked.
	 */
	fun doNameClick()
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

			// Toast the NFC message
			toastNfc()
		}

		// Call the listeners
		callOnCardUpdatedListener()
		onCardUseNfcChangedListener?.onCardUseNfcChanged(this, alarm)
	}

	/**
	 * Act as if the repeat button was clicked.
	 */
	fun doRepeatButtonClick()
	{
		// Toggle the repeat button
		alarm!!.toggleRepeat()

		// Call the listener
		callOnCardUpdatedListener()

		// Toast message
		toastRepeat()
	}

	/**
	 * Act as if the repeat button was long clicked.
	 */
	fun doRepeatButtonLongClick()
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
	fun doSwitchCheckedChanged(state: Boolean)
	{
		// Check if the alarm is disabled and the alarm is in use
		if (!state && alarm!!.isInUse)
		{
			// Dismiss the alarm service
			NacActiveAlarmService.dismissService(context, alarm)

			// Set the alarm as NOT active
			alarm!!.isActive = false
		}

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
	fun doTimeClick()
	{
		showTimeDialog()
	}

	/**
	 * Act as if the vibrate button was clicked.
	 */
	fun doVibrateButtonClick()
	{
		// Toggle the vibrate button
		alarm!!.toggleVibrate()

		// Call the listener
		callOnCardUpdatedListener()

		// Toast message
		toastVibrate()
	}

	/**
	 * Expand the alarm card.
	 */
	fun expand()
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
	 * Initialize the alarm card.
	 */
	fun init(alarm: NacAlarm)
	{
		this.alarm = alarm

		// Unset the listeners
		initListeners(null)

		// Setup the views and colors
		initViews()
		initColors()

		// Set the listeners again
		initListeners(this)
	}

	/**
	 * Initialize the colors of the various views.
	 */
	fun initColors()
	{
		setDividerColor()
		setTimeColor()
		setMeridianColor()
		setSwitchColor()
		setSummaryDaysColor()
		setSummaryNameColor()
		setDismissButtonRippleColor()
		setDayOfWeekRippleColor()
		setRepeatButtonRippleColor()
		setVibrateButtonRippleColor()
		setNfcButtonRippleColor()
		setMediaButtonRippleColor()
		setVolumeSeekBarColor()
		setAudioOptionsButtonRippleColor()
		setNameButtonRippleColor()
		setDeleteButtonRippleColor()
		setCollapseButtonRippleColor()
		setExpandButtonRippleColor()
	}

	/**
	 * Initialize the listeners of the various views.
	 */
	fun initListeners(listener: Any?)
	{
		val click = listener as View.OnClickListener?
		val longClick = listener as OnLongClickListener?
		val dow = listener as OnWeekChangedListener?
		val compound = listener as CompoundButton.OnCheckedChangeListener?
		val seek = listener as OnSeekBarChangeListener?
		val height = listener as OnAnimateHeightListener?

		// Hide the swipe views
		hideSwipeViews()

		// Set the listeners
		cardAnimator.onAnimateHeightListener = height
		headerView.setOnClickListener(click)
		summaryView.setOnClickListener(click)
		timeParentView.setOnClickListener(click)
		switch.setOnCheckedChangeListener(compound)
		dismissParentView.setOnClickListener(click)
		dismissButton.setOnClickListener(click)
		dismissEarlyButton.setOnClickListener(click)
		expandButton.setOnClickListener(click)
		expandOtherButton.setOnClickListener(click)
		collapseButton.setOnClickListener(click)
		collapseParentView.setOnClickListener(click)
		dayOfWeek.onWeekChangedListener = dow
		repeatButton.setOnClickListener(click)
		repeatButton.setOnLongClickListener(longClick)
		vibrateButton.setOnClickListener(click)
		nfcButton.setOnClickListener(click)
		mediaButton.setOnClickListener(click)
		audioOptionsButton.setOnClickListener(click)
		volumeSeekBar.setOnSeekBarChangeListener(seek)
		nameButton.setOnClickListener(click)
		deleteButton.setOnClickListener(click)
	}

	/**
	 * Initialize the various views.
	 */
	fun initViews()
	{
		refreshDismissAndDismissEarlyButtons()
		setTimeView()
		setMeridianView()
		setSwitchView()
		setSummaryDaysView()
		setSummaryNameView()
		setDayOfWeek()
		setStartWeekOn()
		setRepeatButton()
		setVibrateButton()
		setNfcButton()
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
		// Show the time dialog
		showTimeDialog()

		// Check if the alarm should be expanded
		if (sharedPreferences.expandNewAlarm)
		{
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
	 * Called when the card is collapsing.
	 *
	 * Used to set view visibility, animate the background color, and call the
	 * card collapsed listener.
	 */
	override fun onAnimateCollapse(animator: NacHeightAnimator)
	{
		if (animator.isLastUpdate)
		{
			// Quickly change view visibility, no animations
			doCollapse()

			// Check if the card was already collapsed, in which this would not need
			// to be run
			val shared = sharedPreferences
			val fromHeight = animator.fromHeight
			if (fromHeight != shared.cardHeightCollapsed && fromHeight != shared.cardHeightCollapsedDismiss)
			{
				// Card was not already collapsed. Animate the background color
				animateCollapsedBackgroundColor()
			}

			// Call the listener
			callOnCardCollapsedListener()
		}
	}

	/**
	 * Called when the card is expanding.
	 *
	 *
	 * Used to set view visibility, animate the background color, and call the
	 * card collapsed listener.
	 */
	override fun onAnimateExpand(animator: NacHeightAnimator)
	{
		if (animator.isFirstUpdate)
		{
			doExpand()
			animateExpandedBackgroundColor()
			callOnCardExpandedListener()
		}
	}

	/**
	 * A switch has changed state.
	 */
	override fun onCheckedChanged(button: CompoundButton, state: Boolean)
	{
		val id = button.id

		// Switch that enables/disables an alarm
		if (id == R.id.nac_switch)
		{
			respondToSwitchCheckedChanged(button, state)
		}
	}

	/**
	 * A day button was selected.
	 */
	override fun onWeekChanged(button: NacDayButton, day: Day)
	{
		respondToDayButtonClick(button, day)
	}

	/**
	 * A view was clicked.
	 */
	override fun onClick(view: View)
	{
		val id = view.id

		// Expand/collapse the alarm
		if (id == R.id.nac_header || id == R.id.nac_summary || id == R.id.nac_dismiss_parent || id == R.id.nac_expand || id == R.id.nac_expand_other || id == R.id.nac_collapse || id == R.id.nac_collapse_parent)
		{
			respondToCardClick(view)
		} else if (id == R.id.nac_time_parent)
		{
			respondToTimeClick(view)
		} else if (id == R.id.nac_repeat)
		{
			respondToRepeatButtonClick(view)
		} else if (id == R.id.nac_vibrate)
		{
			respondToVibrateButtonClick(view)
		} else if (id == R.id.nac_nfc)
		{
			respondToNfcButtonClick(view)
		} else if (id == R.id.nac_media)
		{
			respondToMediaButtonClick(view)
		} else if (id == R.id.nac_audio_options)
		{
			respondToAudioOptionsButtonClick(view)
		} else if (id == R.id.nac_name)
		{
			respondToNameClick(view)
		} else if (id == R.id.nac_delete)
		{
			respondToDeleteButtonClick(view)
		} else if (id == R.id.nac_dismiss)
		{
			respondToDismissButtonClick(view)
		} else if (id == R.id.nac_dismiss_early)
		{
			respondToDismissEarlyButtonClick(view)
		}
		//else if (this.isShowingTimePicker())
		//{
		//	this.setTime();
		//	this.mTimePicker = null;
		//}
	}

	/**
	 * Notify alarm listener that the alarm has been modified.
	 */
	override fun onNameEntered(name: String)
	{
		val alarm = alarm
		alarm!!.name = name
		//alarm.changed();
		setNameButton()
		setSummaryNameView()
		callOnCardUpdatedListener()
	}

	/**
	 */
	override fun onLongClick(view: View): Boolean
	{
		val id = view.id

		// Repeat button
		if (id == R.id.nac_repeat)
		{
			respondToRepeatButtonLongClick()
		}
		return true
	}

	/**
	 */
	override fun onProgressChanged(seekBar: SeekBar, progress: Int,
		fromUser: Boolean)
	{
		val alarm = alarm
		val alarmVolume = alarm!!.volume

		// Do nothing if the volumes are already the same
		if (alarmVolume == progress)
		{
			return
		}

		// Volume can be changed since the alarm can be modified
		if (checkCanModifyAlarm())
		{
			alarm.volume = progress
			setVolumeImageView()
		} else
		{
			seekBar.progress = alarm.volume
		}
	}

	/**
	 */
	override fun onStartTrackingTouch(seekBar: SeekBar)
	{
	}

	/**
	 */
	override fun onStopTrackingTouch(seekBar: SeekBar)
	{
		val alarm = alarm

		// Unable to update the alarm. It is currently in use (active or snoozed)
		if (alarm!!.isInUse)
		{
			return
		}

		// Update the card
		callOnCardUpdatedListener()
	}

	/**
	 */
	override fun onTimeSet(timepicker: TimePicker, hr: Int, min: Int)
	{
		val alarm = alarm
		alarm!!.hour = hr
		alarm.minute = min
		alarm.isEnabled = true
		setTimeView()
		setMeridianView()
		setMeridianColor()
		setSwitchView()
		setSummaryDaysView()

		// Get the visiblity before refreshing the dismiss buttons
		val dismissView: View = dismissParentView
		val beforeVisibility = dismissView.visibility

		// Refresh dismiss buttons
		refreshDismissAndDismissEarlyButtons()

		// Get the visiblity after refreshing the dismiss buttons
		val afterVisibility = dismissView.visibility

		// Determine if the card is already collapsed and the visibility of the
		// dismiss buttons has changed after the new time was set. If so, there
		// is or should be new space because of the dismiss buttons, so do a collapse
		// due to the refresh
		if (isCollapsed && beforeVisibility != afterVisibility)
		{
			collapseRefresh()
		}

		// Call the card updated listener
		callOnCardUpdatedListener()
	}

	/**
	 * Perform haptic feedback on a view.
	 */
	fun performHapticFeedback(view: View)
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
		val alarm = alarm
		val dismissView: View = dismissParentView
		val expandView: View = expandButton

		// Alarm is in use, or will alarm soon so the "Dismiss" or "Dismiss early"
		// button should be shown
		val dismissVis = if (alarm!!.isInUse || alarm.willAlarmSoon()) View.VISIBLE else View.GONE

		// The dismiss view is being shown so do not show this view
		val expandVis = if (dismissVis == View.GONE) View.VISIBLE else View.INVISIBLE

		// The "Dismiss"/"Dismiss early" button OR the "Expand" down-arrow button
		// are NOT the correct and expected visibilities
		return dismissView.visibility != dismissVis || expandView.visibility != expandVis
	}

	/**
	 * Set the dismiss view parent, which contains both "Dismiss" and
	 * "Dismiss early" to its proper setting.
	 */
	fun refreshDismissAndDismissEarlyButtons()
	{
		val alarm = alarm
		val dismissView: View = dismissParentView
		val expandView: View = expandButton

		// Alarm is in use, or will alarm soon so the "Dismiss" or "Dismiss early"
		// button should be shown
		val dismissVis = if (alarm!!.isInUse || alarm.willAlarmSoon()) View.VISIBLE else View.GONE

		// The dismiss view is being shown so do not show this view
		val expandVis = if (dismissVis == View.GONE) View.VISIBLE else View.INVISIBLE

		// Set the "Dismiss" button visibility
		if (dismissView.visibility != dismissVis)
		{
			dismissView.visibility = dismissVis
		}

		// Set the "Expand" down-arrow button visibility
		if (expandView.visibility != expandVis)
		{
			expandView.visibility = expandVis
		}

		// Set the "Dismiss" and "Dismiss early" visibilities
		if (dismissVis == View.VISIBLE)
		{
			val dismissButton = dismissButton
			val dismissEarlyButton = dismissEarlyButton

			// Alarm is in use so "Dismiss" should be shown
			if (alarm.isInUse)
			{
				dismissButton.visibility = View.VISIBLE
				dismissEarlyButton.visibility = View.GONE
			} else if (alarm.willAlarmSoon())
			{
				dismissButton.visibility = View.GONE
				dismissEarlyButton.visibility = View.VISIBLE
			}
		}
	}

	/**
	 * Respond to the audio options button being clicked.
	 */
	private fun respondToAudioOptionsButtonClick(view: View)
	{
		// Respond to the audio options button since the alarm can be modified
		if (checkCanModifyAlarm())
		{
			doAudioOptionsButtonClick()
		}
		performHapticFeedback(view)
	}

	/**
	 * Respond to the alarm card being clicked.
	 */
	private fun respondToCardClick(view: View)
	{
		doCardClick()
		performHapticFeedback(view)
	}

	/**
	 * Perform the day button state change.
	 */
	fun respondToDayButtonClick(button: NacDayButton, day: Day?)
	{
		// Change the state of the day button since the alarm can be modified
		if (checkCanModifyAlarm())
		{
			doDayButtonClick(day)
		} else
		{
			button.toggle()
		}
		performHapticFeedback(button)
	}

	/**
	 * Respond to the delete button being clicked.
	 */
	private fun respondToDeleteButtonClick(view: View)
	{
		// Delete the alarm, since the alarm can be deleted
		if (checkCanDeleteAlarm())
		{
			doDeleteButtonClick()
		}
		performHapticFeedback(view)
	}

	/**
	 * Respond to the dismiss button being clicked.
	 */
	private fun respondToDismissButtonClick(view: View)
	{
		doDismissButtonClick()
		performHapticFeedback(view)
	}

	/**
	 * Respond to the dismiss early button being clicked.
	 */
	private fun respondToDismissEarlyButtonClick(view: View)
	{
		doDismissEarlyButtonClick()
		performHapticFeedback(view)
	}

	/**
	 * Respond to the media button being clicked.
	 */
	private fun respondToMediaButtonClick(view: View)
	{
		// Respond to the media button since the alarm can be modified
		if (checkCanModifyAlarm())
		{
			doMediaButtonClick()
		}
		performHapticFeedback(view)
	}

	/**
	 * Respond to the name being clicked.
	 */
	private fun respondToNameClick(view: View)
	{
		// Name of an alarm can be clicked since the alarm can be modified
		if (checkCanModifyAlarm())
		{
			doNameClick()
		}
		performHapticFeedback(view)
	}

	/**
	 * Respond to the NFC button being clicked.
	 */
	private fun respondToNfcButtonClick(view: View)
	{
		// NFC button can be clicked since the alarm can be modified
		if (checkCanModifyAlarm())
		{
			doNfcButtonClick()
		} else
		{
			val nfcButton = view as MaterialButton
			val state = nfcButton.isChecked
			nfcButton.isChecked = !state
		}
		performHapticFeedback(view)
	}

	/**
	 * Respond to the repeat button being clicked.
	 */
	private fun respondToRepeatButtonClick(view: View)
	{
		// Repeat button can be clicked since the alarm can be modified
		if (checkCanModifyAlarm())
		{
			doRepeatButtonClick()
		} else
		{
			val repeatButton = view as MaterialButton
			val state = repeatButton.isChecked
			repeatButton.isChecked = !state
		}
		performHapticFeedback(view)
	}

	/**
	 * Perform the repeat button long click action.
	 */
	fun respondToRepeatButtonLongClick()
	{
		// Repeat button can be long clicked since the alarm can be modified
		if (checkCanModifyAlarm())
		{
			doRepeatButtonLongClick()
		}
	}

	/**
	 * Perform the switch state change.
	 */
	fun respondToSwitchCheckedChanged(button: CompoundButton,
		state: Boolean)
	{
		// Change the state of the switch since the alarm can be modified
		if (checkCanModifyAlarm())
		{
			doSwitchCheckedChanged(state)
		} else
		{
			button.isChecked = !state
		}
		performHapticFeedback(button)
	}

	/**
	 * Respond to the time being clicked.
	 */
	private fun respondToTimeClick(view: View)
	{
		// Unable to modify the alarm
		if (!checkCanModifyAlarm())
		{
			performHapticFeedback(view)
			return
		}

		// Time can be clicked
		doTimeClick()
	}

	/**
	 * Respond to the vibrate button being clicked.
	 */
	private fun respondToVibrateButtonClick(view: View)
	{
		// Vibrate can be clicked since the alarm can be modified
		if (checkCanModifyAlarm())
		{
			doVibrateButtonClick()
		} else
		{
			val vibrateButton = view as MaterialButton
			val state = vibrateButton.isChecked
			vibrateButton.isChecked = !state
		}
		performHapticFeedback(view)
	}

	/**
	 * Set the ripple color of the audio options button.
	 */
	private fun setAudioOptionsButtonRippleColor()
	{
		val button = audioOptionsButton
		this.setMaterialButtonColor(button)
	}

	/**
	 * Set the ripple color of the collapse button.
	 */
	private fun setCollapseButtonRippleColor()
	{
		val button = collapseButton
		this.setMaterialButtonColor(button)
	}

	/**
	 * Set the background color for when the card is collapsed.
	 */
	fun setCollapsedBackgroundColor()
	{
		val context = context
		val card = cardView
		val grayDark = ContextCompat.getColor(context, R.color.gray_dark)
		val color = MaterialColors.getColor(context, R.attr.colorCard, grayDark)
		//int color = NacUtility.getThemeAttrColor(context, R.attr.colorCardExpanded);

		//Context context = view.getContext();
		//int bg = NacUtility.getThemeAttrColor(context, id);

		//view.setBackground(null);
		//view.setBackgroundColor(bg);
		//NacUtility.setBackground(card, id);
		card.setBackgroundColor(color)
	}

	/**
	 * Set the day of week to its proper setting.
	 */
	fun setDayOfWeek()
	{
		// Check if the days are not equal
		if (dayOfWeek.days != alarm!!.days)
		{
			// Set the days
			dayOfWeek.setDays(alarm!!.days)
		}
	}

	/**
	 * Set the day of week to start on.
	 */
	fun setStartWeekOn()
	{
		val shared = sharedPreferences
		val dow = dayOfWeek
		dow.setStartWeekOn(shared.startWeekOn)
	}

	/**
	 * Set the ripple color for each day in the day of week view.
	 */
	fun setDayOfWeekRippleColor()
	{
		val newColor = createBlendedThemeColorStateList()
		val dow = dayOfWeek
		for (day in dow.dayButtons)
		{
			val button = day.button
			this.setMaterialButtonColor(button, newColor)
		}
	}

	/**
	 * Set the ripple color of the delete button.
	 */
	private fun setDeleteButtonRippleColor()
	{
		val button = deleteButton
		this.setMaterialButtonColor(button)
	}

	/**
	 * Set the ripple color for the dismiss button.
	 */
	fun setDismissButtonRippleColor()
	{
		val button = dismissButton
		this.setMaterialButtonColor(button)
	}

	/**
	 * Set the divider color.
	 */
	fun setDividerColor()
	{
		val shared = sharedPreferences
		val root = this.root
		val headerDivider = root.findViewById<ViewGroup>(R.id.nac_divider_header)
		val deleteDivider = root.findViewById<View>(R.id.nac_divider_delete)
		val themeColor = ColorStateList.valueOf(shared.themeColor)

		// Header divider
		for (i in 0 until headerDivider.childCount)
		{
			val view = headerDivider.getChildAt(i)
			val color = view.backgroundTintList
			if (!this.compareColorStateList(color, themeColor))
			{
				view.backgroundTintList = themeColor
			}
		}

		// Delete divider
		val color = deleteDivider.backgroundTintList
		if (!this.compareColorStateList(color, themeColor))
		{
			deleteDivider.backgroundTintList = themeColor
		}
	}

	/**
	 * Set the ripple color of the expand button.
	 */
	private fun setExpandButtonRippleColor()
	{
		val button = expandButton
		this.setMaterialButtonColor(button)
	}

	/**
	 * Set the background color for when the card is expanded.
	 */
	fun setExpandedBackgroundColor()
	{
		val context = context
		val card = cardView
		val gray = ContextCompat.getColor(context, R.color.gray)
		val color = MaterialColors.getColor(context, R.attr.colorCardExpanded, gray)
		//int color = NacUtility.getThemeAttrColor(context, R.attr.colorCardExpanded);

		//Context context = view.getContext();
		//int bg = NacUtility.getThemeAttrColor(context, id);

		//view.setBackground(null);
		//view.setBackgroundColor(bg);
		//NacUtility.setBackground(card, id);
		card.setBackgroundColor(color)
	}

	/**
	 * Set the color of a MaterialButton.
	 *
	 * @param  button  The button to color.
	 * @param  newColor  The new color of the button.
	 */
	protected fun setMaterialButtonColor(button: MaterialButton?, newColor: ColorStateList?)
	{
		if (button == null)
		{
			return
		}
		val currentColor = button.rippleColor
		if (!this.compareColorStateList(currentColor, newColor))
		{
			button.rippleColor = newColor
		}
	}

	/**
	 * @see .setMaterialButtonColor
	 */
	protected fun setMaterialButtonColor(button: MaterialButton?)
	{
		if (button == null)
		{
			return
		}
		val newColor = createBlendedThemeColorStateList()
		this.setMaterialButtonColor(button, newColor)
	}

	/**
	 * Set the media button to its proper setting.
	 */
	fun setMediaButton()
	{
		val context = context
		val alarm = alarm
		val button = mediaButton
		val path = alarm!!.mediaPath
		val message = NacSharedPreferences.getMediaMessage(context, path)
		val text = button.text.toString()
		val alpha = if (!path.isEmpty()) 1.0f else 0.3f
		if (text != message)
		{
			button.text = message
		}
		if (java.lang.Float.compare(button.alpha, alpha) != 0)
		{
			button.alpha = alpha
		}
	}

	/**
	 * Set the ripple color of the media button.
	 */
	fun setMediaButtonRippleColor()
	{
		val button = mediaButton
		this.setMaterialButtonColor(button)
	}

	/**
	 * Set the meridian color.
	 */
	fun setMeridianColor()
	{
		val context = context
		val tv = meridianView
		val alarm = alarm
		val meridian = alarm!!.getMeridian(context)
		val color = getMeridianColor(meridian)
		setTextViewColor(tv, color)
	}

	/**
	 * Set the meridian view to its proper setting.
	 */
	fun setMeridianView()
	{
		val context = context
		val alarm = alarm
		val tv = meridianView
		val meridian = alarm!!.getMeridian(context)
		val text = tv.text.toString()
		if (text != meridian)
		{
			tv.text = meridian
		}
	}

	/**
	 * Set the name button to its proper settings.
	 */
	private fun setNameButton()
	{
		val res = context.resources
		val alarm = alarm
		val button = nameButton
		val name = alarm!!.nameNormalized
		val message = NacSharedPreferences.getNameMessage(res, name)
		val text = button.text.toString()
		val alpha = if (!name.isEmpty()) 1.0f else 0.3f
		if (text != message)
		{
			button.text = message
		}
		if (java.lang.Float.compare(button.alpha, alpha) != 0)
		{
			button.alpha = alpha
		}
	}

	/**
	 * Set the ripple color of the name button.
	 */
	private fun setNameButtonRippleColor()
	{
		val button = nameButton
		this.setMaterialButtonColor(button)
	}

	/**
	 * Set the NFC button to its proper settings.
	 */
	private fun setNfcButton()
	{
		val button = nfcButton
		val alarm = alarm
		val shouldUseNfc = alarm!!.shouldUseNfc
		if (button.isChecked != shouldUseNfc)
		{
			button.isChecked = shouldUseNfc
		}
	}

	/**
	 * Set the ripple color of the NFC button.
	 */
	private fun setNfcButtonRippleColor()
	{
		val button = nfcButton
		this.setMaterialButtonColor(button)
	}

	/**
	 * Set listener for when a menu item is clicked.
	 */
	fun setOnCreateContextMenuListener(
		listener: OnCreateContextMenuListener?)
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
		val button = repeatButton
		val alarm = alarm
		val isEnabled = alarm!!.areDaysSelected
		val shouldRepeat = alarm.areDaysSelected && alarm.shouldRepeat
		if (button.isChecked != shouldRepeat)
		{
			button.isChecked = shouldRepeat
		}
		if (button.isEnabled != isEnabled)
		{
			button.isEnabled = isEnabled
		}
	}

	/**
	 * Set the ripple color of the repeat button.
	 */
	private fun setRepeatButtonRippleColor()
	{
		val button = repeatButton
		this.setMaterialButtonColor(button)
	}

	/**
	 * Set the progress and thumb color of a seekbar.
	 *
	 * @param  seekbar  A seekbar.
	 */
	protected fun setSeekBarColor(seekbar: SeekBar?)
	{
		if (seekbar == null)
		{
			return
		}
		val currentProgress = seekbar.progressTintList
		val currentThumb = seekbar.thumbTintList
		val newColor = createThemeColorStateList()
		if (!this.compareColorStateList(currentProgress, newColor))
		{
			seekbar.progressTintList = newColor
		}
		if (!this.compareColorStateList(currentThumb, newColor))
		{
			seekbar.thumbTintList = newColor
		}
	}

	/**
	 * Set the color of the summary days.
	 */
	fun setSummaryDaysColor()
	{
		val shared = sharedPreferences
		val tv = summaryDaysView
		val color = shared.daysColor
		setTextViewColor(tv, color)
	}

	/**
	 * Set the summary days view to its proper setting.
	 */
	fun setSummaryDaysView()
	{
		val context = context
		val shared = sharedPreferences
		val alarm = alarm
		val tv = summaryDaysView
		val start = shared.startWeekOn
		val string = NacCalendar.Days.toString(context, alarm, start)
		val text = tv.text.toString()
		if (text != string)
		{
			tv.text = string
			//tv.requestLayout();
		}
	}

	/**
	 * Set the color of the summary name.
	 */
	fun setSummaryNameColor()
	{
		val shared = sharedPreferences
		val tv = summaryNameView
		val color = shared.nameColor
		setTextViewColor(tv, color)
	}

	/**
	 * Set the summary name view to its proper setting.
	 */
	fun setSummaryNameView()
	{
		val alarm = alarm
		val tv = summaryNameView
		val name = alarm!!.nameNormalized
		val text = tv.text.toString()
		if (text != name)
		{
			tv.text = name
		}
	}

	/**
	 * Set the color of the switch.
	 */
	fun setSwitchColor()
	{
		val shared = sharedPreferences
		val theme = shared.themeColor
		val themeDark = ColorUtils.blendARGB(theme, Color.BLACK, 0.6f)
		val thumbColors = intArrayOf(theme, Color.GRAY)
		val trackColors = intArrayOf(themeDark, Color.DKGRAY)
		val states = arrayOf(intArrayOf(android.R.attr.state_checked),
			intArrayOf(-android.R.attr.state_checked))
		val thumbStateList = ColorStateList(states, thumbColors)
		val trackStateList = ColorStateList(states, trackColors)
		val switchView = switch
		this.setSwitchColor(switchView, thumbStateList, trackStateList)
	}

	/**
	 * Set the thumb and track color of a switch.
	 *
	 * @param  switchView  The switch.
	 * @param  thumbColor  The color of the thumb.
	 * @param  trackColor  The color of the track.
	 */
	protected fun setSwitchColor(switchView: SwitchCompat?,
		thumbColor: ColorStateList?, trackColor: ColorStateList?)
	{
		if (switchView == null)
		{
			return
		}
		val currentThumb = switchView.thumbTintList
		val currentTrack = switchView.trackTintList

		// Thumb color
		if (!this.compareColorStateList(currentThumb, thumbColor))
		{
			switchView.thumbTintList = thumbColor
		}

		// Track color
		if (!this.compareColorStateList(currentTrack, trackColor))
		{
			switchView.trackTintList = trackColor
		}
	}

	/**
	 * Set the switch to its proper setting.
	 */
	fun setSwitchView()
	{
		val alarm = alarm
		val view = switch
		val enabled = alarm!!.isEnabled
		if (view.isChecked != enabled)
		{
			view.isChecked = enabled
		}
	}

	/**
	 * Set the color of a TextView.
	 *
	 * @param  tv  The text view to color.
	 * @param  newColor  The new color of the text view.
	 */
	protected fun setTextViewColor(tv: TextView?, newColor: Int)
	{
		if (tv == null)
		{
			return
		}
		if (tv.currentTextColor != newColor)
		{
			tv.setTextColor(newColor)
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
	 * Set the time color.
	 */
	fun setTimeColor()
	{
		val shared = sharedPreferences
		val tv = timeView
		val color = shared.timeColor
		setTextViewColor(tv, color)
	}

	/**
	 * Set the time view to its proper setting.
	 */
	fun setTimeView()
	{
		val context = context
		val alarm = alarm
		val tv = timeView
		val time = alarm!!.getClockTime(context)
		val text = tv.text.toString()
		if (text != time)
		{
			tv.text = time
		}
	}

	/**
	 * Set the vibrate button to its proper setting.
	 */
	fun setVibrateButton()
	{
		val button = vibrateButton
		val alarm = alarm
		val shouldVibrate = alarm!!.shouldVibrate
		if (button.isChecked != shouldVibrate)
		{
			button.isChecked = shouldVibrate
		}
	}

	/**
	 * Set the ripple color of the vibrate button.
	 */
	private fun setVibrateButtonRippleColor()
	{
		val button = vibrateButton
		this.setMaterialButtonColor(button)
	}

	/**
	 * Set the volume image view.
	 */
	fun setVolumeImageView()
	{
		val image = volumeImageView
		val alarm = alarm
		val progress = alarm!!.volume
		val resId: Int
		resId = if (progress == 0)
		{
			R.mipmap.volume_off
		} else if (progress > 0 && progress <= 33)
		{
			R.mipmap.volume_low
		} else if (progress > 33 && progress <= 66)
		{
			R.mipmap.volume_med
		} else
		{
			R.mipmap.volume_high
		}
		val tag = image.tag
		if (tag == null || tag as Int != resId)
		{
			image.setImageResource(resId)
			image.tag = resId
		}
	}

	/**
	 * Set the volume seekbar.
	 */
	fun setVolumeSeekBar()
	{
		val alarm = alarm
		val bar = volumeSeekBar
		val volume = alarm!!.volume

		//this.mVolume.incrementProgressBy(10);
		if (bar.progress != volume)
		{
			bar.progress = volume
		}
	}

	/**
	 * Set the volume seekbar color.
	 */
	fun setVolumeSeekBarColor()
	{
		val seekbar = volumeSeekBar
		setSeekBarColor(seekbar)
	}

	/**
	 * Show the name dialog.
	 */
	private fun showNameDialog()
	{
		val context = context
		val alarm = alarm

		// Get the fragment manager
		val manager = (context as AppCompatActivity).supportFragmentManager

		// Create the dialog
		val dialog = NacNameDialog()

		// Setup the dialog
		dialog.defaultName = alarm!!.name
		dialog.onNameEnteredListener = this
		dialog.show(manager, NacNameDialog.TAG)
	}

	/**
	 * Show the time picker dialog.
	 */
	private fun showTimeDialog()
	{
		val context = context
		val alarm = alarm
		val hour = alarm!!.hour
		val minute = alarm.minute
		val is24HourFormat = NacCalendar.Time.is24HourFormat(context)

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
		val dialog = TimePickerDialog(context, this, hour, minute,
			is24HourFormat)
		dialog.show()
	}

	/**
	 * Show a toast saying that a user cannot delete an active alarm.
	 */
	private fun toastDeleteActiveAlarmError()
	{
		val context = context
		val message = context.getString(R.string.error_message_active_delete)
		quickToast(context, message)
	}

	/**
	 * Show a toast saying that a user cannot delete a snoozed alarm.
	 */
	private fun toastDeleteSnoozedAlarmError()
	{
		val context = context
		val message = context.getString(R.string.error_message_snoozed_delete)
		quickToast(context, message)
	}

	/**
	 * Show a toast saying that a user cannot modify an active alarm.
	 */
	private fun toastModifyActiveAlarmError()
	{
		val context = context
		val message = context.getString(R.string.error_message_active_modify)
		quickToast(context, message)
	}

	/**
	 * Show a toast saying that a user cannot modify a snoozed alarm.
	 */
	private fun toastModifySnoozedAlarmError()
	{
		val context = context
		val message = context.getString(R.string.error_message_snoozed_modify)
		quickToast(context, message)
	}

	/**
	 * Show a toast when a user clicks the NFC button.
	 */
	private fun toastNfc()
	{
		val alarm = alarm
		val context = context

		// Determine which message to show
		val requiredMessage = context.getString(R.string.message_nfc_required)
		val optionalMessage = context.getString(R.string.message_nfc_optional)
		val message = if (alarm!!.shouldUseNfc) requiredMessage else optionalMessage

		// Toast
		quickToast(context, message)
	}

	/**
	 * Show a toast when a user clicks the repeat button.
	 */
	private fun toastRepeat()
	{
		val alarm = alarm
		val context = context
		val repeatEnabled = context.getString(R.string.message_repeat_enabled)
		val repeatDisabled = context.getString(R.string.message_repeat_disabled)
		val message = if (alarm!!.shouldRepeat) repeatEnabled else repeatDisabled
		quickToast(context, message)
	}

	/**
	 * Show a toast when a user clicks the vibrate button.
	 */
	private fun toastVibrate()
	{
		val alarm = alarm
		val context = context
		val vibrateEnabled = context.getString(R.string.message_vibrate_enabled)
		val vibrateDisabled = context.getString(R.string.message_vibrate_disabled)
		val message = if (alarm!!.shouldVibrate) vibrateEnabled else vibrateDisabled
		quickToast(context, message)
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