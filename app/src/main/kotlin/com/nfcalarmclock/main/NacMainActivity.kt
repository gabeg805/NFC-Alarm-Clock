package com.nfcalarmclock.main

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.AlarmClock
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.insets.ColorProtection
import androidx.core.view.insets.ProtectionLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nfcalarmclock.BuildConfig
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.NacAlarmViewModel
import com.nfcalarmclock.alarm.activealarm.NacActiveAlarmActivity
import com.nfcalarmclock.alarm.activealarm.NacActiveAlarmService
import com.nfcalarmclock.alarm.activealarm.NacDismissErroneousActiveAlarmService
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.log.NacLog
import com.nfcalarmclock.nfc.NacNfc
import com.nfcalarmclock.nfc.NacNfcReaderMode
import com.nfcalarmclock.nfc.SCANNED_NFC_TAG_ID_BUNDLE_NAME
import com.nfcalarmclock.onboarding.NacOnboardingFragment
import com.nfcalarmclock.ratemyapp.NacRateMyApp
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacBundle.BUNDLE_INTENT_ACTION
import com.nfcalarmclock.system.bindToService
import com.nfcalarmclock.system.broadcasts.airplanemode.NacAirplaneModeBroadcastReceiver
import com.nfcalarmclock.system.broadcasts.shutdown.NacShutdownBroadcastReceiver
import com.nfcalarmclock.system.disableActivityAlias
import com.nfcalarmclock.system.getDeviceProtectedStorageContext
import com.nfcalarmclock.system.getSetAlarm
import com.nfcalarmclock.system.getSetTimer
import com.nfcalarmclock.system.media.buildLocalMediaPath
import com.nfcalarmclock.system.media.copyMediaToDeviceEncryptedStorage
import com.nfcalarmclock.system.media.getMediaArtist
import com.nfcalarmclock.system.media.getMediaTitle
import com.nfcalarmclock.system.media.getMediaType
import com.nfcalarmclock.system.permission.NacPermissionRequestManager
import com.nfcalarmclock.system.registerMyReceiver
import com.nfcalarmclock.system.registerMyShutdownBroadcastReceiver
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.system.unregisterMyReceiver
import com.nfcalarmclock.timer.NacShowTimersFragment
import com.nfcalarmclock.timer.NacTimerViewModel
import com.nfcalarmclock.timer.active.NacActiveTimerFragment
import com.nfcalarmclock.timer.active.NacActiveTimerService
import com.nfcalarmclock.view.setupRippleColor
import com.nfcalarmclock.view.setupThemeColor
import com.nfcalarmclock.whatsnew.NacWhatsNewDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

/**
 * The application's main activity.
 */
@AndroidEntryPoint
class NacMainActivity
	: AppCompatActivity()
{

	/**
	 * Nav host fragment.
	 */
	private val navHostFragment by lazy {
		supportFragmentManager.findFragmentById(R.id.hello_content) as NavHostFragment
	}

	/**
	 * Navigation controller.
	 */
	private val navController by lazy { navHostFragment.navController }

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
	 * Airplane mode receiver.
	 */
	private lateinit var airplaneModeReceiver: NacAirplaneModeBroadcastReceiver

	/**
	 * Whether the bottom navigation view item was selected by the user or not.
	 */
	private var wasBottomNavigationSelectedByUser: Boolean = true

	/**
	 * Active alarm to use when starting alarm activity.
	 */
	private var activeAlarmForActivity: NacAlarm? = null

	/**
	 * Active alarm to use when starting alarm activity.
	 */
	private val activeAlarmServiceHandler: Handler by lazy { Handler(mainLooper) }

	/**
	 * Handler to set the "wasNfcJustScannedToDismiss" shared preference, after a delay.
	 */
	private val wasNfcJustScannedToDismissHandler: Handler by lazy { Handler(mainLooper) }

	/**
	 * Connection to the active alarm service.
	 */
	private val serviceConnection = object : ServiceConnection
	{
		@OptIn(UnstableApi::class)
		override fun onServiceConnected(className: ComponentName, serviceBinder: IBinder)
		{
			// Do nothing if the alarm for the activity is not set
			if (activeAlarmForActivity == null)
			{
				return
			}

			// Remove the handler callback
			activeAlarmServiceHandler.removeCallbacksAndMessages(null)

			// Start the alarm activity
			NacActiveAlarmActivity.startAlarmActivity(this@NacMainActivity, activeAlarmForActivity)
			activeAlarmForActivity = null
		}

		override fun onServiceDisconnected(className: ComponentName) {}
	}

	/**
	 * What's new dialog. If it is set, then the dialog is currently being shown.
	 */
	private var whatsNewDialog: NacWhatsNewDialog? = null

	/**
	 * Check if the What's New dialog should be shown.
	 */
	private val shouldShowWhatsNewDialog: Boolean
		get()
		{
			// Get the previous version
			val previousVersion = sharedPreferences.previousAppVersion

			// Only show the dialog if the current version and the previously saved
			// version do not match, and there is not already a dialog being shown.
			//
			// This does not apply to a newly installed app, in which case the
			// previously saved version is empty. Do not show the What's New
			// dialog to a person that just installed the app because they
			// probably do not care
			return previousVersion.isNotEmpty()
				&& (BuildConfig.VERSION_NAME != previousVersion)
				&& (whatsNewDialog == null)
				&& !sharedPreferences.shouldShowOnboardingScreen
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
	 * Attempt to handle an NFC scanning event.
	 */
	@OptIn(UnstableApi::class)
	private fun attemptToHandleNfcScanEvent()
	{
		lifecycleScope.launch {

			// Get any active alarm or timer
			val activeAlarm = alarmViewModel.getActiveAlarm()

			// An NFC tag was scanned to open up the main activity
			if (NacNfc.wasScanned(intent))
			{
				NacLog.i("NFC was scanned")

				// Alarm
				if (activeAlarm != null)
				{
					handleNfcTagScannedForAlarm(activeAlarm)
				}
				// Timer
				else
				{
					val nfcId = NacNfc.parseId(intent) ?: ""
					handleNfcTagScannedForTimer(nfcId)
				}
			}
			else
			{
				// TODO: Collapse icon is not shown when extrabelosummaryview is shown
				// Bind to the active alarm service to ensure it is running, AND THEN start
				// the alarm activity. This is to avoid the scenario when an alarm was marked as
				// active, even after a user had disabled it. I am not sure how this was
				// happening, as I could not replicate it, but this new process is to ensure
				// that it does not happen anymore to people
				if (activeAlarm != null)
				{
					NacLog.i("Binding to active alarm service")

					// Alarm that will be passed into the activity
					activeAlarmForActivity = activeAlarm

					// Bind to the active alarm service
					bindToService(NacActiveAlarmService::class.java, serviceConnection)

					// Handler to dismiss the erroneous active alarm in the event that the
					// service is not bound within 5 sec
					activeAlarmServiceHandler.postDelayed({
						NacLog.w("Active alarm service was not bound after 5 sec. Starting the dismiss erroneous active alarm service")
						NacDismissErroneousActiveAlarmService.startService(this@NacMainActivity, activeAlarm)
					}, 5000)

					// Show the active alarm activity
					//NacActiveAlarmActivity.startAlarmActivity(this@NacMainActivity, activeAlarm)
				}
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
	 * Cleanup the media file that was being used extra media files in device encrypted storage that are not used by any
	 * alarm.
	 *
	 * This will typically happen if an alarm changes the media that they are using for
	 * an alarm.
	 */
	fun <T: NacAlarm> cleanupMediaFileAfterDelete(localMediaPath: String, allItems: List<T>)
	{
		// Local media path is empty or matches the default shared preference path
		if (localMediaPath.isEmpty() || (localMediaPath == sharedPreferences.localMediaPath))
		{
			// Do nothing
			return
		}

		// Ensure that no items are using the local media path
		if (allItems.all { it.localMediaPath != localMediaPath })
		{
			// Delete the local media
			val file = File(localMediaPath)
			file.delete()
		}
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
	 * Handle an NFC tag being scanned for an alarm.
	 */
	@SuppressLint("UnsafeIntentLaunch")
	@OptIn(UnstableApi::class)
	private fun handleNfcTagScannedForAlarm(activeAlarm: NacAlarm)
	{
		// Remove the grant URI permissions in the untrusted intent
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			intent.removeFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			intent.removeFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
		}

		// Check that the nested intent does not grant URI permissions
		if (((intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION) == 0) &&
			((intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION) == 0))
		{
			// Start the alarm activity with the intent containing the NFC tag
			// information in order to dismiss this alarm
			NacActiveAlarmActivity.startAlarmActivity(this@NacMainActivity, intent, activeAlarm)

			// Clear the intent so the NFC intent does not get triggered again
			intent = Intent(this@NacMainActivity, NacMainActivity::class.java)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		}
	}

	/**
	 * Handle an NFC tag being scanned for a timer.
	 */
	@OptIn(UnstableApi::class)
	private suspend fun handleNfcTagScannedForTimer(nfcId: String)
	{
		// NFC ID is invalid
		if (nfcId.isEmpty())
		{
			return
		}

		// Active timer(s)
		if (timerViewModel.countActive() > 0)
		{
			// Get the current fragment and destination ID
			val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment
			val destinationId = navController.currentDestination?.id

			NacLog.i("Active timer in progress. Trying to go/pass NFC ID to: ${navController.currentDestination}")

			// Determine what to do based on the current destination
			when (destinationId)
			{

				// Show timers
				R.id.nacShowTimersFragment ->
				{
					val fragment = currentFragment as NacShowTimersFragment
					fragment.attemptDismissWithScannedNfc(nfcId)
				}

				// Active timer
				R.id.nacActiveTimerFragment ->
				{
					val fragment = currentFragment as NacActiveTimerFragment
					fragment.attemptDismissWithScannedNfc(nfcId)
				}

				// Something else
				else ->
				{
					NacLog.i("Navigate to show timers with NFC ID in tow")

					// Add the NFC tag that was scanned to a bundle
					val bundle = Bundle().apply {
						putString(SCANNED_NFC_TAG_ID_BUNDLE_NAME, nfcId)
					}

					// Navigate to show timers passing in the NFC tag ID
					navController.navigate(R.id.action_global_nacShowTimersFragment, bundle)
				}

			}
		}
		// Start a timer from an NFC tag
		else
		{
			timerViewModel.getAllTimers()
				// Find the first timer that contains the NFC ID and is able to start an NFC tag
				// from a scan
				.firstOrNull {
					it.nfcTagIdList.contains(nfcId) && it.shouldScanningNfcTagStartTimer
				}
				// Start the active timer service and fragment
				?.let { t ->
					NacLog.i("Starting a timer from an NFC tag")

					NacActiveTimerService.startTimerService(this@NacMainActivity, t)
					navController.navigate(R.id.nacActiveTimerFragment, t.toBundle())
				}
		}
	}

	/**
	 * Activity is created.
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

		NacLog.i("Creating main activity")

		// Set member variables
		sharedPreferences = NacSharedPreferences(this)
		toolbar = findViewById(R.id.tb_top_bar)
		floatingActionButton = findViewById(R.id.floating_action_button)
		bottomNavigation = findViewById(R.id.bottom_navigation)
		permissionRequestManager = NacPermissionRequestManager(this)
		shutdownBroadcastReceiver = NacShutdownBroadcastReceiver()
		airplaneModeReceiver = NacAirplaneModeBroadcastReceiver()

		// Set flag that cards need to be measured
		sharedPreferences.cardIsMeasured = false

		// Disable the activity alias so that tapping an NFC tag will NOT open
		// the main activity
		disableActivityAlias(this)

		// Setup UI
		setupEdgeToEdge()
		setupToolbar()
		setupBottomNavigationView()
		setupNavController()
		setupFloatingActionButton()
		setupNfcReaderModeObserver()

		// Setup events from the shared preference
		lifecycleScope.launch {
			setupEventsFromSharedPreferences()
		}

		// Register broadcast receivers
		registerMyShutdownBroadcastReceiver(this, shutdownBroadcastReceiver)
		registerMyReceiver(this, airplaneModeReceiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))

		// Cleanup any extra media files in device encrypted storage and old zip files
		// that were created when sending a statistics email
		lifecycleScope.launch {
			cleanupExtraMediaFilesInDeviceEncryptedStorage()
			cleanupEmailZipFiles()
		}
	}

	/**
	 * Activity is destroyed.
	 */
	override fun onDestroy()
	{
		// Super
		super.onDestroy()

		NacLog.i("Destroying main activity")

		// Cleanup
		unregisterMyReceiver(this, shutdownBroadcastReceiver)
		unregisterMyReceiver(this, airplaneModeReceiver)
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

		NacLog.i("New intent received in main activity")

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

		// Stop NFC
		NacNfc.disableReaderMode(this)
	}

	/**
	 * Activity is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Check if the main activity should be refreshed
		if (sharedPreferences.shouldRefreshMainActivity)
		{
			// Refresh the activity
			refreshMainActivity()
			return
		}

		// Do not proceed with setup when showing onboarding
		if (sharedPreferences.shouldShowOnboardingScreen)
		{
			return
		}

		// Setup NFC
		setupNfcReaderMode()
		attemptToHandleNfcScanEvent()
		setupWasNfcJustScannedToDismiss()

		// Determine which intent action to do
		when (intent.action)
		{
			// Add alarm that was created from the SET_ALARM intent
			AlarmClock.ACTION_SET_ALARM ->
			{
				addAlarmFromSetAlarmIntent()
			}

			// Add timer that was created from the SET_TIMER intent
			AlarmClock.ACTION_SET_TIMER ->
			{
				addTimerFromSetTimerIntent()
			}

			// Show alarms
			AlarmClock.ACTION_SHOW_ALARMS ->
			{
				navController.navigate(R.id.action_global_nacShowAlarmsFragment)
			}

			// Show timers
			AlarmClock.ACTION_SHOW_TIMERS ->
			{
				navController.navigate(R.id.action_global_nacShowTimersFragment)
			}

			// Setup intial dialog to show
			else ->
			{
				setupInitialDialogToShow()
			}
		}
	}

	/**
	 * Activity stopped.
	 */
	override fun onStop()
	{
		// Super
		super.onStop()

		// Unbind service
		try
		{
			unbindService(serviceConnection)
		}
		catch (_: IllegalArgumentException) {}
	}

	/**
	 * Refresh the main activity.
	 */
	private fun refreshMainActivity()
	{
		NacLog.i("Refreshing main activity")

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

		// Item selected listener
		bottomNavigation.setOnItemSelectedListener { item ->

			// User did not selected a bottom navigation item so do not navigate anywhere
			if (!wasBottomNavigationSelectedByUser)
			{
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
					navController.navigate(R.id.action_global_nacShowAlarmsFragment)
					true
				}

				// Timer
				R.id.bottom_navigation_timer ->
				{

					navController.navigate(R.id.action_global_nacShowTimersFragment)
					true
				}

				// Settings
				R.id.bottom_navigation_settings ->
				{
					// Remove the media fragment from the back stack
					if ((navController.currentDestination?.id == R.id.nacAlarmMainMediaPickerFragment)
						|| (navController.currentDestination?.id == R.id.nacAlarmMainMediaPickerFragment2)
						|| (navController.currentDestination?.id == R.id.nacTimerMainMediaPickerFragment))
					{
						navController.popBackStack()
					}

					// Go to settings
					navController.navigate(R.id.nacMainSettingFragment)
					true
				}

				// Unknown
				else -> false
			}

		}

		// Reselected listener. Do nothing so that the backstack does not get more
		// destinations added to it
		bottomNavigation.setOnItemReselectedListener {}
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

		// Get the views
		val protectionLayout: ProtectionLayout = findViewById(R.id.protection_layout)
		val constraintLayout: ConstraintLayout = findViewById(R.id.constraint_layout)

		// Set the color of the protection view so that the status and navigation bar appear
		// black
		protectionLayout.setProtections(
				listOf(
					ColorProtection(WindowInsetsCompat.Side.TOP, Color.BLACK),
					ColorProtection(WindowInsetsCompat.Side.BOTTOM, Color.BLACK),
				)
			)

		// Set the margin of the constraint view, which is inside the protection view, so
		// that the window insets are handled
		ViewCompat.setOnApplyWindowInsetsListener(constraintLayout) { v, windowInsets ->

			// Get the insets
			val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

			// Apply the insets as a margin to the view
			v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
				topMargin = insets.top
				bottomMargin = insets.bottom
				leftMargin = insets.left
				rightMargin = insets.right
			}

			// Return CONSUMED so that window insets do not propagate down to descendant views
			WindowInsetsCompat.CONSUMED
		}
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
	}

	/**
	 * Setup an initial dialog, if any, that need to be shown.
	 */
	@SuppressLint("NotifyDataSetChanged")
	private fun setupInitialDialogToShow()
	{
		// Get the delay counter for showing the what's new dialog
		val delayCounter = sharedPreferences.delayShowingWhatsNewDialogCounter

		// Missing permissions that should be requested
		if (permissionRequestManager.isMissingPermissions)
		{
			// Check if the what's new dialog should be shown
			if (shouldShowWhatsNewDialog)
			{
				// Set the delay counter for showing the what's new dialog.
				// Do not want to show the what's new dialog immediately after
				// all the permissions are requested
				sharedPreferences.delayShowingWhatsNewDialogCounter = 1
			}

			NacLog.i("Request app permissions")

			// Request permissions
			permissionRequestManager.requestPermissions(this, onDone = {

				// Refresh the destination in case this is the first time the user is
				// using the app and the alarm cards do not show because of the
				// request manager showing up first
				navController.navigate(R.id.action_global_nacShowAlarmsFragment)

			})
		}
		// Attempt to show the What's new dialog
		else if (shouldShowWhatsNewDialog && delayCounter == 0)
		{
			NacLog.i("Show what's new dialog")

			// Show the What's New dialog
			whatsNewDialog = NacWhatsNewDialog.show(supportFragmentManager,
				listener = {

					// Set the previous app version as the current version. This way, the What's
					// New dialog does not show again
					sharedPreferences.previousAppVersion = BuildConfig.VERSION_NAME

					// Clear the whats new dialog
					whatsNewDialog = null

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
			NacLog.i("Request to rate my app")

			// Request for the user to rate my app
			NacRateMyApp.request(this, sharedPreferences)
		}
	}

	/**
	 * Setup the navigation controller.
	 */
	private fun setupNavController()
	{
		// Navigate to onboarding
		if (sharedPreferences.shouldShowOnboardingScreen)
		{
			NacLog.i("Navigating to user onboarding")
			navController.navigate(R.id.action_global_nacOnboardingFragment)
		}

		// Destination changed listener
		navController.addOnDestinationChangedListener { _, destination, bundle ->

			NacLog.i("Navigating to destination : $destination")

			// Previous destination was onboarding
			if (bundle?.getBoolean(NacOnboardingFragment.ONBOARDING_NAV_KEY) == true)
			{
				// Re-run the permission request manager in case permissions were not allowed
				permissionRequestManager.refresh(this)
				setupInitialDialogToShow()
			}

			// Setup the flag when NFC was just scanned to dismiss
			setupWasNfcJustScannedToDismiss()

			// Toolbar visibility
			toolbar.visibility = if ((destination.id == R.id.nacGeneralSettingFragment)
				|| (destination.id == R.id.nacAppearanceSettingFragment)
				|| (destination.id == R.id.nacNfcTagSettingFragment)
				|| (destination.id == R.id.nacStatisticsSettingFragment)
				|| (destination.id == R.id.nacAboutSettingFragment)
				|| (destination.id == R.id.nacHelpSettingFragment)
				|| (destination.id == R.id.nacAlarmMainMediaPickerFragment)
				|| (destination.id == R.id.nacAlarmMainMediaPickerFragment2)
				|| (destination.id == R.id.nacTimerMainMediaPickerFragment)
				|| (destination.id == R.id.nacAddTimerFragment)
				|| (destination.id == R.id.nacEditTimerFragment)
				|| (destination.id == R.id.nacActiveTimerFragment)
				)
			{
				View.VISIBLE
			}
			else
			{
				View.GONE
			}

			// Floating action button visibility
			when (destination.id)
			{
				// Show alarms
				R.id.nacShowAlarmsFragment -> floatingActionButton.show()

				// Show timers
				R.id.nacShowTimersFragment -> floatingActionButton.show()

				// Everything else
				else -> floatingActionButton.hide()
			}

			// Bottom navigation visibility
			bottomNavigation.visibility = if ((destination.id == R.id.nacOnboardingFragment)
				|| (destination.id == R.id.nacAlarmMainMediaPickerFragment)
				|| (destination.id == R.id.nacAlarmMainMediaPickerFragment2)
				|| (destination.id == R.id.nacTimerMainMediaPickerFragment))
			{
				View.GONE
			}
			else
			{
				View.VISIBLE
			}

			// Get the bottom navigation ID to go to
			val bottomNavId = when (destination.id)
			{
				// Alarm
				R.id.nacShowAlarmsFragment           -> R.id.bottom_navigation_alarm
				R.id.nacAlarmMainMediaPickerFragment -> R.id.bottom_navigation_alarm

				// Timer
				R.id.nacShowTimersFragment           -> R.id.bottom_navigation_timer
				R.id.nacAddTimerFragment             -> R.id.bottom_navigation_timer
				R.id.nacEditTimerFragment            -> R.id.bottom_navigation_timer
				R.id.nacActiveTimerFragment          -> R.id.bottom_navigation_timer
				R.id.nacTimerMainMediaPickerFragment -> R.id.bottom_navigation_timer

				// Settings
				R.id.nacMainSettingFragment           -> R.id.bottom_navigation_settings
				R.id.nacGeneralSettingFragment        -> R.id.bottom_navigation_settings
				R.id.nacAppearanceSettingFragment     -> R.id.bottom_navigation_settings
				R.id.nacNfcTagSettingFragment         -> R.id.bottom_navigation_settings
				R.id.nacStatisticsSettingFragment     -> R.id.bottom_navigation_settings
				R.id.nacAboutSettingFragment          -> R.id.bottom_navigation_settings
				R.id.nacAlarmMainMediaPickerFragment2 -> R.id.bottom_navigation_settings
				R.id.nacHelpSettingFragment           -> R.id.bottom_navigation_settings

				// Everything else
				else -> R.id.bottom_navigation_alarm
			}

			// Navigate to that ID. Update the flag indicating that this change was not
			// done by a user
			if (bottomNavigation.selectedItemId != bottomNavId)
			{
				wasBottomNavigationSelectedByUser = false
				bottomNavigation.selectedItemId = bottomNavId
			}
		}
	}

	/**
	 * Setup NFC reader mode.
	 */
	private fun setupNfcReaderMode()
	{
		// Start NFC
		if (NacNfc.exists(this))
		{
			NacNfc.enableReaderMode(this) { tag ->

				// NFC was just scanned to dismiss an alarm or timer so do nothing
				if (sharedPreferences.wasNfcJustScannedToDismiss)
				{
					setupWasNfcJustScannedToDismiss()
					return@enableReaderMode
				}

				// Parse the NFC ID
				val nfcId = NacNfc.parseId(tag) ?: return@enableReaderMode

				NacLog.i("NFC reader detected scan")

				// Handle an NFC tag being scanned
				lifecycleScope.launch {
					handleNfcTagScannedForTimer(nfcId)
				}

			}
		}
	}

	/**
	 * Setup NFC reader mode observer.
	 */
	private fun setupNfcReaderModeObserver()
	{
		// Observer the reader mode status
		NacNfcReaderMode.liveData.observe(this) { status ->

			// Reader mode was disabled so re-enable it here
			if (!status)
			{
				setupNfcReaderMode()
			}

		}
	}

	/**
	 * Setup the toolbar.
	 */
	private fun setupToolbar()
	{
		// Create the appbar configuration. The two root destinations are show alarms and
		// show timers
		val appBarConfiguration = AppBarConfiguration(
			setOf(R.id.nacShowAlarmsFragment, R.id.nacShowTimersFragment, R.id.nacMainSettingFragment)
		)

		// Setup navigation with the toolbar
		toolbar.setupWithNavController(navController, appBarConfiguration)
		toolbar.setNavigationOnClickListener {
			lifecycleScope.launch {

				// From active timer, go directly to show timers, skipping over add/edit
				// since do not need to go back to those
				@Suppress("CascadeIf")
				if (navController.currentDestination?.id == R.id.nacActiveTimerFragment)
				{
					NacLog.i("Navigating to show timers")
					navController.navigate(R.id.action_global_nacShowTimersFragment)
				}
				// From add timer and no timers have been saved yet, go back to show alarms
				else if ((navController.currentDestination?.id == R.id.nacAddTimerFragment)
					&& (timerViewModel.count() == 0))
				{
					NacLog.i("Navigating to show alarms")
					navController.navigate(R.id.action_global_nacShowAlarmsFragment)
				}
				// Normal navigate up
				else
				{
					NacLog.i("Navigating up from : ${navController.currentDestination}")
					navController.navigateUp(appBarConfiguration)
				}

			}
		}
	}

	/**
	 * Setup the flag for if NFC was just scanned to dismiss an alarm/timer.
	 *
	 * If the flag was set, disable it after a delay. Otherwise, do nothing.
	 */
	private fun setupWasNfcJustScannedToDismiss()
	{
		// The flag is not set so do nothing
		if (!sharedPreferences.wasNfcJustScannedToDismiss)
		{
			return
		}

		// Cleanup the handler
		wasNfcJustScannedToDismissHandler.removeCallbacksAndMessages(null)

		// Disable the flag after a delay
		NacLog.i("Preparing to disable NFC just scanned flag")

		wasNfcJustScannedToDismissHandler.postDelayed({
			NacLog.i("Disabling NFC just scanned flag")
			sharedPreferences.wasNfcJustScannedToDismiss = false
		}, 1000)

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