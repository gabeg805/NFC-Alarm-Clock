package com.nfcalarmclock.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.os.Handler
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnCreateContextMenuListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.nfcalarmclock.BuildConfig
import com.nfcalarmclock.R
import com.nfcalarmclock.activealarm.NacActiveAlarmActivity
import com.nfcalarmclock.activealarm.NacActiveAlarmService
import com.nfcalarmclock.alarm.NacAlarmViewModel
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarmoptions.NacAlarmOptionsDialog
import com.nfcalarmclock.alarmoptions.NacAlarmOptionsDialog.OnAlarmOptionClickedListener
import com.nfcalarmclock.audiosource.NacAudioSourceDialog
import com.nfcalarmclock.audiosource.NacAudioSourceDialog.OnAudioSourceSelectedListener
import com.nfcalarmclock.card.NacCardAdapter
import com.nfcalarmclock.card.NacCardAdapter.OnViewHolderBoundListener
import com.nfcalarmclock.card.NacCardAdapter.OnViewHolderCreatedListener
import com.nfcalarmclock.card.NacCardAdapterLiveData
import com.nfcalarmclock.card.NacCardHolder
import com.nfcalarmclock.card.NacCardHolder.OnCardAudioOptionsClickedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardCollapsedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardDeleteClickedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardMediaClickedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardUpdatedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardUseNfcChangedListener
import com.nfcalarmclock.card.NacCardLayoutManager
import com.nfcalarmclock.card.NacCardTouchHelper
import com.nfcalarmclock.card.NacCardTouchHelper.OnSwipedListener
import com.nfcalarmclock.dismissearly.NacDismissEarlyDialog
import com.nfcalarmclock.dismissearly.NacDismissEarlyDialog.OnDismissEarlyOptionSelectedListener
import com.nfcalarmclock.graduallyincreasevolume.NacGraduallyIncreaseVolumeDialog
import com.nfcalarmclock.graduallyincreasevolume.NacGraduallyIncreaseVolumeDialog.OnGraduallyIncreaseVolumeListener
import com.nfcalarmclock.mediapicker.NacMediaActivity
import com.nfcalarmclock.nfc.NacNfc
import com.nfcalarmclock.nfc.NacNfcTagViewModel
import com.nfcalarmclock.nfc.NacSaveNfcTagDialog
import com.nfcalarmclock.nfc.NacScanNfcTagDialog
import com.nfcalarmclock.nfc.NacScanNfcTagDialog.OnScanNfcTagListener
import com.nfcalarmclock.nfc.db.NacNfcTag
import com.nfcalarmclock.permission.NacPermissionRequestManager
import com.nfcalarmclock.ratemyapp.NacRateMyApp
import com.nfcalarmclock.restrictvolume.NacRestrictVolumeDialog
import com.nfcalarmclock.restrictvolume.NacRestrictVolumeDialog.OnRestrictVolumeListener
import com.nfcalarmclock.scheduler.NacScheduler
import com.nfcalarmclock.settings.NacMainSettingActivity
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.shutdown.NacShutdownBroadcastReceiver
import com.nfcalarmclock.statistics.NacAlarmStatisticViewModel
import com.nfcalarmclock.tts.NacTextToSpeechDialog
import com.nfcalarmclock.tts.NacTextToSpeechDialog.OnTextToSpeechOptionsSelectedListener
import com.nfcalarmclock.upcomingreminder.NacUpcomingReminderDialog
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.util.NacIntent
import com.nfcalarmclock.util.NacUtility.quickToast
import com.nfcalarmclock.util.createTimeTickReceiver
import com.nfcalarmclock.util.disableActivityAlias
import com.nfcalarmclock.util.registerMyReceiver
import com.nfcalarmclock.util.unregisterMyReceiver
import com.nfcalarmclock.whatsnew.NacWhatsNewDialog
import com.nfcalarmclock.whatsnew.NacWhatsNewDialog.OnReadWhatsNewListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

/**
 * The application's main activity.
 */
@AndroidEntryPoint
class NacMainActivity

	// Constructor
	: AppCompatActivity(),

	// Interface
	OnCreateContextMenuListener,
	Toolbar.OnMenuItemClickListener,
	OnSwipedListener,
	OnViewHolderBoundListener,
	OnViewHolderCreatedListener,
	OnCardCollapsedListener,
	OnCardUpdatedListener,
	OnCardUseNfcChangedListener
{

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
	 * Last saved/selected NFC tag when enabling NFC on an alarm.
	 */
	private var lastNfcTag: NacNfcTag = NacNfcTag()

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
	 * Handler to refresh next alarm message.
	 */
	private lateinit var nextAlarmMessageHandler: Handler

	/**
	 * Alarm that is being used by an open audio options dialog.
	 */
	private var audioOptionsAlarm: NacAlarm? = null

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
	 * Check if the app has been newly installed.
	 */
	private val isNewInstall: Boolean
		get()
		{
			// Get the previous version
			val previousVersion = sharedPreferences.previousAppVersion

			// When the previous saved version is empty, this means that the
			// alarm has been newly installed
			return previousVersion.isEmpty()
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

	private fun setupWhichAlarmScreenToUse()
	{
		// Check if the app was not newly installed
		if (!isNewInstall)
		{
			// Do nothing
			return
		}

		// Change the alarm screen settings for a new install so that all the
		// new stuff is shown
		sharedPreferences.editUseNewAlarmScreen(true)
		sharedPreferences.editShowAlarmName(true)
		sharedPreferences.editShowCurrentDateAndTime(true)
		sharedPreferences.editShowMusicInfo(true)
	}

	/**
	 * Add an alarm to the database.
	 *
	 * @param alarm    An alarm.
	 * @param interact Whether the alarm should be interacted with by the card
	 *                 holder via interact() or not.
	 */
	private fun addAlarm(alarm: NacAlarm, interact: Boolean=true)
	{
		lifecycleScope.launch {

			// Insert alarm
			alarmViewModel.insert(alarm) {

				// Schedule the alarm
				NacScheduler.update(this@NacMainActivity, alarm)

				// Check if the alarm card should be interacted with. This
				// would essentially mean calling interact() on the alarm card
				if (interact)
				{
					// Save the recently added alarm ID
					recentlyAddedAlarmIds.add(alarm.id)
				}

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

		// Add the alarm and avoid having interact() called for the alarm card,
		// that way it does not get expanded and show the time dialog
		addAlarm(alarm, interact = false)
	}

	/**
	 * Add an alarm that was created from the SET_ALARM intent.
	 */
	private fun addSetAlarmFromIntent()
	{
		// Get the alarm from the intent
		val alarm = NacIntent.getSetAlarm(this, intent)

		// Check if the alarm is not null
		if (alarm != null)
		{
			// Add the alarm
			addAlarm(alarm)
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
	 * Copy an alarm and add it to the database.
	 *
	 * @param  alarm  An alarm.
	 */
	private fun copyAlarm(alarm: NacAlarm)
	{
		// Create a copy of the alarm
		val copiedAlarm = alarm.copy()

		// Add the copied alarm
		addAlarm(copiedAlarm)

		// Show the snackbar
		val message = getString(R.string.message_alarm_copy)
		val action = getString(R.string.action_undo)

		showSnackbar(message, action) {

			// Undo the copy, so delete the alarm
			deleteAlarm(copiedAlarm)

		}
	}

	/**
	 * Delete an alarm from the database.
	 *
	 * @param  alarm  An alarm.
	 */
	private fun deleteAlarm(alarm: NacAlarm)
	{
		// Delete the alarm
		alarmViewModel.delete(alarm)

		// Save the statistics
		statisticViewModel.insertDeleted(alarm)

		// Cancel the alarm
		NacScheduler.cancel(this, alarm)

		// Show the snackbar
		val message = getString(R.string.message_alarm_delete)
		val action = getString(R.string.action_undo)

		showSnackbar(message, action) {

			// Undo the delete, so restore the alarm
			restoreAlarm(alarm)

		}
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
		sharedPreferences.editCardHeightCollapsed(heights[0])
		sharedPreferences.editCardHeightCollapsedDismiss(heights[1])
		sharedPreferences.editCardHeightExpanded(heights[2])
		sharedPreferences.editCardIsMeasured(true)
	}

	/**
	 * Called when the alarm card is collapsed.
	 */
	override fun onCardCollapsed(holder: NacCardHolder, alarm: NacAlarm)
	{
		// Sort the list when no cards are expanded
		if (cardsExpandedCount == 0)
		{
			alarmCardAdapterLiveData.sort()
		}

		// Check if this alarm was recently updated
		if (recentlyUpdatedAlarmIds.contains(alarm.id))
		{
			// Show the next time the alarm will go off
			showUpdatedAlarmSnackbar(alarm)

			// Highlight the card
			holder.highlight()

			// Remove the alarm from the recently updated list
			recentlyUpdatedAlarmIds.remove(alarm.id)
		}
	}

	/**
	 * Called when the alarm has been changed.
	 *
	 * @param  alarm  The alarm that was changed.
	 */
	override fun onCardUpdated(holder: NacCardHolder, alarm: NacAlarm)
	{
		// Set the next alarm message
		setNextAlarmMessage()

		// Card is collapsed
		if (holder.isCollapsed)
		{
			showUpdatedAlarmSnackbar(alarm)
			holder.highlight()
		}
		else
		{
			recentlyUpdatedAlarmIds.add(alarm.id)
		}

		// Update the view model
		alarmViewModel.update(alarm)

		// Reschedule the alarm
		NacScheduler.update(this, alarm)
	}

	/**
	 * Called when the use NFC button is clicked.
	 */
	override fun onCardUseNfcChanged(holder: NacCardHolder, alarm: NacAlarm)
	{
		// Check if the alarm had use NFC disabled
		if (!alarm.shouldUseNfc)
		{
			return
		}

		// Show the scan NFC tag dialog
		showScanNfcTagDialog(alarm)
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

		// Set the content view
		setContentView(R.layout.act_main)

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
		sharedPreferences.editCardIsMeasured(false)

		// Setup live data
		setupLiveDataObservers()

		// Setup UI
		toolbar.setOnMenuItemClickListener(this)
		setupAlarmCardAdapter()
		setupRecyclerView()
		setupWhichAlarmScreenToUse()

		// Disable the activity alias so that tapping an NFC tag will NOT open
		// the main activity
		disableActivityAlias(this)

		// Cleanup any old zip files that were created when sending a
		// statistics email
		cleanupEmailZipFiles()

		//lifecycleScope.launch {
		//	nfcTagViewModel.deleteAll()
		//	nfcTagViewModel.insert(NacNfcTag("Bathroom", "akjshdlaksdh"))
		//	nfcTagViewModel.insert(NacNfcTag("Car", "9083kjjhllkjls"))
		//	nfcTagViewModel.insert(NacNfcTag("Garage", "09kj091kj"))
		//	nfcTagViewModel.insert(NacNfcTag("Medicine", "102938kjh3l12"))
		//	nfcTagViewModel.insert(NacNfcTag("Take out the trash", "loi120910j"))
		//}
	}

	/**
	 * Create the context menu.
	 *
	 * TODO: I saw double of the context menu items after an alarm was auto dismissed
	 */
	override fun onCreateContextMenu(
		menu: ContextMenu,
		view: View,
		menuInfo: ContextMenuInfo?
	)
	{
		// Check if it has already been created. Saw double the menu items one
		// time, but cannot seem to replicate it. Adding a check just to avoid
		// this happening in production
		if (menu.size() > 0)
		{
			return
		}

		// Inflate the context menu
		menuInflater.inflate(R.menu.menu_card, menu)

		// Iterate over each menu item
		for (i in 0 until menu.size())
		{
			// Get the menu item
			val item = menu.getItem(i)

			// Set the listener for a menu item
			item.setOnMenuItemClickListener { menuItem: MenuItem ->

				// Get the card holder
				val holder = recyclerView.findContainingViewHolder(view) as NacCardHolder?

				// Check to make sure the card holder is not null
				if (holder != null)
				{
					when (menuItem.itemId)
					{
						// Show the next time the alarm will run
						R.id.menu_show_next_alarm -> showAlarmSnackbar(holder.alarm!!)

						// Show the NFC tag ID that was set for this alarm
						R.id.menu_show_nfc_tag_id -> showNfcTagId(holder.alarm!!)

						// Unknown
						else -> {}
					}
				}

				// Return
				true
			}
		}
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
	override fun onResume()
	{
		// Super
		super.onResume()

		lifecycleScope.launch {

			// Get the active alarm
			val activeAlarm = alarmViewModel.getActiveAlarm()

			// Check if the active alarm is not null
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
	}

	/**
	 * Needed for NacAlarmCardAdapter.OnViewHolderBoundListener.
	 */
	override fun onViewHolderBound(holder: NacCardHolder, index: Int)
	{
		// Check if the alarm card has not been measured
		if (!sharedPreferences.cardIsMeasured)
		{
			// Measure the card
			measureCard(holder)
		}

		// Get the alarm
		val alarm = alarmCardAdapter.getAlarmAt(index)

		// Check if the alarm was recently added
		if (recentlyAddedAlarmIds.contains(alarm.id))
		{
			// Interact with card holder of that alarm, showing the time dialog
			// and expanding the card if the user wants that
			holder.interact()

			// Remove the alarm from the recently added list
			recentlyAddedAlarmIds.remove(alarm.id)
		}
	}

	/**
	 * Needed for NacAlarmCardAdapter.OnViewHolderCreatedListener.
	 */
	override fun onViewHolderCreated(holder: NacCardHolder)
	{
		// Set the card collapsed listener
		holder.onCardCollapsedListener = this

		// Set the listener for when the delete button is clicked
		holder.onCardDeleteClickedListener = OnCardDeleteClickedListener { _, alarm ->

			// Delete the alarm
			deleteAlarm(alarm)

		}

		// Set the listener for when the media button is clicked
		holder.onCardMediaClickedListener = OnCardMediaClickedListener { _, alarm ->

			// Create an intent for the media activity with the alarm attached
			val intent = NacMediaActivity.getStartIntentWithAlarm(this, alarm)

			// Start the activity
			startActivity(intent)

		}

		// Set the listener for when the audio options button is clicked
		holder.onCardAudioOptionsClickedListener = OnCardAudioOptionsClickedListener { _, alarm ->

			// Show the audio options dialog
			showAudioOptionsDialog(alarm)

		}

		holder.onCardUpdatedListener = this
		holder.onCardUseNfcChangedListener = this
		holder.setOnCreateContextMenuListener(this)
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
				&& card.shouldRefreshDismissView())
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
		sharedPreferences.editShouldRefreshMainActivity(false)

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
			alarmViewModel.insert(alarm)

			// Save the statistics
			statisticViewModel.insertCreated()

			// Reschedule the alarm
			NacScheduler.update(this@NacMainActivity, alarm)

			// Show the snackbar
			val message = getString(R.string.message_alarm_restore)
			val action = getString(R.string.action_undo)

			showSnackbar(message, action) {

				// Undo the restore, so delete the alarm
				deleteAlarm(alarm)

			}

		}
	}

	/**
	 * Save the NFC tag ID to the alarm.
	 */
	private fun saveNfcTagId(alarm: NacAlarm, tagId: String)
	{
		// Set the NFC tag ID
		alarm.nfcTagId = tagId

		// Update the alarm
		alarmViewModel.update(alarm)

		// Reschedule the alarm
		NacScheduler.update(this, alarm)

		lifecycleScope.launch(Dispatchers.Main) {

			// Toast to the that NFC is required
			quickToast(this@NacMainActivity, R.string.message_nfc_required)

		}
	}

	/**
	 * Set the next alarm message in the text view.
	 */
	private fun setNextAlarmMessage()
	{
		// Set the next alarm message from the current list of alarms
		setNextAlarmMessage(alarmCardAdapter.currentList)

		// Check if the next alarm message should be refreshed
		if (shouldRefreshNextAlarmMessage())
		{
			// Set the message for when the next alarm will be run
			nextAlarmMessageHandler.postDelayed({ setNextAlarmMessage() },
				REFRESH_NEXT_ALARM_MESSAGE_PERIOD)
		}
	}

	/**
	 * Set the next alarm message in the text view.
	 */
	private fun setNextAlarmMessage(alarms: List<NacAlarm>)
	{
		// Get the next alarm
		val nextAlarm = NacCalendar.getNextAlarm(alarms)

		// Get the next alarm message
		val message = NacCalendar.Message.getNextAlarm(this, nextAlarm,
			sharedPreferences.nextAlarmFormat)

		// Set the message in the text view
		nextAlarmTextView.text = message
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
	}

	/**
	 * Setup the floating action button.
	 */
	private fun setupFloatingActionButton()
	{
		// Get the theme color
		val color = ColorStateList.valueOf(sharedPreferences.themeColor)

		// Set the listener
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

			// Add the alarm
			addAlarm(alarm)
		}

		// Set the color
		floatingActionButton.backgroundTintList = color
	}

	/**
	 * Run the setup when it is the app's first time running.
	 */
	private fun setupForAppFirstRun()
	{
		// Set the flags indicating that this is no longer the app's first run
		// and that statistics does not need to be started, since they have already started
		sharedPreferences.editAppFirstRun(this, false)
		sharedPreferences.editAppStartStatistics(false)

		// Add the first alarm
		addFirstAlarm()
	}

	/**
	 * Setup an initial dialog, if any, that need to be shown.
	 */
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
				sharedPreferences.editDelayShowingWhatsNewDialogCounter(1)
			}

			// Request permissions
			permissionRequestManager.requestPermissions(this)
		}
		// Attempt to show the What's new dialog
		else if (shouldShowWhatsNewDialog && delayCounter == 0)
		{
			// Show the What's New dialog
			showWhatsNewDialog()
		}
		// Check if the delay counter has been set
		else if (delayCounter > 0)
		{
			// Check if the delay counter has exceeded the max count
			if (delayCounter >= 4)
			{
				// Reset the delay counter
				sharedPreferences.editDelayShowingWhatsNewDialogCounter(0)
			}
			// The delay counter has not been exceeded yet
			else
			{
				// Increment the delay counter
				sharedPreferences.editDelayShowingWhatsNewDialogCounter(delayCounter + 1)
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

			// Check if statistics have not started yet
			if (!sharedPreferences.appStartStatistics)
			{
				// Setup statistics
				setupStatistics(alarms)
			}

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
				alarmCardAdapterLiveData.merge(alarms)
			}

			// Set the next alarm message
			setNextAlarmMessage(alarms)
		}

		// Observe any changes to the alarms in the adapter
		alarmCardAdapterLiveData.observe(this) { alarms ->

			 // Alarm list has changed.
			 // TODO: There is a race condition between snoozing an alarm, writing to the database, and refreshing the main activity.

			// If this is the first time the app is running, set the flags accordingly
			if (sharedPreferences.appFirstRun)
			{
				setupForAppFirstRun()
			}

			// Update the alarm adapter
			alarmCardAdapter.storeIndicesOfExpandedCards(recyclerView)
			alarmCardAdapter.submitList(alarms)

			// Check if the main activity should be refreshed and if so, refresh it
			// TODO: Why is this here?
			if (sharedPreferences.shouldRefreshMainActivity)
			{
				refreshMainActivity()
			}

		}

		// Observe list of NFC tags
		nfcTagViewModel.allNfcTags.observe(this) {

			// Get the list of all NFC tags
			allNfcTags = it

			// Set the shared preference whether to show the Manage NFC tags
			// preference or not. It will be shown if there are NFC tags to
			// manage
			sharedPreferences.editShouldShowManageNfcTagsPreference(it.isNotEmpty())

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
				// Iterate over each alarm
				for (a in alarms)
				{
					// Add a created alarm row to the table
					statisticViewModel.insertCreated()
				}
			}

			// Disable the flag indicating that statistics does not need to be started
			// anymore
			sharedPreferences.editAppStartStatistics(false)

		}
	}

	/**
	 * Check if the next alarm message should be refreshed each second.
	 *
	 * @return True if the difference between the next alarm and right now is
	 *         less than 60 minutes, and False otherwise.
	 */
	private fun shouldRefreshNextAlarmMessage(): Boolean
	{
		// Get the next alarm or return if it is null
		val nextAlarm = NacCalendar.getNextAlarm(alarmCardAdapter.currentList)
			?: return false

		// Get the next calendar day the alarm will run as well as the
		// calendar right now
		val now = Calendar.getInstance()
		val nextCal = NacCalendar.getNextAlarmDay(nextAlarm)

		// Compute the difference between the two calendars and convert
		// it to minutes
		val diff = (nextCal.timeInMillis - now.timeInMillis) / 1000L / 60L

		// Check if the difference is less than an hour
		return diff < 60
	}

	/**
	 * Show a snackbar for the alarm.
	 */
	private fun showAlarmSnackbar(alarm: NacAlarm)
	{
		// Get the message and action for the snackbar
		val message = NacCalendar.Message.getWillRun(this, alarm,
			sharedPreferences.nextAlarmFormat)
		val action = getString(R.string.action_alarm_dismiss)

		// Show the snackbar
		showSnackbar(message, action)
	}

	/**
	 * Show the audio options dialog.
	 */
	private fun showAudioOptionsDialog(alarm: NacAlarm)
	{
		// Create the dialog
		val dialog = NacAlarmOptionsDialog()

		// Setup the dialog
		dialog.alarmId = alarm.id
		dialog.onAlarmOptionClickedListener = OnAlarmOptionClickedListener { alarmId, id ->

			lifecycleScope.launch {

				// Set the alarm that had the audio options button clicked
				audioOptionsAlarm = alarmViewModel.findAlarm(alarmId)

				// Determine which option to do
				when (id)
				{
					R.id.alarm_option_audio_source -> showAudioSourceDialog()
					R.id.alarm_option_gradually_increase_volume -> showGraduallyIncreaseVolumeDialog()
					R.id.alarm_option_restrict_volume -> showRestrictVolumeDialog()
					R.id.alarm_option_text_to_speech -> showTextToSpeechDialog()
					R.id.alarm_option_dismiss_early -> showDismissEarlyDialog()
					R.id.alarm_option_upcoming_reminder -> showUpcomingReminderDialog()
					else -> {}
				}

			}

		}

		// Show the dialog
		dialog.show(supportFragmentManager, NacAlarmOptionsDialog.TAG)
	}

	/**
	 * Show the audio source dialog.
	 */
	private fun showAudioSourceDialog()
	{
		// Create the dialog
		val dialog = NacAudioSourceDialog()

		// Set the default audio source
		dialog.defaultAudioSource = audioOptionsAlarm!!.audioSource

		// Setup the listener
		dialog.onAudioSourceSelectedListener = OnAudioSourceSelectedListener { audioSource ->

			// Set the audio source of the audio options alarm
			audioOptionsAlarm!!.audioSource = audioSource

			// Update the alarm
			alarmViewModel.update(audioOptionsAlarm!!)

			// Reschedule the alarm
			NacScheduler.update(this, audioOptionsAlarm!!)

		}

		// Show the dialog
		dialog.show(supportFragmentManager, NacAudioSourceDialog.TAG)
	}

	/**
	 * Show the dismiss early dialog.
	 */
	private fun showDismissEarlyDialog()
	{
		// Create the dialog
		val dialog = NacDismissEarlyDialog()

		// Set the default values
		dialog.defaultShouldDismissEarly = audioOptionsAlarm!!.shouldUseDismissEarly
		dialog.setDefaultIndexFromDismissEarlyTime(audioOptionsAlarm!!.dismissEarlyTime)

		// Setup the listener
		dialog.onDismissEarlyOptionSelectedListener = OnDismissEarlyOptionSelectedListener { useDismissEarly, _, time ->

			// Set the new dismiss early values
			audioOptionsAlarm!!.useDismissEarly = useDismissEarly
			audioOptionsAlarm!!.dismissEarlyTime = time

			// Update the alarm
			alarmViewModel.update(audioOptionsAlarm!!)

			// Reschedule the alarm
			NacScheduler.update(this, audioOptionsAlarm!!)

		}

		// Show the dialog
		dialog.show(supportFragmentManager, NacDismissEarlyDialog.TAG)
	}

	/**
	 * Show the gradually increase volume dialog.
	 */
	private fun showGraduallyIncreaseVolumeDialog()
	{
		// Create the dialog
		val dialog = NacGraduallyIncreaseVolumeDialog()

		// Set the default value
		dialog.defaultShouldGraduallyIncreaseVolume = audioOptionsAlarm!!.shouldGraduallyIncreaseVolume
		dialog.setDefaultIndexFromWaitTime(audioOptionsAlarm!!.graduallyIncreaseVolumeWaitTime)

		// Setup the listener
		dialog.onGraduallyIncreaseVolumeListener = OnGraduallyIncreaseVolumeListener { shouldIncrease, _, waitTime ->

			// Set the new gradually increase value
			audioOptionsAlarm!!.shouldGraduallyIncreaseVolume = shouldIncrease
			audioOptionsAlarm!!.graduallyIncreaseVolumeWaitTime = waitTime

			// Update the alarm
			alarmViewModel.update(audioOptionsAlarm!!)

			// Reschedule the alarm
			NacScheduler.update(this, audioOptionsAlarm!!)

		}

		// Show the dialog
		dialog.show(supportFragmentManager, NacGraduallyIncreaseVolumeDialog.TAG)
	}

	/**
	 * Show a snackbar for the next alarm that will run.
	 */
	private fun showNextAlarmSnackbar()
	{
		// Get the next alarm
		val nextAlarm = NacCalendar.getNextAlarm(alarmCardAdapter.currentList)

		// Get the message and action for the snackbar
		val message = NacCalendar.Message.getNextAlarm(this, nextAlarm,
			sharedPreferences.nextAlarmFormat)
		val action = getString(R.string.action_alarm_dismiss)

		// Show the snackbar
		showSnackbar(message, action)
	}

	/**
	 * Show the saved NFC tag ID of the given alarm.
	 */
	private fun showNfcTagId(alarm: NacAlarm)
	{
		// Get the default locale
		val locale = Locale.getDefault()

		// Build the message
		val message: String = if (alarm.nfcTagId.isNotEmpty())
		{
			// Get the string to show a specific NFC tag
			val nfcId = getString(R.string.message_show_nfc_tag_id)

			String.format(locale, "$nfcId ${alarm.nfcTagId}")
		}
		else
		{
			// Get the string to show any NFC tag
			val anyNfc = getString(R.string.message_any_nfc_tag_id)

			String.format(locale, anyNfc)
		}

		// Toast the message
		quickToast(this, message)
	}

	/**
	 * Show the restrict volume dialog.
	 */
	private fun showRestrictVolumeDialog()
	{
		// Create the dialog
		val dialog = NacRestrictVolumeDialog()

		// Set the default value
		dialog.defaultShouldRestrictVolume = audioOptionsAlarm!!.shouldRestrictVolume

		// Setup the listener
		dialog.onRestrictVolumeListener = OnRestrictVolumeListener { shouldRestrict ->

			// Set the new restrict volume value
			audioOptionsAlarm!!.shouldRestrictVolume = shouldRestrict

			// Update the alarm
			alarmViewModel.update(audioOptionsAlarm!!)

			// Reschedule the alarm
			NacScheduler.update(this, audioOptionsAlarm!!)

		}

		// Show the dialog
		dialog.show(supportFragmentManager, NacRestrictVolumeDialog.TAG)
	}

	/**
	 * Show the scan NFC tag dialog.
	 */
	private fun showScanNfcTagDialog(alarm: NacAlarm)
	{
		// Create and set the dialog
		val scanNfcTagDialog = NacScanNfcTagDialog()

		// Setup the dialog
		scanNfcTagDialog.alarm = alarm
		scanNfcTagDialog.allNfcTags = allNfcTags
		scanNfcTagDialog.lastNfcTag = lastNfcTag
		scanNfcTagDialog.onScanNfcTagListener = object: OnScanNfcTagListener {

			/**
			 * Called when the user cancels the scan NFC tag dialog.
			 */
			override fun onCancel(alarm: NacAlarm)
			{
				// Get the card that corresponds to the alarm
				val cardHolder = recyclerView.findViewHolderForItemId(alarm.id) as NacCardHolder

				// Uncheck the NFC button when the dialog is canceled.
				cardHolder.nfcButton.isChecked = false
				cardHolder.doNfcButtonClick()
			}

			/**
			 * Called when the user cancels the scan NFC tag dialog.
			 */
			override fun onDone(alarm: NacAlarm)
			{
				// Start NFC. This is here so that if an NFC tag accidentally gets
				// scanned multiple times after this dialog is closed, it will not
				// popup some unwanted NFC Entry intent. Instead, it will keep the
				// focus on this app
				NacNfc.start(this@NacMainActivity)
			}

			/**
			 * Called when an NFC tag is scanned from the Scan NFC Tag dialog.
			 */
			override fun onScanned(alarm: NacAlarm, tagId: String)
			{
				// Create the dialog
				val saveNfcTagDialog = NacSaveNfcTagDialog()

				// Setup the dialog
				saveNfcTagDialog.allNfcTags = allNfcTags
				saveNfcTagDialog.nfcId = tagId
				saveNfcTagDialog.onSaveNfcTagListener = object: NacSaveNfcTagDialog.OnSaveNfcTagListener
				{

					/**
					 * Called when saving the NFC tag is skipped.
					 */
					override fun onCancel()
					{
						// Save the NFC tag ID that was scanned to the alarm
						saveNfcTagId(alarm, tagId)
					}

					/**
					 * Called when saving the NFC tag.
					 */
					override fun onSave(nfcTag: NacNfcTag)
					{
						// Save the NFC tag ID that was scanned to the alarm
						saveNfcTagId(alarm, tagId)

						// Save the NFC tag to the database
						nfcTagViewModel.insert(nfcTag)

						// Set the last saved/used NFC tag
						lastNfcTag = nfcTag
					}

				}

				// Show the dialog
				saveNfcTagDialog.show(supportFragmentManager, NacSaveNfcTagDialog.TAG)
			}

			/**
			 * Called when an NFC tag is selected in the Select NFC Tag dialog.
			 */
			override fun onSelected(alarm: NacAlarm, nfcTag: NacNfcTag)
			{
				// Save the NFC tag ID that was scanned to the alarm
				saveNfcTagId(alarm, nfcTag.nfcId)

				// Set the last saved/used NFC tag
				lastNfcTag = nfcTag
			}

			/**
			 * Called when the user wants to use any NFC tag.
			 */
			override fun onUseAny(alarm: NacAlarm)
			{
				// Save the default (empty) NFC tag ID, indicating that any NFC tag can
				// be used to dismiss this alarm
				saveNfcTagId(alarm, "")
			}

		}

		// Show the dialog
		scanNfcTagDialog.show(supportFragmentManager, NacScanNfcTagDialog.TAG)
	}

	/**
	 * Show a snackbar.
	 */
	private fun showSnackbar(
		message: String,
		action: String,
		listener: View.OnClickListener? = null)
	{
		// Create the snackbar
		val snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG)

		// Setup the snackbar
		snackbar.setActionTextColor(sharedPreferences.themeColor)
		snackbar.setAction(action, listener ?: View.OnClickListener { })

		// Show the snackbar
		snackbar.show()
	}

	/**
	 * Show the text-to-speech dialog.
	 */
	private fun showTextToSpeechDialog()
	{
		// Create the dialog
		val dialog = NacTextToSpeechDialog()

		// Set the default values
		dialog.defaultSayCurrentTime = audioOptionsAlarm!!.shouldSayCurrentTime
		dialog.defaultSayAlarmName = audioOptionsAlarm!!.shouldSayAlarmName
		dialog.defaultTtsFrequency = audioOptionsAlarm!!.ttsFrequency

		// Setup the listener
		dialog.onTextToSpeechOptionsSelectedListener = OnTextToSpeechOptionsSelectedListener { shouldSayCurrentTime, shouldSayAlarmName, ttsFreq ->

			// Set the new text to speech values
			audioOptionsAlarm!!.sayCurrentTime = shouldSayCurrentTime
			audioOptionsAlarm!!.sayAlarmName = shouldSayAlarmName
			audioOptionsAlarm!!.ttsFrequency = ttsFreq

			// Update the alarm
			alarmViewModel.update(audioOptionsAlarm!!)

			// Reschedule the alarm
			NacScheduler.update(this, audioOptionsAlarm!!)

		}

		// Show the dialog
		dialog.show(supportFragmentManager, NacTextToSpeechDialog.TAG)
	}

	/**
	 * Show the upcoming reminder dialog.
	 */
	private fun showUpcomingReminderDialog()
	{
		// Create the dialog
		val dialog = NacUpcomingReminderDialog()

		// Set the default values
		dialog.defaultShouldShowReminder = audioOptionsAlarm!!.shouldShowReminder
		dialog.setDefaultIndexFromTime(audioOptionsAlarm!!.timeToShowReminder)
		dialog.defaultReminderFrequencyIndex = audioOptionsAlarm!!.reminderFrequency
		dialog.defaultShouldUseTts = audioOptionsAlarm!!.shouldUseTtsForReminder
		dialog.canShowTts = audioOptionsAlarm!!.shouldUseTts

		// Setup the listener
		dialog.onUpcomingReminderOptionSelectedListener = NacUpcomingReminderDialog.OnUpcomingReminderOptionSelectedListener { shouldShowReminder, timeToShow, reminderFreq, shouldUseTts ->

			// Set the upcoming reminder options
			audioOptionsAlarm!!.showReminder = shouldShowReminder
			audioOptionsAlarm!!.timeToShowReminder = timeToShow
			audioOptionsAlarm!!.reminderFrequency = reminderFreq
			audioOptionsAlarm!!.useTtsForReminder = shouldUseTts

			// Update the alarm
			alarmViewModel.update(audioOptionsAlarm!!)

			// Reschedule the alarm
			NacScheduler.update(this, audioOptionsAlarm!!)

		}

		// Show the dialog
		dialog.show(supportFragmentManager, NacUpcomingReminderDialog.TAG)
	}

	/**
	 * Show a snackbar for the updated alarm.
	 *
	 *
	 * If this alarm is disabled, a snackbar for the next alarm will be shown.
	 */
	private fun showUpdatedAlarmSnackbar(alarm: NacAlarm)
	{
		// Check if the alarm is enabled
		if (alarm.isEnabled)
		{
			// Show the snackbar for the alarm
			showAlarmSnackbar(alarm)
		}
		else
		{
			// Show the snackbar for the next alarm that will run
			showNextAlarmSnackbar()
		}
	}

	/**
	 * Show the What's New dialog.
	 */
	private fun showWhatsNewDialog()
	{
		// Create the dialog
		val dialog = NacWhatsNewDialog()

		// Setup the listener
		dialog.onReadWhatsNewListener = OnReadWhatsNewListener {

			// Set the previous app version as the current version. This way, the What's
			// New dialog does not show again
			sharedPreferences.editPreviousAppVersion(BuildConfig.VERSION_NAME)

		}

		// Show the dialog
		dialog.show(supportFragmentManager, NacWhatsNewDialog.TAG)
	}

	companion object
	{

		/**
		 * Rate at which the next alarm message is refreshed.
		 */
		const val REFRESH_NEXT_ALARM_MESSAGE_PERIOD = 1000L

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
		 * Start the main activity.
		 */
		fun startMainActivity(context: Context)
		{
			// Create the intent
			val intent = getStartIntent(context)

			// Start the activity
			context.startActivity(intent)
		}

	}

}