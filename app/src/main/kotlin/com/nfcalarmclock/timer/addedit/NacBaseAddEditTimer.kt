package com.nfcalarmclock.timer.addedit

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.timer.NacTimerViewModel
import com.nfcalarmclock.timer.active.NacActiveTimerService
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.timer.options.NacTimerCardOptionsDialog
import com.nfcalarmclock.util.media.NacMedia
import com.nfcalarmclock.view.calcContrastColor
import com.nfcalarmclock.view.performHapticFeedback
import com.nfcalarmclock.view.setupBackgroundColor
import com.nfcalarmclock.view.setupRippleColor
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.getValue

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
			println("TIME IS FULL")
			return
		}

		// Build the new time
		val newTime = "${hour.toInt()}$min$sec$value"
		println("New : $newTime")

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
		println("New : $newTime")

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

		// Setup the views
		initTimer()
		setupHourMinuteSecondTextViews()
		setupNumberPadButtons()
		setupMoreButton()
		setupStartButton()
		setupDoneButton()
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
	 * Setup the done button.
	 */
	protected fun setupDoneButton()
	{
		// Get the view
		val view = requireView()
		val startButton: MaterialButton = view.findViewById(R.id.timer_start)
		val doneButton: MaterialButton = view.findViewById(R.id.timer_done_edit)

		// Get the contrast color
		val contrastColor = calcContrastColor(sharedPreferences.themeColor)

		// Setup the view
		startButton.visibility = View.INVISIBLE
		doneButton.visibility = View.VISIBLE
		doneButton.setupBackgroundColor(sharedPreferences)
		doneButton.setTextColor(contrastColor)

		// On click listener
		doneButton.setOnClickListener {

			println("Updating timer")
			// Set the duration
			setDuration()

			timerViewModel.update(timer) {
				println("Going back to show timers")
				findNavController().popBackStack(R.id.nacShowTimersFragment, false)
			}
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
			seconds= zeros
		}

		// Set the hour, minute, and seconds
		println("Hour:Minute:Seconds  $hour:$minute:$seconds")
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
	 * Setup the more button.
	 */
	protected fun setupMoreButton()
	{
		// Get the views
		val view = requireView()
		val moreButton: MaterialButton = view.findViewById(R.id.timer_more_button)

		// Setup more button click
		moreButton.setOnClickListener {
			NacTimerCardOptionsDialog.navigate(navController, timer)
				?.observe(viewLifecycleOwner) { t ->
					println("EHHLOOOOOOOOOOOOOOOOO")
					t.print()
					timer = t
				}
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
	 * Setup the start button.
	 */
	@OptIn(UnstableApi::class)
	protected fun setupStartButton()
	{
		// Get the view
		val context = requireContext()
		val view = requireView()
		val startButton: MaterialButton = view.findViewById(R.id.timer_start)

		// Get the contrast color
		val contrastColor = calcContrastColor(sharedPreferences.themeColor)

		// Setup the view
		startButton.setupBackgroundColor(sharedPreferences)
		startButton.iconTint = ColorStateList.valueOf(contrastColor)

		// On click listener
		startButton.setOnClickListener {

			// Set the duration
			setDuration()

			// Save the timer
			println("Inserting into jank")
			timerViewModel.insert(timer) {
				println("Does timer have id? ${timer.id}")

				// Start the timer
				NacActiveTimerService.startTimerService(context, timer)
				findNavController().navigate(R.id.nacActiveTimerFragment, timer.toBundle())

			}

		}
	}

}