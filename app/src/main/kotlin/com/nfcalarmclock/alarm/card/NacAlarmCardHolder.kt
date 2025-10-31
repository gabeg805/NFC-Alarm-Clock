package com.nfcalarmclock.alarm.card

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.view.View.MeasureSpec
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.activealarm.NacActiveAlarmService.Companion.dismissAlarmService
import com.nfcalarmclock.alarm.activealarm.NacActiveAlarmService.Companion.startAlarmService
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.nfc.db.NacNfcTag
import com.nfcalarmclock.system.NacCalendar.Day
import com.nfcalarmclock.view.quickToast
import com.nfcalarmclock.view.dayofweek.NacDayOfWeek
import com.nfcalarmclock.view.dayofweek.NacDayOfWeek.OnWeekChangedListener
import com.nfcalarmclock.view.performHapticFeedback
import com.nfcalarmclock.view.setupProgressAndThumbColor
import com.nfcalarmclock.view.setupRippleColor
import com.nfcalarmclock.view.setupSwitchColor
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.nfcalarmclock.card.NacBaseCardHolder
import com.nfcalarmclock.card.NacHeightAnimator
import com.nfcalarmclock.system.toDayString
import java.util.EnumSet

/**
 * Alarm ViewHolder for a CardView.
 *
 * @param root Root view.
 */
class NacAlarmCardHolder(root: View)
	: NacBaseCardHolder<NacAlarm>(root)
{

	/**
	 * Listener for when the alarm options button is clicked.
	 */
	fun interface OnCardAlarmOptionsClickedListener
	{
		fun onCardAlarmOptionsClicked(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when a button is long clicked.
	 */
	fun interface OnCardButtonLongClickedListener
	{
		fun onCardButtonLongClicked(card: NacAlarmCardHolder, alarm: NacAlarm, destinationId: Int)
	}

	/**
	 * Listener for when a card is collapsed.
	 */
	fun interface OnCardCollapsedListener
	{
		fun onCardCollapsed(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when the days changed.
	 */
	fun interface OnCardDaysChangedListener
	{
		fun onCardDaysChanged(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when the dismiss early button is clicked.
	 */
	fun interface OnCardDismissEarlyClickedListener
	{
		fun onCardDismissEarlyClicked(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when the dismiss options button is clicked.
	 */
	fun interface OnCardDismissOptionsClickedListener
	{
		fun onCardDismissOptionsClicked(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when a card is expanded.
	 */
	fun interface OnCardExpandedListener
	{
		fun onCardExpanded(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when the media button is clicked.
	 */
	fun interface OnCardMediaClickedListener
	{
		fun onCardMediaClicked(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when the name button is clicked.
	 */
	fun interface OnCardNameClickedListener
	{
		fun onCardNameClicked(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when the snooze options button is clicked.
	 */
	fun interface OnCardSnoozeOptionsClickedListener
	{
		fun onCardSnoozeOptionsClicked(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when the switch is changed.
	 */
	fun interface OnCardSwitchChangedListener
	{
		fun onCardSwitchChanged(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when the time is clicked.
	 */
	fun interface OnCardTimeClickedListener
	{
		fun onCardTimeClicked(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when a card is updated.
	 */
	fun interface OnCardUpdatedListener
	{
		fun onCardUpdated(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when a card will use the flashlight or not is changed.
	 */
	fun interface OnCardUseFlashlightChangedListener
	{
		fun onCardUseFlashlightChanged(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when a card will use NFC or not is changed.
	 */
	fun interface OnCardUseNfcChangedListener
	{
		fun onCardUseNfcChanged(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when a card will repeat or not is changed.
	 */
	fun interface OnCardUseRepeatChangedListener
	{
		fun onCardUseRepeatChanged(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when a card will vibrate or not is changed.
	 */
	fun interface OnCardUseVibrateChangedListener
	{
		fun onCardUseVibrateChanged(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Listener for when the volume is changed.
	 */
	fun interface OnCardVolumeChangedListener
	{
		fun onCardVolumeChanged(card: NacAlarmCardHolder, alarm: NacAlarm)
	}

	/**
	 * Alarm.
	 */
	var alarm: NacAlarm? = null

	/**
	 * Header view.
	 */
	private val headerView: LinearLayout = root.findViewById(R.id.nac_collapsed_alarm)

	/**
	 * Summary view.
	 */
	private val summaryView: LinearLayout = root.findViewById(R.id.nac_summary)

	/**
	 * Extra view below the summary.
	 */
	private val extraBelowSummaryView: LinearLayout = root.findViewById(R.id.nac_extra_below_summary)

	/**
	 * Dismiss snoozed alarm button.
	 */
	private val dismissButton: MaterialButton = root.findViewById(R.id.nac_dismiss)

	/**
	 * Dismiss early alarm button.
	 */
	private val dismissEarlyButton: MaterialButton = root.findViewById(R.id.nac_dismiss_early)

	/**
	 * Skip next alarm icon.
	 */
	private val skipNextAlarmIcon: ImageView = root.findViewById(R.id.nac_skip_next_alarm)

	/**
	 * Delete alarm after it is dismissed icon.
	 */
	private val deleteAlarmAfterDismissedIcon: ImageView = root.findViewById(R.id.nac_delete_alarm_after_dismissed)

	/**
	 * Expanded alarm view.
	 */
	private val expandedView: LinearLayout = root.findViewById(R.id.nac_expanded_alarm)

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
	private val nfcButton: MaterialButton = root.findViewById(R.id.nac_nfc)

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
	 * Name button.
	 */
	private val nameButton: MaterialButton = root.findViewById(R.id.nac_name)

	/**
	 * Dismiss options button.
	 */
	private val dismissOptionsButton: MaterialButton = root.findViewById(R.id.nac_dismiss_options)

	/**
	 * Snooze options button.
	 */
	private val snoozeOptionsButton: MaterialButton = root.findViewById(R.id.nac_snooze_options)

	/**
	 * Alarm options button.
	 */
	private val alarmOptionsButton: MaterialButton = root.findViewById(R.id.nac_alarm_options)

	/**
	 * The button to expand the alarm card.
	 */
	private val expandButton: MaterialButton = root.findViewById(R.id.nac_expand)

	/**
	 * The other button to expand the alarm card (there are 2).
	 */
	private val expandOtherButton: MaterialButton = root.findViewById(R.id.nac_expand_other)

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

	/**
	 * Listener for when the alarm options button is clicked.
	 */
	var onCardAlarmOptionsClickedListener: OnCardAlarmOptionsClickedListener? = null

	/**
	 * Listener for when a button is long clicked.
	 */
	var onCardButtonLongClickedListener: OnCardButtonLongClickedListener? = null

	/**
	 * Listener for when the alarm card is collapsed.
	 */
	var onCardCollapsedListener: OnCardCollapsedListener? = null

	/**
	 * Listener for when the days changed.
	 */
	var onCardDaysChangedListener: OnCardDaysChangedListener? = null

	/**
	 * Listener for when the dismiss early button is clicked.
	 */
	var onCardDismissEarlyClickedListener: OnCardDismissEarlyClickedListener? = null

	/**
	 * Listener for when the dismiss options button is clicked.
	 */
	var onCardDismissOptionsClickedListener: OnCardDismissOptionsClickedListener? = null

	/**
	 * Listener for when the alarm card is expanded.
	 */
	var onCardExpandedListener: OnCardExpandedListener? = null

	/**
	 * Listener for when the media button is clicked.
	 */
	var onCardMediaClickedListener: OnCardMediaClickedListener? = null

	/**
	 * Listener for when the name button is clicked.
	 */
	var onCardNameClickedListener: OnCardNameClickedListener? = null

	/**
	 * Listener for when the snooze options button is clicked.
	 */
	var onCardSnoozeOptionsClickedListener: OnCardSnoozeOptionsClickedListener? = null

	/**
	 * Listener for when the switch is changed.
	 */
	var onCardSwitchChangedListener: OnCardSwitchChangedListener? = null

	/**
	 * Listener for when the time is clicked.
	 */
	var onCardTimeClickedListener: OnCardTimeClickedListener? = null

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
	 * Listener for when a card will repeat or not is changed.
	 */
	var onCardUseRepeatChangedListener: OnCardUseRepeatChangedListener? = null

	/**
	 * Listener for when a card will vibrate or not is changed.
	 */
	var onCardUseVibrateChangedListener: OnCardUseVibrateChangedListener? = null

	/**
	 * Listener for when the volume is changed.
	 */
	var onCardVolumeChangedListener: OnCardVolumeChangedListener? = null

	/**
	 * The context.
	 */
	val context: Context
		get() = root.context

	/**
	 * The height when the card is collapsed.
	 */
	private val heightCollapsed: Int
		// Check if the alarm is snoozed or will alarm soon
		get() = if (alarm!!.isSnoozed || alarm!!.willAlarmSoon())
		{
			// Show a little extra space for stuff right beneath the summary
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
		get() = (expandedView.isGone)
			|| (cardView.measuredHeight == sharedPreferences.cardHeightCollapsed)
			|| (cardView.measuredHeight == sharedPreferences.cardHeightCollapsedDismiss)

	/**
	 * Check if the alarm card is expanded.
	 */
	val isExpanded: Boolean
		get() = (expandedView.isVisible)
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

		// Initialize the colors and listeners
		initColors()
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
	override fun bind(item: NacAlarm)
	{
		// Set the alarm and binding flag
		alarm = item
		isBinding = true

		// Hide the views such as vibrate, NFC, and flashlight, if the user does not want them visible
		hideAppearanceSettingViews()

		// Super
		super.bind(item)

		// Setup the meridian color because it is dependent on the alarm being bound to
		// the card holder
		setMeridianColor()

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
	 * Check if the alarm can be modified, and if it cannot, display toasts to the
	 * user indicating as such.
	 *
	 * @return True if the check passed successfully, and the alarm can be
	 *         modified, and False otherwise.
	 */
	private fun checkCanModifyAlarm(view: View? = null): Boolean
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
			// Haptic feedback
			view?.performHapticFeedback()
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
		if (extraBelowSummaryView.isVisible)
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

		// Setup the expanded view
		expandedView.visibility = View.GONE
		expandedView.isEnabled = false

		// Refresh the extra view
		refreshExtraView()
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

		// Reset the height because it was showing up as expanded for some
		// reason
		if (sharedPreferences.cardIsMeasured && (heightCollapsed > 0))
		{
			cardView.layoutParams.height = heightCollapsed
		}
	}

	/**
	 * Expand the alarm card without any animations.
	 */
	private fun doExpand()
	{
		// Setup the summary
		summaryView.visibility = View.GONE
		summaryView.isEnabled = false

		// Setup the expanded view
		expandedView.visibility = View.VISIBLE
		expandedView.isEnabled = true
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
	 * Determine the height of the view.
	 *
	 * @param  view  The view.
	 *
	 * @return The height of the view.
	 */
	private fun getHeight(view: View): Int
	{
		// Measure the view
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
			MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))

		// Calculate the height, including the margins
		val lp = view.layoutParams as MarginLayoutParams
		val margins = lp.topMargin + lp.bottomMargin
		val height = view.measuredHeight

		return height + margins
	}

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
	 *
	 * Do not initialize the meridian color because when this is called, then alarm still
	 * has not been bound to the card holder.
	 */
	override fun initColors()
	{
		setDividerColor()
		timeView.setTextColor(sharedPreferences.timeColor)
		switch.setupSwitchColor(sharedPreferences)
		deleteAlarmAfterDismissedIcon.imageTintList = ColorStateList.valueOf(sharedPreferences.deleteAfterDismissedColor)
		skipNextAlarmIcon.imageTintList = ColorStateList.valueOf(sharedPreferences.skipNextAlarmColor)
		summaryDaysView.setTextColor(sharedPreferences.daysColor)
		summaryNameView.setTextColor(sharedPreferences.nameColor)
		dismissButton.setupRippleColor(sharedPreferences)
		dayOfWeek.dayButtons.forEach { it.button?.setupRippleColor(sharedPreferences) }
		repeatButton.setupRippleColor(sharedPreferences)
		vibrateButton.setupRippleColor(sharedPreferences)
		nfcButton.setupRippleColor(sharedPreferences)
		flashlightButton.setupRippleColor(sharedPreferences)
		mediaButton.setupRippleColor(sharedPreferences)
		volumeSeekBar.setupProgressAndThumbColor(sharedPreferences)
		nameButton.setupRippleColor(sharedPreferences)
		dismissOptionsButton.setupRippleColor(sharedPreferences)
		snoozeOptionsButton.setupRippleColor(sharedPreferences)
		alarmOptionsButton.setupRippleColor(sharedPreferences)
		expandButton.setupRippleColor(sharedPreferences)
	}

	/**
	 * Initialize the listeners of the various views.
	 */
	override fun initListeners()
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
		setupVibrateButtonLongClickListener()
		setupNfcButtonListener()
		setupNfcButtonLongClickListener()
		setupFlashlightButtonListener()
		setupFlashlightButtonLongClickListener()
		setupMediaButtonListener()
		setupVolumeSeekBarListener()
		setupNameListener()
		setupDismissOptionsListener()
		setupSnoozeOptionsListener()
		setupAlarmOptionsListener()
	}

	/**
	 * Initialize the various views.
	 */
	override fun initViews()
	{
		refreshExtraViewWithCollapse()
		setTimeView()
		setMeridianView()
		setSwitchView()
		setSummarySkipNextAlarmIcon()
		setSummaryShoulDeleteAlarmAfterDismissedIcon()
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

		// Hide the extra view
		extraBelowSummaryView.visibility = View.GONE

		// Save the height
		heights[0] = getHeight(cardView)

		// Show the extra view
		extraBelowSummaryView.visibility = View.VISIBLE

		// Save the height
		heights[1] = getHeight(cardView)

		// Refresh the extra view
		refreshExtraView()
	}

	/**
	 * Refresh the extra view, which contains any one of the "Dismiss", or
	 * "Dismiss early" views.
	 */
	private fun refreshExtraView()
	{
		// Alarm is in use (so "Dismiss" should be shown),
		// or will alarm soon (so "Dismiss early" should be shown)
		val extraVis = if (shouldShowExtraView()) View.VISIBLE else View.GONE

		// The extra view should be hidden so show the expand button on its
		// normal row (the summary row)
		val expandVis = if (extraVis == View.GONE) View.VISIBLE else View.INVISIBLE

		// Set the extra view visibility
		if (extraBelowSummaryView.visibility != extraVis)
		{
			extraBelowSummaryView.visibility = extraVis
		}

		// Set the expand button visibility
		if (expandButton.visibility != expandVis)
		{
			expandButton.visibility = expandVis
		}

		// Set the extra view visibility
		if (extraVis == View.VISIBLE)
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
	private fun refreshExtraViewWithCollapse()
	{
		// Get the visiblity before refreshing the extra view
		val beforeVisibility = extraBelowSummaryView.visibility

		// Refresh the extra view
		refreshExtraView()

		// Get the visiblity after refreshing the extra view
		val afterVisibility = extraBelowSummaryView.visibility

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
	 * Refresh all the views that can be affected when the name is changed.
	 */
	fun refreshNameViews()
	{
		setNameButton()
		setSummaryNameView()
		setSummarySkipNextAlarmIcon()
	}

	/**
	 * Refresh all the views associated with a change to the repeat options.
	 */
	fun refreshRepeatOptionViews()
	{
		setDayOfWeek()
		setRepeatButton()
		setSummaryDaysView()
		setSummarySkipNextAlarmIcon()
	}

	/**
	 * Refresh all the views that can be affected when a date is scheduled.
	 */
	fun refreshScheduleDateViews()
	{
		setSwitchView()
		setDayOfWeek()
		setRepeatButton()
		setSummaryDaysView()
		setSummarySkipNextAlarmIcon()

		// Refresh dismiss buttons and collapse refresh if need be
		refreshExtraViewWithCollapse()
	}

	/**
	 * Refresh all the views that can be affected when time is changed.
	 */
	fun refreshTimeViews()
	{
		setTimeView()
		setMeridianView()
		setMeridianColor()
		setSwitchView()
		setSummaryDaysView()
		setSummarySkipNextAlarmIcon()

		// Refresh dismiss buttons and collapse refresh if need be
		refreshExtraViewWithCollapse()
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
	 * Set the divider color.
	 */
	private fun setDividerColor()
	{
		// Get the dividers
		val headerDivider = root.findViewById<ViewGroup>(R.id.nac_divider_header)
		val optionsDivider1 = root.findViewById<View>(R.id.nac_options_divider)
		val optionsDivider2 = root.findViewById<View>(R.id.nac_options_divider2)

		// Get the color
		val themeColor = ColorStateList.valueOf(sharedPreferences.themeColor)

		// Iterate over each header divider (the 3 dots)
		for (i in 0 until headerDivider.childCount)
		{
			// Get one of the dots
			val view = headerDivider.getChildAt(i)

			// Set the color
			view.backgroundTintList = themeColor
		}

		// Set the delete divider color
		optionsDivider1.backgroundTintList = themeColor
		optionsDivider2.backgroundTintList = themeColor
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
		val shouldUseFlashlight = alarm!!.shouldUseFlashlight

		// Check if the state of the button and the alarm are different
		if (flashlightButton.isChecked != shouldUseFlashlight)
		{
			// Set the new state of the button
			flashlightButton.isChecked = shouldUseFlashlight
		}
	}

	/**
	 * Set the media button to its proper setting.
	 */
	fun setMediaButton()
	{
		val mediaTitle = alarm!!.mediaTitle
		val message : String
		val alpha : Float

		// Check if the media path is not empty
		if (mediaTitle.isNotEmpty())
		{
			// Get the message and alpha of the alarm
			message = mediaTitle
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
		// Get the AM and PM strings
		val am = context.getString(R.string.am)
		val pm = context.getString(R.string.pm)

		// Get the meridian (AM/PM) of the alarm
		val meridian = alarm?.getMeridian(context) ?: ""

		// Get the color based on the meridian
		val color = when (meridian)
		{
			am -> sharedPreferences.amColor
			pm -> sharedPreferences.pmColor
			else -> context.resources.getInteger(R.integer.default_color)
		}

		// Set the color
		meridianView.setTextColor(color)
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
			context.resources.getString(R.string.title_alarm_name)
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
		//val isEnabled = alarm!!.areDaysSelected
		//val shouldRepeat = alarm!!.areDaysSelected && alarm!!.shouldRepeat
		val shouldRepeat = alarm!!.shouldRepeat

		// Check if the repeat button status and the alarm should repeat status are
		// different
		if (repeatButton.isChecked != shouldRepeat)
		{
			// Set the new status in the button
			repeatButton.isChecked = shouldRepeat
		}

		// Check if the repeat button is enabled status and the alarm is enabled status
		// are different
		//if (repeatButton.isEnabled != isEnabled)
		//{
		//	// Set the new is enabled status in the button
		//	repeatButton.isEnabled = isEnabled
		//}
	}

	/**
	 * Set the summary days view to its proper setting.
	 */
	private fun setSummaryDaysView()
	{
		// Get the string from the alarm
		val string = alarm!!.toDayString(context, sharedPreferences.startWeekOn)

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
	 * Set the should delete alarm after it is disimssed icon in the summary area.
	 */
	private fun setSummaryShoulDeleteAlarmAfterDismissedIcon()
	{
		// Get the what the visibility should be
		val vis = if (alarm!!.shouldDeleteAfterDismissed) View.VISIBLE else View.GONE

		// Check if the visibilty does not match
		if (vis != deleteAlarmAfterDismissedIcon.visibility)
		{
			deleteAlarmAfterDismissedIcon.visibility = vis
		}
	}

	/**
	 * Set the skip next alarm icon in the summary area.
	 */
	private fun setSummarySkipNextAlarmIcon()
	{
		// Get the what the visibility should be
		val vis = if (alarm!!.shouldSkipNextAlarm) View.VISIBLE else View.GONE

		// Check if the visibilty does not match
		if (vis != skipNextAlarmIcon.visibility)
		{
			skipNextAlarmIcon.visibility = vis
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
	 * Setup the alarm options button listener.
	 */
	private fun setupAlarmOptionsListener()
	{
		// Set the listener
		alarmOptionsButton.setOnClickListener { view ->

			// Check if the alarm can be modified
			if (checkCanModifyAlarm(view))
			{
				// Call the listener for when the button is clicked
				onCardAlarmOptionsClickedListener?.onCardAlarmOptionsClicked(this, alarm!!)
			}

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

				// Update the delete alarm after dismissed icon, if necessary
				setSummaryShoulDeleteAlarmAfterDismissedIcon()

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

		// List of all the views that need the same on click listener for the card
		val allCardViews = listOf(headerView, summaryView, extraBelowSummaryView,
			expandButton, expandOtherButton)

		// Iterate over all the main card views that will control expanding and
		// collapsing the alarm
		for (view in allCardViews)
		{

			// Expand/collapse the alarm
			view.setOnClickListener {

				// Do the card click
				doCardClick()

				// Haptic feedback
				view.performHapticFeedback()

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
			if (checkCanModifyAlarm(button))
			{
				// Reset the skip next alarm flag
				alarm!!.shouldSkipNextAlarm = false

				// Toggle the day
				alarm!!.toggleDay(day)

				// No days are selected
				if (alarm!!.days.isEmpty())
				{
					// Repeat frequency is every 1 week
					if ((alarm!!.repeatFrequency == 1) && (alarm!!.repeatFrequencyUnits == 4))
					{
						println("Change to daily")
						// Change to daily
						alarm!!.repeatFrequency = 1
						alarm!!.repeatFrequencyUnits = 3
						alarm!!.repeatFrequencyDaysToRunBeforeStarting = Day.NONE
					}
				}
				// Days are selected
				else
				{
					// Repeat frequency units
					when (alarm!!.repeatFrequencyUnits)
					{
						// Minutes, hours, and days
						1, 2, 3 ->
						{
							// Repeat frequency is the default value of 1 but not on a weekly
							// cadence. When days are selected, the repeat frequency should be
							// weekly, with few exceptions
							if ((alarm!!.repeatFrequency == 1) && (alarm!!.repeatFrequencyUnits == 3))
							{
								println("Change to weekly")
								// Change to weekly
								alarm!!.repeatFrequency = 1
								alarm!!.repeatFrequencyUnits = 4
								alarm!!.repeatFrequencyDaysToRunBeforeStarting = Day.WEEK
							}
							// Custom repeat frequency
							else
							{
								// The day was selected, and not deselected
								if (day in alarm!!.days)
								{
									// Deselect any other days that may be selected. Only
									// allow one day to be selected at a time for
									// minute/hour/day repeat frequencies
									alarm!!.days = EnumSet.of(day)
									dayOfWeek.setDays(alarm!!.days)
									println("Only active : $day")
								}
							}
						}

						// Do nothing for weeks/months. These types can have multiple days selected
						else -> {}
					}
				}
				println("Repeat : ${alarm!!.repeatFrequency} | ${alarm!!.repeatFrequencyUnits}")

				// Clear the date
				alarm!!.date = ""

				// Setup the views
				setRepeatButton()
				setSummaryDaysView()
				setSummarySkipNextAlarmIcon()

				// Call the listener
				onCardDaysChangedListener?.onCardDaysChanged(this, alarm!!)
			}
			// Alarm cannot be modified
			else
			{
				// Revert the button press
				button.toggle()
			}

		}
	}

	/**
	 * Setup the dismiss button listener.
	 */
	@OptIn(UnstableApi::class)
	private fun setupDismissButtonListener()
	{
		// Set the listener
		dismissButton.setOnClickListener { view ->

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
				// TODO: Can bind to service in Show Alarms and then catch a listener here that does both of these branches
				dismissAlarmService(context, alarm)
			}

			// Haptic feedback
			view.performHapticFeedback()

		}
	}

	/**
	 * Setup the dismiss early button listener.
	 */
	private fun setupDismissEarlyButtonListener()
	{
		// Set the listener
		dismissEarlyButton.setOnClickListener { view ->

			// Dismiss the alarm early
			alarm!!.dismissEarly()

			// Refresh the extra view
			refreshTimeViews()

			// Call the listener
			onCardDismissEarlyClickedListener?.onCardDismissEarlyClicked(this, alarm!!)

			// Haptic feedback
			view.performHapticFeedback()

		}
	}

	/**
	 * Setup the disimss options button listener.
	 */
	private fun setupDismissOptionsListener()
	{
		// Set the listener
		dismissOptionsButton.setOnClickListener { view ->

			// Check if the alarm can be modified
			if (checkCanModifyAlarm(view))
			{
				// Call the listener for when the button is clicked
				onCardDismissOptionsClickedListener?.onCardDismissOptionsClicked(this, alarm!!)
			}

		}
	}

	/**
	 * Setup the listener for the flashlight button.
	 */
	private fun setupFlashlightButtonListener()
	{
		// Set the listener
		flashlightButton.setOnClickListener { view ->

			// Check if the alarm can be modified
			if (checkCanModifyAlarm(view))
			{
				// Reset the skip next alarm flag
				alarm!!.shouldSkipNextAlarm = false

				// Toggle the flashlight button
				alarm!!.toggleUseFlashlight()

				// Setup the skip icon
				setSummarySkipNextAlarmIcon()

				// Call the listener
				onCardUseFlashlightChangedListener?.onCardUseFlashlightChanged(this, alarm!!)
			}
			// Alarm cannot be modified
			else
			{
				// Revert the button press
				(view as MaterialButton).toggle()
			}

		}
	}

	/**
	 * Setup the listener for when the flashlight button is long clicked.
	 */
	private fun setupFlashlightButtonLongClickListener()
	{
		// Set the listener
		flashlightButton.setOnLongClickListener {

			// Check if the alarm can be modified
			if (checkCanModifyAlarm(it))
			{
				// Call the listener
				onCardButtonLongClickedListener?.onCardButtonLongClicked(this, alarm!!, R.id.nacFlashlightOptionsDialog2)
			}

			true
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
			if (checkCanModifyAlarm(view))
			{
				// Call the listener
				onCardMediaClickedListener?.onCardMediaClicked(this, alarm!!)
			}

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
			if (checkCanModifyAlarm(view))
			{
				// Call the listener
				onCardNameClickedListener?.onCardNameClicked(this, alarm!!)
			}

		}
	}

	/**
	 * Setup the listener for the NFC button.
	 */
	private fun setupNfcButtonListener()
	{
		// Set the listener
		nfcButton.setOnClickListener { view ->

			// Check if the alarm can be modified
			if (checkCanModifyAlarm(view))
			{
				// Reset the skip next alarm flag
				alarm!!.shouldSkipNextAlarm = false

				// Toggle the NFC button
				alarm!!.toggleUseNfc()

				// Setup the skip icon
				setSummarySkipNextAlarmIcon()

				// Call the listener
				onCardUseNfcChangedListener?.onCardUseNfcChanged(this, alarm!!)
			}
			// Alarm cannot be modified
			else
			{
				// Revert the button press
				(view as MaterialButton).toggle()
			}

		}
	}

	/**
	 * Setup the listener for when the NFC button is long clicked.
	 */
	private fun setupNfcButtonLongClickListener()
	{
		// Set the listener
		nfcButton.setOnLongClickListener {

			// Check if the alarm can be modified
			if (checkCanModifyAlarm(it))
			{
				// Call the listener
				onCardButtonLongClickedListener?.onCardButtonLongClicked(this, alarm!!, R.id.nacScanNfcTagDialog2)
			}

			true
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
			if (checkCanModifyAlarm(view))
			{
				// Reset the skip next alarm flag
				alarm!!.shouldSkipNextAlarm = false

				// Toggle the repeat button
				alarm!!.toggleRepeat()

				// Setup the skip icon and days string
				setSummarySkipNextAlarmIcon()
				setSummaryDaysView()

				// Call the listener
				onCardUseRepeatChangedListener?.onCardUseRepeatChanged(this, alarm!!)
			}
			// Alarm cannot be modified
			else
			{
				// Revert the button press
				(view as MaterialButton).toggle()
			}

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
			if (checkCanModifyAlarm(it))
			{
				// Call the listener
				onCardButtonLongClickedListener?.onCardButtonLongClicked(this, alarm!!, R.id.nacRepeatOptionsDialog2)
			}

			true
		}
	}

	/**
	 * Setup the snooze options button listener.
	 */
	private fun setupSnoozeOptionsListener()
	{
		// Set the listener
		snoozeOptionsButton.setOnClickListener { view ->

			// Check if the alarm can be modified
			if (checkCanModifyAlarm(view))
			{
				// Call the listener for when the button is clicked
				onCardSnoozeOptionsClickedListener?.onCardSnoozeOptionsClicked(this, alarm!!)
			}

		}
	}

	/**
	 * Setup the listener for when the switch is changed.
	 */
	private fun setupSwitchChangedListener()
	{
		// Set the listener
		switch.setOnCheckedChangeListener { button, state ->

			// Do nothing if binding
			if (isBinding)
			{
				return@setOnCheckedChangeListener
			}

			// Check if the alarm can be modified
			if (checkCanModifyAlarm(button))
			{
				// Reset the skip next alarm flag
				alarm!!.shouldSkipNextAlarm = false

				// Reset the snooze counter and dismiss early time
				alarm!!.snoozeCount = 0
				alarm!!.timeOfDismissEarlyAlarm = 0

				// Set the alarm enabled state
				alarm!!.isEnabled = state

				// Setup the views
				setSummaryDaysView()
				setSummarySkipNextAlarmIcon()

				// Call the listener
				onCardSwitchChangedListener?.onCardSwitchChanged(this, alarm!!)

				// Refresh dismiss buttons and collapse refresh if need be
				refreshExtraViewWithCollapse()
			}
			// Alarm cannot be modified
			else
			{
				// Change the state back
				button.isChecked = !state
			}

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
			if (checkCanModifyAlarm(view))
			{
				// Call the listener
				onCardTimeClickedListener?.onCardTimeClicked(this, alarm!!)
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
			if (checkCanModifyAlarm(view))
			{
				// Reset the skip next alarm flag
				alarm!!.shouldSkipNextAlarm = false

				// Toggle the vibrate button
				alarm!!.toggleVibrate()

				// Setup the skip icon
				setSummarySkipNextAlarmIcon()

				// Call the listener
				onCardUseVibrateChangedListener?.onCardUseVibrateChanged(this, alarm!!)
			}
			// Unable to modify the alarm
			else
			{
				// Change the state back
				(view as MaterialButton).toggle()
			}

		}
	}

	/**
	 * Setup the listener for when the vibrate button is long clicked.
	 */
	private fun setupVibrateButtonLongClickListener()
	{
		// Set the listener
		vibrateButton.setOnLongClickListener {

			// Check if the alarm can be modified
			if (checkCanModifyAlarm(it))
			{
				// Call the listener
				onCardButtonLongClickedListener?.onCardButtonLongClicked(this, alarm!!, R.id.nacVibrateOptionsDialog2)
			}

			true
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
				// Do nothing if binding or the volumes are already the same
				if (isBinding || (alarm!!.volume == progress))
				{
					return
				}

				// Alarm can be changed
				if (checkCanModifyAlarm())
				{
					// Reset the skip next alarm flag
					alarm!!.shouldSkipNextAlarm = false

					// Set the new volume
					alarm!!.volume = progress

					// Change the volume icon, if needed
					setVolumeImageView()

					// Setup the skip icon
					setSummarySkipNextAlarmIcon()
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

				// Call the listener
				onCardVolumeChangedListener?.onCardVolumeChanged(this@NacAlarmCardHolder, alarm!!)
			}

		})
	}

	/**
	 * Check if the extra view should be refreshed or not.
	 *
	 * @return True if the extra view should be refreshed, and False otherwise.
	 */
	fun shouldRefreshExtraView(): Boolean
	{
		// Alarm is in use (so "Dismiss" should be shown),
		// or will alarm soon (so "Dismiss early" should be shown),
		val extraVis = if (shouldShowExtraView()) View.VISIBLE else View.GONE

		// The extra view should be hidden so show the expand button on its
		// normal row (the summary row)
		val expandVis = if (extraVis == View.GONE) View.VISIBLE else View.INVISIBLE

		// The views are NOT the correct and expected visibilities
		return (extraBelowSummaryView.visibility != extraVis)
			|| (expandButton.visibility != expandVis)
	}

	/**
	 * Check whether the extra view should be shown or not.
	 *
	 * @return True if the extra view should be shown, and False otherwise.
	 */
	private fun shouldShowExtraView(): Boolean
	{
		return alarm!!.isInUse || alarm!!.willAlarmSoon()
	}

	/**
	 * Skip the next alarm.
	 */
	fun skipNextAlarm()
	{
		// Set the skip flag
		alarm!!.shouldSkipNextAlarm = true

		// Setup the skip icon
		setSummarySkipNextAlarmIcon()
		refreshExtraViewWithCollapse()

		// Call the update listener
		callOnCardUpdatedListener()
	}

	/**
	 * Toast the flashlight message.
	 */
	fun toastFlashlight(context: Context)
	{
		// Determine which message to show
		val messageId = if (alarm!!.shouldUseFlashlight)
		{
			R.string.message_flashlight_enabled
		}
		else
		{
			R.string.message_flashlight_disabled
		}

		// Toast the message
		quickToast(context, messageId)
	}

	/**
	 * Toast the NFC message.
	 */
	fun toastNfc(context: Context, allNfcTags: List<NacNfcTag>)
	{
		// Determine which message to show
		val message = if (alarm!!.shouldUseNfc)
		{
			// NFC ID
			if (alarm!!.nfcTagId.isNotEmpty())
			{
				// Find a matching NFC tag
				val tag = allNfcTags.firstOrNull { it.nfcId == alarm!!.nfcTagId }

				// Saved and named
				if (tag != null)
				{
					context.getString(R.string.message_nfc_required_saved, tag.name)
				}
				// Unsaved and no name
				else
				{
					context.getString(R.string.message_nfc_required_unsaved)
				}
			}
			// Use any
			else
			{
				context.getString(R.string.message_nfc_required_use_any)
			}
		}
		// Normal
		else
		{
			context.getString(R.string.message_nfc_optional)
		}

		// Toast the message
		quickToast(context, message)
	}

	/**
	 * Toast the NFC ID message.
	 */
	fun toastNfcId(context: Context)
	{
		// Determine which message to show
		val message = if (alarm!!.nfcTagId.isNotEmpty())
		{
			// Get the string to show a specific NFC tag
			val nfcId = context.getString(R.string.message_show_nfc_tag_id)

			"$nfcId: ${alarm!!.nfcTagId}"
		}
		else
		{
			// Get the string to show any NFC tag
			context.getString(R.string.message_nfc_required_use_any)
		}

		// Toast the message
		quickToast(context, message)
	}

	/**
	 * Toast the repeat message.
	 */
	fun toastRepeat(context: Context)
	{
		// Determine which message to show
		val messageId = if (alarm!!.shouldRepeat)
		{
			R.string.message_repeat_enabled
		}
		else
		{
			R.string.message_repeat_disabled
		}

		// Toast the message
		quickToast(context, messageId)
	}

	/**
	 * Toast the vibrate message.
	 */
	fun toastVibrate(context: Context)
	{
		// Determine which message to show
		val messageId = if (alarm!!.shouldVibrate)
		{
			R.string.message_vibrate_enabled
		}
		else
		{
			R.string.message_vibrate_disabled
		}


		// Toast the message
		quickToast(context, messageId)
	}

	/**
	 * Unskip the next alarm.
	 */
	fun unskipNextAlarm()
	{
		// Reset the skip next alarm flag
		alarm!!.shouldSkipNextAlarm = false

		// Setup the skip icon
		setSummarySkipNextAlarmIcon()

		// Call the update listener
		callOnCardUpdatedListener()
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