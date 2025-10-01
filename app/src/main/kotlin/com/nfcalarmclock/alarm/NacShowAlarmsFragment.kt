package com.nfcalarmclock.alarm

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.provider.AlarmClock
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.view.MenuCompat
import androidx.core.view.get
import androidx.core.view.isNotEmpty
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.db.NacNextAlarm
import com.nfcalarmclock.alarm.options.NacAlarmOptionsDialog
import com.nfcalarmclock.alarm.options.dateandtime.NacDateAndTimePickerDialog
import com.nfcalarmclock.alarm.options.dismissoptions.NacDismissEarlyService
import com.nfcalarmclock.alarm.options.dismissoptions.NacDismissOptionsDialog
import com.nfcalarmclock.alarm.options.mediapicker.NacMediaPickerActivity
import com.nfcalarmclock.alarm.options.name.NacNameDialog
import com.nfcalarmclock.alarm.options.nfc.NacNfcTagViewModel
import com.nfcalarmclock.alarm.options.nfc.db.NacNfcTag
import com.nfcalarmclock.alarm.options.snoozeoptions.NacSnoozeOptionsDialog
import com.nfcalarmclock.alarm.options.upcomingreminder.NacUpcomingReminderService
import com.nfcalarmclock.card.NacCardAdapter
import com.nfcalarmclock.card.NacCardAdapter.OnViewHolderBoundListener
import com.nfcalarmclock.card.NacCardAdapter.OnViewHolderCreatedListener
import com.nfcalarmclock.card.NacCardAdapterLiveData
import com.nfcalarmclock.card.NacCardHolder
import com.nfcalarmclock.card.NacCardHolder.OnCardAlarmOptionsClickedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardCollapsedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardDaysChangedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardDismissEarlyClickedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardDismissOptionsClickedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardExpandedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardMediaClickedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardNameClickedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardSnoozeOptionsClickedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardSwitchChangedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardTimeClickedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardUpdatedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardUseFlashlightChangedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardUseNfcChangedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardUseRepeatChangedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardUseVibrateChangedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardVolumeChangedListener
import com.nfcalarmclock.card.NacCardLayoutManager
import com.nfcalarmclock.card.NacCardTouchHelper
import com.nfcalarmclock.card.NacCardTouchHelper.OnSwipedListener
import com.nfcalarmclock.main.NacMainActivity.Companion.ACTION_DISMISS_ALARM_EARLY
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.statistics.NacAlarmStatisticViewModel
import com.nfcalarmclock.system.NacBundle.BUNDLE_INTENT_ACTION
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.system.addAlarm
import com.nfcalarmclock.system.createTimeTickReceiver
import com.nfcalarmclock.system.getAlarm
import com.nfcalarmclock.system.registerMyReceiver
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.system.unregisterMyReceiver
import com.nfcalarmclock.timer.NacTimerViewModel
import com.nfcalarmclock.util.NacUtility.quickToast
import com.nfcalarmclock.view.setupThemeColor
import com.nfcalarmclock.view.toSpannedString
import com.nfcalarmclock.widget.refreshAppWidgets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

/**
 * Show all the alarms.
 */
@AndroidEntryPoint
class NacShowAlarmsFragment
	: Fragment(),
	OnSwipedListener,
	OnViewHolderBoundListener,
	OnViewHolderCreatedListener
{

	/**
	 * Navigation controller.
	 */
	private val navController by lazy {
		(childFragmentManager.findFragmentById(R.id.options_content) as NavHostFragment).navController
	}

	/**
	 * Shared preferences.
	 */
	private lateinit var sharedPreferences: NacSharedPreferences

	/**
	 * Floating action button to add new alarms.
	 */
	private lateinit var floatingActionButton: FloatingActionButton

	/**
	 * Next alarm text view.
	 */
	private lateinit var nextAlarmTextView: MaterialTextView

	/**
	 * Recycler view containing the alarm cards.
	 */
	private lateinit var recyclerView: RecyclerView

	/**
	 * Alarm card adapter.
	 */
	private lateinit var alarmCardAdapter: NacCardAdapter

	/**
	 * Alarm view model.
	 */
	private val alarmViewModel: NacAlarmViewModel by viewModels()

	/**
	 * Timer view model.
	 */
	private val timerViewModel: NacTimerViewModel by viewModels()

	/**
	 * Statistic view model.
	 */
	private val statisticViewModel: NacAlarmStatisticViewModel by viewModels()

	/**
	 * NFC tag view model.
	 */
	private val nfcTagViewModel: NacNfcTagViewModel by viewModels()

	/**
	 * List of all NFC tags.
	 */
	private var allNfcTags: List<NacNfcTag> = ArrayList()

	/**
	 * Mutable live data for the alarm card that can be modified and sorted, or
	 * not sorted, depending on the circumstance.
	 *
	 * Live data from the view model cannot be sorted, hence the need for this.
	 */
	private lateinit var alarmCardAdapterLiveData: NacCardAdapterLiveData

	/**
	 * Alarm card touch helper.
	 */
	private lateinit var alarmCardTouchHelper: NacCardTouchHelper

	/**
	 * Receiver for the time tick intent. This is called when the time increments
	 * every minute.
	 */
	private val timeTickReceiver = createTimeTickReceiver { _, _ ->

		// Set the message for when the next alarm will be run
		setNextAlarmMessage()

		// Refresh alarms that will run soon
		refreshAlarmsThatWillAlarmSoon()

	}

	/**
	 * The IDs of alarms that were recently added.
	 */
	private var recentlyAddedAlarmIds: MutableList<Long> = ArrayList()

	/**
	 * The IDs of alarms that were recently updated.
	 */
	private var recentlyUpdatedAlarmIds: MutableList<Long> = ArrayList()

	/**
	 * The IDs of two alarms. The first is the alarm that was copied, and the second is
	 * the alarm that resulted from the copy.
	 */
	private var recentlyCopiedAlarmIds: Pair<Long, Long>? = null

	/**
	 * Handler to refresh next alarm message.
	 */
	private lateinit var nextAlarmMessageHandler: Handler

	/**
	 * The current snackbar being used.
	 */
	private var currentSnackbar: Snackbar? = null

	/**
	 * Previous Y position of the current snackbar.
	 */
	private var prevSnackbarY: Float = 0f

	/**
	 * Saved state of the recyclerview so that it does not change scroll position when
	 * disabling/enabling or changing the time. Anything that causes a sort to occur.
	 */
	private var recyclerViewSavedState: Parcelable? = null

	/**
	 * List of alarm IDs corresponding to cards that are expanded.
	 */
	private var expandedAlarmCardIds: MutableList<Long> = arrayListOf()

	/**
	 * Check if the user has created the maximum number of alarms.
	 */
	private val hasCreatedMaxAlarms: Boolean
		get()
		{
			// Get the current and max counts
			val currentSize = alarmCardAdapter.itemCount
			val maxAlarms = resources.getInteger(R.integer.max_alarms)

			// Check the size
			return (currentSize+1 > maxAlarms)
		}

	/**
	 * Add an alarm to the database.
	 *
	 * @param alarm    An alarm.
	 * @param onInsertListener Listener to call after the alarm is inserted and has an ID.
	 */
	private fun addAlarm(
		alarm: NacAlarm,
		onInsertListener: () -> Unit = {})
	{
		lifecycleScope.launch {

			// Insert alarm
			alarmViewModel.insert(alarm) {

				// Schedule the alarm and call the listener
				NacScheduler.update(requireContext(), alarm)
				onInsertListener()

			}

		}

		// Save the statistics
		statisticViewModel.insertCreated()
	}

	/**
	 * Setup the app version if it is not already setup.
	 */
	/**
	 * Add the first alarm, when the app is first run.
	 */
	private fun addFirstAlarm()
	{
		// Create the alarm
		val alarm = NacAlarm.build(sharedPreferences)

		alarm.id = 0
		alarm.hour = 8
		alarm.minute = 0
		alarm.name = getString(R.string.example_name)

		// Add the alarm
		addAlarm(alarm)
	}

	/**
	 * Copy an alarm and add it to the database.
	 *
	 * @param  alarm  An alarm.
	 */
	private fun copyAlarm(alarm: NacAlarm)
	{
		// Create a copy of the alarm
		val copiedAlarm = alarm.copy()

		// Add the copied alarm. When it is added, it will be interacted with but most
		// likely will not be scrolled as it is probably already on screen. This is
		// because the index of the copied alarm is right after the original alarm
		addAlarm(copiedAlarm) {
			recentlyAddedAlarmIds.add(copiedAlarm.id)
			recentlyCopiedAlarmIds = Pair(alarm.id, copiedAlarm.id)
		}

		// Show the snackbar
		val message = getString(R.string.message_alarm_copy)
		val action = getString(R.string.action_undo)

		showSnackbar(message, action,
			onClickListener = {
				// Undo the copy. This will delete the alarm
				deleteAlarm(copiedAlarm)
			})
	}

	/**
	 * Delete an alarm from the database.
	 *
	 * @param  alarm  An alarm.
	 */
	private fun deleteAlarm(alarm: NacAlarm)
	{
		// Get the local media path
		val localMediaPath = alarm.localMediaPath

		// Delete the alarm, save the stats, and cancel the alarm
		alarmViewModel.delete(alarm)
		statisticViewModel.insertDeleted(alarm)
		NacScheduler.cancel(requireContext(), alarm)

		// Remove the alarm id in the recently added list. If it is not present, this
		// will not do anything
		recentlyAddedAlarmIds.remove(alarm.id)

		// Show the snackbar
		val message = getString(R.string.message_alarm_delete)
		val action = getString(R.string.action_undo)

		showSnackbar(message, action,
			onClickListener = {
				// Undo the delete. This will restore the alarm
				restoreAlarm(alarm)
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
				lifecycleScope.launch {

					// Check if no alarms are using the local media path
					val noMatchingMedia = alarmViewModel.getAllAlarms()
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
	 */
	private fun addAlarmFromSetAlarmIntent(alarm: NacAlarm)
	{
		addAlarm(alarm) {
			recentlyAddedAlarmIds.add(alarm.id)
		}
	}

	/**
	 * Dismiss an alarm early that was sent the intent action to do so.
	 */
	private fun dismissAlarmEarlyFromIntent(alarm: NacAlarm)
	{
		// Dismiss the alarm early and update it
		alarm.dismissEarly()
		updateAlarm(alarm)

		// Clear any notifications
		val context = requireContext()

		NacDismissEarlyService.stopService(context, alarm)
		NacUpcomingReminderService.stopService(context, alarm)
	}

	/**
	 * Get the alarm card at the given index.
	 *
	 * @return The alarm card at the given index.
	 */
	private fun getAlarmCardAt(index: Int): NacCardHolder?
	{
		return recyclerView.findViewHolderForAdapterPosition(index) as NacCardHolder?
	}

	/**
	 * Measure a card.
	 */
	private fun measureCard(card: NacCardHolder)
	{
		// Array that will store the heights
		val heights = IntArray(3)

		// Measure the card
		card.measureCard(heights)

		// Setup the shared preferences with those heights
		sharedPreferences.cardHeightCollapsed = heights[0]
		sharedPreferences.cardHeightCollapsedDismiss = heights[1]
		sharedPreferences.cardHeightExpanded = heights[2]
		sharedPreferences.cardIsMeasured = true
	}

	/**
	 * Called when an alarm card was swiped to copy.
	 *
	 * @param  index  The index of the alarm card.
	 */
	override fun onCopySwipe(alarm: NacAlarm, index: Int)
	{
		// Get the alarm card
		val card = getAlarmCardAt(index)

		// Haptic feedback so that the user knows the action was received
		card?.root?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

		// Reset the view on the alarm that was swiped
		alarmCardAdapter.notifyItemChanged(index)

		// Check if the max number of alarms was created
		if (hasCreatedMaxAlarms)
		{
			// Show toast that the max number of alarms were created
			quickToast(requireContext(), R.string.error_message_max_alarms)
			return
		}

		// Copy the alarm
		copyAlarm(alarm)
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
		return inflater.inflate(R.layout.frg_show_alarms, container, false)
	}

	/**
	 * Called when an alarm card was swiped to delete.
	 *
	 * @param  index  The index of the alarm card.
	 */
	override fun onDeleteSwipe(alarm: NacAlarm, index: Int)
	{
		// Get the alarm card
		val card = getAlarmCardAt(index)

		// Haptic feedback so that the user knows the action was received
		card?.root?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

		// Delete the alarm
		deleteAlarm(alarm)
	}

	/**
	 * Called when the activity is paused.
	 */
	override fun onPause()
	{
		// Super
		super.onPause()

		// Cleanup
		unregisterMyReceiver(requireContext(), timeTickReceiver)
		// TODO: Can this cause the message stopping and not restarting jank? Check onResume()
		nextAlarmMessageHandler.removeCallbacksAndMessages(null)
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
		val alarm = arguments?.getAlarm()

		// Alarm should be dismissed early
		if ((action == ACTION_DISMISS_ALARM_EARLY) && (alarm != null))
		{
			dismissAlarmEarlyFromIntent(alarm)
		}

		// Add alarm that was created from the SET_ALARM intent
		if ((action == AlarmClock.ACTION_SET_ALARM) && (alarm != null))
		{
			addAlarmFromSetAlarmIntent(alarm)
		}

		// Setup UI
		setupFloatingActionButton()

		// Register the time tick receiver
		registerMyReceiver(requireContext(), timeTickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
	}

	/**
	 * Called when the activity is created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Setup
		super.onViewCreated(view, savedInstanceState)

		// Set member variables
		val context = requireContext()
		sharedPreferences = NacSharedPreferences(context)
		nextAlarmTextView = view.findViewById(R.id.tv_next_alarm)
		floatingActionButton = requireActivity().findViewById(R.id.fab_add_alarm)
		recyclerView = view.findViewById(R.id.rv_alarm_list)
		alarmCardAdapter = NacCardAdapter()
		alarmCardAdapterLiveData = NacCardAdapterLiveData()
		alarmCardTouchHelper = NacCardTouchHelper(this)
		nextAlarmMessageHandler = Handler(context.mainLooper)

		// Set flag that cards need to be measured
		sharedPreferences.cardIsMeasured = false

		// Setup live data
		lifecycleScope.launch {
			setupLiveDataObservers()
		}

		// Setup UI
		setupAlarmCardAdapter()
		setupRecyclerView()
	}

	/**
	 * Needed for NacAlarmCardAdapter.OnViewHolderBoundListener.
	 */
	override fun onViewHolderBound(card: NacCardHolder, index: Int)
	{
		// Get the alarm
		val alarm = alarmCardAdapter.getAlarmAt(index)

		// Check if the index is part of the expanded cards
		if (expandedAlarmCardIds.contains(alarm.id))
		{
			// Expand the card and change its color
			card.doExpandWithColor()
		}
		// Index is not part of the expanded cards, but the card is expanded.
		// A card should not be in this state, however, it has been seen to
		// happen after a new install.
		//
		// Expand an alarm, click on the media
		// button, select a ringtone, (maybe) change some other component of an
		// alarm such as an audio option, click on the card to collapse it, and
		// then copy the alarm. For some reason, the card will act as if it is
		// expanded
		//
		// Note: This could occur because by default, the alarm card shows all
		//       the widgets, which could explain why it thinks it is expanded.
		else if (card.isExpanded)
		{
			// Collapse the card
			card.doCollapseWithColor()
		}

		// Check if the alarm card has not been measured
		if (!sharedPreferences.cardIsMeasured)
		{
			// Measure the card
			measureCard(card)
		}

		// Check if the alarm was recently added
		if (recentlyAddedAlarmIds.contains(alarm.id))
		{
			// Card is already visible so did not smooth scroll
			if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE)
			{
				// Highlight the alarm card
				showNewAlarm(card, alarm)
			}
			// Smooth scrolling to card
			else
			{
				// Set a scroll listener so that once the recyclerview has reached where its
				// destination, then the new alarm can be shown
				recyclerView.addOnScrollListener(object : OnScrollListener()
				{

					/**
					 * Called when the scroll state is changed.
					 */
					override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int)
					{
						// Highlight the alarm card and clear the scroll listeners
						// once the recyclerview is no longer scrolling
						if (newState == RecyclerView.SCROLL_STATE_IDLE)
						{
							recyclerView.clearOnScrollListeners()
							showNewAlarm(card, alarm)
						}
					}

				})
			}

			// Remove the alarm from the recently added list
			recentlyAddedAlarmIds.remove(alarm.id)
		}
	}

	/**
	 * Needed for NacAlarmCardAdapter.OnViewHolderCreatedListener.
	 */
	@OptIn(UnstableApi::class)
	override fun onViewHolderCreated(card: NacCardHolder)
	{
		// Get the context
		val context = requireContext()

		// Collapsed listener
		card.onCardCollapsedListener = OnCardCollapsedListener { _, alarm ->

			// Remove the ID from the expanded list
			expandedAlarmCardIds.remove(alarm.id)

			// Sort the list when no cards are expanded
			if (expandedAlarmCardIds.isEmpty())
			{
				// TODO: This could be the cause of poor performance when there are a lot of alarms?
				alarmCardAdapterLiveData.sort()
			}

			// Highlight the alarm card if it was recently updated and then remove it
			// from the recently updated list
			if (recentlyUpdatedAlarmIds.contains(alarm.id))
			{
				card.highlight()
				recentlyUpdatedAlarmIds.remove(alarm.id)
			}

		}

		// Expanded listener
		card.onCardExpandedListener = OnCardExpandedListener { _, alarm ->

			// Add the ID to the expanded list
			expandedAlarmCardIds.add(alarm.id)

		}

		// Updated listener
		// setupDismissEarly
		// setupRepeatButtonLongPress
		// unskip/skipNextAlarm
		card.onCardUpdatedListener = OnCardUpdatedListener { _, alarm ->
			showNextAlarm(card, alarm)
			updateAlarm(alarm)
		}

		// Time
		card.onCardTimeClickedListener = OnCardTimeClickedListener { _, alarm ->
			showTimeDialog(card, alarm)
		}

		// Switch
		card.onCardSwitchChangedListener = OnCardSwitchChangedListener { _, alarm ->

			// Show next alarm and update the alarm
			showNextAlarm(card, alarm)
			updateAlarm(alarm)

			// Alarm was disabled
			if (!alarm.isEnabled)
			{
				// Clear any notifications, just in case
				NacDismissEarlyService.stopService(context, alarm)
				NacUpcomingReminderService.stopService(context, alarm)
			}

		}

		// Dismiss early
		card.onCardDismissEarlyClickedListener = OnCardDismissEarlyClickedListener { _, alarm ->

			// Show next alarm and update the alarm
			showNextAlarm(card, alarm)
			updateAlarm(alarm)

			// Clear any notifications
			NacDismissEarlyService.stopService(context, alarm)
			NacUpcomingReminderService.stopService(context, alarm)

		}

		// Days
		card.onCardDaysChangedListener = OnCardDaysChangedListener { _, alarm ->
			showNextAlarm(card, alarm)
			updateAlarm(alarm)
		}

		// Repeat
		card.onCardUseRepeatChangedListener = OnCardUseRepeatChangedListener { _, alarm ->
			updateAlarm(alarm)
			card.toastRepeat(context)
		}

		// Vibrate
		card.onCardUseVibrateChangedListener = OnCardUseVibrateChangedListener { _, alarm ->
			updateAlarm(alarm)
			card.toastVibrate(context)
		}

		// NFC
		card.onCardUseNfcChangedListener = OnCardUseNfcChangedListener { _, alarm ->
			updateAlarm(alarm)

			lifecycleScope.launch {
				card.toastNfc(context, nfcTagViewModel.getAllNfcTags())
			}
		}

		// Flashlight
		card.onCardUseFlashlightChangedListener = OnCardUseFlashlightChangedListener { _, alarm ->
			updateAlarm(alarm)
			card.toastFlashlight(context)
		}

		// Media
		card.onCardMediaClickedListener = OnCardMediaClickedListener { _, alarm ->

			// Create an intent for the media activity with the alarm attached
			val intent = NacMediaPickerActivity.getStartIntentWithAlarm(context, alarm)

			// Start the activity
			startActivity(intent)

		}

		// Volume
		card.onCardVolumeChangedListener = OnCardVolumeChangedListener { _, alarm ->
			updateAlarm(alarm)
		}

		// Name
		card.onCardNameClickedListener = OnCardNameClickedListener { _, alarm ->

			// Show the dialog
			NacNameDialog.create(
				alarm.name,
				onNameEnteredListener = { name ->

					// Reset the skip next alarm flag
					alarm.shouldSkipNextAlarm = false

					// Set the alarm name
					alarm.name = name

					// Refresh the views and update the alarm
					card.refreshNameViews()
					updateAlarm(alarm)

				})
				.show(parentFragmentManager, NacNameDialog.TAG)

		}

		// Dismiss options
		card.onCardDismissOptionsClickedListener = OnCardDismissOptionsClickedListener { _, alarm ->

			// Show the dialog
			NacDismissOptionsDialog.create(
				alarm,
				onSaveAlarmListener = { updateAlarm(it) })
				.show(parentFragmentManager, NacDismissOptionsDialog.TAG)

		}

		// Snooze options
		card.onCardSnoozeOptionsClickedListener = OnCardSnoozeOptionsClickedListener { _, alarm ->

			// Show the dialog
			NacSnoozeOptionsDialog.create(
				alarm,
				onSaveAlarmListener = { updateAlarm(it) })
				.show(parentFragmentManager, NacSnoozeOptionsDialog.TAG)

		}

		// Alarm options
		card.onCardAlarmOptionsClickedListener = OnCardAlarmOptionsClickedListener { _, alarm ->

			// Show the dialog
			NacAlarmOptionsDialog.navigate(navController, alarm)
				?.observe(this) { a ->

					// Update the alarm
					updateAlarm(a)
					card.refreshRepeatOptionViews()

					// Show next alarm when changing repeat options
					if (navController.currentDestination?.id == R.id.nacRepeatOptionsDialog)
					{
						showNextAlarm(card, alarm)
					}

				}

		}

		// Repeat, vibrate, NFC, and flashlight long click listener
		card.onCardButtonLongClickedListener = NacCardHolder.OnCardButtonLongClickedListener { _, alarm, destinationId ->

			// Show the dialog
			NacAlarmOptionsDialog.quickNavigate(navController, destinationId, alarm)
				?.observe(this) { a ->
					updateAlarm(a)
					card.refreshRepeatOptionViews()
				}

		}

		// Context menu for a card listener
		card.setOnCreateContextMenuListener { menu, _, _ ->

			// Check if it has already been created. Saw double the menu items one
			// time, but cannot seem to replicate it. Adding a check just to avoid
			// this happening in production
			if (menu.isNotEmpty())
			{
				return@setOnCreateContextMenuListener
			}

			// Inflate the context menu
			val menuInflater = MenuInflater(context)
			menuInflater.inflate(R.menu.menu_card, menu)

			// Show group dividers
			MenuCompat.setGroupDividerEnabled(menu, true)

			// Get the alarm for this card holder
			val alarm = card.alarm!!

			// Iterate over each menu item
			for (i in 0 until menu.size)
			{
				// Get the menu item
				val item = menu[i]

				// Check the ID of the menu item
				when (item.itemId)
				{
					// Show next alarm
					R.id.menu_show_next_alarm ->
					{
						// Show the next time the alarm will run
						item.setOnMenuItemClickListener { _ ->
							showAlarmSnackbar(alarm)
							true
						}
					}

					// Show NFC tag
					R.id.menu_show_nfc_tag_id ->
					{
						// Set the visibility of this item based on if NFC is being
						// used for the alarm
						item.isVisible = alarm.shouldUseNfc

						// Show the NFC tag for the current alarm
						item.setOnMenuItemClickListener { _ ->
							card.toastNfcId(context)
							true
						}
					}

					// Skip next alarm
					R.id.menu_skip_next_alarm ->
					{
						// Set the visibility of the menu item based on if the
						// next alarm is NOT skipped
						item.isVisible = !card.alarm!!.shouldSkipNextAlarm && card.alarm!!.isEnabled

						// Skip the next alarm
						item.setOnMenuItemClickListener { _ ->
							card.skipNextAlarm()
							true
						}
					}

					// Unskip next alarm
					R.id.menu_unskip_next_alarm ->
					{
						// Set the visibility of the menu item based on if the
						// next alarm is skipped
						item.isVisible = card.alarm!!.shouldSkipNextAlarm && card.alarm!!.isEnabled

						// Unskip the next alarm
						item.setOnMenuItemClickListener { _ ->
							card.unskipNextAlarm()
							true
						}
					}

					// Unknown
					else -> {}

				}
			}

		}
	}

	/**
	 * Refresh alarms that will alarm soon.
	 */
	private fun refreshAlarmsThatWillAlarmSoon()
	{
		// Iterate over each alarm card in the adapter
		for (i in 0 until alarmCardAdapter.itemCount)
		{
			// Get the card and the alarm
			val card = getAlarmCardAt(i)
			val alarm = alarmCardAdapter.getAlarmAt(i)

			// Check if the alarm will alarm soon and the card needs to be updated
			if ((card != null)
				&& alarm.willAlarmSoon()
				&& card.shouldRefreshExtraView())
			{
				// Refresh the alarm
				alarmCardAdapter.notifyItemChanged(i)
			}
		}
	}

	/**
	 * Restore an alarm and add it back to the database.
	 *
	 * @param  alarm  An alarm.
	 */
	private fun restoreAlarm(alarm: NacAlarm)
	{
		lifecycleScope.launch {

			// Insert the alarm
			// Save the statistics
			// Reschedule the alarm
			alarmViewModel.insert(alarm)
			statisticViewModel.insertCreated()
			NacScheduler.update(requireContext(), alarm)

			// Show the snackbar
			val message = getString(R.string.message_alarm_restore)
			val action = getString(R.string.action_undo)

			showSnackbar(message, action,
				onClickListener = {
					// Undo the restore. This will delete the alarm
					deleteAlarm(alarm)
				})

		}
	}

	/**
	 * Set the next alarm message in the text view.
	 */
	private fun setNextAlarmMessage(nextAlarm: NacNextAlarm? = null): NacNextAlarm?
	{
		// Cancel any post delayed runnables
		nextAlarmMessageHandler.removeCallbacksAndMessages(null)

		// Get the next alarm. Use the parameter when it is supplied, otherwise use the
		// alarm card adapter list
		val nextAlarm = nextAlarm
			?: NacCalendar.getNextAlarm(alarmCardAdapter.currentList)

		// Get the next alarm message
		val message = NacCalendar.Message.getNext(requireContext(),
			nextAlarm?.calendar, sharedPreferences.nextAlarmFormat)

		// Set the message in the text view
		nextAlarmTextView.text = message

		// Check if the next alarm message should be refreshed
		if (shouldRefreshNextAlarmMessage(nextAlarm))
		{
			// Set the message for when the next alarm will be run
			nextAlarmMessageHandler.postDelayed({
				setNextAlarmMessage(nextAlarm = nextAlarm.takeIf { nextAlarm!!.alarm.isEnabled })
			}, REFRESH_NEXT_ALARM_MESSAGE_PERIOD)
		}

		// Return the next alarm
		return nextAlarm
	}

	/**
	 * Setup the alarm card adapter.
	 */
	private fun setupAlarmCardAdapter()
	{
		// Setup the listeners
		alarmCardAdapter.onViewHolderBoundListener = this
		alarmCardAdapter.onViewHolderCreatedListener = this

		// Attach the recycler view to the touch helper
		alarmCardTouchHelper.attachToRecyclerView(recyclerView)

		alarmCardAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver()
		{

			/**
			 * Called when a range of items are moved.
			 */
			override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int)
			{
				// Super
				super.onItemRangeMoved(fromPosition, toPosition, itemCount)

				// Restore the recyclerview saved state
				if (recyclerViewSavedState != null)
				{
					recyclerView.layoutManager?.onRestoreInstanceState(recyclerViewSavedState)
					recyclerViewSavedState = null
				}
			}

		})

	}

	/**
	 * Setup the floating action button.
	 */
	private fun setupFloatingActionButton()
	{
		// Set the click listener
		floatingActionButton.setOnClickListener { view: View ->

			// Haptic feedback so that the user knows the action was received
			view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

			// Max number of alarms reached
			if (hasCreatedMaxAlarms)
			{
				// Show a toast that the max number of alarms was created
				quickToast(requireContext(), R.string.error_message_max_alarms)
				return@setOnClickListener
			}

			// Create the alarm
			val alarm = NacAlarm.build(sharedPreferences)

			// Add the alarm. When it is added, it will be scrolled to and interacted with
			addAlarm(alarm) {
				recentlyAddedAlarmIds.add(alarm.id)
			}
		}
	}

	/**
	 * Run the setup when it is the app's first time running.
	 */
	private fun setupForAppFirstRun()
	{
		// Set the flags indicating that this is no longer the app's first run
		// and that statistics does not need to be started, since they have already started
		sharedPreferences.appFirstRun = false
		sharedPreferences.appStartStatistics = false

		// Add the first alarm
		addFirstAlarm()
	}

	/**
	 * Setup LiveData observers.
	 */
	private fun setupLiveDataObservers()
	{
		// Observer is called when list of all alarms changes. Including when the app
		// starts and the list is initially empty
		alarmViewModel.allAlarms.observe(viewLifecycleOwner) { alarms ->

			// Check if statistics should be started or not
			if (sharedPreferences.appStartStatistics)
			{
				// Setup statistics
				setupStatistics(alarms)
			}

			// Save the next alarm in the app, as opposed to using any alarm set on the
			// device
			if (sharedPreferences.appShouldSaveNextAlarm)
			{
				// Get the previous timezone ID and alarm time
				val preTimeZoneId = sharedPreferences.appNextAlarmTimezoneId
				val preAlarmTimeMillis = sharedPreferences.appNextAlarmTimeMillis

				// Save the next alarm. This will overwrite the timezone ID and alarm time
				sharedPreferences.saveNextAlarm(alarms)

				// Compare the previous and current timezone ID and alarm time. This will
				// indicate if the next alarm changed or not
				if ((preTimeZoneId != sharedPreferences.appNextAlarmTimezoneId)
					|| (preAlarmTimeMillis !=  sharedPreferences.appNextAlarmTimeMillis))
				{
					// Refresh widgets
					refreshAppWidgets(requireContext())
				}
			}

			// Save the recyclerview state so that it does not scroll down with an
			// item that was changed. Instead it should retain its current scroll
			// position
			recyclerViewSavedState = recyclerView.layoutManager?.onSaveInstanceState()

			// Check if no cards are expanded
			if (expandedAlarmCardIds.isEmpty())
			{
				// Merge and sort the alarms
				alarmCardAdapterLiveData.mergeSort(alarms)
			}
			// One or more cards is expanded
			else
			{
				// Merge the alarms but do not sort yet
				alarmCardAdapterLiveData.merge(alarms, copiedIds = recentlyCopiedAlarmIds)

			}

		}

		// Observe any changes to the alarms in the adapter
		alarmCardAdapterLiveData.observe(viewLifecycleOwner) { alarms ->

			// Get if the adapter list is currently empty
			val isAdapterListEmpty = alarmCardAdapter.currentList.isEmpty()

			// If this is the first time the app is running, set the flags accordingly
			if (sharedPreferences.appFirstRun)
			{
				setupForAppFirstRun()
			}

			// Update the alarm adapter
			alarmCardAdapter.submitList(alarms)

			// Refresh the next alarm message when the adapter list was empty. This
			// always happens when the adapter is first created and populated from
			// onCreate()
			if (isAdapterListEmpty)
			{
				setNextAlarmMessage()
			}

			// Scroll down to any newly added alarms
			if (recentlyAddedAlarmIds.isNotEmpty())
			{
				// Find the index of the of first recently added alarm
				val id = recentlyAddedAlarmIds.first()

				// Clear the alarm IDs if there was a recently copied alarm
				if (id == recentlyCopiedAlarmIds?.second)
				{
					recentlyCopiedAlarmIds = null
				}

				// Find the first and last currently visible indices
				val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
				val firstIndex = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
				val lastIndex = linearLayoutManager.findLastCompletelyVisibleItemPosition()
				val index = alarms.indexOfFirst { it.id == id }

				// Scroll down to that alarm card if it is not visible
				if ((index < firstIndex) || (index > lastIndex))
				{
					recyclerView.smoothScrollToPosition(index)
				}
			}

		}

		// Observe list of NFC tags
		nfcTagViewModel.allNfcTags.observe(viewLifecycleOwner) {

			// Get the list of all NFC tags
			allNfcTags = it

			// Set the shared preference whether to show the Manage NFC tags
			// preference or not. It will be shown if there are NFC tags to
			// manage
			sharedPreferences.shouldShowManageNfcTagsPreference = it.isNotEmpty()

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
		val padding = resources.getDimensionPixelSize(R.dimen.normal)
		val drawable = ContextCompat.getDrawable(context, R.drawable.card_divider)
		val divider = InsetDrawable(drawable, padding, 0, padding, 0)

		// Create the item decoration
		val decoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)

		// Set the divider on the decoration
		decoration.setDrawable(divider)

		// Add the decoration to the recycler view. This will divide every item by this
		// decoration
		recyclerView.addItemDecoration(decoration)

		// Setup everything else
		recyclerView.adapter = alarmCardAdapter
		recyclerView.layoutManager = NacCardLayoutManager(context)
		recyclerView.setHasFixedSize(true)
	}

	/**
	 * Setup statistics, and start collecting the data.
	 *
	 * Note: This is only done if this is not the app's first time running and
	 *       the statistics table was not created yet, so it should be started.
	 *
	 * @param  alarms  List of alarms.
	 */
	private fun setupStatistics(alarms: List<NacAlarm>)
	{
		lifecycleScope.launch {

			// Check if nothing has been created
			if (statisticViewModel.createdCount() == 0L)
			{
				// Add a created alarm row to the table for each alarm
				alarms.forEach { _ ->
					statisticViewModel.insertCreated()
				}
			}

			// Disable the flag indicating that statistics does not need to be started
			// anymore
			sharedPreferences.appStartStatistics = false

		}
	}

	/**
	 * Check if the next alarm message should be refreshed each second.
	 *
	 * @return True if the difference between the next alarm and right now is
	 *         less than 60 minutes, and False otherwise.
	 */
	private fun shouldRefreshNextAlarmMessage(nextAlarm: NacNextAlarm?): Boolean
	{
		// No next alarm
		if ((nextAlarm == null) || !nextAlarm.alarm.isEnabled)
		{
			return false
		}

		// Get the next calendar day the alarm will run as well as the
		// calendar right now
		val now = Calendar.getInstance()

		// Compute the difference between the two calendars and convert
		// it to minutes
		val diff = (nextAlarm.calendar.timeInMillis - now.timeInMillis) / 1000L / 60L

		// Check if the difference is less than an hour
		return diff < 60
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
			val next = nextAlarm ?: NacCalendar.getNextAlarm(alarmCardAdapter.currentList)
			NacCalendar.Message.getNext(context, next?.calendar, sharedPreferences.nextAlarmFormat)
		}

		// Show the snackbar
		showSnackbar(message, action)
	}

	/**
	 * Show a snackbar for the next alarm that will run.
	 */
	private fun showNewAlarm(card: NacCardHolder, alarm: NacAlarm)
	{
		// Expand new alarm cards and show the time dialog. Then scroll a little bit
		// further because the expanded card messes with the initial scroll
		if (sharedPreferences.expandNewAlarm)
		{
			card.expand()
			showTimeDialog(card, alarm)
			recyclerView.smoothScrollToPosition(card.bindingAdapterPosition+2)
		}
		// Highlight new alarm cards
		else
		{
			card.highlight()
		}
	}

	/**
	 * Show a snackbar for the next alarm that will run.
	 */
	private fun showNextAlarm(card: NacCardHolder, alarm: NacAlarm)
	{
		// Set the next alarm message and get the next alarm
		val nextAlarm = setNextAlarmMessage()

		// Alarm is enabled
		if (alarm.isEnabled)
		{
			// Show a snackbar for the next time this alarm will run
			showAlarmSnackbar(alarm)
		}
		// Alarm is disabled. Find the next alarm and use it
		else
		{
			// Show a snackbar for the next alarm
			showAlarmSnackbar(nextAlarm = nextAlarm)
		}

		// Highlight the alarm card if it is collapsed
		if (card.isCollapsed)
		{
			card.highlight()
		}
		// Card is expanded
		else
		{
			// Save the ID of the alarm that was modified so that later when it is
			// collapsed, the collapsed logic can be run
			recentlyUpdatedAlarmIds.add(alarm.id)
		}
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
			// Listener when the snackbar is being drawn and thus when it is moving up and down
			snackbar.view.viewTreeObserver.addOnPreDrawListener(object: ViewTreeObserver.OnPreDrawListener
			{

				/**
				 * Called when the view tree is about to be drawn.
				 */
				override fun onPreDraw(): Boolean
				{
					// Get the current Y position
					val y = snackbar.view.y

					// Do nothing when the snackbar has not been shown yet
					if (prevSnackbarY == 0f)
					{
						return true
					}
					// Snackbar is moving down
					else if (prevSnackbarY < y)
					{
						// Animate the FAB moving back to its original position
						floatingActionButton.animate()
							.apply {
								translationY(0f)
								duration = 250
							}
							.start()

						// Remove the listener
						snackbar.view.viewTreeObserver.removeOnPreDrawListener(this)
					}
					// Snackbar is moving up. Update the previous Y position to compare
					// later
					else
					{
						prevSnackbarY = y
					}

					return true
				}

			})

			// Listener for when the snackbar is starting to change and become visible.
			// This means the view has been measured and has a height, so the animation
			// of the FAB can be started at the same time since now it is known how much
			// to animate the FAB's Y position by
			snackbar.view.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->

				/**
				 * Called when the view layout has changed.
				 */
				// Get the height of the snackbar
				val height = view.height.toFloat()

				// Animate the FAB moving up
				floatingActionButton.animate()
					.apply {
						translationY(-height)
						duration = 250
					}
					.start()

				// Snackbar was already being shown. Update the Y position to the
				// snackbar's current Y position in case its height changed
				if (prevSnackbarY > 0)
				{
					prevSnackbarY = view.y
				}
			}

			// Add the normal show/dismiss callback
			snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>()
			{

				/**
				 * Called when the snackbar is shown.
				 */
				override fun onShown(transientBottomBar: Snackbar?)
				{
					// The snackbar is visible now, so get its starting Y position
					prevSnackbarY = snackbar.view.y
				}

				/**
				 * Called when the snackbar has been dismissed.
				 */
				override fun onDismissed(transientBottomBar: Snackbar?, event: Int)
				{
					// Call the listener
					onDismissListener(event)

					// Reset the values of the FAB and snackbar
					floatingActionButton.translationY = 0f
					prevSnackbarY = 0f
				}

			})
		}

		// Show the snackbar
		snackbar.show()

		// Set the current snackbar
		currentSnackbar = snackbar.takeIf { onClickListener == null }
	}

	/**
	 * Show the time picker dialog.
	 */
	private fun showTimeDialog(card: NacCardHolder, alarm: NacAlarm)
	{
		NacDateAndTimePickerDialog.create(
			alarm,
			onDateClearedListener = {

				// Clear the date
				alarm.date = ""

				// Enable the alarm and clear the skip next alarm flag
				alarm.isEnabled = true
				alarm.shouldSkipNextAlarm = false

				// Clear the dismiss early time
				alarm.timeOfDismissEarlyAlarm = 0

				// Refresh the schedule date views
				card.refreshScheduleDateViews()

				// Show the next alarm, update the alarm, and save the next alarm
				showNextAlarm(card, alarm)
				updateAlarm(alarm)

			},
			onDateSelectedListener = { _, year, month, day ->

				// Set the date
				alarm.date = "$year-${month+1}-$day"

				// Enable the alarm and clear the repeat and skip next alarm flag
				alarm.isEnabled = true
				alarm.shouldRepeat = false
				alarm.shouldSkipNextAlarm = false

				// Clear the dismiss early time
				alarm.timeOfDismissEarlyAlarm = 0

				// Clear all the days
				alarm.setDays(0)

				// Refresh the schedule date views
				card.refreshScheduleDateViews()

				// Show the next alarm, update the alarm, and save the next alarm
				showNextAlarm(card, alarm)
				updateAlarm(alarm)

			},
			onTimeSelectedListener = { _, hr, min ->

				// Set the time
				alarm.hour = hr
				alarm.minute = min

				// Enable the alarm and reset the skip next alarm flag
				alarm.isEnabled = true
				alarm.shouldSkipNextAlarm = false

				// Clear the dismiss early time
				alarm.timeOfDismissEarlyAlarm = 0

				// Refresh the time views
				card.refreshTimeViews()

				// Show the next alarm, update the alarm, and save the next alarm
				showNextAlarm(card, alarm)
				updateAlarm(alarm)

			})
			.show(parentFragmentManager, NacDateAndTimePickerDialog.TAG)
	}

	/**
	 * Update the alarm and reschedule it.
	 */
	private fun updateAlarm(alarm: NacAlarm)
	{
		// Update the alarm
		alarmViewModel.update(alarm)

		// Reschedule the alarm
		NacScheduler.update(requireContext(), alarm)
	}

	companion object
	{

		/**
		 * Rate at which the next alarm message is refreshed.
		 */
		const val REFRESH_NEXT_ALARM_MESSAGE_PERIOD = 1000L

	}

}