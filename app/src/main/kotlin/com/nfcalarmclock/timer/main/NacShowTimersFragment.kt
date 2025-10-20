package com.nfcalarmclock.timer.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.db.NacNextAlarm
import com.nfcalarmclock.card.NacBaseCardAdapter
import com.nfcalarmclock.card.NacBaseCardTouchHelperCallback
import com.nfcalarmclock.card.NacCardLayoutManager
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacBundle.BUNDLE_INTENT_ACTION
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.system.NacNfcIntent
import com.nfcalarmclock.system.getTimer
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.timer.NacTimerViewModel
import com.nfcalarmclock.timer.active.NacActiveTimerService
import com.nfcalarmclock.timer.card.NacTimerCardAdapter
import com.nfcalarmclock.timer.card.NacTimerCardHolder
import com.nfcalarmclock.timer.card.NacTimerCardTouchHelper
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.util.NacUtility.quickToast
import com.nfcalarmclock.view.animateProgress
import com.nfcalarmclock.view.performHapticFeedback
import com.nfcalarmclock.view.toSpannedString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

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
	//private lateinit var timerCardAdapterLiveData: NacCardAdapterLiveData

	/**
	 * Timer card touch helper.
	 */
	private lateinit var timerCardTouchHelper: NacTimerCardTouchHelper

	/**
	 * NFC intent LiveData.
	 */
	private val nfcIntentLiveData: LiveData<Intent> = NacNfcIntent.liveData

	/**
	 * Active timer service.
	 */
	private var service: NacActiveTimerService? = null

	/**
	 * The current snackbar being used.
	 */
	private var currentSnackbar: Snackbar? = null

	/**
	 * Previous Y position of the current snackbar.
	 */
	private var prevSnackbarY: Float = 0f

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
						card.updateHourMinuteSecondsTextViews(secUntilFinished)
						card.setResetVisibility()

					})
			}

			override fun onCountdownTick(timer: NacTimer, secUntilFinished: Long, newProgress: Int)
			{
				println("SHOW TIMERS COUNTDOWN Tick")
				// Get the card
				val card = recyclerView.findViewHolderForItemId(timer.id) as NacTimerCardHolder? ?: return

				if (isRunningStartingAnimation[timer.id] == true)
				{
					println("Still running the starting animation for : ${timer.id}")
					return
				}

				// Update the views
				card.setResumeVisibility()
				card.updateHourMinuteSecondsTextViews(secUntilFinished)

				// Animate the progress to the new progress
				card.progressIndicator.animateProgress(card.progressIndicator.progress, newProgress, 250)
			}

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

			// Initialize each timer being used by the service
			service!!.allTimersReadOnly.forEach { timer ->
				initTimerCard(timer)
			}

			// Add a countdown timer change listener for each timer in the table
			lifecycleScope.launch {
				timerViewModel.getAllTimers().forEach {
					service!!.addOnCountdownTimerChangedListener(it.id, onCountdownTimerChangedListener)
				}
			}
		}

		override fun onServiceDisconnected(className: ComponentName) {}
	}

	fun initTimerCard(card: NacTimerCardHolder)
	{
		// Get the progress and seconds
		val progress = service!!.getProgress(card.timer!!)
		println("Active Fragment SERVICE IS NOW CONNECTED : ${card.timer!!.id} | $progress")

		// Ringing
		if (service?.isTimerRinging(card.timer!!) == true)
		{
			println("Card : ${card.timer!!.id} | RINGING")

			card.secondsTextView.text = resources.getString(R.string.number0)
			card.setStopVisibility()
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
			card.progressIndicator.progress = progress
		}
		// Normal, not doing anything
		else
		{
			println("Card bound : ${card.timer!!.id} | NORMAL")
			card.setResetVisibility()
		}
	}

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
	 * Add a timer to the database.
	 *
	 * TODO: Do this for android.intent.action.SET_TIMER
	 *
	 * @param timer A timer.
	 * @param onInsertListener Listener to call after the timer is inserted and has an ID.
	 */
	private fun addTimer(
		timer: NacTimer,
		messageId: Int,
		onInsertListener: () -> Unit = {})
	{
		// Insert timer
		lifecycleScope.launch {
			timerViewModel.insert(timer) {

				// Countdown timer change listener
				service?.addOnCountdownTimerChangedListener(timer.id, onCountdownTimerChangedListener)

				// Show the snackbar
				val message = getString(messageId)
				val action = getString(R.string.action_undo)

				showSnackbar(message, action,
					onClickListener = {
						// Undo the insert. This will delete the timer
						deleteTimer(timer)
					})

				// Call the listener
				onInsertListener()

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

		// Add the copied timer. When it is added, it will be interacted with but most
		// likely will not be scrolled as it is probably already on screen. This is
		// because the index of the copied alarm is right after the original alarm
		addTimer(copiedTimer, R.string.message_timer_copy) {
			//recentlyAddedAlarmIds.add(copiedTimer.id)
			//recentlyCopiedAlarmIds = Pair(timer.id, copiedTimer.id)
		}
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

		// Remove the countdown timer change listener
		service?.removeOnCountdownTimerChangedListener(timer.id, onCountdownTimerChangedListener)

		// Delete the alarm, save the stats, and cancel the alarm
		timerViewModel.delete(timer)

		// Remove the alarm id in the recently added list. If it is not present, this
		// will not do anything
		// TODO: Recent jank
		//recentlyAddedAlarmIds.remove(timer.id)

		// Show the snackbar
		val message = getString(R.string.message_timer_delete)
		val action = getString(R.string.action_undo)

		showSnackbar(message, action,
			onClickListener = {
				// Undo the delete. This will restore the timer
				restoreTimer(timer)
			},
			onDismissListener = { event ->

				// Check if the snackbar was not dismissed via timeout
				// or if the local media path is empty
				// or if the local media path is equal to the shared preference path
				if ((event != BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_TIMEOUT)
					|| localMediaPath.isEmpty()
					|| (localMediaPath == sharedPreferences.localMediaPath))
				{
					// Do nothing
					return@showSnackbar
				}

				// Check if the local media path is empty or if it is equal to the shared
				// preference local media path
				// TODO: Generalize this or maybe not do this and just delete this logic?
				lifecycleScope.launch {

					// Check if no alarms are using the local media path
					val noMatchingMedia = timerViewModel.getAllTimers()
						.all { it.localMediaPath != localMediaPath }

					if (noMatchingMedia)
					{
						// Delete the local media
						val file = File(localMediaPath)
						file.delete()
					}

				}
			})
	}

	/**
	 * Add an alarm that was created from the SET_ALARM intent.
	 * TODO: See AlarmManager and android.intent.action.SET_TIMER
	 */
	//private fun addAlarmFromSetAlarmIntent(alarm: NacAlarm)
	//{
	//	addAlarm(alarm) {
	//		recentlyAddedAlarmIds.add(alarm.id)
	//	}
	//}

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

		// Get the intent action and alarm from the fragment arguments bundle. These
		// could be null, but if an action occurred, they will not be
		val action = arguments?.getString(BUNDLE_INTENT_ACTION)
		val timer = arguments?.getTimer()

		// Add alarm that was created from the SET_ALARM intent
		if ((action == AlarmClock.ACTION_SET_TIMER) && (timer != null))
		{
			// TODO: Add a timer from this jank
			//addAlarmFromSetAlarmIntent(timer)
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

		NacActiveTimerService.bindToService(context, NacActiveTimerService::class.java, serviceConnection)
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
		recyclerView = view.findViewById(R.id.rv_timer_list)
		timerCardAdapter = NacTimerCardAdapter()
		timerCardAdapterLiveData = MutableLiveData<List<NacTimer>>()
		timerCardTouchHelper = NacTimerCardTouchHelper(object : NacBaseCardTouchHelperCallback.OnCardSwipedListener<NacTimer> {

			override fun onCopySwipe(item: NacTimer, index: Int)
			{
				// Haptic feedback
				view.performHapticFeedback()

				// Reset the view on the alarm that was swiped
				timerCardAdapter.notifyItemChanged(index)

				// Check if the max number of alarms was created
				if (hasCreatedMaxTimers)
				{
					// Show toast that the max number of alarms were created
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
		setupAlarmCardAdapter()
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
	 * Setup the alarm card adapter.
	 */
	@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
	private fun setupAlarmCardAdapter()
	{
		// Bound view holder
		timerCardAdapter.onViewHolderBoundListener = NacBaseCardAdapter.OnViewHolderBoundListener { card, index ->

			// Initialize the card if the service has been bound
			if (service?.isUsingTimer(card.timer!!) == true)
			{
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
					R.id.nacActiveTimerFragment
				}
				else
				{
					R.id.nacEditTimerFragment
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

					// Do a little circular animation from 0% to 100% on the first tick
					// and then reset the running animation flag
					card.progressIndicator.animateProgress(0, 100, 500, onEnd = {
						isRunningStartingAnimation[timer.id] = false
					})

					// Start the service
					val context = requireContext()

					NacActiveTimerService.startTimerService(context, timer)
					NacActiveTimerService.bindToService(context, NacActiveTimerService::class.java, serviceConnection)
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

			}

			// Stop timer listener
			card.onStopTimerClickedListener = NacTimerCardHolder.OnStopTimerClickedListener { timer ->

				// Update views back to normal
				card.setResetVisibility()
				card.updateHourMinuteSecondsTextViews(timer.duration)
				card.progressIndicator.animateProgress(card.progressIndicator.progress, 0, 250)

				// Dismiss the timer
				service?.dismiss(timer)

			}

		}

		// Attach the recycler view to the touch helper
		timerCardTouchHelper.attachToRecyclerView(recyclerView)
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

			// Max number of alarms reached
			if (hasCreatedMaxTimers)
			{
				// Show a toast that the max number of alarms was created
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
		// Observer is called when list of all alarms changes. Including when the app
		// starts and the list is initially empty
		timerViewModel.allTimers.observe(viewLifecycleOwner) { timers ->

			// Sort timers by duration. No need to move around timers if they become
			// active. It just gets confusing as a user if they move around
			timerCardAdapterLiveData.value = timers.sortedBy { it.duration }

		}

		// Observe any changes to the alarms in the adapter
		timerCardAdapterLiveData.observe(viewLifecycleOwner) { timers ->

			// Update the alarm adapter
			timerCardAdapter.submitList(timers)

			//// Scroll down to any newly added alarms
			//if (recentlyAddedAlarmIds.isNotEmpty())
			//{
			//	// Find the index of the of first recently added alarm
			//	val id = recentlyAddedAlarmIds.first()

			//	// Clear the alarm IDs if there was a recently copied alarm
			//	if (id == recentlyCopiedAlarmIds?.second)
			//	{
			//		recentlyCopiedAlarmIds = null
			//	}

			//	// Find the first and last currently visible indices
			//	val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
			//	val firstIndex = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
			//	val lastIndex = linearLayoutManager.findLastCompletelyVisibleItemPosition()
			//	val index = alarms.indexOfFirst { it.id == id }

			//	// Scroll down to that alarm card if it is not visible
			//	if ((index < firstIndex) || (index > lastIndex))
			//	{
			//		recyclerView.smoothScrollToPosition(index)
			//	}
			//}

		}

		// TODO: Do I want an alarm intent watcher and timer intent watcher?
		// NFC intent
		nfcIntentLiveData.observe(viewLifecycleOwner) { intent ->

			println("Show timers new NFC intent! ${intent.action}")
			lifecycleScope.launch {

				// TODO: If this logic is not here, what is the timing between seeing that
				//  NFC was scanned, posting the intent value, checking the alarm view model
				//  for an active jank, and then starting the alarm activity/service?
				// Get the active timer
				val activeTimer = timerViewModel.getActiveTimer()
				println("Show timers active timer check : ${activeTimer != null}")

				// Check if the active alarm is not null
				// here and then add checks to make sure it is safe to pass into these start
				// activity/service functions
				if (activeTimer != null)
				{
					println("Navigate to active timer fragment yo")

					// TODO: Is this necessary or do I need to popBackStack()?
					//// Finish the main activity
					//finish()
				}

			}

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
	}

	/**
	 * Show a snackbar for an alarm.
	 */
	private fun showAlarmSnackbar(alarm: NacAlarm? = null, nextAlarm: NacNextAlarm? = null)
	{
		// Get the context
		val context = requireContext()

		// Get the message and action text
		val action = getString(R.string.action_alarm_dismiss)
		val message = if (alarm != null)
		{
			// When the given alarm will run
			NacCalendar.Message.getWillRun(context, alarm, sharedPreferences.nextAlarmFormat)
		}
		else
		{
			// When the next alarm will run
			val next = nextAlarm ?: NacCalendar.getNextAlarm(timerCardAdapter.currentList)
			NacCalendar.Message.getNext(context, next?.calendar, sharedPreferences.nextAlarmFormat)
		}

		// Show the snackbar
		showSnackbar(message, action)
	}

	/**
	 * Show a snackbar.
	 */
	private fun showSnackbar(
		message: String,
		action: String,
		onClickListener: View.OnClickListener? = null,
		onDismissListener: (Int) -> Unit = {})
	{
		// Check if there is a normal "Dismiss" snackbar that is currently shown, in
		// which case it will be reused
		val shouldReuseSnackbar = (currentSnackbar?.isShown == true) && (onClickListener == null)

		// Check if there is a normal "Dismiss" snackbar that is currently shown
		val snackbar = if (shouldReuseSnackbar)
		{
			// Reuse the snackbar
			currentSnackbar!!.setText(message.toSpannedString())
			currentSnackbar!!.show()
			currentSnackbar!!
		}
		else
		{
			// Create the snackbar
			Snackbar.make(floatingActionButton, message.toSpannedString(),
				Snackbar.LENGTH_LONG)
		}

		// Setup the snackbar
		snackbar.setActionTextColor(sharedPreferences.themeColor)
		snackbar.setAction(action, onClickListener ?: View.OnClickListener { })
		snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)

		// Add callback if listener is set
		if (!shouldReuseSnackbar)
		{
			//// Listener when the snackbar is being drawn and thus when it is moving up and down
			//snackbar.view.viewTreeObserver.addOnPreDrawListener(object: ViewTreeObserver.OnPreDrawListener
			//{

			//	/**
			//	 * Called when the view tree is about to be drawn.
			//	 */
			//	override fun onPreDraw(): Boolean
			//	{
			//		// Get the current Y position
			//		val y = snackbar.view.y

			//		// Do nothing when the snackbar has not been shown yet
			//		if (prevSnackbarY == 0f)
			//		{
			//			return true
			//		}
			//		// Snackbar is moving down
			//		else if (prevSnackbarY < y)
			//		{
			//			// Animate the FAB moving back to its original position
			//			floatingActionButton.animate()
			//				.apply {
			//					translationY(0f)
			//					duration = 250
			//				}
			//				.start()

			//			// Remove the listener
			//			snackbar.view.viewTreeObserver.removeOnPreDrawListener(this)
			//		}
			//		// Snackbar is moving up. Update the previous Y position to compare
			//		// later
			//		else
			//		{
			//			prevSnackbarY = y
			//		}

			//		return true
			//	}

			//})

			//// Listener for when the snackbar is starting to change and become visible.
			//// This means the view has been measured and has a height, so the animation
			//// of the FAB can be started at the same time since now it is known how much
			//// to animate the FAB's Y position by
			//snackbar.view.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->

			//	/**
			//	 * Called when the view layout has changed.
			//	 */
			//	// Get the height of the snackbar
			//	val height = view.height.toFloat()

			//	// Animate the FAB moving up
			//	floatingActionButton.animate()
			//		.apply {
			//			translationY(-height)
			//			duration = 250
			//		}
			//		.start()

			//	// Snackbar was already being shown. Update the Y position to the
			//	// snackbar's current Y position in case its height changed
			//	if (prevSnackbarY > 0)
			//	{
			//		prevSnackbarY = view.y
			//	}
			//}

			//// Add the normal show/dismiss callback
			//snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>()
			//{

			//	/**
			//	 * Called when the snackbar is shown.
			//	 */
			//	override fun onShown(transientBottomBar: Snackbar?)
			//	{
			//		// The snackbar is visible now, so get its starting Y position
			//		prevSnackbarY = snackbar.view.y
			//	}

			//	/**
			//	 * Called when the snackbar has been dismissed.
			//	 */
			//	override fun onDismissed(transientBottomBar: Snackbar?, event: Int)
			//	{
			//		// Call the listener
			//		onDismissListener(event)

			//		// Reset the values of the FAB and snackbar
			//		floatingActionButton.translationY = 0f
			//		prevSnackbarY = 0f
			//	}

			//})
		}

		// Show the snackbar
		snackbar.show()

		// Set the current snackbar
		currentSnackbar = snackbar.takeIf { onClickListener == null }
	}

}