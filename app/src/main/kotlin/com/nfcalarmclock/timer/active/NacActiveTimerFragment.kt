package com.nfcalarmclock.timer.active

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.nfcalarmclock.R
import com.nfcalarmclock.nfc.NacNfc
import com.nfcalarmclock.nfc.NacNfcTagViewModel
import com.nfcalarmclock.nfc.SCANNED_NFC_TAG_ID_BUNDLE_NAME
import com.nfcalarmclock.nfc.db.NacNfcTag
import com.nfcalarmclock.nfc.getNfcTagNamesForDismissing
import com.nfcalarmclock.nfc.getNfcTagsForDismissing
import com.nfcalarmclock.nfc.shouldUseNfc
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.bindToService
import com.nfcalarmclock.system.getTimer
import com.nfcalarmclock.system.registerMyReceiver
import com.nfcalarmclock.system.unregisterMyReceiver
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.view.animateProgress
import com.nfcalarmclock.view.calcContrastColor
import com.nfcalarmclock.view.setupBackgroundColor
import com.nfcalarmclock.view.setupRippleColor
import com.nfcalarmclock.view.startTimerRingingAnimation
import com.nfcalarmclock.view.updateHourMinuteSecondsTextViews
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Active timer.
 */
@UnstableApi
@AndroidEntryPoint
class NacActiveTimerFragment
	: Fragment()
{

	/**
	 * NFC tag view model.
	 */
	private val nfcTagViewModel: NacNfcTagViewModel by viewModels()

	/**
	 * Shared preferences.
	 */
	private lateinit var sharedPreferences: NacSharedPreferences

	/**
	 * Timer.
	 */
	private lateinit var timer: NacTimer

	/**
	 * Circular progress indicator.
	 */
	private lateinit var progressIndicator: CircularProgressIndicator

	/**
	 * Name.
	 */
	private lateinit var nameTextView: TextView

	/**
	 * Hour textview.
	 */
	private lateinit var hourTextView: TextView

	/**
	 * Minute textview.
	 */
	private lateinit var minuteTextView: TextView

	/**
	 * Second textview.
	 */
	private lateinit var secondsTextView: TextView

	/**
	 * Hour units textview.
	 */
	private lateinit var hourUnits: TextView

	/**
	 * Minute units textview.
	 */
	private lateinit var minuteUnits: TextView

	/**
	 * Seconds units textview.
	 */
	private lateinit var secondsUnits: TextView

	/**
	 * Resume button.
	 */
	private lateinit var resumeButton: MaterialButton

	/**
	 * Pause button.
	 */
	private lateinit var pauseButton: MaterialButton

	/**
	 * Stop button.
	 */
	private lateinit var stopButton: MaterialButton

	/**
	 * Reset button.
	 */
	private lateinit var resetButton: MaterialButton

	/**
	 * Scan NFC container.
	 */
	private lateinit var scanNfcContainer: LinearLayout

	/**
	 * Scan NFC textview.
	 */
	private lateinit var scanNfcTextView: TextView

	/**
	 * Add 5 seconds button.
	 */
	private lateinit var add5sButton: MaterialButton

	/**
	 * Add 30 seconds button.
	 */
	private lateinit var add30sButton: MaterialButton

	/**
	 * Add 1 minute button.
	 */
	private lateinit var add1mButton: MaterialButton

	/**
	 * Active timer service.
	 */
	private var service: NacActiveTimerService? = null

	/**
	 * List of NFC tags.
	 */
	private var nfcTags: MutableList<NacNfcTag>? = null

	/**
	 * Whether the circular progress animation is running for a timer or not.
	 */
	private var isRunningStartingAnimation: Boolean = false

	/**
	 * The last time the reset button was clicked.
	 */
	private var lastClickTimeResetButton: Long = 0

	/**
	 * NFC adapter state changed broadcast receiver.
	 */
	private val nfcAdapterStateChangedBroadcastReceiver: BroadcastReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent)
		{
			// Setup NFC textview
			setupScanNfcTagTextView()
		}
	}

	/**
	 * Callback when back is pressed.
	 */
	private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
		override fun handleOnBackPressed()
		{
			// Go back to show timers fragment
			findNavController().navigate(R.id.action_global_nacShowTimersFragment)
		}
	}

	/**
	 * Listener for when the countdown timer changes.
	 */
	private val onCountdownTimerChangedListener: NacActiveTimerService.OnCountdownTimerChangedListener =
		object : NacActiveTimerService.OnCountdownTimerChangedListener {

			/**
			 * Countdown finished.
			 */
			override fun onCountdownFinished(timer: NacTimer)
			{
				// Change the seconds text and progress to indicate done
				secondsTextView.text = resources.getString(R.string.number0)
				progressIndicator.animateProgress(progressIndicator.progress, 0, 250,
					onEnd = {

						// Get the context
						val context = requireContext()

						// Start the timer ringing animation
						startTimerRingingAnimation()

						// Set the visibility
						setScanNfcVisibility(timer.shouldUseNfc(context))
						setStopVisibility()

					})
			}

			/**
			 * Countdown paused.
			 */
			override fun onCountdownPaused(timer: NacTimer)
			{
				// Set the visibility
				setPauseVisibility()
			}

			/**
			 * Countdown reset.
			 */
			override fun onCountdownReset(timer: NacTimer, secUntilFinished: Long)
			{
				// Reset progress back to 0
				progressIndicator.animateProgress(progressIndicator.progress, 0, 250,
					onEnd = {

						// Update the views
						updateHourMinuteSecondsTextViews(secUntilFinished)
						setScanNfcVisibility(false)
						setResetVisibility()

					})
			}

			/**
			 * Countdown tick every second.
			 */
			override fun onCountdownTick(timer: NacTimer, secUntilFinished: Long, newProgress: Int)
			{
				// Animation is still running so do nothing
				if (isRunningStartingAnimation)
				{
					return
				}

				// Update the views
				setResumeVisibility()
				updateHourMinuteSecondsTextViews(secUntilFinished)

				// Animate the progress to the new progress
				progressIndicator.setProgressCompat(newProgress, true)
			}

		}

	/**
	 * Listener for when the countup handler ticks.
	 */
	private val onCountupTickListener: NacActiveTimerService.OnCountupTickListener = NacActiveTimerService.OnCountupTickListener { timer, secOfRinging ->

		// Update the time
		updateHourMinuteSecondsTextViews(secOfRinging)

	}

	/**
	 * Listener for when the service is stopped.
	 */
	private val onServiceStoppedListener: NacActiveTimerService.OnServiceStoppedListener =
		NacActiveTimerService.OnServiceStoppedListener {

			// Navigate back to show timers fragment
			findNavController().navigate(R.id.action_global_nacShowTimersFragment)

		}

	/**
	 * Connection to the active timer service.
	 */
	private val serviceConnection = object : ServiceConnection
	{
		override fun onServiceConnected(className: ComponentName, serviceBinder: IBinder)
		{
			// Set the active timer service
			val binder = serviceBinder as NacActiveTimerService.NacLocalBinder
			service = binder.getService()

			// Service has not started yet
			if (service!!.allTimersReadOnly.none { it.id == timer.id })
			{
				// Add the listeners
				service!!.addOnServiceStoppedListener(timer.id, onServiceStoppedListener)
				service!!.addOnCountdownTimerChangedListener(timer.id, onCountdownTimerChangedListener)
				service!!.addOnCountupTickListener(timer.id, onCountupTickListener)
				return
			}

			// Get the progress and seconds
			val context = requireContext()
			val secUntilFinished = service!!.getSecUntilFinished(timer)
			val progress = service!!.getProgress(timer)

			// This is the first tick of the countdown timer, while the service is
			// connected to this fragment
			if (service!!.allIsFirstTick[timer.id]!!)
			{
				// Update the hour, minute, and seconds textviews
				updateHourMinuteSecondsTextViews(secUntilFinished)

				// Set the flag that the animation is running
				isRunningStartingAnimation = true

				// Do a little circular animation from 0% to 100% on the first tick
				// and then reset the running animation flag
				progressIndicator.animateProgress(0, 100, 500, onEnd = {
					isRunningStartingAnimation = false
				})
			}
			else
			{
				// Ringing
				if (service!!.isTimerRinging(timer))
				{
					// Get the seconds that the timer has been ringing
					val secOfRinging = service!!.getSecOfRinging(timer)

					// Update the views
					updateHourMinuteSecondsTextViews(secOfRinging)
					setScanNfcVisibility(timer.shouldUseNfc(context))
					setStopVisibility()

					// Start the timer ringing animation
					startTimerRingingAnimation()
				}
				// Paused
				else if (service!!.isTimerPaused(timer))
				{
					// Update the views
					updateHourMinuteSecondsTextViews(secUntilFinished)
					setPauseVisibility()
					progressIndicator.progress = progress
				}
				// Active
				else if (service!!.isTimerActive(timer))
				{
					// Update the views
					updateHourMinuteSecondsTextViews(secUntilFinished)
					setResumeVisibility()
					progressIndicator.progress = progress
				}
				// Reset, not doing anything
				else
				{
					setResetVisibility()
					progressIndicator.progress = 0
				}
			}

			// Set the state of the resume/pause/stop buttons
			resumeButton.visibility = if (service!!.isTimerPaused(timer)) View.VISIBLE else View.INVISIBLE
			pauseButton.visibility = if (service!!.isTimerPaused(timer) || service!!.isTimerRinging(timer)) View.INVISIBLE else View.VISIBLE
			stopButton.visibility = if (service!!.isTimerRinging(timer) && !timer.shouldUseNfc(context)) View.VISIBLE else View.INVISIBLE

			// Add the listeners
			service!!.addOnServiceStoppedListener(timer.id, onServiceStoppedListener)
			service!!.addOnCountdownTimerChangedListener(timer.id, onCountdownTimerChangedListener)
			service!!.addOnCountupTickListener(timer.id, onCountupTickListener)
		}

		override fun onServiceDisconnected(className: ComponentName) {}
	}

	/**
	 * Attempt to dismiss the timer with a scanned NFC tag.
	 */
	fun attemptDismissWithScannedNfc(nfcId: String)
	{
		// Get the context
		val context = requireContext()

		// NFC tag was scanned so check if it is able to dismiss the timer
		if (NacNfc.canDismissWithScannedNfc(context, timer, nfcId, nfcTags))
		{
			// Dismiss the service
			service?.dismiss(timer)

			// Set the NFC just scanned flag
			sharedPreferences.wasNfcJustScannedToDismiss = true
		}

		// Setup the scan NFC tag textview, in the event that more NFC tags need to be
		// scanned
		setupScanNfcTagTextView()
	}

	/**
	 * Create the root view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.frg_active_timer, container, false)
	}

	/**
	 * Fragment paused.
	 */
	override fun onPause()
	{
		// Super
		super.onPause()

		// Unregister the broadcast receivers
		unregisterMyReceiver(requireContext(), nfcAdapterStateChangedBroadcastReceiver)
	}

	/**
	 * Fragment resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Attempt to get the ID of an NFC tag that was scanned
		val nfcId = arguments?.getString(SCANNED_NFC_TAG_ID_BUNDLE_NAME)

		lifecycleScope.launch {

			// Populate the NFC tags list
			if (nfcTags == null)
			{
				// Get the list of NFC tags that can be used to dismiss the timer, and
				// order them based on how the user wants them ordered. If NFC is not
				// required, this will just be an empty list
				nfcTags = timer.getNfcTagsForDismissing(nfcTagViewModel)
			}

			// NFC was scanned
			if (nfcId != null)
			{
				// Attempt to dismiss the timer with the NFC tag
				attemptDismissWithScannedNfc(nfcId)

				// Remove the NFC tag from the arguments so it does not retrigger if the
				// fragment is redrawn
				arguments?.remove(SCANNED_NFC_TAG_ID_BUNDLE_NAME)
			}

			// Setup the scan NFC tag textview, in the event that NFC tags was populated
			// above and/or NFC was scanned and more NFC tags need to be scanned
			setupScanNfcTagTextView()
		}

		// Register the broadcast receivers
		registerMyReceiver(requireContext(), nfcAdapterStateChangedBroadcastReceiver,
			IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED))
	}

	/**
	 * Fragment started.
	 */
	override fun onStart()
	{
		// Super
		super.onStart()

		// Bind to the active timer service
		requireContext().bindToService(NacActiveTimerService::class.java, serviceConnection)
	}

	/**
	 * Fragment stopped.
	 */
	override fun onStop()
	{
		// Super
		super.onStop()

		// Remove the back press callback
		onBackPressedCallback.remove()

		// Unbind from the active timer service
		requireContext().unbindService(serviceConnection)

		// Clear the service listeners
		service?.removeOnServiceStoppedListener(timer.id, onServiceStoppedListener)
		service?.removeOnCountdownTimerChangedListener(timer.id, onCountdownTimerChangedListener)
		service?.removeOnCountupTickListener(timer.id, onCountupTickListener)
	}

	/**
	 * View is created.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the timer
		val t = arguments?.getTimer()

		// Set the timer
		if (t != null)
		{
			timer = t
		}
		// Stop the fragment if there is no timer
		else
		{
			println("POPPING BACK STACK")
			findNavController().popBackStack()
			return
		}

		// Get the context and shared preferences
		val context = requireContext()
		sharedPreferences = NacSharedPreferences(context)

		// Get the views
		progressIndicator = view.findViewById(R.id.timer_progress)
		nameTextView = view.findViewById(R.id.timer_name)
		hourTextView = view.findViewById(R.id.timer_hour)
		minuteTextView = view.findViewById(R.id.timer_minute)
		secondsTextView = view.findViewById(R.id.timer_seconds)
		hourUnits = view.findViewById(R.id.timer_hour_units)
		minuteUnits = view.findViewById(R.id.timer_minute_units)
		secondsUnits = requireView().findViewById(R.id.timer_seconds_units)
		resumeButton = view.findViewById(R.id.timer_resume_button)
		pauseButton = view.findViewById(R.id.timer_pause_button)
		stopButton = view.findViewById(R.id.timer_stop_button)
		resetButton = view.findViewById(R.id.timer_reset_button)
		scanNfcContainer = view.findViewById(R.id.timer_scan_nfc_container)
		scanNfcTextView = view.findViewById(R.id.timer_scan_nfc_text)
		add5sButton = view.findViewById(R.id.timer_add_5s)
		add30sButton = view.findViewById(R.id.timer_add_30s)
		add1mButton = view.findViewById(R.id.timer_add_1m)

		// Setup back press
		requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)

		// Setup views
		setupName()
		setupHourAndMinuteVisibility()
		setupResetButton()
		setupPauseButton()
		setupResumeButton()
		setupStopButton()
		setScanNfcVisibility(false)
		setupAddTimeButtons()
		setupProgressIndicator()
		setupScanNfcTagTextView()
	}

	/**
	 * Set the visibility of views when timer is paused.
	 */
	private fun setPauseVisibility()
	{
		// Show the resume and reset buttons. Hide the pause and stop buttons
		resumeButton.visibility = View.VISIBLE
		pauseButton.visibility = View.INVISIBLE
		stopButton.visibility = View.INVISIBLE
		resetButton.visibility = View.VISIBLE

		// Show the add time buttons
		add5sButton.visibility = View.VISIBLE
		add30sButton.visibility = View.VISIBLE
		add1mButton.visibility = View.VISIBLE
	}

	/**
	 * Set the visibility of views when timer is reset.
	 */
	private fun setResetVisibility()
	{
		// Show the resume button. Hide the pause, stop, and reset buttons
		resumeButton.visibility = View.VISIBLE
		pauseButton.visibility = View.INVISIBLE
		stopButton.visibility = View.INVISIBLE
		resetButton.visibility = View.INVISIBLE

		// Show the add time buttons as the timer can be run again, if the user wants to
		add5sButton.visibility = View.VISIBLE
		add30sButton.visibility = View.VISIBLE
		add1mButton.visibility = View.VISIBLE
	}

	/**
	 * Set the visibility of views when timer is started or resumed.
	 */
	private fun setResumeVisibility()
	{
		// Show the pause and reset buttons. Hide the resume and stop buttons
		resumeButton.visibility = View.INVISIBLE
		pauseButton.visibility = View.VISIBLE
		stopButton.visibility = View.INVISIBLE
		resetButton.visibility = View.VISIBLE

		// Show the add time buttons
		add5sButton.visibility = View.VISIBLE
		add30sButton.visibility = View.VISIBLE
		add1mButton.visibility = View.VISIBLE
	}

	/**
	 * Set the scan NFC views visibility.
	 */
	private fun setScanNfcVisibility(shouldBeVisible: Boolean)
	{
		if (shouldBeVisible)
		{
			scanNfcContainer.visibility = View.VISIBLE
		}
		else
		{
			scanNfcContainer.visibility = View.INVISIBLE
		}
	}

	/**
	 * Set the visibility of views when timer should be stopped.
	 */
	private fun setStopVisibility()
	{
		// Get the context
		val context = requireContext()

		// Show the stop button. Hide the resume and pause button
		resumeButton.visibility = View.INVISIBLE
		pauseButton.visibility = View.INVISIBLE
		stopButton.visibility = if (timer.shouldUseNfc(context)) View.INVISIBLE else View.VISIBLE
		resetButton.visibility = View.INVISIBLE

		// Hide the add time buttons as the timer is no longer running
		add5sButton.visibility = View.INVISIBLE
		add30sButton.visibility = View.INVISIBLE
		add1mButton.visibility = View.INVISIBLE
	}

	/**
	 * Setup the add time buttons.
	 */
	private fun setupAddTimeButtons()
	{
		add5sButton.setOnClickListener { service?.addTimeToCountdown(timer, 5) }
		add30sButton.setOnClickListener { service?.addTimeToCountdown(timer, 30) }
		add1mButton.setOnClickListener { service?.addTimeToCountdown(timer, 60) }
	}

	/**
	 * Setup the visibility of the hour and minute textviews. The seconds should always
	 * be visible.
	 */
	private fun setupHourAndMinuteVisibility()
	{
		// Hide hours
		if (timer.duration < 3600)
		{
			hourTextView.visibility = View.GONE
			hourUnits.visibility = View.GONE
		}

		// Hide minutes
		if (timer.duration < 60)
		{
			minuteTextView.visibility = View.GONE
			minuteUnits.visibility = View.GONE
		}
	}

	/**
	 * Setup the name TextView.
	 */
	private fun setupName()
	{
		nameTextView.text = timer.name
		nameTextView.setTextColor(sharedPreferences.nameColor)
	}

	/**
	 * Setup the pause button.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	private fun setupPauseButton()
	{
		// Color
		val contrastColor = calcContrastColor(sharedPreferences.themeColor)

		pauseButton.setupBackgroundColor(sharedPreferences)
		pauseButton.iconTint = ColorStateList.valueOf(contrastColor)

		// Click listener
		pauseButton.setOnClickListener {

			// Pause the timer
			setPauseVisibility()
			service?.cancelCountdownTimer(timer)
			service?.updateNotification(timer)

		}
	}

	/**
	 * Setup the progress indicator.
	 */
	private fun setupProgressIndicator()
	{
		// Setup color
		progressIndicator.setIndicatorColor(sharedPreferences.themeColor)
	}

	/**
	 * Setup the reset button.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	private fun setupResetButton()
	{
		// Setup color
		resetButton.setupRippleColor(sharedPreferences)

		// Click listener
		resetButton.setOnClickListener {

			// Button was clicked too recently
			if ((SystemClock.elapsedRealtime() - lastClickTimeResetButton) < 500)
			{
				return@setOnClickListener
			}

			// Clear the running starting animation flag
			isRunningStartingAnimation = false

			// Reset the timer
			service?.resetCountdownTimer(timer)
			service?.cleanup(timer)

			// Stop the service when no timers are using it
			if (service?.allTimersReadOnly?.isEmpty() == true)
			{
				service!!.stopThisService()
			}

			// Set the last click time
			lastClickTimeResetButton = SystemClock.elapsedRealtime()

		}
	}

	/**
	 * Setup the resume button.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	private fun setupResumeButton()
	{
		// Color
		val contrastColor = calcContrastColor(sharedPreferences.themeColor)

		resumeButton.setupBackgroundColor(sharedPreferences)
		resumeButton.iconTint = ColorStateList.valueOf(contrastColor)

		// Click listener
		resumeButton.setOnClickListener {

			// Set the visibility
			setResumeVisibility()

			// Resume the timer
			if (service?.isTimerPaused(timer) == true)
			{
				service!!.startCountdownTimer(timer)
				service!!.updateNotification(timer)
			}
			// Start the timer
			else
			{
				val context = requireContext()

				NacActiveTimerService.startTimerService(context, timer)
				context.bindToService(NacActiveTimerService::class.java, serviceConnection)
			}

		}
	}

	/**
	 * Setup the Scan NFC Tag textview.
	 */
	private fun setupScanNfcTagTextView()
	{
		// Get the names of the NFC tags that can dismiss the timer
		val nfcTagNames = if (nfcTags != null)
		{
			val prefix = "(${resources.getString(R.string.message_show_nfc_tag_id)}) "
			timer.getNfcTagNamesForDismissing(nfcTags!!, prefix)
		}
		else
		{
			null
		}

		// Get colors
		val context = requireContext()
		val yellow = ContextCompat.getColor(context, R.color.yellow)
		val white = ContextCompat.getColor(context, R.color.white)

		// Normal usage
		if (NacNfc.isEnabled(context))
		{
			// Set the name of the NFC tags that are needed to dismiss the timer
			scanNfcTextView.text = nfcTagNames ?: resources.getString(R.string.title_scan_nfc_tag)
			scanNfcTextView.setTextColor(white)
		}
		// NFC needs to be enabled
		else
		{
			// Show warning that NFC needs to be enabled to be able to dismiss the timer
			scanNfcTextView.text = resources.getString(R.string.message_nfc_enable_on_device_request)
			scanNfcTextView.setTextColor(yellow)
		}
	}

	/**
	 * Setup the stop button.
	 */
	private fun setupStopButton()
	{
		// Color
		val contrastColor = calcContrastColor(sharedPreferences.themeColor)

		stopButton.setupBackgroundColor(sharedPreferences)
		stopButton.iconTint = ColorStateList.valueOf(contrastColor)

		// Click listener
		stopButton.setOnClickListener {
			service?.dismiss(timer)
		}
	}

	/**
	 * Start the timer ringing animation.
	 */
	private fun startTimerRingingAnimation()
	{
		// Get the colors
		val context = requireContext()

		// Start the timer ringing animation
		startTimerRingingAnimation(
			context, progressIndicator,
			hourTextView, hourUnits,
			minuteTextView, minuteUnits,
			secondsTextView, secondsUnits)
	}

	/**
	 * Update the hour, minute, and seconds textviews based on the milliseconds until
	 * finished.
	 */
	private fun updateHourMinuteSecondsTextViews(secUntilFinished: Long)
	{
		updateHourMinuteSecondsTextViews(
			hourTextView, hourUnits,
			minuteTextView, minuteUnits,
			secondsTextView,
			secUntilFinished)
	}

}