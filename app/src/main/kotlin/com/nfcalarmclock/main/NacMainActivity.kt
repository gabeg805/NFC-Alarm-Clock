package com.nfcalarmclock.main

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.view.Menu
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nfcalarmclock.BuildConfig
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.NacAlarmViewModel
import com.nfcalarmclock.alarm.activealarm.NacActiveAlarmActivity
import com.nfcalarmclock.nfc.NacNfc
import com.nfcalarmclock.ratemyapp.NacRateMyApp
import com.nfcalarmclock.settings.NacMainSettingActivity
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacBundle.BUNDLE_INTENT_ACTION
import com.nfcalarmclock.system.NacNfcIntent
import com.nfcalarmclock.system.disableActivityAlias
import com.nfcalarmclock.system.getDeviceProtectedStorageContext
import com.nfcalarmclock.system.getSetAlarm
import com.nfcalarmclock.system.getSetTimer
import com.nfcalarmclock.system.permission.NacPermissionRequestManager
import com.nfcalarmclock.system.registerMyReceiver
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.system.triggers.shutdown.NacShutdownBroadcastReceiver
import com.nfcalarmclock.system.unregisterMyReceiver
import com.nfcalarmclock.timer.NacTimerViewModel
import com.nfcalarmclock.system.media.buildLocalMediaPath
import com.nfcalarmclock.system.media.copyMediaToDeviceEncryptedStorage
import com.nfcalarmclock.system.media.getMediaArtist
import com.nfcalarmclock.system.media.getMediaTitle
import com.nfcalarmclock.system.media.getMediaType
import com.nfcalarmclock.view.setupRippleColor
import com.nfcalarmclock.view.setupThemeColor
import com.nfcalarmclock.whatsnew.NacWhatsNewDialog
import com.nfcalarmclock.widget.refreshAppWidgets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * The application's main activity.
 */
@AndroidEntryPoint
class NacMainActivity
	: AppCompatActivity()
{

	/**
	 * Navigation controller.
	 */
	private val navController by lazy {
		(supportFragmentManager.findFragmentById(R.id.hello_content) as NavHostFragment).navController
	}

	/**
	 * Alarm view model.
	 */
	private val alarmViewModel: NacAlarmViewModel by viewModels()

	/**
	 * Timer view model.
	 */
	private val timerViewModel: NacTimerViewModel by viewModels()

	/**
	 * Shared preferences.
	 */
	private lateinit var sharedPreferences: NacSharedPreferences

	/**
	 * Top toolbar.
	 */
	private lateinit var toolbar: MaterialToolbar

	/**
	 * Floating action button to add new alarms.
	 */
	private lateinit var floatingActionButton: FloatingActionButton

	/**
	 * Bottom navigation.
	 */
	private lateinit var bottomNavigation: BottomNavigationView

	/**
	 * Permission request manager, handles requesting permissions from the user.
	 */
	private lateinit var permissionRequestManager: NacPermissionRequestManager

	/**
	 * Shutdown broadcast receiver.
	 */
	private lateinit var shutdownBroadcastReceiver: NacShutdownBroadcastReceiver

	/**
	 * Whether the bottom navigation view item was selected by the user or not.
	 */
	private var wasBottomNavigationSelectedByUser: Boolean = true

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
	 * Add an alarm that was created from the SET_ALARM intent.
	 */
	private fun addAlarmFromSetAlarmIntent()
	{
		// Get the alarm from the intent
		val alarm = intent.getSetAlarm(this)

		// Navigate to the show alarms fragment with that alarm
		if (alarm != null)
		{
			// Create a bundle with the alarm and intent action
			val bundle = alarm.toBundle()
				.apply {
					putString(BUNDLE_INTENT_ACTION, intent.action)
				}

			// Navigate to the fragment
			navController.navigate(R.id.nacShowAlarmsFragment, bundle)
		}
	}

	/**
	 * Add a timer that was created from the SET_TIMER intent.
	 */
	private fun addTimerFromSetTimerIntent()
	{
		// Get the timer from the intent
		val timer = intent.getSetTimer(this)

		// Navigate to the show timers fragment with that timer
		if (timer != null)
		{
			// Create a bundle with the timer and intent action
			val bundle = timer.toBundle()
				.apply {
					putString(BUNDLE_INTENT_ACTION, intent.action)
				}

			// Navigate to the fragment
			navController.navigate(R.id.nacShowTimersFragment, bundle)
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
	 * Activity is created.
	 */
	@SuppressLint("NewApi")
	override fun onCreate(savedInstanceState: Bundle?)
	{
		// Setup
		super.onCreate(savedInstanceState)
		println("Main activity onCreate()")

		// Move the shared preference to device protected storage
		NacSharedPreferences.moveToDeviceProtectedStorage(this)

		// Set the content view
		setContentView(R.layout.act_main)

		// Set member variables
		sharedPreferences = NacSharedPreferences(this)
		toolbar = findViewById(R.id.tb_top_bar)
		floatingActionButton = findViewById(R.id.floating_action_button)
		bottomNavigation = findViewById(R.id.bottom_navigation)
		permissionRequestManager = NacPermissionRequestManager(this)
		shutdownBroadcastReceiver = NacShutdownBroadcastReceiver()

		// Setup events from the shared preference
		lifecycleScope.launch {
			setupEventsFromSharedPreferences()
		}

		// Setup UI
		setupEdgeToEdge()
		setupToolbar()
		setupFloatingActionButton()
		setupBottomNavigationView()
		setupNavController()

		// Disable the activity alias so that tapping an NFC tag will NOT open
		// the main activity
		disableActivityAlias(this)

		// Cleanup any old zip files that were created when sending a
		// statistics email
		cleanupEmailZipFiles()

		// Cleanup any extra media files in device encrypted storage
		lifecycleScope.launch {  cleanupExtraMediaFilesInDeviceEncryptedStorage() }
		//lifecycleScope.launch {
		//	timerViewModel.allTimers.observe(this@NacMainActivity) { allTimers ->
		//		allTimers.forEach {
		//			it.isActive = false
		//			timerViewModel.update(it)
		//		}
		//	}
		//}
	}

	/**
	 * Activity creates the options menu.
	 */
	override fun onCreateOptionsMenu(menu: Menu): Boolean
	{
		// Inflate the menu bar
		menuInflater.inflate(R.menu.menu_action_bar, menu)
		return true
	}

	/**
	 * Activity received new intent. Happens when an NFC tag is discovered.
	 *
	 * After this, onResume() will be called, which will check if an NFC tag was scanned.
	 */
	override fun onNewIntent(intent: Intent)
	{
		// Super
		super.onNewIntent(intent)
		println("Main activity onNewIntent()")

		// Set the intent
		setIntent(intent)
	}

	/**
	 * Activity is paused.
	 */
	override fun onPause()
	{
		// Super
		super.onPause()
		println("Main activity onPause()")

		// Cleanup
		unregisterMyReceiver(this, shutdownBroadcastReceiver)

		// Stop NFC
		NacNfc.stop(this)
	}

	/**
	 * Activity is resumed.
	 */
	@SuppressLint("UnsafeIntentLaunch")
	@OptIn(UnstableApi::class)
	override fun onResume()
	{
		// Super
		super.onResume()
		println("Main activity onResume()")

		// An NFC tag was scanned to open up the main activity
		if (NacNfc.wasScanned(intent))
		{
			println("Updating NFC intent")
			NacNfcIntent.update(intent)
		}
		else
		{
			lifecycleScope.launch {

				// TODO: If this logic is not here, what is the timing between seeing that
				//  NFC was scanned, posting the intent value, checking the alarm view model
				//  for an active jank, and then starting the alarm activity/service?
				// Get any active alarm or timer
				val activeAlarm = alarmViewModel.getActiveAlarm()

				// Show the active alarm activity
				if (activeAlarm != null)
				{
					println("Main activity start the active alarm activity : ${activeAlarm.id}")
					// TODO: Do I even need to do this? The alarm service would already be running?
					NacActiveAlarmActivity.startAlarmActivity(this@NacMainActivity, activeAlarm)
				}

			}
		}

		// Add alarm that was created from the SET_ALARM intent
		if (intent.action == AlarmClock.ACTION_SET_ALARM)
		{
			println("addAlarmFromSetAlarmIntent()")
			addAlarmFromSetAlarmIntent()
		}

		// Add timer that was created from the SET_TIMER intent
		if (intent.action == AlarmClock.ACTION_SET_TIMER)
		{
			println("addTimerFromSetTimerIntent()")
			addTimerFromSetTimerIntent()
		}

		// Check if the main activity should be refreshed
		if (sharedPreferences.shouldRefreshMainActivity)
		{
			// Refresh the activity
			refreshMainActivity()
			return
		}

		// Setup UI
		println("setupIntitialDialogToShow()")
		setupInitialDialogToShow()

		// Register the shutdown receiver
		val shutdownIntentFilter = IntentFilter()

		shutdownIntentFilter.addAction(Intent.ACTION_SHUTDOWN)
		shutdownIntentFilter.addAction(Intent.ACTION_REBOOT)
		registerMyReceiver(this, shutdownBroadcastReceiver, shutdownIntentFilter)

		// Start NFC
		NacNfc.start(this)

		// Refresh widgets
		refreshAppWidgets(this)
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
	 * Setup the bottom navigation view.
	 */
	private fun setupBottomNavigationView()
	{
		// Get color
		val gray = ContextCompat.getColor(this, R.color.gray_dark2)

		// Colors
		bottomNavigation.itemActiveIndicatorColor = ColorStateList.valueOf(gray)
		bottomNavigation.setupRippleColor(sharedPreferences)

		// Bottom navigation item selected listener
		bottomNavigation.setOnItemSelectedListener { item ->

			// User did not selected a bottom navigation item so do not navigate anywhere
			if (!wasBottomNavigationSelectedByUser)
			{
				println("DUMMY RETURN EARLY")
				// Reset the value back to normal
				wasBottomNavigationSelectedByUser = true
				return@setOnItemSelectedListener true
			}

			// Navigate to a destination based on what the user selected
			when (item.itemId)
			{
				// Alarm
				R.id.bottom_navigation_alarm ->
				{
					navController.navigate(R.id.nacShowAlarmsFragment)
					true
				}

				// Timer
				R.id.bottom_navigation_timer ->
				{
					lifecycleScope.launch {

						// No timers. Have user add a timer
						if (timerViewModel.count() == 0)
						{
							println("Showing add a single timer")
							navController.navigate(R.id.nacAddTimerFragment)
						}
						// 1+ timers.
						else
						{
							println("Showing list of timers")
							navController.navigate(R.id.nacShowTimersFragment)
						}
					}
					true
				}

				// Unknown
				else -> false
			}

		}
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

		// Change the visibility based on the current destination
		navController.addOnDestinationChangedListener { _, destination, _ ->
			when (destination.id)
			{

				// Show alarms
				R.id.nacShowAlarmsFragment -> floatingActionButton.show()

				// Show timers
				R.id.nacShowTimersFragment -> floatingActionButton.show()

				// Unknown
				else -> floatingActionButton.hide()

			}
		}
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
				// TODO: FIX THIS
				//recyclerView.adapter = null
				//recyclerView.layoutManager = null
				//recyclerView.adapter = alarmCardAdapter
				//recyclerView.layoutManager = NacCardLayoutManager(this)
				//alarmCardAdapter.notifyDataSetChanged()

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
	 * Setup the navigation controller.
	 */
	private fun setupNavController()
	{
		// Destination changed listener
		navController.addOnDestinationChangedListener { controller, destination, arguments ->

			// Update the flag indicating that this change was not done by a user
			wasBottomNavigationSelectedByUser = false

			// Change the bottom navigation view selected item based on the destination
			println("Changeing bottom nav id : $destination")
			when (destination.id)
			{
				// Alarm
				R.id.nacShowAlarmsFragment -> bottomNavigation.selectedItemId = R.id.bottom_navigation_alarm

				// Everything else
				else -> bottomNavigation.selectedItemId = R.id.bottom_navigation_timer
			}

		}
	}

	/**
	 * Setup the toolbar.
	 */
	private fun setupToolbar()
	{
		// Menu item click listener
		toolbar.setOnMenuItemClickListener { item ->

			when (item.itemId)
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
	}

	companion object
	{

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