package com.nfcalarmclock.main

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.view.Menu
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
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
import com.nfcalarmclock.alarm.activealarm.NacActiveAlarmService
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.nfc.NacNfc
import com.nfcalarmclock.ratemyapp.NacRateMyApp
import com.nfcalarmclock.settings.NacMainSettingActivity
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacBundle.BUNDLE_INTENT_ACTION
import com.nfcalarmclock.system.addAlarm
import com.nfcalarmclock.system.disableActivityAlias
import com.nfcalarmclock.system.getAlarm
import com.nfcalarmclock.system.getDeviceProtectedStorageContext
import com.nfcalarmclock.system.getSetAlarm
import com.nfcalarmclock.system.permission.NacPermissionRequestManager
import com.nfcalarmclock.system.registerMyReceiver
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.system.triggers.shutdown.NacShutdownBroadcastReceiver
import com.nfcalarmclock.system.unregisterMyReceiver
import com.nfcalarmclock.timer.NacTimerViewModel
import com.nfcalarmclock.util.NacUtility.quickToast
import com.nfcalarmclock.util.media.buildLocalMediaPath
import com.nfcalarmclock.util.media.copyMediaToDeviceEncryptedStorage
import com.nfcalarmclock.util.media.getMediaArtist
import com.nfcalarmclock.util.media.getMediaTitle
import com.nfcalarmclock.util.media.getMediaType
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

		// Check if the alarm is not null
		if (alarm != null)
		{
			// Create a bundle with the alarm and intent action
			val bundle = alarm.toBundle().apply {
				putString(BUNDLE_INTENT_ACTION, intent.action)
			}

			// Navigate to the show alarms fragment
			navController.navigate(R.id.nacShowAlarmsFragment, bundle)
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
	 * Dismiss an alarm early that was sent the intent action to do so.
	 */
	private fun dismissAlarmEarlyFromIntent()
	{
		// Get the alarm from the intent
		val alarm = intent.getAlarm()

		// Check if the alarm is in the intent
		if (alarm != null)
		{
			// Create a bundle with the alarm and intent action
			val bundle = alarm.toBundle().apply {
				putString(BUNDLE_INTENT_ACTION, intent.action)
			}

			// Navigate to the show alarms fragment
			navController.navigate(R.id.nacShowAlarmsFragment, bundle)
		}
		// Null alarm
		else
		{
			// Show error toast
			quickToast(this@NacMainActivity, R.string.error_message_unable_to_dismiss_early)
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
		//		allTimers.forEach { timerViewModel.delete(it) }
		//	}
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
	 * Called when the activity is paused.
	 */
	override fun onPause()
	{
		// Super
		super.onPause()

		// Cleanup
		unregisterMyReceiver(this, shutdownBroadcastReceiver)

		// Stop NFC
		NacNfc.stop(this)
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

		lifecycleScope.launch {

			// Get the active alarm
			val activeAlarm = alarmViewModel.getActiveAlarm()

			// Check if the active alarm is not null
			// here and then add checks to make sure it is safe to pass into these start
			// activity/service functions
			if (activeAlarm != null)
			{

				// Check if an NFC tag was scanned to open up the main activity
				if (NacNfc.wasScanned(intent))
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
					}
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

		// Alarm should be dismissed early
		if (intent.action == ACTION_DISMISS_ALARM_EARLY)
		{
			dismissAlarmEarlyFromIntent()
		}

		// Add alarm that was created from the SET_ALARM intent
		if (intent.action == AlarmClock.ACTION_SET_ALARM)
		{
			addAlarmFromSetAlarmIntent()
		}

		// Check if the main activity should be refreshed
		if (sharedPreferences.shouldRefreshMainActivity)
		{
			// Refresh the activity
			refreshMainActivity()
			return
		}

		// Setup UI
		setupInitialDialogToShow()

		// Bottom navigation
		bottomNavigation.setOnItemSelectedListener { item ->

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
							// Get an active timer, if present
							val activeTimer = timerViewModel.getActiveTimer()

							// Show active timer
							if (activeTimer != null)
							{
								println("Showing active timer")
								navController.navigate(R.id.nacActiveTimerFragment, activeTimer.toBundle())
							}
							// Show list of timers
							else
							{
								println("Showing list of timers")
								navController.navigate(R.id.nacShowTimersFragment)
							}
						}
					}
					true
				}

				// Unknown
				else -> false

			}

		}

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
	 * Setup the toolbar.
	 */
	private fun setupToolbar()
	{
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
		 * Dismiss an alarm early action.
		 */
		const val ACTION_DISMISS_ALARM_EARLY = "com.nfcalarmclock.alarm.ACTION_DISMISS_ALARM_EARLY"

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
					or Intent.FLAG_ACTIVITY_CLEAR_TASK
					or Intent.FLAG_ACTIVITY_NO_HISTORY)

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