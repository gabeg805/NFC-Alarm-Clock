package com.nfcalarmclock.main

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Parcelable
import android.text.format.DateFormat
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.MenuCompat
import androidx.core.view.get
import androidx.core.view.isNotEmpty
import androidx.core.view.size
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.nfcalarmclock.BuildConfig
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.NacAlarmViewModel
import com.nfcalarmclock.alarm.activealarm.NacActiveAlarmActivity
import com.nfcalarmclock.alarm.activealarm.NacActiveAlarmService
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.db.NacNextAlarm
import com.nfcalarmclock.alarm.options.NacAlarmOptionsDialog
import com.nfcalarmclock.alarm.options.dateandtime.NacDateAndTimePickerDialog
import com.nfcalarmclock.alarm.options.dismissoptions.NacDismissEarlyService
import com.nfcalarmclock.alarm.options.dismissoptions.NacDismissOptionsDialog
import com.nfcalarmclock.alarm.options.mediapicker.NacMediaPickerActivity
import com.nfcalarmclock.alarm.options.name.NacNameDialog
import com.nfcalarmclock.alarm.options.nfc.NacNfc
import com.nfcalarmclock.alarm.options.nfc.NacNfcTagViewModel
import com.nfcalarmclock.alarm.options.nfc.db.NacNfcTag
import com.nfcalarmclock.alarm.options.snoozeoptions.NacSnoozeOptionsDialog
import com.nfcalarmclock.card.NacCardAdapter
import com.nfcalarmclock.card.NacCardAdapter.OnViewHolderBoundListener
import com.nfcalarmclock.card.NacCardAdapter.OnViewHolderCreatedListener
import com.nfcalarmclock.card.NacCardAdapterLiveData
import com.nfcalarmclock.card.NacCardHolder
import com.nfcalarmclock.card.NacCardHolder.OnCardAlarmOptionsClickedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardCollapsedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardDaysChangedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardDismissOptionsClickedListener
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
import com.nfcalarmclock.ratemyapp.NacRateMyApp
import com.nfcalarmclock.settings.NacMainSettingActivity
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.statistics.NacAlarmStatisticViewModel
import com.nfcalarmclock.system.permission.NacPermissionRequestManager
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.system.triggers.shutdown.NacShutdownBroadcastReceiver
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.util.NacUtility.quickToast
import com.nfcalarmclock.util.addAlarm
import com.nfcalarmclock.util.createTimeTickReceiver
import com.nfcalarmclock.util.disableActivityAlias
import com.nfcalarmclock.util.getAlarm
import com.nfcalarmclock.util.getDeviceProtectedStorageContext
import com.nfcalarmclock.util.getSetAlarm
import com.nfcalarmclock.util.media.buildLocalMediaPath
import com.nfcalarmclock.util.media.copyMediaToDeviceEncryptedStorage
import com.nfcalarmclock.util.media.getMediaArtist
import com.nfcalarmclock.util.media.getMediaTitle
import com.nfcalarmclock.util.media.getMediaType
import com.nfcalarmclock.util.registerMyReceiver
import com.nfcalarmclock.util.unregisterMyReceiver
import com.nfcalarmclock.view.setupThemeColor
import com.nfcalarmclock.view.toSpannedString
import com.nfcalarmclock.whatsnew.NacWhatsNewDialog
import com.nfcalarmclock.widget.refreshAppWidgets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

/**
 * The application's main activity.
 */
@AndroidEntryPoint
class NacMainActivity

	// Constructor
	: AppCompatActivity(),

	// Interface
	Toolbar.OnMenuItemClickListener,
	OnSwipedListener,
	OnViewHolderBoundListener,
	OnViewHolderCreatedListener
{

	/**
	 * Navigation controller.
	 */
	private val navController by lazy {
		(supportFragmentManager.findFragmentById(R.id.hello_content) as NavHostFragment).navController
	}

	/**
	 * Shared preferences.
	 */
	private lateinit var sharedPreferences: NacSharedPreferences

	/**
	 * Root view.
	 */
	private lateinit var root: View

	/**
	 * Top toolbar.
	 */
	private lateinit var toolbar: MaterialToolbar

	/**
	 * Next alarm text view.
	 */
	private lateinit var nextAlarmTextView: MaterialTextView

	/**
	 * Recycler view containing the alarm cards.
	 */
	private lateinit var recyclerView: RecyclerView

	/**
	 * Floating action button to add new alarms.
	 */
	private lateinit var floatingActionButton: FloatingActionButton

	/**
	 * Alarm card adapter.
	 */
	private lateinit var alarmCardAdapter: NacCardAdapter

	/**
	 * Alarm view model.
	 */
	private val alarmViewModel: NacAlarmViewModel by viewModels()

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
	 * Shutdown broadcast receiver.
	 */
	private val shutdownBroadcastReceiver: NacShutdownBroadcastReceiver = NacShutdownBroadcastReceiver()

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
	 * Permission request manager, handles requesting permissions from the user.
	 */
	private lateinit var permissionRequestManager: NacPermissionRequestManager

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
	 * Get the number of alarm cards that are expanded.
	 */
	private val cardsExpandedCount: Int
		get() = alarmCardAdapter.getCardsExpandedCount(recyclerView)

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
	 * Check if the What's New dialog should be shown.
	 */
	private val shouldShowWhatsNewDialog: Boolean
		get()
		{
			// Get the previous version
			val previousVersion = sharedPreferences.previousAppVersion

			// Only show the dialog if the current version and the previously
			// saved version do not match.
			//
			// This does not apply to a newly installed app, in which case the
			// previously saved version is empty. Do not show the What's New
			// dialog to a person that just installed the app because they
			// probably do not care
			return previousVersion.isNotEmpty() && (BuildConfig.VERSION_NAME != previousVersion)
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
				NacScheduler.update(this@NacMainActivity, alarm)
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
	 * Add an alarm that was created from the SET_ALARM intent.
	 */
	private fun addSetAlarmFromIntent()
	{
		// Get the alarm from the intent
		val alarm = intent.getSetAlarm(this)

		// Check if the alarm is not null
		if (alarm != null)
		{
			// Add the alarm. When it is added, it will be scrolled to and interacted with
			addAlarm(alarm) {
				recentlyAddedAlarmIds.add(alarm.id)
			}
		}
	}

	/**
	 * Cleanup any zip files from emailing statistics.
	 */
	private fun cleanupEmailZipFiles()
	{
		// Get the file listing in the app-specific directory
		val appFileListing = filesDir.listFiles() ?: emptyArray()

		// Iterate over each file
		for (file in appFileListing)
		{
			// Check if file does not end in zip. Only care about zip files
			if (file.extension != "zip")
			{
				// Skip this file
				continue
			}

			try
			{
				// Delete the file
				file.delete()
			}
			catch (_: SecurityException)
			{
			}
		}
	}

	/**
	 * Cleanup any extra media files in device encrypted storage that are not used by any
	 * alarm.
	 *
	 * This will typically happen if an alarm changes the media that they are using for
	 * an alarm.
	 */
	private suspend fun cleanupExtraMediaFilesInDeviceEncryptedStorage()
	{
		// Get the device context
		val deviceContext = getDeviceProtectedStorageContext(this)

		// Get all the local media paths for each alarm
		val allAlarmLocalMediaPaths = alarmViewModel.getAllAlarms().map { it.localMediaPath }

		// Cleanup any extra media files that are not used by any alarm
		deviceContext.filesDir.listFiles()
			?.filter { !allAlarmLocalMediaPaths.contains(it.path) }
			?.forEach { it.delete() }
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
		NacScheduler.cancel(this, alarm)

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
	 * Do the event to update and backup media info in all alarms starting at database
	 * version 31.
	 */
	private suspend fun doEventUpdateAndBackupMediaInfoInAlarmsDbV31()
	{
		// Iterate over each alarm that has the media path set
		alarmViewModel.getAllAlarms()
			.filter { it.mediaPath.isNotEmpty() }
			.forEach { alarm ->

				// Get the media uri
				val uri = alarm.mediaPath.toUri()

				// Update the alarm
				alarm.mediaArtist = uri.getMediaArtist(this)
				alarm.mediaTitle = uri.getMediaTitle(this)
				alarm.mediaType = uri.getMediaType(this)
				alarm.localMediaPath = buildLocalMediaPath(this,
					alarm.mediaArtist, alarm.mediaTitle, alarm.mediaType)

				// Update the database
				alarmViewModel.update(alarm)

				// Copy the media to device encrypted storage in case of having to run an
				// alarm in direct boot mode
				copyMediaToDeviceEncryptedStorage(this, alarm.mediaPath, alarm.mediaArtist,
					alarm.mediaTitle, alarm.mediaType)

			}

		// Mark the event as completed
		sharedPreferences.eventUpdateAndBackupMediaInfoInAlarmsDbV31 = true
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
			quickToast(this, R.string.error_message_max_alarms)
			return
		}

		// Copy the alarm
		copyAlarm(alarm)
	}

	/**
	 * Called when the activity is created.
	 */
	@SuppressLint("NewApi")
	override fun onCreate(savedInstanceState: Bundle?)
	{
		// Setup
		super.onCreate(savedInstanceState)

		// Move the shared preference to device protected storage
		NacSharedPreferences.moveToDeviceProtectedStorage(this)

		// Set the content view
		setContentView(R.layout.act_main)
		println(Environment.getExternalStorageDirectory().path)

		// Set member variables
		sharedPreferences = NacSharedPreferences(this)

		root = findViewById(R.id.activity_main)
		toolbar = findViewById(R.id.tb_top_bar)
		nextAlarmTextView = findViewById(R.id.tv_next_alarm)
		recyclerView = findViewById(R.id.rv_alarm_list)
		floatingActionButton = findViewById(R.id.fab_add_alarm)
		alarmCardAdapter = NacCardAdapter()
		alarmCardAdapterLiveData = NacCardAdapterLiveData()
		alarmCardTouchHelper = NacCardTouchHelper(this)
		permissionRequestManager = NacPermissionRequestManager(this)
		nextAlarmMessageHandler = Handler(applicationContext.mainLooper)

		// Set flag that cards need to be measured
		sharedPreferences.cardIsMeasured = false

		// Run alarm update events in order, namely, before the livedata observer is setup
		lifecycleScope.launch {

			// Setup events
			setupEventsFromSharedPreferences()

			// Setup live data
			setupLiveDataObservers()

		}

		// Setup UI
		toolbar.setOnMenuItemClickListener(this)
		setupAlarmCardAdapter()
		setupRecyclerView()
		setupEdgeToEdge()

		// Disable the activity alias so that tapping an NFC tag will NOT open
		// the main activity
		disableActivityAlias(this)

		// Cleanup any old zip files that were created when sending a
		// statistics email
		cleanupEmailZipFiles()

		// Cleanup any extra media files in device encrypted storage
		lifecycleScope.launch {  cleanupExtraMediaFilesInDeviceEncryptedStorage() }

		//lifecycleScope.launch {
		//	nfcTagViewModel.getAllNfcTags().forEach { nfcTagViewModel.delete(it) }
		//	nfcTagViewModel.insert(NacNfcTag("Bathroom", "akjshdlaksdh"))
		//	nfcTagViewModel.insert(NacNfcTag("Car", "9083kjjhllkjls"))
		//	nfcTagViewModel.insert(NacNfcTag("Garage", "09kj091kj"))
		//	nfcTagViewModel.insert(NacNfcTag("Medicine", "102938kjh3l12"))
		//	nfcTagViewModel.insert(NacNfcTag("Take out the trash", "loi120910j"))
		//}
	}

	/**
	 * Called when the options menu is created.
	 */
	override fun onCreateOptionsMenu(menu: Menu): Boolean
	{
		// Inflate the menu bar
		menuInflater.inflate(R.menu.menu_action_bar, menu)

		return true
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
	 * Catch when a menu item is clicked.
	 */
	override fun onMenuItemClick(item: MenuItem): Boolean
	{
		// Check which menu item was clicked
		return when (item.itemId)
		{
			// Settings
			R.id.menu_settings ->
			{
				// Create the intent to show the settings activity
				val settingsIntent = Intent(this, NacMainSettingActivity::class.java)

				// Start the activity
				startActivity(settingsIntent)
				true
			}

			// Unknown
			else -> false
		}
	}

	/**
	 * Called when the activity is paused.
	 */
	override fun onPause()
	{
		// Super
		super.onPause()

		// Cleanup
		unregisterMyReceiver(this, timeTickReceiver)
		unregisterMyReceiver(this, shutdownBroadcastReceiver)
		nextAlarmMessageHandler.removeCallbacksAndMessages(null)

		// Stop NFC
		NacNfc.stop(this)
	}

	/**
	 * Called when the activity is resumed.
	 */
	@OptIn(UnstableApi::class)
	override fun onResume()
	{
		// Super
		super.onResume()

		lifecycleScope.launch {

			// Get the active alarm
			val activeAlarm = alarmViewModel.getActiveAlarm()

			// Check if the active alarm is not null
			// TODO: Add print statements to see what a normal intent should look like
			// here and then add checks to make sure it is safe to pass into these start
			// activity/service functions
			if (activeAlarm != null)
			{

				// Check if an NFC tag was scanned to open up the main activity
				if (NacNfc.wasScanned(intent))
				{
					// Start the alarm activity with the intent containing the
					// NFC tag information in order to dismiss this alarm
					NacActiveAlarmActivity.startAlarmActivity(this@NacMainActivity, intent, activeAlarm)
				}
				// An NFC tag was not scanned so start the alarm service normally
				else
				{
					// Start the alarm service for this alarm
					NacActiveAlarmService.startAlarmService(this@NacMainActivity, activeAlarm)
				}

				// Finish the main activity
				finish()
			}

			// Check if an alarm should be dismissed early
			if (intent.action == ACTION_DISMISS_ALARM_EARLY)
			{
				// Get the alarm from the intent
				val alarm = intent.getAlarm()

				// Check if the alarm is in the intent
				if (alarm != null)
				{
					// Dismiss the alarm early and update it
					alarm.dismissEarly()
					updateAlarm(alarm)

					// Clear the notification
					val intent = NacDismissEarlyService.getStopIntent(this@NacMainActivity, alarm)
					startService(intent)
				}
				// Null alarm
				else
				{
					// Show error toast
					quickToast(this@NacMainActivity, R.string.error_message_unable_to_dismiss_early)
				}
			}

		}

		// Check if the main activity should be refreshed
		if (sharedPreferences.shouldRefreshMainActivity)
		{
			// Refresh the activity
			refreshMainActivity()
			return
		}

		// Set the message for when the next alarm will be run
		setNextAlarmMessage()

		// Setup UI
		setupFloatingActionButton()
		setupInitialDialogToShow()

		// Register the time tick receiver
		registerMyReceiver(this, timeTickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))

		// Register the shutdown receiver
		val shutdownIntentFilter = IntentFilter()

		shutdownIntentFilter.addAction(Intent.ACTION_SHUTDOWN)
		shutdownIntentFilter.addAction(Intent.ACTION_REBOOT)
		registerMyReceiver(this, shutdownBroadcastReceiver, shutdownIntentFilter)

		// Add alarm from SET_ALARM intent (if it is present in intent)
		addSetAlarmFromIntent()

		// Start NFC
		NacNfc.start(this)

		// Refresh widgets
		refreshAppWidgets(this)
	}

	/**
	 * Needed for NacAlarmCardAdapter.OnViewHolderBoundListener.
	 */
	override fun onViewHolderBound(card: NacCardHolder, index: Int)
	{
		// Check if the alarm card has not been measured
		if (!sharedPreferences.cardIsMeasured)
		{
			// Measure the card
			measureCard(card)
		}

		// Get the alarm
		val alarm = alarmCardAdapter.getAlarmAt(index)

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
		// Collapsed listener
		card.onCardCollapsedListener = OnCardCollapsedListener { _, alarm ->

			// Sort the list when no cards are expanded
			if (cardsExpandedCount == 0)
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
			showNextAlarm(card, alarm)
			updateAlarm(alarm)
		}

		// Days
		card.onCardDaysChangedListener = OnCardDaysChangedListener { _, alarm ->
			showNextAlarm(card, alarm)
			updateAlarm(alarm)
		}

		// Repeat
		card.onCardUseRepeatChangedListener = OnCardUseRepeatChangedListener { _, alarm ->
			updateAlarm(alarm)
			card.toastRepeat(this)
		}

		// Vibrate
		card.onCardUseVibrateChangedListener = OnCardUseVibrateChangedListener { _, alarm ->
			updateAlarm(alarm)
			card.toastVibrate(this)
		}

		// NFC
		card.onCardUseNfcChangedListener = OnCardUseNfcChangedListener { _, alarm ->
			updateAlarm(alarm)

			lifecycleScope.launch {
				card.toastNfc(this@NacMainActivity, nfcTagViewModel.getAllNfcTags())
			}
		}

		// Flashlight
		card.onCardUseFlashlightChangedListener = OnCardUseFlashlightChangedListener { _, alarm ->
			updateAlarm(alarm)
			card.toastFlashlight(this)
		}

		// Media
		card.onCardMediaClickedListener = OnCardMediaClickedListener { _, alarm ->

			// Create an intent for the media activity with the alarm attached
			val intent = NacMediaPickerActivity.getStartIntentWithAlarm(this, alarm)

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
				.show(supportFragmentManager, NacNameDialog.TAG)

		}

		// Dismiss options
		card.onCardDismissOptionsClickedListener = OnCardDismissOptionsClickedListener { _, alarm ->

			// Show the dialog
			NacDismissOptionsDialog.create(
				alarm,
				onSaveAlarmListener = { updateAlarm(it) })
				.show(supportFragmentManager, NacDismissOptionsDialog.TAG)

		}

		// Snooze options
		card.onCardSnoozeOptionsClickedListener = OnCardSnoozeOptionsClickedListener { _, alarm ->

			// Show the dialog
			NacSnoozeOptionsDialog.create(
				alarm,
				onSaveAlarmListener = { updateAlarm(it) })
				.show(supportFragmentManager, NacSnoozeOptionsDialog.TAG)

		}

		// Alarm options
		card.onCardAlarmOptionsClickedListener = OnCardAlarmOptionsClickedListener { _, alarm ->

			// Show the dialog
			NacAlarmOptionsDialog.navigate(navController, alarm)
				?.observe(this) { a ->
					println("Updating alarm")
					updateAlarm(a)
					card.refreshRepeatOptionViews()
				}

		}

		// Repeat, vibrate, NFC, and flashlight long click listener
		card.onCardButtonLongClickedListener = NacCardHolder.OnCardButtonLongClickedListener { _, alarm, destinationId ->

			// Show the dialog
			NacAlarmOptionsDialog.quickNavigate(navController, destinationId, alarm)
				?.observe(this) { a ->
					println("YO I'm HERE")
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
							card.toastNfcId(this)
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
	 * Refresh the main activity.
	 */
	private fun refreshMainActivity()
	{
		// Disable that flag indicating that the main activity should refresh
		sharedPreferences.shouldRefreshMainActivity = false

		// Recreate the activity
		recreate()
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
			NacScheduler.update(this@NacMainActivity, alarm)

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
	private fun setNextAlarmMessage(): NacNextAlarm?
	{
		// Set the next alarm message from the current list of alarms
		val nextAlarm = setNextAlarmMessage(alarmCardAdapter.currentList)

		// Check if the next alarm message should be refreshed
		if (shouldRefreshNextAlarmMessage(nextAlarm))
		{
			// Set the message for when the next alarm will be run
			nextAlarmMessageHandler.postDelayed({ setNextAlarmMessage() },
				REFRESH_NEXT_ALARM_MESSAGE_PERIOD)
		}

		// Return the next alarm
		return nextAlarm
	}

	/**
	 * Set the next alarm message in the text view.
	 */
	private fun setNextAlarmMessage(alarms: List<NacAlarm>): NacNextAlarm?
	{
		// Get the next alarm
		val nextAlarm = NacCalendar.getNextAlarm(alarms)

		// Get the next alarm message
		val message = NacCalendar.Message.getNext(this, nextAlarm?.calendar,
			sharedPreferences.nextAlarmFormat)

		// Set the message in the text view
		nextAlarmTextView.text = message

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
	 * Setup any views that need changing due to API 35+ edge-to-edge.
	 */
	private fun setupEdgeToEdge()
	{
		// Check if API < 35, then edge-to-edge is not enforced and do not need to do
		// anything
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM)
		{
			return
		}

		// TODO: Can maybe customize this more when going up to API 36, but for now opting out
		// Set edge to edge color of top status bar
		//findViewById<ProtectionLayout>(R.id.protection_layout)
		//	.setProtections(
		//		listOf(
		//			ColorProtection(WindowInsetsCompat.Side.TOP, Color.BLACK)
		//		)
		//	)
	}

	/**
	 * Setup the events from shared preferences.
	 */
	private suspend fun setupEventsFromSharedPreferences()
	{
		// Check if should update and backup media information in alarms, starting at
		// database version 31
		if (!sharedPreferences.eventUpdateAndBackupMediaInfoInAlarmsDbV31)
		{
			doEventUpdateAndBackupMediaInfoInAlarmsDbV31()
		}

		// Check if should fix any auto dismiss, auto snooze, or snooze duration values
		// that are set to 0 in alarms.
		if (!sharedPreferences.eventFixZeroAutoDismissAndSnooze)
		{
			sharedPreferences.runEventFixZeroAutoDismissAndSnooze(
				alarmViewModel.getAllAlarms(),
				onAlarmChanged = { alarm ->

					// Update the database and reschedule the alarm
					alarmViewModel.update(alarm)
					NacScheduler.update(this, alarm)

				})
		}
	}

	/**
	 * Setup the floating action button.
	 */
	private fun setupFloatingActionButton()
	{
		// Set the color
		floatingActionButton.setupThemeColor(sharedPreferences)

		// Set the click listener
		floatingActionButton.setOnClickListener { view: View ->

			// Haptic feedback so that the user knows the action was received
			view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

			// Max number of alarms reached
			if (hasCreatedMaxAlarms)
			{
				// Show a toast that the max number of alarms was created
				quickToast(this@NacMainActivity, R.string.error_message_max_alarms)
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
	 * Setup an initial dialog, if any, that need to be shown.
	 */
	@SuppressLint("NotifyDataSetChanged")
	private fun setupInitialDialogToShow()
	{
		// Get the delay counter for showing the what's new dialog
		val delayCounter = sharedPreferences.delayShowingWhatsNewDialogCounter

		// Check if there are any permissions that need to be requested
		if (permissionRequestManager.count() > 0)
		{
			// Check if the what's new dialog should be shown
			if (shouldShowWhatsNewDialog)
			{
				// Set the delay counter for showing the what's new dialog.
				// Do not want to show the what's new dialog immediately after
				// all the permissions are requested
				sharedPreferences.delayShowingWhatsNewDialogCounter = 1
			}

			// Request permissions
			permissionRequestManager.requestPermissions(this, onDone = {

				// Refresh the recyclerview in case this is the first time the user is
				// using the app and the alarm cards do not show because of the
				// request manager showing up first
				recyclerView.adapter = null
				recyclerView.layoutManager = null
				recyclerView.adapter = alarmCardAdapter
				recyclerView.layoutManager = NacCardLayoutManager(this)
				alarmCardAdapter.notifyDataSetChanged()

			})
		}
		// Attempt to show the What's new dialog
		else if (shouldShowWhatsNewDialog && delayCounter == 0)
		{
			// Show the What's New dialog
			NacWhatsNewDialog.show(supportFragmentManager,
				listener = {

					// Set the previous app version as the current version. This way, the What's
					// New dialog does not show again
					sharedPreferences.previousAppVersion = BuildConfig.VERSION_NAME

				})
		}
		// Check if the delay counter has been set
		else if (delayCounter > 0)
		{
			// Check if the delay counter has exceeded the max count
			if (delayCounter >= 4)
			{
				// Reset the delay counter
				sharedPreferences.delayShowingWhatsNewDialogCounter = 0
			}
			// The delay counter has not been exceeded yet
			else
			{
				// Increment the delay counter
				sharedPreferences.delayShowingWhatsNewDialogCounter = delayCounter + 1
			}
		}
		// Check if should request to show the rate my app flow
		else if (NacRateMyApp.shouldRequest(sharedPreferences))
		{
			// Request for the user to rate my app
			NacRateMyApp.request(this, sharedPreferences)
		}
	}

	/**
	 * Setup LiveData observers.
	 */
	private fun setupLiveDataObservers()
	{
		// Observer is called when list of all alarms changes. Including when the app
		// starts and the list is initially empty
		alarmViewModel.allAlarms.observe(this) { alarms ->

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
				sharedPreferences.saveNextAlarm(alarms)
			}

			// Save the recyclerview state so that it does not scroll down with an
			// item that was changed. Instead it should retain its current scroll
			// position
			recyclerViewSavedState = recyclerView.layoutManager?.onSaveInstanceState()

			// Check if no cards are expanded
			if (cardsExpandedCount == 0)
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

			// Set the next alarm message
			setNextAlarmMessage(alarms)
		}

		// Observe any changes to the alarms in the adapter
		alarmCardAdapterLiveData.observe(this) { alarms ->

			// If this is the first time the app is running, set the flags accordingly
			if (sharedPreferences.appFirstRun)
			{
				setupForAppFirstRun()
			}

			// Update the alarm adapter
			alarmCardAdapter.storeIndicesOfExpandedCards(recyclerView)
			alarmCardAdapter.submitList(alarms)

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
		nfcTagViewModel.allNfcTags.observe(this) {

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
		// Create the divider drawable
		val padding = resources.getDimensionPixelSize(R.dimen.normal)
		val drawable = ContextCompat.getDrawable(this, R.drawable.card_divider)
		val divider = InsetDrawable(drawable, padding, 0, padding, 0)

		// Create the item decoration
		val decoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)

		// Set the divider on the decoration
		decoration.setDrawable(divider)

		// Add the decoration to the recycler view. This will divide every item by this
		// decoration
		recyclerView.addItemDecoration(decoration)

		// Setup everything else
		recyclerView.adapter = alarmCardAdapter
		recyclerView.layoutManager = NacCardLayoutManager(this)
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
		// Check if there is no next alarm
		if (nextAlarm == null)
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
		// Get the message and action text
		val action = getString(R.string.action_alarm_dismiss)
		val message = if (alarm != null)
		{
			// When the given alarm will run
			NacCalendar.Message.getWillRun(this, alarm, sharedPreferences.nextAlarmFormat)
		}
		else
		{
			// When the next alarm will run
			val next = nextAlarm ?: NacCalendar.getNextAlarm(alarmCardAdapter.currentList)
			NacCalendar.Message.getNext(this, next?.calendar, sharedPreferences.nextAlarmFormat)
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
		// Set the next alarm message
		val nextAlarm = setNextAlarmMessage()

		// Show a snackbar for the next time this alarm will run, if it is enabled
		if (alarm.isEnabled)
		{
			showAlarmSnackbar(alarm)
		}
		// Show a snackbar for the next alarm that will run
		else
		{
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

	//private fun showNfcTagDialog(alarm: NacAlarm)
	//{
	//	// Create and set the dialog
	//	val scanNfcTagDialog = NacScanNfcTagDialog()

	//	// Setup the dialog
	//	scanNfcTagDialog.onScanNfcTagListener = object: OnScanNfcTagListener {

	//		/**
	//		 * Called when the user cancels the scan NFC tag dialog.
	//		 */
	//		override fun onDone(alarm: NacAlarm)
	//		{
	//			// Start NFC. This is here so that if an NFC tag accidentally gets
	//			// scanned multiple times after this dialog is closed, it will not
	//			// popup some unwanted NFC Entry intent. Instead, it will keep the
	//			// focus on this app
	//			NacNfc.start(this@NacMainActivity)
	//		}

	//	}

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
			Snackbar.make(root, message.toSpannedString(),
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
		// Get whether 24 hour format should be used
		val is24HourFormat = DateFormat.is24HourFormat(this)

		// Create the dialog
		val dialog = NacDateAndTimePickerDialog()

		// Setup the date picker
		dialog.onSetupDatePickerListener = NacDateAndTimePickerDialog.OnSetupDatePickerListener {

			println("Current  : ${System.currentTimeMillis()}")
			println("Calendar : ${Calendar.getInstance().timeInMillis}")

			val now = Calendar.getInstance()
			val alarmCal = NacCalendar.alarmToCalendar(alarm, skipDate = true)

			// Min date
			it.minDate = if (alarmCal.before(now))
			{
				now.add(Calendar.DAY_OF_MONTH, 1)
				now.timeInMillis
			}
			else
			{
				System.currentTimeMillis() - 1000
			}

			// First day of week
			it.firstDayOfWeek = if (sharedPreferences.startWeekOn == 1) Calendar.MONDAY else Calendar.SUNDAY
		}

		// Setup the time picker
		dialog.onSetupTimePickerListener = NacDateAndTimePickerDialog.OnSetupTimePickerListener {
			it.hour = alarm.hour
			it.minute = alarm.minute
			it.setIs24HourView(is24HourFormat)
		}

		// Date listener
		dialog.onDateSelectedListener = NacDateAndTimePickerDialog.OnDateSelectedListener { _, year, month, day ->

			// Set the date
			println("Year : $year | Month : $month | Day : $day")
			alarm.date = "$year-${month+1}-$day"
			println("Date : ${alarm.date}")

			// Set various other alarm attributes that setting the date affects
			alarm.isEnabled = true
			alarm.setDays(0)
			alarm.shouldRepeat = false
			alarm.shouldSkipNextAlarm = false
			//alarm.repeatFrequencyDaysToRunBeforeStarting = NacCalendar.Day.WEEK

			// Refresh the schedule date views
			card.refreshScheduleDateViews()

			// Show the next alarm, update the alarm, and save the next alarm
			showNextAlarm(card, alarm)
			updateAlarm(alarm)

		}

		// Time listener
		dialog.onTimeSelectedListener = NacDateAndTimePickerDialog.OnTimeSelectedListener { _, hr, min ->
			println("HERE TIME")

			// Reset the skip next alarm flag
			alarm.shouldSkipNextAlarm = false

			// Set the alarm attributes
			alarm.hour = hr
			alarm.minute = min
			alarm.isEnabled = true

			// Refresh the time views
			card.refreshTimeViews()

			// Show the next alarm, update the alarm, and save the next alarm
			showNextAlarm(card, alarm)
			updateAlarm(alarm)
		}

		// Show the dialog
		dialog.show(supportFragmentManager, NacDateAndTimePickerDialog.TAG)

		//FragmentManager fragmentManager = ((AppCompatActivity)context)
		//	.getSupportFragmentManager();
		//MaterialTimePicker timepicker = new MaterialTimePicker.Builder()
		//	.setHour(hour)
		//	.setMinute(minute)
		//	.setTimeFormat(is24HourFormat ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H)
		//	.build();

		//timepicker.addOnPositiveButtonClickListener(this);
		//timepicker.show(fragmentManager, "TimePicker");
	}

	/**
	 * Update the alarm and reschedule it.
	 */
	private fun updateAlarm(alarm: NacAlarm)
	{
		// Update the alarm
		alarmViewModel.update(alarm)

		// Reschedule the alarm
		NacScheduler.update(this, alarm)
	}

	companion object
	{

		/**
		 * Rate at which the next alarm message is refreshed.
		 */
		const val REFRESH_NEXT_ALARM_MESSAGE_PERIOD = 1000L

		/**
		 * Dismiss an alarm early action.
		 */
		private const val ACTION_DISMISS_ALARM_EARLY = "com.nfcalarmclock.ACTION_DISMISS_ALARM_EARLY"

		/**
		 * Create an intent that will be used to dismiss an alarm early.
		 *
		 * @param  context  A context.
		 *
		 * @return The Main activity intent.
		 */
		fun getDismissEarlyIntent(context: Context, alarm: NacAlarm): Intent
		{
			// Create an intent with the main activity
			val intent = Intent(context, NacMainActivity::class.java)
			val flags = (Intent.FLAG_ACTIVITY_NEW_TASK
					or Intent.FLAG_ACTIVITY_CLEAR_TASK)

			// Add the action, alarm, and flags to the intent
			intent.apply {
				action = ACTION_DISMISS_ALARM_EARLY
				addAlarm(alarm)
				addFlags(flags)
			}

			return intent
		}

		/**
		 * Create an intent that will be used to start the Main activity.
		 *
		 * @param  context  A context.
		 *
		 * @return The Main activity intent.
		 */
		fun getStartIntent(context: Context): Intent
		{
			// Create an intent with the main activity
			val intent = Intent(context, NacMainActivity::class.java)
			val flags = (Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)

			// Add the flags to the intent
			intent.addFlags(flags)

			return intent
		}

		/**
		 * Create a pending intent that will be used to start the Main activity.
		 *
		 * @param  context  A context.
		 *
		 * @return The Main activity pending intent.
		 */
		fun getStartPendingIntent(context: Context): PendingIntent
		{
			// Get the start intent
			val intent = getStartIntent(context)

			// Set the pending intent flags
			val flags = PendingIntent.FLAG_IMMUTABLE

			// Return the pending intent for the activity
			return PendingIntent.getActivity(context, 0, intent, flags)
		}

	}

}