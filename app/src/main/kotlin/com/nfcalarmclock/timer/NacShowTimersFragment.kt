package com.nfcalarmclock.timer

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ServiceConnection
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.os.IBinder
import android.provider.AlarmClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.options.nfc.NacNfcTagDismissOrder
import com.nfcalarmclock.card.NacBaseCardAdapter
import com.nfcalarmclock.card.NacBaseCardTouchHelperCallback
import com.nfcalarmclock.card.NacCardLayoutManager
import com.nfcalarmclock.main.NacMainActivity
import com.nfcalarmclock.nfc.SCANNED_NFC_TAG_ID_BUNDLE_NAME
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacBundle
import com.nfcalarmclock.system.getTimer
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.timer.active.NacActiveTimerService
import com.nfcalarmclock.timer.card.NacTimerCardAdapter
import com.nfcalarmclock.timer.card.NacTimerCardHolder
import com.nfcalarmclock.timer.card.NacTimerCardTouchHelper
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.view.animateProgress
import com.nfcalarmclock.view.performHapticFeedback
import com.nfcalarmclock.view.quickToast
import com.nfcalarmclock.view.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Show all timers.
 */
@UnstableApi
@AndroidEntryPoint
class NacShowTimersFragment
	: Fragment()
{

	/**
	 * Timer view model.
	 */
	private val timerViewModel: NacTimerViewModel by viewModels()

	/**
	 * Shared preferences.
	 */
	private lateinit var sharedPreferences: NacSharedPreferences

	/**
	 * Floating action button to add new timers.
	 */
	private lateinit var floatingActionButton: FloatingActionButton

	/**
	 * Bottom navigation.
	 */
	private lateinit var bottomNavigation: BottomNavigationView

	/**
	 * Recycler view containing the timer cards.
	 */
	private lateinit var recyclerView: RecyclerView

	/**
	 * Timer card adapter.
	 */
	private lateinit var timerCardAdapter: NacTimerCardAdapter

	/**
	 * Mutable live data for the timer card that can be modified and sorted, or
	 * not sorted, depending on the circumstance.
	 *
	 * Live data from the view model cannot be sorted, hence the need for this.
	 */
	private lateinit var timerCardAdapterLiveData: MutableLiveData<List<NacTimer>>

	/**
	 * Timer card touch helper.
	 */
	private lateinit var timerCardTouchHelper: NacTimerCardTouchHelper

	/**
	 * Active timer service.
	 */
	private var service: NacActiveTimerService? = null

	/**
	 * The current snackbar being used.
	 */
	private var currentSnackbar: Snackbar? = null

	/**
	 * Whether the circular progress animation is running for a timer or not.
	 */
	private var isRunningStartingAnimation: HashMap<Long, Boolean> = hashMapOf()

	/**
	 * Check if the user has created the maximum number of timers.
	 */
	private val hasCreatedMaxTimers: Boolean
		get()
		{
			// Get the current and max counts
			val currentSize = timerCardAdapter.itemCount
			val maxTimers = resources.getInteger(R.integer.max_alarms)

			// Check the size
			return (currentSize+1 > maxTimers)
		}

	/**
	 * Listener for when the countdown timer changes.
	 */
	private val onCountdownTimerChangedListener: NacActiveTimerService.OnCountdownTimerChangedListener =
		object : NacActiveTimerService.OnCountdownTimerChangedListener {

			override fun onCountdownFinished(timer: NacTimer)
			{
				println("SHOW TIMERS DONE WITH THE COUNTDOWN : ${timer.id} | ${timer.isActive}")
				val card = recyclerView.findViewHolderForItemId(timer.id) as NacTimerCardHolder? ?: return

				// Change the seconds text and progress to indicate done
				card.secondsTextView.text = resources.getString(R.string.number0)
				card.progressIndicator.animateProgress(card.progressIndicator.progress, 0, 250,
					onEnd = {

						// Start the timer ringing animation
						card.startTimerRingingAnimation(requireContext())

						// Set the visibility
						card.setStopVisibility()

					})

			}

			override fun onCountdownPaused(timer: NacTimer)
			{
				println("SHOW TIMERS PAUSED TIMER")
				// Get the card
				val card = recyclerView.findViewHolderForItemId(timer.id) as NacTimerCardHolder? ?: return

				// Set the visibility
				card.setPauseVisibility()
			}

			override fun onCountdownReset(timer: NacTimer, secUntilFinished: Long)
			{
				println("SHOW TIMERS COUNTDOWN RESET : $secUntilFinished")
				// Get the card
				val card = recyclerView.findViewHolderForItemId(timer.id) as NacTimerCardHolder? ?: return

				// Reset progress back to 0
				card.progressIndicator.animateProgress(card.progressIndicator.progress, 0, 250,
					onEnd = {

						// Update the views
						card.setResetVisibility()
						card.updateHourMinuteSecondsTextViews(secUntilFinished)
						card.resetTimerRingingAnimation(requireContext())

					})
			}

			override fun onCountdownTick(timer: NacTimer, secUntilFinished: Long, newProgress: Int)
			{
				println("SHOW TIMERS COUNTDOWN Tick")
				// Get the card
				val card = recyclerView.findViewHolderForItemId(timer.id) as NacTimerCardHolder? ?: return

				// Starting animation is still running. Do nothing yet
				if (isRunningStartingAnimation[timer.id] == true)
				{
					println("Still running the starting animation for : ${timer.id}")
					return
				}

				// Update the views
				card.setResumeVisibility()
				card.updateHourMinuteSecondsTextViews(secUntilFinished)

				// Animate the progress to the new progress
				card.progressIndicator.setProgressCompat(newProgress, true)
			}

		}

	/**
	 * Listener for when the countup handler ticks.
	 */
	private val onCountupTickListener: NacActiveTimerService.OnCountupTickListener = NacActiveTimerService.OnCountupTickListener { timer, secOfRinging ->

		println("On countup yoyoyo : $secOfRinging")

		// Get the card
		val card = recyclerView.findViewHolderForItemId(timer.id) as NacTimerCardHolder? ?: return@OnCountupTickListener

		// Update the time
		card.updateHourMinuteSecondsTextViews(secOfRinging)

	}

	/**
	 * Listener for when the service is stopped.
	 */
	private val onServiceStoppedListener: NacActiveTimerService.OnServiceStoppedListener =
		NacActiveTimerService.OnServiceStoppedListener { timer ->
			println("YOYOYOYO JANK STOPPED NOW")

			// Get the card
			val card = recyclerView.findViewHolderForItemId(timer.id) as NacTimerCardHolder? ?: return@OnServiceStoppedListener

			println("REeseting the timer animation jank")
			// Update views back to normal
			card.setResetVisibility()
			card.updateHourMinuteSecondsTextViews(timer.duration)
			card.resetTimerRingingAnimation(requireContext())
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
			println("Show timers SERVICE IS NOW CONNECTED")
			// TODO: Null pointer exception when screen turns off and then on again and onTick() is called again? Line 150 in Active timer from show timers.... weird

			// TODO: Repeat toast to explain what that does for timers
			// TODO: Test saving the default ringtone in general settings

			// Initialize each timer being used by the service
			service!!.allTimersReadOnly.forEach { timer ->
				initTimerCard(timer)
			}

			// Add a countdown timer change listener for each timer in the table
			lifecycleScope.launch {
				timerViewModel.getAllTimers().forEach {
					service!!.addOnServiceStoppedListener(it.id, onServiceStoppedListener)
					service!!.addOnCountdownTimerChangedListener(it.id, onCountdownTimerChangedListener)
					service!!.addOnCountupTickListener(it.id, onCountupTickListener)
				}
			}
		}

		override fun onServiceDisconnected(className: ComponentName) {}
	}

	/**
	 * Add a timer to the database.
	 *
	 * @param timer A timer.
	 * @param onInsertListener Listener to call after the timer is inserted and has an ID.
	 */
	private fun addTimer(
		timer: NacTimer,
		messageId: Int? = null,
		onInsertListener: () -> Unit = {})
	{
		lifecycleScope.launch {

			// Insert timer
			timerViewModel.insert(timer) {

				// Countdown timer change listener
				service?.addOnCountdownTimerChangedListener(timer.id, onCountdownTimerChangedListener)
				service?.addOnCountupTickListener(timer.id, onCountupTickListener)

				// Show the snackbar
				if (messageId != null)
				{
					val message = getString(messageId)
					val action = getString(R.string.action_undo)

					currentSnackbar = showSnackbar(
						currentSnackbar, bottomNavigation, floatingActionButton,
						message, action, sharedPreferences.themeColor,
						onClickListener = {
							// Undo the insert. This will delete the timer
							deleteTimer(timer)
						})
				}

				// Call the listener
				onInsertListener()

			}

		}
	}

	/**
	 * Add a timer that was created from the SET_TIMER intent.
	 */
	private fun addTimerFromSetTimerIntent(timer: NacTimer)
	{
		println("addTimerFromSetTimerIntent()")
		addTimer(timer, onInsertListener = {
			println("Hello added timer from set timer intent")

			// Navigate to the edit timer fragment
			findNavController().navigate(R.id.action_nacShowTimersFragment_to_nacEditTimerFragment, timer.toBundle())
		})
	}

	/**
	 * Attempt to dismiss the timer with a scanned NFC tag.
	 */
	fun attemptDismissWithScannedNfc(nfcId: String)
	{
		lifecycleScope.launch {

			// Get all the active timers
			val context = requireContext()
			val allActiveTimers = timerViewModel.getAllActiveTimers()
			var nonNfcTimer: NacTimer? = null
			var anyNfcTimer: NacTimer? = null

			println("Iterate over each active timer")
			allActiveTimers.forEach { t ->

				// Timer does not use NFC so ignore
				if (!t.shouldUseNfc)
				{
					nonNfcTimer = nonNfcTimer ?: t
					println("Timer maybe using non NFC : ${nonNfcTimer.id}")
					return@forEach
				}

				// Timer can use any NFC tag to dismiss
				if (t.nfcTagDismissOrder == NacNfcTagDismissOrder.ANY)
				{
					anyNfcTimer = anyNfcTimer ?: t
					println("Timer can dismiss using ANY NFC tags : ${anyNfcTimer.id}")
					return@forEach
				}

				// Parse the NFC ID and acceptable NFC tags that can be used to dismiss the timer
				val nfcTagIdList = t.nfcTagIdList

				// NFC tag list contains the NFC tag that was scanned
				if (nfcTagIdList.contains(nfcId))
				{
					println("Timer : ${t.id} contains NFC tag : $nfcId")
					if (nfcTagIdList.size == 1)
					{
						println("ONLY 1 NFC required for this timer! Dismiss this jank")
						NacActiveTimerService.Companion.dismissTimerServiceWithNfc(context, t)
					}
					else
					{
						println("Timer NEEDS more than 1 nFC jakn : $nfcTagIdList")
						val bundle = t.toBundle()
							.apply {
								putString(SCANNED_NFC_TAG_ID_BUNDLE_NAME, nfcId)
							}

						// Navigate to the active timer and try to dismiss with this NFC tag
						findNavController().navigate(R.id.nacActiveTimerFragment, bundle)
					}

					return@launch
				}

			}

			// None of the timers had an NFC tag list that contained the scanned NFC tag.
			// Now try dismiss one of the timers that accepts any NFC tag to dismiss
			if (anyNfcTimer != null)
			{
				println("DISMISSING ANY NFC TIMER : ${anyNfcTimer.id}")
				NacActiveTimerService.Companion.dismissTimerServiceWithNfc(context, anyNfcTimer)
			}

			// As a last resort, try to dismiss one of the timers that does not even use
			// NFC, but NFC is still accepted to dismiss if a user wants to
			if (nonNfcTimer != null)
			{
				println("DISMISSING NON NFC TIMER : ${nonNfcTimer.id}")
				NacActiveTimerService.Companion.dismissTimerServiceWithNfc(context, nonNfcTimer)
			}
		}
	}

	/**
	 * Copy a timer and add it to the database.
	 *
	 * @param timer A timer.
	 */
	private fun copyTimer(timer: NacTimer)
	{
		// Create a copy of the timer
		val copiedTimer = timer.copy()

		// Add the copied timer
		addTimer(copiedTimer, R.string.message_timer_copy)
	}

	/**
	 * Delete a timer from the database.
	 *
	 * @param timer A timer.
	 */
	private fun deleteTimer(timer: NacTimer)
	{
		// Get the local media path
		val localMediaPath = timer.localMediaPath

		// Remove the countdown/up timer change listeners
		service?.removeOnCountdownTimerChangedListener(timer.id, onCountdownTimerChangedListener)
		service?.removeOnCountupTickListener(timer.id, onCountupTickListener)

		// Delete the timer
		timerViewModel.delete(timer)

		// Show the snackbar
		val message = getString(R.string.message_timer_delete)
		val action = getString(R.string.action_undo)

		currentSnackbar = showSnackbar(
			currentSnackbar, bottomNavigation, floatingActionButton,
			message, action, sharedPreferences.themeColor,
			onClickListener = {
				// Undo the delete. This will restore the timer
				restoreTimer(timer)
			},
			onDismissListener = { event ->

				// Snackbar was not dismissed via timeout
				if (event != BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_TIMEOUT)
				{
					return@showSnackbar
				}

				// Cleanup media file
				lifecycleScope.launch {
					(requireActivity() as NacMainActivity).cleanupMediaFileAfterDelete(
						localMediaPath, timerViewModel.getAllTimers()
					)
				}

			})
	}

	/**
	 * Initialize the timer card, updating components depending on what state it is in in
	 * the service.
	 */
	fun initTimerCard(card: NacTimerCardHolder)
	{
		// Get the progress and seconds
		val progress = service!!.getProgress(card.timer!!)
		println("Active Fragment SERVICE IS NOW CONNECTED : ${card.timer!!.id} | $progress")

		// Ringing
		if (service?.isTimerRinging(card.timer!!) == true)
		{
			println("Card : ${card.timer!!.id} | RINGING")

			// Get the seconds that the timer has been ringing
			val secOfRinging = service!!.getSecOfRinging(card.timer!!)

			// Update the views and start the timer ringing animation
			card.updateHourMinuteSecondsTextViews(secOfRinging)
			card.setStopVisibility()
			card.startTimerRingingAnimation(requireContext())
		}
		// Paused
		else if (service?.isTimerPaused(card.timer!!) == true)
		{
			println("Card : ${card.timer!!.id}| PAUSED")

			// Get the seconds until finished and current progress
			val secUntilFinished = service!!.getSecUntilFinished(card.timer!!)
			val progress = service!!.getProgress(card.timer!!)

			// Update the views
			card.updateHourMinuteSecondsTextViews(secUntilFinished)
			card.setPauseVisibility()
			card.progressIndicator.progress = progress
		}
		// Active
		else if (service?.isTimerActive(card.timer!!) == true)
		{
			println("Card : ${card.timer!!.id}| ACTIVE")
			// Get the seconds until finished and current progress
			val secUntilFinished = service!!.getSecUntilFinished(card.timer!!)
			val progress = service!!.getProgress(card.timer!!)

			// Update the views
			card.updateHourMinuteSecondsTextViews(secUntilFinished)
			card.setResumeVisibility()

			if (isRunningStartingAnimation[card.timer!!.id] == true)
			{
				println("HELLOOOOOO Still running the starting animation for : ${card.timer!!.id}")
			}
			else
			{
				card.progressIndicator.progress = progress
			}
		}
		// Normal, not doing anything
		else
		{
			println("Card bound : ${card.timer!!.id} | NORMAL")
			card.setResetVisibility()
		}
	}

	/**
	 * @see initTimerCard
	 */
	fun initTimerCard(timer: NacTimer)
	{
		// Get the card
		val card = recyclerView.findViewHolderForItemId(timer.id) as NacTimerCardHolder?

		if (card == null)
		{
			println("NOT DOING JANK MANG")
			return
		}

		initTimerCard(card)
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
		return inflater.inflate(R.layout.frg_show_timers, container, false)
	}

	/**
	 * Called when the activity is resumed.
	 */
	@SuppressLint("UnsafeIntentLaunch")
	@OptIn(UnstableApi::class)
	override fun onResume()
	{
		// Super
		super.onResume()

		// Get the intent action and timer from the fragment arguments bundle. These
		// could be null, but if an action occurred, they will not be
		val action = arguments?.getString(NacBundle.BUNDLE_INTENT_ACTION)
		val timer = arguments?.getTimer()

		// Add timer that was created from the SET_TIMER intent
		if ((action == AlarmClock.ACTION_SET_TIMER) && (timer != null))
		{
			println("Show timers addTimerFromSetTimerIntent()")
			addTimerFromSetTimerIntent(timer)
		}

		// Attempt to get the ID of an NFC tag that was scanned
		val nfcId = arguments?.getString(SCANNED_NFC_TAG_ID_BUNDLE_NAME)

		// NFC was scanned before launching this fragment
		if (nfcId != null)
		{
			println("NFC was scanned in onResume() of show timers! $nfcId")
			attemptDismissWithScannedNfc(nfcId)
		}
	}

	/**
	 * Fragment started.
	 */
	override fun onStart()
	{
		// Super
		super.onStart()

		// Bind to the active timer service
		val context = requireContext()

		NacActiveTimerService.Companion.bindToService(context, NacActiveTimerService::class.java, serviceConnection)
	}

	/**
	 * Fragment stopped.
	 */
	override fun onStop()
	{
		// Super
		super.onStop()

		// Unbind from the active timer service
		requireContext().unbindService(serviceConnection)

		// Clear the service listeners
		service?.removeAllMatchingOnCountdownTimerChangedListener(onCountdownTimerChangedListener)
		service?.removeAllMatchingOnCountupTickListener(onCountupTickListener)
	}

	/**
	 * Called when the activity is created.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Setup
		super.onViewCreated(view, savedInstanceState)

		// Set member variables
		val context = requireContext()
		sharedPreferences = NacSharedPreferences(context)
		floatingActionButton = requireActivity().findViewById(R.id.floating_action_button)
		bottomNavigation = requireActivity().findViewById(R.id.bottom_navigation)
		recyclerView = view.findViewById(R.id.rv_timer_list)
		timerCardAdapter = NacTimerCardAdapter()
		timerCardAdapterLiveData = MutableLiveData<List<NacTimer>>()
		timerCardTouchHelper = NacTimerCardTouchHelper(object : NacBaseCardTouchHelperCallback.OnCardSwipedListener<NacTimer>
		{

			override fun onCopySwipe(item: NacTimer, index: Int)
			{
				// Haptic feedback
				view.performHapticFeedback()

				// Reset the view on the timer that was swiped
				timerCardAdapter.notifyItemChanged(index)

				// Check if the max number of timers was created
				if (hasCreatedMaxTimers)
				{
					// Show toast that the max number of timers were created
					quickToast(requireContext(), R.string.error_message_max_timers)
					return
				}

				// Copy the timer
				copyTimer(item)
			}

			override fun onDeleteSwipe(item: NacTimer, index: Int)
			{
				// Haptic feedback
				view.performHapticFeedback()

				// Delete the timer
				deleteTimer(item)
			}

		})

		// Setup
		setupLiveDataObservers()
		setupTimerCardAdapter()
		setupRecyclerView()
		setupFloatingActionButton()
	}

	/**
	 * Restore a timer and add it back to the database.
	 *
	 * @param timer A timer.
	 */
	private fun restoreTimer(timer: NacTimer)
	{
		addTimer(timer, R.string.message_timer_restore)
	}

	/**
	 * Setup the floating action button.
	 */
	private fun setupFloatingActionButton()
	{
		// Set the click listener
		floatingActionButton.setOnClickListener { view: View ->

			// Haptic feedback so that the user knows the action was received
			view.performHapticFeedback()

			// Max number of timers reached
			if (hasCreatedMaxTimers)
			{
				// Show a toast that the max number of timers was created
				quickToast(requireContext(), R.string.error_message_max_timers)
				return@setOnClickListener
			}

			// Navigate to add timer fragment
			findNavController().navigate(R.id.nacAddTimerFragment)
		}
	}

	/**
	 * Setup LiveData observers.
	 */
	private fun setupLiveDataObservers()
	{
		// Observer is called when list of all timers changes. Including when the app
		// starts and the list is initially empty
		timerViewModel.allTimers.observe(viewLifecycleOwner) { timers ->

			// Sort timers by duration. No need to move around timers if they become
			// active. It just gets confusing as a user if they move around
			timerCardAdapterLiveData.value = timers.sortedBy { it.duration }

			// Navigate to the add timer fragment when there are no timers
			lifecycleScope.launch {
				println("Timers size : ${timers.size} | Count : ${timerViewModel.count()}")

				if (timerViewModel.count() == 0)
				{
					println("GOING TO ADD TIMERS")
					findNavController().navigate(R.id.nacAddTimerFragment)
				}

			}

		}

		// Observe any changes to the timers in the adapter
		timerCardAdapterLiveData.observe(viewLifecycleOwner) { timers ->

			// Update the timer adapter
			timerCardAdapter.submitList(timers)

		}
	}

	/**
	 * Setup the recycler view.
	 */
	private fun setupRecyclerView()
	{
		// Get the context
		val context = requireContext()

		// Create the divider drawable
		val padding = resources.getDimensionPixelSize(R.dimen.medium)
		val drawable = ContextCompat.getDrawable(context, R.drawable.card_divider)
		val divider = InsetDrawable(drawable, padding, 0, padding, 0)
		//val divider = InsetDrawable(drawable, 0, 0, 0, 0)

		// Create the item decoration
		val decoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)

		// Set the divider on the decoration
		decoration.setDrawable(divider)

		// Add the decoration to the recycler view. This will divide every item by this
		// decoration
		recyclerView.addItemDecoration(decoration)

		// Setup everything else
		recyclerView.adapter = timerCardAdapter
		recyclerView.layoutManager = NacCardLayoutManager(context)
		recyclerView.setHasFixedSize(true)

		// Show/hide the FAB on scroll
		recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener()
		{

			/**
			 * Scrolled.
			 */
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int)
			{
				super.onScrolled(recyclerView, dx, dy)

				if (dy > 0)
				{
					// Scroll Down
					if (floatingActionButton.isShown)
					{
						floatingActionButton.hide()
					}
				}
				else if (dy < 0)
				{
					// Scroll Up
					if (!floatingActionButton.isShown)
					{
						floatingActionButton.show()
					}
				}
			}

		})
	}

	/**
	 * Setup the timer card adapter.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	private fun setupTimerCardAdapter()
	{
		// Bound view holder
		timerCardAdapter.onViewHolderBoundListener = NacBaseCardAdapter.OnViewHolderBoundListener { card, index ->

			// Timer was just started so do a little circular animation from 0% to 100%
			// on the first tick and then reset the running animation flag
			if (isRunningStartingAnimation[card.timer!!.id] == true)
			{
				card.progressIndicator.animateProgress(0, 100, 500, onEnd = {
					isRunningStartingAnimation[card.timer!!.id] = false
				})
			}

			// Initialize the card if the service has been bound
			if (service?.isUsingTimer(card.timer!!) == true)
			{
				println("SERVICE STILL USING TIMER")
				initTimerCard(card)
			}

		}

		// Created view holder
		timerCardAdapter.onViewHolderCreatedListener = NacBaseCardAdapter.OnViewHolderCreatedListener { card ->

			// Timer clicked listener
			card.onTimerClickedListener = NacTimerCardHolder.OnTimerClickedListener { timer ->

				// Determine the destination fragment to use
				val destinationId = if (service?.isTimerActive(timer) == true)
				{
					R.id.action_nacShowTimersFragment_to_nacActiveTimerFragment
				}
				else
				{
					R.id.action_nacShowTimersFragment_to_nacEditTimerFragment
				}

				// Navigate to the fragment
				findNavController().navigate(destinationId, timer.toBundle())

			}

			// Start timer listener
			card.onStartTimerClickedListener = NacTimerCardHolder.OnStartTimerClickedListener { timer ->
				println("Hello start : ${timer.id} = ${card.timer!!.id} | ${service?.isTimerActive(timer)} or ${timer.isActive} | ${service?.isTimerPaused(timer)}")

				// Resume the timer
				if (service?.isTimerPaused(timer) == true)
				{
					service!!.startCountdownTimer(timer)
					service!!.updateNotification(timer)
				}
				// Start the timer
				else
				{
					// Set the flag that the animation is running
					isRunningStartingAnimation[timer.id] = true

					// Start the service
					val context = requireContext()

					NacActiveTimerService.Companion.startTimerService(context, timer)
					NacActiveTimerService.Companion.bindToService(context, NacActiveTimerService::class.java, serviceConnection)
				}

			}

			// Pause timer listener
			card.onPauseTimerClickedListener = NacTimerCardHolder.OnPauseTimerClickedListener { timer ->
				service?.cancelCountdownTimer(timer)
				service?.updateNotification(timer)
			}

			// Reset timer listener
			card.onResetTimerClickedListener = NacTimerCardHolder.OnResetTimerClickedListener { timer ->

				// Clear the running starting animation flag
				isRunningStartingAnimation[timer.id] = false

				// Reset the timer
				service?.resetCountdownTimer(timer)
				service?.cleanup(timer)

				// Stop the service when no timers are using it
				if (service?.allTimersReadOnly?.isEmpty() == true)
				{
					service!!.stopThisService()
				}

			}

			// Stop timer listener
			card.onStopTimerClickedListener = NacTimerCardHolder.OnStopTimerClickedListener { timer ->

				// Dismiss the timer
				service?.dismiss(timer)

				// Update views back to normal
				card.setResetVisibility()
				card.updateHourMinuteSecondsTextViews(timer.duration)
				card.resetTimerRingingAnimation(requireContext())

			}

		}

		// Attach the recycler view to the touch helper
		timerCardTouchHelper.attachToRecyclerView(recyclerView)
	}

}