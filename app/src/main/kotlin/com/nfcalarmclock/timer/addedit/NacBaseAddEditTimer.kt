package com.nfcalarmclock.timer.addedit

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.options.dismissoptions.NacDismissOptionsDialog
import com.nfcalarmclock.alarm.options.name.NacNameDialog
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.timer.NacTimerViewModel
import com.nfcalarmclock.timer.active.NacActiveTimerService
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.timer.options.NacTimerOptionsDialog
import com.nfcalarmclock.system.media.NacMedia
import com.nfcalarmclock.view.calcContrastColor
import com.nfcalarmclock.view.performHapticFeedback
import com.nfcalarmclock.view.setupBackgroundColor
import com.nfcalarmclock.view.setupRippleColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
abstract class NacBaseAddEditTimer
	: Fragment()
{

	/**
	 * Navigation controller.
	 */
	protected val navController by lazy {
		(childFragmentManager.findFragmentById(R.id.options_content) as NavHostFragment).navController
	}

	/**
	 * Timer view model.
	 */
	protected val timerViewModel: NacTimerViewModel by viewModels()

	/**
	 * Timer.
	 */
	protected lateinit var timer: NacTimer

	/**
	 * Shared preferences.
	 */
	protected lateinit var sharedPreferences: NacSharedPreferences

	/**
	 * Hour textview.
	 */
	protected lateinit var hourTextView: TextView

	/**
	 * Minute textview.
	 */
	protected lateinit var minuteTextView: TextView

	/**
	 * Second textview.
	 */
	protected lateinit var secondsTextView: TextView

	/**
	 * Append the time to the timer.
	 */
	protected fun appendTime(value: CharSequence)
	{
		// Get the hour, min, and sec
		val hour = hourTextView.text.toString()
		val min = minuteTextView.text
		val sec = secondsTextView.text

		// Time is already full
		if (!hour.startsWith("0"))
		{
			return
		}

		// Build the new time
		val newTime = "${hour.toInt()}$min$sec$value"

		// Set the new time
		hourTextView.text = newTime.substring(0, 2)
		minuteTextView.text = newTime.substring(2, 4)
		secondsTextView.text = newTime.substring(4, 6)

		// Haptic feedback
		view?.performHapticFeedback()
	}

	/**
	 * Delete the seconds digit in the time.
	 */
	protected fun deleteTime()
	{
		// Get the hour, min, and sec
		val hour = hourTextView.text
		val min = minuteTextView.text
		val sec = secondsTextView.text[0]

		// Build the new time
		val newTime = "0$hour$min$sec"

		// Set the new time
		hourTextView.text = newTime.substring(0, 2)
		minuteTextView.text = newTime.substring(2, 4)
		secondsTextView.text = newTime.substring(4, 6)

		// Haptic feedback
		view?.performHapticFeedback()
	}

	/**
	 * Initialize the timer that will be used in the fragment.
	 */
	protected abstract fun initTimer()

	/**
	 * Navigate to a destination in the navigation graph, and save the alarm.
	 */
	private fun navigateTo(destinationId: Int)
	{
		// Get the navigation controller
		val navController = findNavController()

		// Navigate to the destination
		NacTimerOptionsDialog.navigateTo(navController, destinationId, timer)
			?.observe(viewLifecycleOwner) { t ->
				timer = t
			}
	}

	/**
	 * Called to create the root view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.frg_add_edit_timer, container, false)
	}

	/**
	 * Called when the fragment is created.
	 */
	@OptIn(UnstableApi::class)
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the context and shared preferences
		val context = requireContext()
		sharedPreferences = NacSharedPreferences(context)

		// Setup the initial media that should be used for a timer, if it has not already
		// been set or changed
		setupInitialMediaForTimer()

		// Get the views
		hourTextView = view.findViewById(R.id.timer_hour)
		minuteTextView = view.findViewById(R.id.timer_minute)
		secondsTextView = view.findViewById(R.id.timer_seconds)
		val repeatButton: MaterialButton = view.findViewById(R.id.timer_repeat)
		val vibrateButton: MaterialButton = view.findViewById(R.id.timer_vibrate)
		val nfcButton: MaterialButton = view.findViewById(R.id.timer_nfc)
		val flashlightButton: MaterialButton = view.findViewById(R.id.timer_flashlight)
		val mediaButton: MaterialButton = view.findViewById(R.id.timer_media)
		val volumeImageView: ImageView = view.findViewById(R.id.timer_volume_icon)
		val volumeSeekBar: SeekBar = view.findViewById(R.id.timer_volume_slider)
		val nameButton: MaterialButton = view.findViewById(R.id.timer_name)

		// Setup the views
		initTimer()
		setupHourMinuteSecondTextViews()
		setupNumberPadButtons()
		setupStartButton()
		setupMoreButton()
		setupMoreOptionsContainerVisibility()
		setupRepeatButton(repeatButton)
		setupVibrateButton(vibrateButton)
		setupNfcButton(nfcButton)
		setupFlashlightButton(flashlightButton)
		setupMediaButton(mediaButton)
		setupVolume(volumeSeekBar, volumeImageView)
		setupName(nameButton)
		setupOptionsSection(view)
	}

	/**
	 * Set the timer duration.
	 */
	protected fun setDuration()
	{
		// Get the hour, minutes, and seconds
		val hour = hourTextView.text.toString().toLong()
		val minute = minuteTextView.text.toString().toLong()
		val seconds = secondsTextView.text.toString().toLong()

		// Set the duration
		timer.duration = seconds + minute*60 + hour*3600
	}

	/**
	 * Set the message and alpha of the name view.
	 */
	private fun setNameMessageAndAlpha(button: MaterialButton)
	{
		// Get the name message
		val message = timer.nameNormalized.ifEmpty {
			resources.getString(R.string.title_alarm_name)
		}

		// Get the alpha that the view should be
		val alpha = if (timer.nameNormalized.isNotEmpty()) 1.0f else 0.3f

		// Set the name as text for the button, and the alpha
		button.text = message
		button.alpha = alpha
	}

	/**
	 * Set the volume image view.
	 */
	private fun setVolumeImageView(imageView: ImageView)
	{
		// Set the resource ID depending on the volume level
		val resId: Int = when (timer.volume)
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

		// Set the new resource ID and tag
		if ((imageView.tag == null) || (imageView.tag as Int != resId))
		{
			imageView.setImageResource(resId)
			imageView.tag = resId
		}
	}

	/**
	 * Setup the flashlight button.
	 */
	private fun setupFlashlightButton(button: MaterialButton)
	{
		// Color
		button.setupRippleColor(sharedPreferences)

		// Initial state
		button.isChecked = timer.shouldUseFlashlight

		// Toggle on click
		button.setOnClickListener {
			timer.toggleUseFlashlight()
		}

		// Show the quick navigate dialog on long click
		button.setOnLongClickListener {
			navigateTo(R.id.nacFlashlightOptionsDialog3)
			true
		}
	}

	/**
	 * Setup the hour, minute, and seconds textviews.
	 */
	protected fun setupHourMinuteSecondTextViews()
	{
		// Get the hour, minute, and seconds
		var (hour, minute, seconds) = NacCalendar.getTimerHourMinuteSecondsZeroPadded(timer.duration)
		val zeros = resources.getString(R.string.number00)

		// Set the values to "00" if they are empty
		if (hour.isEmpty())
		{
			hour = zeros
		}

		if (minute.isEmpty())
		{
			minute = zeros
		}

		if (seconds.isEmpty())
		{
			seconds = zeros
		}

		// Always zero pad seconds
		seconds = seconds.padStart(2, '0')


		// Set the hour, minute, and seconds
		hourTextView.text = hour
		minuteTextView.text = minute
		secondsTextView.text = seconds
	}

	/**
	 * Setup the initial media path for a timer if it has not already been set.
	 */
	protected fun setupInitialMediaForTimer()
	{
		// Media path or type have already been set for a timer
		if (sharedPreferences.mediaPathTimer.isNotEmpty()
			|| sharedPreferences.mediaTypeTimer != NacMedia.TYPE_RINGTONE)
		{
			return
		}

		// Get all ringtones
		val context = requireContext()
		val ringtones = NacMedia.getRingtones(context)

		// Iterate over each ringtone
		for ((title, path) in ringtones)
		{
			// Skip if path is empty
			if (path.isEmpty())
			{
				continue
			}

			// Set the default on the first item and then break out of the loop
			sharedPreferences.mediaPathTimer = path
			sharedPreferences.mediaTitleTimer = title
			break
		}
	}

	/**
	 * Setup the media button.
	 */
	private fun setupMediaButton(button: MaterialButton)
	{
		// Default message and alpha
		val mediaTitle = timer.mediaTitle
		var message = mediaTitle
		var alpha = 1f

		// No media selected
		if (mediaTitle.isEmpty())
		{
			message = resources.getString(R.string.description_media)
			alpha = 0.3f
		}

		// Set the text and alpha
		button.text = message
		button.alpha = alpha

		// Show the media activity
		button.setOnClickListener {
			// TODO: How will this work with the fragment? Maybe need to do something in the navigation graph?
		}
	}

	/**
	 * Setup the more button.
	 */
	protected fun setupMoreButton()
	{
		// Get the views
		val view = requireView()
		val moreButton: MaterialButton = view.findViewById(R.id.timer_more_button)

		// Setup more button click
		moreButton.setOnClickListener {
			println("SCROLLING DOWN")
			requireView().findViewById<ScrollView>(R.id.timer_scrollview).fullScroll(ScrollView.FOCUS_DOWN)
			//NacTimerCardOptionsDialog.navigate(navController, timer)
			//	?.observe(viewLifecycleOwner) { t ->
			//		println("EHHLOOOOOOOOOOOOOOOOO")
			//		t.print()
			//		timer = t
			//	}
		}
	}

	/**
	 * Setup margin of hour textview so that it physically takes up all the space at the
	 * top, instead of just floating in the space due to the constraints. This will allow
	 * the more options container to seemlessly become visible while off screen, but it
	 * can be scrolled to easily.
	 */
	private fun setupMoreOptionsContainerVisibility()
	{
		// Get the views
		val view = requireView()
		val scrollview: ScrollView = view.findViewById(R.id.timer_scrollview)
		val numberpadContainer: RelativeLayout = view.findViewById(R.id.timer_numberpad_container)
		val moreOptionsContainer: LinearLayout = view.findViewById(R.id.timer_more_options_container)

		// More options container is already visible so do nothing
		if (moreOptionsContainer.isVisible)
		{
			return
		}

		// Remove constraint toptotopof from hour and determine what the padding should be
		// After the hour textview has been laid out
		scrollview.doOnLayout {

			// Get the location of the hour textview and numberpad container on screen
			val hourLocation = IntArray(2)
			val numberpadContainerLocation = IntArray(2)

			hourTextView.getLocationOnScreen(hourLocation)
			numberpadContainer.getLocationOnScreen(numberpadContainerLocation)

			// Update the top and bottom margins to match the current spacing of the
			// views while the more options container is invisible. That way, when making
			// it visible, it will be shown off screen because every view is taking up
			// the proper amount of space
			hourTextView.updateLayoutParams<ViewGroup.MarginLayoutParams> {

				// Set the new margin
				val tinyDimen = resources.getDimension(R.dimen.tiny).toInt()
				bottomMargin = (numberpadContainerLocation[1] - hourLocation[1] - tinyDimen) / 2
				topMargin = bottomMargin

				// Make the more options container visible
				moreOptionsContainer.visibility = View.VISIBLE

			}

		}
	}

	/**
	 * Setup the name.
	 */
	private fun setupName(button: MaterialButton)
	{
		// Set the message and alpha
		setNameMessageAndAlpha(button)

		// Show a dialog to set a new name
		button.setOnClickListener { view ->

			NacNameDialog.create(
				timer.name,
				onNameEnteredListener = { name ->
					timer.name = name
					setNameMessageAndAlpha(button)
				})
				.show(parentFragmentManager, NacNameDialog.TAG)

		}
	}

	/**
	 * Setup the NFC button.
	 */
	private fun setupNfcButton(button: MaterialButton)
	{
		// Color
		button.setupRippleColor(sharedPreferences)

		// Initial state
		button.isChecked = timer.shouldUseNfc

		// Click listener
		button.setOnClickListener {
			timer.toggleUseNfc()
		}

		// Show the quick navigate dialog on long click
		button.setOnLongClickListener {
			navigateTo(R.id.nacScanNfcTagDialog3)
			true
		}
	}

	/**
	 * Setup the numberpad buttons.
	 */
	protected fun setupNumberPadButtons()
	{
		// Get the views
		val view = requireView()
		val numpad1: MaterialButton = view.findViewById(R.id.timer_numberpad1)
		val numpad2: MaterialButton = view.findViewById(R.id.timer_numberpad2)
		val numpad3: MaterialButton = view.findViewById(R.id.timer_numberpad3)
		val numpad4: MaterialButton = view.findViewById(R.id.timer_numberpad4)
		val numpad5: MaterialButton = view.findViewById(R.id.timer_numberpad5)
		val numpad6: MaterialButton = view.findViewById(R.id.timer_numberpad6)
		val numpad7: MaterialButton = view.findViewById(R.id.timer_numberpad7)
		val numpad8: MaterialButton = view.findViewById(R.id.timer_numberpad8)
		val numpad9: MaterialButton = view.findViewById(R.id.timer_numberpad9)
		val numpad0: MaterialButton = view.findViewById(R.id.timer_numberpad0)
		val numpadDel: MaterialButton = view.findViewById(R.id.timer_numberpad_del)

		// Setup numberpad colors
		numpad1.setupRippleColor(sharedPreferences)
		numpad2.setupRippleColor(sharedPreferences)
		numpad3.setupRippleColor(sharedPreferences)
		numpad4.setupRippleColor(sharedPreferences)
		numpad5.setupRippleColor(sharedPreferences)
		numpad6.setupRippleColor(sharedPreferences)
		numpad7.setupRippleColor(sharedPreferences)
		numpad8.setupRippleColor(sharedPreferences)
		numpad9.setupRippleColor(sharedPreferences)
		numpad0.setupRippleColor(sharedPreferences)
		numpadDel.setupRippleColor(sharedPreferences)

		// Setup button click listeners
		numpad1.setOnClickListener { appendTime(numpad1.text) }
		numpad2.setOnClickListener { appendTime(numpad2.text) }
		numpad3.setOnClickListener { appendTime(numpad3.text) }
		numpad4.setOnClickListener { appendTime(numpad4.text) }
		numpad5.setOnClickListener { appendTime(numpad5.text) }
		numpad6.setOnClickListener { appendTime(numpad6.text) }
		numpad7.setOnClickListener { appendTime(numpad7.text) }
		numpad8.setOnClickListener { appendTime(numpad8.text) }
		numpad9.setOnClickListener { appendTime(numpad9.text) }
		numpad0.setOnClickListener { appendTime(numpad0.text) }
		numpadDel.setOnClickListener { deleteTime() }

		// Setup long press
		numpadDel.setOnLongClickListener {

			// Delete all values on long press
			val doubleZero = resources.getString(R.string.number00)
			hourTextView.text = doubleZero
			minuteTextView.text = doubleZero
			secondsTextView.text = doubleZero

			true
		}
	}

	/**
	 * Setup the options section with the divider and stop and settings options buttons.
	 */
	private fun setupOptionsSection(view: View)
	{
		// Get the views
		val stopOptionsButton: MaterialButton = view.findViewById(R.id.timer_stop_options)
		val settingsOptionsButton: MaterialButton = view.findViewById(R.id.timer_settings_options)
		val optionsDivider1: View = view.findViewById(R.id.timer_options_divider1)
		val optionsDivider2: View = view.findViewById(R.id.timer_options_divider2)

		// Divider color
		val themeColor = ColorStateList.valueOf(sharedPreferences.themeColor)
		optionsDivider1.backgroundTintList = themeColor
		optionsDivider2.backgroundTintList = themeColor

		// Stop options dialog on click
		stopOptionsButton.setOnClickListener {

			NacDismissOptionsDialog.create(
				timer,
				onSaveAlarmListener = {
					// TODO: Add this logic
					//updateAlarm(it)
				})
				.show(parentFragmentManager, NacDismissOptionsDialog.TAG)

		}

		// Settings options dialog on click
		settingsOptionsButton.setOnClickListener {
			navigateTo(R.id.nacTimerOptionsDialog)
		}
	}

	/**
	 * Setup the repeat button.
	 */
	private fun setupRepeatButton(button: MaterialButton)
	{
		// Color
		button.setupRippleColor(sharedPreferences)

		// Initial state
		button.isChecked = timer.shouldRepeat

		// Click listener
		button.setOnClickListener {
			timer.toggleRepeat()
		}

		// TODO: Should timer have repeat options on long click?
	}

	/**
	 * Setup the save button.
	 */
	protected fun setupSaveButton()
	{
		// Get the view
		val view = requireView()
		val saveButton: MaterialButton = view.findViewById(R.id.timer_save_button)

		// Show the view
		saveButton.visibility = View.VISIBLE

		// On click listener
		saveButton.setOnClickListener {

			// Set the duration
			setDuration()
			println("Save set duration : ${timer.id} | ${timer.duration}")

			// Update the timer and then go back to the show timers fragment
			timerViewModel.update(timer) {
				findNavController().popBackStack(R.id.nacShowTimersFragment, false)
			}

		}
	}

	/**
	 * Setup the start button.
	 */
	@OptIn(UnstableApi::class)
	protected fun setupStartButton()
	{
		// Get the view
		val context = requireContext()
		val view = requireView()
		val startButton: MaterialButton = view.findViewById(R.id.timer_start_button)

		// Get the contrast color
		val contrastColor = calcContrastColor(sharedPreferences.themeColor)

		// Setup the view
		startButton.visibility = View.VISIBLE
		startButton.iconTint = ColorStateList.valueOf(contrastColor)
		startButton.setupBackgroundColor(sharedPreferences)

		// On click listener
		startButton.setOnClickListener {

			// Set the duration
			setDuration()

			// Save the timer
			lifecycleScope.launch {

				// Unit to start the timer
				val unit = {
					NacActiveTimerService.startTimerService(context, timer)
					findNavController().navigate(R.id.nacActiveTimerFragment, timer.toBundle())
				}

				// Update the timer
				if (timerViewModel.hasTimer(timer.id))
				{
					timerViewModel.update(timer, unit = unit)
				}
				// Add the timer to the table
				else
				{
					timerViewModel.insert(timer, unit = unit)
				}

			}

		}
	}

	/**
	 * Setup the vibrate button.
	 */
	private fun setupVibrateButton(button: MaterialButton)
	{
		// Color
		button.setupRippleColor(sharedPreferences)

		// Initial state
		button.isChecked = timer.shouldVibrate

		// Click listener
		button.setOnClickListener {
			timer.toggleVibrate()
		}

		// Show the quick navigate dialog on long click
		button.setOnLongClickListener {
			navigateTo(R.id.nacVibrateOptionsDialog3)
			true
		}
	}

	/**
	 * Setup the volume.
	 */
	private fun setupVolume(seekBar: SeekBar, imageView: ImageView)
	{
		// Initial state
		seekBar.progress = timer.volume
		setVolumeImageView(imageView)

		// Volume change listener
		seekBar.setOnSeekBarChangeListener(object: OnSeekBarChangeListener {

			/**
			 * Progress is changed.
			 */
			override fun onProgressChanged(
				seekBar: SeekBar,
				progress: Int,
				fromUser: Boolean)
			{
				// Volume did not change
				if (timer.volume == progress)
				{
					return
				}

				// Set the new volume
				timer.volume = progress

				// Change the volume icon, if needed
				setVolumeImageView(imageView)
			}

			/**
			 * Start touching the seekbar.
			 */
			override fun onStartTrackingTouch(seekBar: SeekBar) {}

			/**
			 * Stop touching the seekbar.
			 */
			override fun onStopTrackingTouch(seekBar: SeekBar) {}

		})
	}

}