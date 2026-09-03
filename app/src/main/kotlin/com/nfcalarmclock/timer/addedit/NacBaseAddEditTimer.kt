package com.nfcalarmclock.timer.addedit

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.normalizeName
import com.nfcalarmclock.alarm.options.name.NacNameDialog
import com.nfcalarmclock.nfc.NacNfcTagViewModel
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.system.addMediaInfo
import com.nfcalarmclock.system.getMediaArtist
import com.nfcalarmclock.system.getMediaPath
import com.nfcalarmclock.system.getMediaTitle
import com.nfcalarmclock.system.getMediaType
import com.nfcalarmclock.system.getRecursivelyPlayMedia
import com.nfcalarmclock.system.getShuffleMedia
import com.nfcalarmclock.system.media.NacMedia
import com.nfcalarmclock.system.media.buildLocalMediaPath
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.timer.NacTimerViewModel
import com.nfcalarmclock.timer.active.NacActiveTimerService
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.timer.options.NacTimerOptionsDialog
import com.nfcalarmclock.timer.options.dismissoptions.NacDismissOptionsDialog
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.calcContrastColor
import com.nfcalarmclock.view.performHapticFeedback
import com.nfcalarmclock.view.quickToast
import com.nfcalarmclock.view.setupBackgroundColor
import com.nfcalarmclock.view.setupRippleColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Base class to add or edit a timer.
 */
@AndroidEntryPoint
abstract class NacBaseAddEditTimer
	: Fragment()
{

	/**
	 * Navigation controller.
	 */
	private val navController by lazy {
		(childFragmentManager.findFragmentById(R.id.options_content) as NavHostFragment).navController
	}

	/**
	 * Timer view model.
	 */
	protected val timerViewModel: NacTimerViewModel by viewModels()

	/**
	 * NFC tag view model.
	 */
	protected val nfcTagViewModel: NacNfcTagViewModel by viewModels()

	/**
	 * Timer.
	 */
	protected lateinit var timer: NacTimer

	/**
	 * Shared preferences.
	 */
	protected lateinit var sharedPreferences: NacSharedPreferences

	/**
	 * Bottom navigation.
	 */
	private lateinit var bottomNavigation: BottomNavigationView

	/**
	 * Scrollview.
	 */
	protected lateinit var scrollView: NestedScrollView

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
	 * Number pad container.
	 */
	protected lateinit var numpadContainer: LinearLayout

	/**
	 * Number pad buttons.
	 */
	protected lateinit var numpad1: MaterialButton
	protected lateinit var numpad2: MaterialButton
	protected lateinit var numpad3: MaterialButton
	protected lateinit var numpad4: MaterialButton
	protected lateinit var numpad5: MaterialButton
	protected lateinit var numpad6: MaterialButton
	protected lateinit var numpad7: MaterialButton
	protected lateinit var numpad8: MaterialButton
	protected lateinit var numpad9: MaterialButton
	protected lateinit var numpad0: MaterialButton
	protected lateinit var numpad00: MaterialButton
	protected lateinit var numpadDel: MaterialButton

	/**
	 * Start button.
	 */
	protected lateinit var startButton: MaterialButton

	/**
	 * More options container.
	 */
	protected lateinit var moreOptionsContainer: LinearLayout

	/**
	 * Media button.
	 */
	protected lateinit var mediaButton: MaterialButton

	/**
	 * Repeat button.
	 */
	protected lateinit var repeatButton: MaterialButton

	/**
	 * Vibrate button.
	 */
	protected lateinit var vibrateButton: MaterialButton

	/**
	 * NFC button.
	 */
	protected lateinit var nfcButton: MaterialButton

	/**
	 * Flashlight button.
	 */
	protected lateinit var flashlightButton: MaterialButton

	/**
	 * Stop options button.
	 */
	protected lateinit var stopOptionsButton: MaterialButton

	/**
	 * Settings options button.
	 */
	protected lateinit var settingsOptionsButton: MaterialButton

	/**
	 * Name of the timer before it is saved.
	 */
	protected var nameBeforeSaving: String = ""

	/**
	 * Whether to scroll the scrollview up or not.
	 */
	protected var shouldScrollUp: Boolean = true

	/**
	 * Append the time to the timer.
	 */
	protected fun appendTime(value: CharSequence)
	{
		// Get the current hour, min, and sec
		val hour = hourTextView.text.toString()
		val min = minuteTextView.text
		val sec = secondsTextView.text
		val currentTime = "$hour$min$sec"
		var newTime= currentTime

		// Build the new time
		value.forEach {

			// Time is already full, do nothing
			if (!newTime.startsWith("0"))
			{
				return@forEach
			}

			// Remove the first digit and append the current digit of the passed in value
			newTime  = newTime.substring(1, 6)
			newTime += it

		}

		// Set the new time
		hourTextView.text = newTime.substring(0, 2)
		minuteTextView.text = newTime.substring(2, 4)
		secondsTextView.text = newTime.substring(4, 6)

		// Haptic feedback
		view?.performHapticFeedback()
	}

	/**
	 * Delay scrolling down.
	 *
	 * This happens after picking media so scrolling down ensures that those more option
	 * views are shown again.
	 */
	private fun delayScrollingDown()
	{
		scrollView.doOnNextLayout{
			lifecycleScope.launch {

				// Delay for up to 450 milliseconds
				var i = 0
				do
				{
					delay(150.milliseconds)
					i++
				} while (!moreOptionsContainer.isVisible && (i < 3))

				// Scroll down and hide the bottom navigation view
				scrollView.smoothScrollTo(0, moreOptionsContainer.height)
				//bottomNavigation.slideDown(250)

				// Set the scroll flags
				shouldScrollUp = true

			}
		}
	}

	/**
	 * Delay scrolling up.
	 *
	 * This happens after the more options container is shown to ensure that the bottom
	 * navigation view is shown.
	 */
	private fun delayScrollingUp()
	{
		moreOptionsContainer.doOnNextLayout {

			// Scroll up and show the bottom navigation view
			scrollView.smoothScrollTo(0, 0)
			//bottomNavigation.slideUp(250)

		}
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
		// Navigate to the destination
		NacTimerOptionsDialog.navigateTo(navController, destinationId, timer)
			?.observe(viewLifecycleOwner) { t ->
				timer = t
			}
	}

	/**
	 * Navigate to the media picker.
	 */
	abstract fun navigateToMediaPicker(bundle: Bundle)

	/**
	 * Create the view.
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
	 * View is created.
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
		bottomNavigation = requireActivity().findViewById(R.id.bottom_navigation)
		scrollView = view.findViewById(R.id.timer_scrollview)
		hourTextView = view.findViewById(R.id.timer_hour)
		minuteTextView = view.findViewById(R.id.timer_minute)
		secondsTextView = view.findViewById(R.id.timer_seconds)
		moreOptionsContainer = view.findViewById(R.id.timer_more_options_container)
		repeatButton = view.findViewById(R.id.timer_repeat)
		vibrateButton = view.findViewById(R.id.timer_vibrate)
		nfcButton = view.findViewById(R.id.timer_nfc)
		flashlightButton = view.findViewById(R.id.timer_flashlight)
		mediaButton = view.findViewById(R.id.timer_media)
		stopOptionsButton = view.findViewById(R.id.timer_stop_options)
		settingsOptionsButton = view.findViewById(R.id.timer_settings_options)
		val volumeImageView: ImageView = view.findViewById(R.id.timer_volume_icon)
		val volumeSeekBar: SeekBar = view.findViewById(R.id.timer_volume_slider)
		val nameButton: MaterialButton = view.findViewById(R.id.timer_name)

		// Timer needs to be initialized
		if (!this::timer.isInitialized)
		{
			initTimer()
		}

		// Setup the views
		setupHourMinuteSecondTextViews()
		setupNumberPadButtons()
		setupStartButton()
		setupMoreButton()
		setupMoreOptionsContainerVisibility()
		setupSaveButton()
		setupTimeAndNumpadButtonSizes()
		setupRepeatButton()
		setupVibrateButton()
		setupNfcButton()
		setupFlashlightButton()
		setupMediaButton()
		setupVolume(volumeSeekBar, volumeImageView)
		setupName(nameButton)
		setupOptionsSection(view)
		setupMediaPickerObserver()
		setupButtonLabels()
	}

	/**
	 * Setup the sizes of the time and number pad buttons.
	 */
	protected fun setupTimeAndNumpadButtonSizes()
	{
		// Ensure the number pad is laid out
		numpadContainer.doOnLayout {

			// Clear the percent height constraint and make the height wrap content
			numpadContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
				matchConstraintPercentHeight = -1f
				height = ConstraintLayout.LayoutParams.WRAP_CONTENT
			}

			// Calculate a good button size based on the 3x5 columns and rows, and use the min
			// of the width/height so that the button can be circular
			val totalWidth = numpadContainer.width - numpadContainer.paddingStart - numpadContainer.paddingEnd
			val totalHeight = numpadContainer.height - numpadContainer.paddingTop - numpadContainer.paddingBottom
			val cellWidth = totalWidth / 3
			val cellHeight = totalHeight / 5
			val buttonSize = minOf(cellWidth, cellHeight)

			// All buttons that will have their size changed
			val allButtons = arrayOf(
				numpad1, numpad2, numpad3,
				numpad4, numpad5, numpad6,
				numpad7, numpad8, numpad9,
				numpad00, numpad0, numpadDel,
				startButton,
			)

			// Update the size of all the buttons
			allButtons.forEach { b ->

				// Update the width and height
				val params = b.layoutParams as LinearLayout.LayoutParams
				params.width = buttonSize
				params.height = buttonSize

				// Force layout update
				b.layoutParams = params

			}

			// Ensure the hour is laid out
			hourTextView.doOnLayout {

				// Update the attributes of the hour (this is the main time view that the other
				// time views follow)
				hourTextView.updateLayoutParams<ConstraintLayout.LayoutParams> {

					// Clear the percent height constraint
					matchConstraintPercentHeight = -1f

					// Make the height the remaining in the scrollview, though subtracting
					// hour height is odd. Shouldn't that be the margin instead? It seems to
					// be working for now, so I won't change it yet
					//height = ConstraintLayout.LayoutParams.WRAP_CONTENT
					height = scrollView.height - numpadContainer.height - hourTextView.height

				}

			}
		}

	}

	/**
	 * Save the timer.
	 */
	protected fun saveTimer(unit: () -> Unit)
	{
		lifecycleScope.launch {

			// Update the timer
			if (timerViewModel.exists(timer.id))
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
	 * Set the media message and alpha of the media view.
	 */
	protected fun setMediaMessageAndAlpha()
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
		mediaButton.text = message
		mediaButton.alpha = alpha
	}

	/**
	 * Set the message and alpha of the name view.
	 */
	private fun setNameMessageAndAlpha(button: MaterialButton)
	{
		// Normalize the name
		val nameNormalized = nameBeforeSaving.normalizeName()

		// Get the name message
		val message = nameNormalized.ifEmpty {
			resources.getString(R.string.title_alarm_name)
		}

		// Get the alpha that the view should be
		val alpha = calcAlpha(nameNormalized.isNotEmpty())

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
	 * Setup the button labels.
	 */
	private fun setupButtonLabels()
	{
		// Show labels
		if (sharedPreferences.shouldShowCardButtonLabels)
		{
			repeatButton.text = resources.getString(R.string.title_alarm_repeat)
			vibrateButton.text = resources.getString(R.string.title_alarm_vibrate)
			nfcButton.text = resources.getString(R.string.title_alarm_nfc)
			flashlightButton.text = resources.getString(R.string.action_alarm_option_flashlight)
			stopOptionsButton.text = resources.getString(R.string.action_alarm_dismiss)
			settingsOptionsButton.text = resources.getString(R.string.title_settings)
		}
		// Only show icons. Do not show labels
		else
		{
			repeatButton.text = ""
			vibrateButton.text = ""
			nfcButton.text = ""
			flashlightButton.text = ""
			stopOptionsButton.text = ""
			settingsOptionsButton.text = ""
		}
	}

	/**
	 * Setup the flashlight button.
	 */
	private fun setupFlashlightButton()
	{
		// Color
		flashlightButton.setupRippleColor(sharedPreferences)

		// Initial state
		flashlightButton.isChecked = timer.shouldUseFlashlight

		// Hide the button and do nothing else
		if (!sharedPreferences.shouldShowFlashlightButton)
		{
			flashlightButton.visibility = View.GONE
			return
		}

		// Toggle on click
		flashlightButton.setOnClickListener {
			timer.toggleUseFlashlight()
			timer.toastFlashlight(requireContext())
		}

		// Show the quick navigate dialog on long click
		flashlightButton.setOnLongClickListener {
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

		// Always zero pad when setting up
		hour = hour.padStart(2, '0')
		minute = minute.padStart(2, '0')
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
	private fun setupMediaButton()
	{
		// Set the message and alpha
		setMediaMessageAndAlpha()

		// Show the media picker
		mediaButton.setOnClickListener {

			// Create a bundle with the media info
			val bundle = Bundle()
				.addMediaInfo(
					timer.mediaPath,
					timer.mediaArtist,
					timer.mediaTitle,
					timer.mediaType,
					timer.shouldShuffleMedia,
					timer.shouldRecursivelyPlayMedia)

			// Navigate to the media picker
			navigateToMediaPicker(bundle)

		}
	}

	/**
	 * Setup an observer for when media is picked in the media picker.
	 */
	private fun setupMediaPickerObserver()
	{
		// Get the saved state handle
		val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle

		// Set the observer for the media picker
		savedStateHandle
			?.getLiveData<Bundle>("YOYOYO")
			?.observe(viewLifecycleOwner) { result ->

				// Disable the scroll up flag
				shouldScrollUp = false

				// Set all the media information
				val context = requireContext()
				timer.mediaPath = result.getMediaPath()
				timer.mediaArtist = result.getMediaArtist()
				timer.mediaTitle = result.getMediaTitle()
				timer.mediaType	= result.getMediaType()
				timer.localMediaPath = buildLocalMediaPath(context, timer.mediaArtist, timer.mediaTitle, timer.mediaType)
				timer.shouldShuffleMedia = result.getShuffleMedia()
				timer.shouldRecursivelyPlayMedia = result.getRecursivelyPlayMedia()

				// Set the media message
				setMediaMessageAndAlpha()

				// Scroll down
				delayScrollingDown()

				// Remove the item from the saved state handle so the same item does not
				// get observed again
				savedStateHandle.remove<Bundle>("YOYOYO")

			}
	}

	/**
	 * Setup the more button.
	 */
	protected fun setupMoreButton()
	{
		// Get the view
		val view = requireView()
		val moreButton: MaterialButton = view.findViewById(R.id.timer_more_button)

		// Setup more button click
		moreButton.setOnClickListener {

			// Scroll down and hide the bottom navigation view
			scrollView.smoothScrollTo(0, moreOptionsContainer.height)
			//bottomNavigation.slideDown(250)

			// Haptic feedback
			it.performHapticFeedback()

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
		val moreOptionsDivider: View = view.findViewById(R.id.timer_more_options_divider)

		// More options container is already visible so do nothing
		if (moreOptionsContainer.isVisible)
		{
			return
		}

		// Setup divider between the timer and more options
		moreOptionsDivider.setupBackgroundColor(sharedPreferences)

		// Remove constraint toptotopof from hour and determine what the padding should be
		// After the hour textview has been laid out
		scrollView.doOnLayout {

			// Update the top and bottom margins to match the current spacing of the
			// views while the more options container is invisible. That way, when making
			// it visible, it will be shown off screen because every view is taking up
			// the proper amount of space
			hourTextView.updateLayoutParams<ViewGroup.MarginLayoutParams> {

				// Make the more options container visible
				moreOptionsContainer.visibility = View.VISIBLE
				moreOptionsDivider.visibility = View.VISIBLE

				// Scroll up to
				if (shouldScrollUp)
				{
					delayScrollingUp()
				}

			}

		}
	}

	/**
	 * Setup the name.
	 */
	private fun setupName(button: MaterialButton)
	{
		// Set the name
		nameBeforeSaving = timer.name

		// Set the message and alpha
		setNameMessageAndAlpha(button)

		// Show a dialog to set a new name
		button.setOnClickListener { _ ->

			NacNameDialog.create(
				nameBeforeSaving,
				onNameEnteredListener = { name ->
					nameBeforeSaving = name
					setNameMessageAndAlpha(button)
				})
				.show(parentFragmentManager, NacNameDialog.TAG)

		}
	}

	/**
	 * Setup the NFC button.
	 */
	private fun setupNfcButton()
	{
		// Color
		nfcButton.setupRippleColor(sharedPreferences)

		// Initial state
		nfcButton.isChecked = timer.shouldUseNfc

		// Hide the button and do nothing else
		if (!sharedPreferences.shouldShowNfcButton)
		{
			nfcButton.visibility = View.GONE
			return
		}

		// Click listener
		nfcButton.setOnClickListener {
			timer.toggleUseNfc()
			lifecycleScope.launch {
				timer.toastNfc(requireContext(), nfcTagViewModel.getAllNfcTags())
			}
		}

		// Show the quick navigate dialog on long click
		nfcButton.setOnLongClickListener {
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
		numpadContainer = view.findViewById(R.id.timer_numberpad_container)
		numpad1 = view.findViewById(R.id.timer_numberpad1)
		numpad2 = view.findViewById(R.id.timer_numberpad2)
		numpad3 = view.findViewById(R.id.timer_numberpad3)
		numpad4 = view.findViewById(R.id.timer_numberpad4)
		numpad5 = view.findViewById(R.id.timer_numberpad5)
		numpad6 = view.findViewById(R.id.timer_numberpad6)
		numpad7 = view.findViewById(R.id.timer_numberpad7)
		numpad8 = view.findViewById(R.id.timer_numberpad8)
		numpad9 = view.findViewById(R.id.timer_numberpad9)
		numpad0 = view.findViewById(R.id.timer_numberpad0)
		numpad00 = view.findViewById(R.id.timer_numberpad00)
		numpadDel = view.findViewById(R.id.timer_numberpad_del)

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
		numpad00.setupRippleColor(sharedPreferences)
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
		numpad00.setOnClickListener { appendTime(numpad00.text) }
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
		val optionsDivider1: View = view.findViewById(R.id.timer_options_divider1)
		val optionsDivider2: View = view.findViewById(R.id.timer_options_divider2)

		// Divider color
		val themeColor = ColorStateList.valueOf(sharedPreferences.themeColor)
		optionsDivider1.backgroundTintList = themeColor
		optionsDivider2.backgroundTintList = themeColor

		// Stop/dismiss options dialog on click
		stopOptionsButton.setOnClickListener {
			NacDismissOptionsDialog.create(timer)
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
	private fun setupRepeatButton()
	{
		// Color
		repeatButton.setupRippleColor(sharedPreferences)

		// Initial state
		repeatButton.isChecked = timer.shouldRepeat

		// Click listener
		repeatButton.setOnClickListener {
			timer.toggleRepeat()
			timer.toastRepeat(requireContext())
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

		// On click listener
		saveButton.setOnClickListener {

			// Set the duration
			setDuration()

			// Duration is 0
			if (timer.duration == 0L)
			{
				quickToast(requireContext(), R.string.error_message_enter_timer_duration)
				return@setOnClickListener
			}

			// Set the name
			timer.name = nameBeforeSaving

			// Save the timer, then go back to the show timers fragment
			saveTimer {
				findNavController().popBackStack(R.id.nacShowTimersFragment, false)
			}

			// Haptic feedback
			it.performHapticFeedback()

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
		startButton = view.findViewById(R.id.timer_start_button)

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

			// Duration is 0
			if (timer.duration == 0L)
			{
				quickToast(context, R.string.error_message_enter_timer_duration)
				return@setOnClickListener
			}

			// Set the name
			timer.name = nameBeforeSaving

			// Save the timer, then start the timer and go to the active timer fragment
			saveTimer {
				NacActiveTimerService.startTimerService(context, timer)
				findNavController().navigate(R.id.nacActiveTimerFragment, timer.toBundle())
			}

			// Haptic feedback
			it.performHapticFeedback()

		}
	}

	/**
	 * Setup the vibrate button.
	 */
	private fun setupVibrateButton()
	{
		// Color
		vibrateButton.setupRippleColor(sharedPreferences)

		// Initial state
		vibrateButton.isChecked = timer.shouldVibrate

		// Hide the button and do nothing else
		if (!sharedPreferences.shouldShowVibrateButton)
		{
			vibrateButton.visibility = View.GONE
			return
		}

		// Click listener
		vibrateButton.setOnClickListener {
			timer.toggleVibrate()
			timer.toastVibrate(requireContext())
		}

		// Show the quick navigate dialog on long click
		vibrateButton.setOnLongClickListener {
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