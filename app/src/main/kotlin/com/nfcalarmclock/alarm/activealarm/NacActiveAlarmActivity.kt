package com.nfcalarmclock.alarm.activealarm

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.Window
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.NacAlarmViewModel
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.nfc.NacNfc
import com.nfcalarmclock.nfc.NacNfcTagViewModel
import com.nfcalarmclock.nfc.db.NacNfcTag
import com.nfcalarmclock.nfc.getNfcTagNamesForDismissing
import com.nfcalarmclock.nfc.getNfcTagsForDismissing
import com.nfcalarmclock.nfc.shouldUseNfc
import com.nfcalarmclock.nfc.toNfcIdList
import com.nfcalarmclock.nfc.toNfcIdString
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacBundle
import com.nfcalarmclock.system.addAlarm
import com.nfcalarmclock.system.bindToService
import com.nfcalarmclock.system.broadcasts.shutdown.NacShutdownBroadcastReceiver
import com.nfcalarmclock.system.enableActivityAlias
import com.nfcalarmclock.system.getAlarm
import com.nfcalarmclock.system.registerMyReceiver
import com.nfcalarmclock.system.registerMyShutdownBroadcastReceiver
import com.nfcalarmclock.system.unregisterMyReceiver
import com.nfcalarmclock.view.quickToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Activity to dismiss/snooze the alarm.
 */
@UnstableApi
@AndroidEntryPoint
class NacActiveAlarmActivity
	: AppCompatActivity()
{

	/**
	 * Alarm view model.
	 */
	private val alarmViewModel: NacAlarmViewModel by viewModels()

	/**
	 * NFC tag view model.
	 */
	private val nfcTagViewModel: NacNfcTagViewModel by viewModels()

	/**
	 * Shared preferences.
	 */
	private lateinit var sharedPreferences: NacSharedPreferences

	/**
	 * The layout handler to use (either original or swipe).
	 */
	private lateinit var layoutHandler: NacActiveAlarmLayoutHandler

	/**
	 * Alarm.
	 */
	private var alarm: NacAlarm? = null

	/**
	 * Active alarm service.
	 */
	private var service: NacActiveAlarmService? = null

	/**
	 * List of NFC tags that are needed to dismiss the alarm.
	 */
	private var nfcTagsNeededToDismissList: MutableList<NacNfcTag>? = null

	/**
	 * Initial size of the list of NFC tags that are needed to dismiss the alarm, before a scan
	 * check occurs. The scan check may change the list, hence why this is needed.
	 */
	private var initialSizeOfNfcTagsNeededToDismiss: Int = 0

	/**
	 * Flag whether the service can be dismissed with NFC once it is connected or not.
	 */
	private var canDisimssServiceWithNfcUponConnection: Boolean = false

	/**
	 * Service bound watchdog, to ensure that the service is actually bound. If it is not
	 * after a timeout then the activity is stopped.
	 */
	private val serviceBoundWatchdogHandler: Handler by lazy { Handler(mainLooper) }

	/**
	 * Keyguard manager.
	 */
	private val keyguardManager: KeyguardManager by lazy {
		getSystemService(KEYGUARD_SERVICE) as KeyguardManager
	}

	/**
	 * Shutdown broadcast receiver.
	 */
	private val shutdownBroadcastReceiver: NacShutdownBroadcastReceiver = NacShutdownBroadcastReceiver()

	/**
	 * Device unlocked broadcast receiver.
	 */
	private val deviceUnlockedBroadcastReceiver: BroadcastReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent)
		{
			// Setup NFC for the layout handler
			setupLayoutHandlerNfc()
		}
	}

	/**
	 * NFC adapter state changed broadcast receiver.
	 */
	private val nfcAdapterStateChangedBroadcastReceiver: BroadcastReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent)
		{
			// Setup NFC
			startNfc()
			setupLayoutHandlerNfc()
		}
	}

	/**
	 * Listener for an alarm action, such as snooze or dismiss.
	 */
	private val onAlarmActionListener: NacActiveAlarmLayoutHandler.OnAlarmActionListener =
		object: NacActiveAlarmLayoutHandler.OnAlarmActionListener
		{

			/**
			 * Alarm should be snoozed.
			 */
			@OptIn(UnstableApi::class)
			override fun onSnooze(alarm: NacAlarm)
			{
				// Snooze the alarm service. Whether the alarm is actually
				// snoozed is determined in the service
				service?.attemptSnooze()
			}

			/**
			 * Alarm should be dismissed.
			 */
			@OptIn(UnstableApi::class)
			override fun onDismiss(alarm: NacAlarm)
			{
				// Dismiss the alarm service
				service?.dismiss()
			}

		}

	/**
	 * Callback when back is pressed.
	 */
	private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
		override fun handleOnBackPressed()
		{
		}
	}

	/**
	 * Connection to the active alarm service.
	 */
	private val serviceConnection = object : ServiceConnection
	{
		/**
		 * Binding to the service connection is dead.
		 */
		override fun onBindingDied(name: ComponentName?)
		{
			// Super
			super.onBindingDied(name)

			// Finish the activity
			finish()
		}

		/**
		 * Service connected.
		 */
		override fun onServiceConnected(className: ComponentName, serviceBinder: IBinder)
		{
			// Set the active alarm service
			val binder = serviceBinder as NacActiveAlarmService.NacLocalBinder
			service = binder.getService()

			// Remove the service watchdog
			serviceBoundWatchdogHandler.removeCallbacksAndMessages(null)

			// Dismiss the service
			if (canDisimssServiceWithNfcUponConnection)
			{
				service!!.dismiss(usedNfc = true)
			}
		}

		/**
		 * Service disconnected.
		 */
		override fun onServiceDisconnected(className: ComponentName) {}
	}

	/**
	 * Handle when an NFC tag is scanned.
	 *
	 */
	private fun handleNfcTagScanned()
	{
		println("NFC was scanning and can be used to dismiss alarm!  Calling dismiss() on service? ${service != null}")
		// Dismiss the alarm service with NFC
		if (service != null)
		{
			service!!.dismiss(usedNfc = true)
		}
		// Set flag that service can dismiss the alarm once it is connected, however,
		// if the service does not get connected after 2 sec, there may be a problem
		// with the alarm so use a handler to dismiss it
		else
		{
			// Set the flag
			canDisimssServiceWithNfcUponConnection = true

			// Handler to dismiss the erroneous active alarm, in the event that the
			// service is not bound within 2 sec, and then finish the activity
			serviceBoundWatchdogHandler.postDelayed({
				lifecycleScope.launch {
					sharedPreferences.wasNfcJustScannedToDismiss = true
					NacDismissErroneousActiveAlarmService.startService(this@NacActiveAlarmActivity, alarm)
					delay(500)
					finish()
				}
			}, 2000)
		}
	}

	/**
	 * Activity is created.
	 */
	override fun onCreate(savedInstanceState: Bundle?)
	{
		// Super
		super.onCreate(savedInstanceState)

		// Setup the shared preferences
		sharedPreferences = NacSharedPreferences(this)

		// Set the alarm from the bundle
		setAlarm(savedInstanceState)

		// Setup
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		setupAlarmScreen()
		setupScreenOn()
		onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

		// Register device unlocked receiver
		registerMyReceiver(this, deviceUnlockedBroadcastReceiver, IntentFilter(Intent.ACTION_USER_UNLOCKED))
	}

	/**
	 * Activity is destroyed.
	 */
	override fun onDestroy()
	{
		// Super
		super.onDestroy()

		// Unregister device unlocked receiver
		unregisterMyReceiver(this, deviceUnlockedBroadcastReceiver)

		// Stop the watchdog
		serviceBoundWatchdogHandler.removeCallbacksAndMessages(null)
	}

	/**
	 * NFC tag discovered.
	 *
	 * After this, onResume() will be called, which will check if an NFC tag was scanned
	 * and, if so, will disimss the alarm.
	 */
	override fun onNewIntent(intent: Intent)
	{
		// Super
		super.onNewIntent(intent)

		// Set the intent
		setIntent(intent)
	}

	/**
	 * Activity is paused.
	 */
	public override fun onPause()
	{
		// Super
		super.onPause()

		// Stop scanning for NFC
		NacNfc.stop(this)

		// Re-enable the activity alias
		enableActivityAlias(this)

		// Unregister the broadcast receivers
		unregisterMyReceiver(this, shutdownBroadcastReceiver)
		unregisterMyReceiver(this, nfcAdapterStateChangedBroadcastReceiver)
	}

	/**
	 * Activity is resumed.
	 */
	@OptIn(UnstableApi::class)
	public override fun onResume()
	{
		// Super
		super.onResume()

		lifecycleScope.launch {

			// Start NFC and run setup
			startNfc()
			layoutHandler.setup(this@NacActiveAlarmActivity)
			setupNfcTags()

			// NFC tag was scanned. This checks if multiple NFC tags needed to be scanned in
			// sequence as well
			if (wasNfcTagScanned())
			{
				handleNfcTagScanned()
			}

			// Size of the NFC tags dismiss list changed during the scan check.
			// Save the list to the alarm and update the database
			if (nfcTagsNeededToDismissList!!.size != initialSizeOfNfcTagsNeededToDismiss)
			{
				println("Size of NFC tags list changed! ${nfcTagsNeededToDismissList!!.size} | $initialSizeOfNfcTagsNeededToDismiss")
				alarm!!.currentNfcTagsNeededToDismiss = nfcTagsNeededToDismissList!!.toNfcIdString()
				alarmViewModel.update(alarm!!)
			}

			// Setup NFC for the layout handler
			setupLayoutHandlerNfc()

		}

		// Register the broadcast receivers
		registerMyShutdownBroadcastReceiver(this, shutdownBroadcastReceiver)
		registerMyReceiver(this, nfcAdapterStateChangedBroadcastReceiver,
			IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED))
	}

	/**
	 * Save the alarm before the activity is killed.
	 */
	public override fun onSaveInstanceState(outState: Bundle)
	{
		// Super
		super.onSaveInstanceState(outState)

		// Save the alarm to the save instance state
		if (alarm != null)
		{
			outState.putParcelable(NacBundle.ALARM_PARCEL_NAME, alarm)
		}
	}


	/**
	 * Activity started.
	 */
	@OptIn(UnstableApi::class)
	override fun onStart()
	{
		// Super
		super.onStart()

		// Bind to the active alarm service
		bindToService(NacActiveAlarmService::class.java, serviceConnection)
	}

	/**
	 * Activity stopped.
	 */
	override fun onStop()
	{
		// Super
		super.onStop()

		// Remove the back press callback
		onBackPressedCallback.remove()

		// Unbind from the active alarm service
		unbindService(serviceConnection)
	}

	/**
	 * Called when the window focus has changed. This is the best indicator of
	 * whether the activity is visible to the user, or not.
	 */
	override fun onWindowFocusChanged(hasFocus: Boolean)
	{
		// Super
		super.onWindowFocusChanged(hasFocus)

		// Check if the window focus has changed
		if (hasFocus)
		{
			// Start the layout handler
			layoutHandler.start(this)
		}
		else
		{
			// Stop the layout handler
			layoutHandler.stop(this)
		}
	}

	/**
	 * Set the alarm.
	 */
	private fun setAlarm(savedInstanceState: Bundle?)
	{
		// Attempt to get the alarm from the intent
		var intentAlarm = intent.getAlarm()

		// Unable to get the alarm from the intent
		if (intentAlarm == null)
		{
			// Attempt to get the alarm from the saved instance state
			intentAlarm = savedInstanceState?.getAlarm()
		}

		// Alarm is still null, finish the activity
		if (intentAlarm == null)
		{
			finish()
		}

		// Check if the current alarm and the intent alarm are different
		// Set the alarm
		this.alarm = intentAlarm
	}

	/**
	 * Setup the alarm screen.
	 */
	private fun setupAlarmScreen()
	{
		// New alarm screen
		if (sharedPreferences.shouldUseNewAlarmScreen)
		{
			// Set the screen view
			setContentView(R.layout.act_alarm_new)

			// Set the layout handler
			layoutHandler = NacSwipeLayoutHandler(this, alarm, onAlarmActionListener)
		}
		// Original alarm screen
		else
		{
			// Set the screen view
			setContentView(R.layout.act_alarm)

			// Set the layout handler
			layoutHandler = NacOriginalLayoutHandler(this, alarm, onAlarmActionListener)
		}
	}

	/**
	 * Setup NFC for the layout handler.
	 */
	private fun setupLayoutHandlerNfc()
	{
		// NFC does not need to be used so do nothing with the layout handler
		if (alarm?.shouldUseNfc(this) != true)
		{
			return
		}

		// Get the names of the NFC tags that can dismiss the alarm
		val prefix = "(${resources.getString(R.string.message_show_nfc_tag_id)}) "
		val nfcTagNames = alarm!!.getNfcTagNamesForDismissing(nfcTagsNeededToDismissList!!, prefix)

		// Setup the NFC tag
		layoutHandler.setupNfcTag(this, nfcTagNames, keyguardManager.isDeviceLocked)
	}

	/**
	 * Setup the NFC tags member variable.
	 *
	 * It will contain the list of NFC tags that can be used to dismiss the alarm, and
	 * will be ordered based on how the user wants them ordered (Sequential/Random)
	 */
	private suspend fun setupNfcTags()
	{
		// Already been setup
		if (nfcTagsNeededToDismissList != null)
		{
			println("Already been setup : " + nfcTagsNeededToDismissList!!.map { it.nfcId })
			// Set the size of the list
			initialSizeOfNfcTagsNeededToDismiss = nfcTagsNeededToDismissList!!.size
			return
		}

		// Find the current list of NFC tags that need to be dismissed
		val currentNfcTagsNeededToDismiss = alarmViewModel.findCurrentNfcTagsNeededToDismiss(alarm!!.id)

		// The current list has not been saved yet
		if (currentNfcTagsNeededToDismiss.isEmpty())
		{
			// Get the list
			nfcTagsNeededToDismissList = alarm!!.getNfcTagsForDismissing(nfcTagViewModel)

			// Save the list to the alarm and update the database
			alarm!!.currentNfcTagsNeededToDismiss = nfcTagsNeededToDismissList!!.toNfcIdString()
			alarmViewModel.update(alarm!!)
		}
		// The list has already been saved to the database
		else
		{
			// Re-compute the list of NFC tags from the current list
			nfcTagsNeededToDismissList = currentNfcTagsNeededToDismiss.toNfcIdList()
				.map { nfcTagViewModel.findNfcTag(it) ?: NacNfcTag("", it) }
				.toMutableList()
		}

		// Set the size of the list
		println("Final computation : "+nfcTagsNeededToDismissList!!.map { it.nfcId })
		initialSizeOfNfcTagsNeededToDismiss = nfcTagsNeededToDismissList!!.size
	}

	/**
	 * Setup the screen and handle the case when the device is locked.
	 */
	@Suppress("deprecation")
	private fun setupScreenOn()
	{
		// Use updated method calls to control screen for APK >= 27
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
		{
			// Check if should NOT save battery and turn screen on
			if (!sharedPreferences.shouldSaveBatteryInAlarmScreen)
			{
				setTurnScreenOn(true)
			}

			// Show when locked
			setShowWhenLocked(true)
		}
		else
		{
			// Check if should NOT save battery and turn screen on
			if (!sharedPreferences.shouldSaveBatteryInAlarmScreen)
			{
				window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
			}

			// Add flag to show when locked
			window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
		}

		// Check if should NOT save battery and keep screen on
		if (!sharedPreferences.shouldSaveBatteryInAlarmScreen)
		{
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		}
	}

	/**
	 * Start NFC.
	 */
	private fun startNfc()
	{
		// NFC is not enabled
		if (!NacNfc.isEnabled(this))
		{
			return
		}

		// NFC exists on the device. The device is NFC capable
		if (NacNfc.exists(this))
		{
			// Create the intent
			val intent = Intent(this, NacActiveAlarmActivity::class.java)
				.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING)

			// Start NFC
			NacNfc.start(this, intent)
		}
		// Unable to use NFC on this device
		else
		{
			// Show a toast
			quickToast(this, R.string.error_message_nfc_unsupported)
		}
	}

	/**
	 * Check whether an NFC tag was scanned and if it can dismiss the alarm.
	 *
	 * If multiple NFC tags need to be used to dismiss the alarm, canDismissWithScannedNfc()
	 * will handle removing the NFC tag that was just scanned.
	 */
	private fun wasNfcTagScanned(): Boolean
	{
		// Parse the NFC ID
		val nfcId = NacNfc.parseId(intent)

		// Run the check
		return NacNfc.wasScanned(intent)
				&& NacNfc.canDismissWithScannedNfc(this@NacActiveAlarmActivity, alarm, nfcId, nfcTagsNeededToDismissList)
	}

	companion object
	{

		/**
		 * Stop the activity action.
		 */
		private const val ACTION_STOP_ACTIVITY = "com.nfcalarmclock.ACTION_STOP_ALARM_ACTIVITY"

		/**
		 * Create an intent that will be used to start the Alarm activity.
		 *
		 * @param context A context.
		 * @param alarm   An alarm.
		 *
		 * @return The Alarm activity intent.
		 */
		fun getStartIntent(context: Context, alarm: NacAlarm?): Intent
		{
			// Intent flags
			val flags = (Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)

			// Create the intent
			return Intent(context, NacActiveAlarmActivity::class.java)
				.addFlags(flags)
				.addAlarm(alarm)
		}

		/**
		 * Create an intent that will be used to start the Alarm activity.
		 *
		 * @param context A context.
		 * @param intent An intent.
		 * @param alarm An alarm.
		 *
		 * @return The Alarm activity intent.
		 */
		private fun getStartIntent(
			context: Context,
			intent: Intent,
			alarm: NacAlarm?
		): Intent
		{

			// Intent flags
			val flags = (Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)

			// Set the class of the intent
			return intent.setClass(context, NacActiveAlarmActivity::class.java)
				.addFlags(flags)
				.addAlarm(alarm)
		}

		/**
		 * Start the alarm activity with the given alarm.
		 */
		fun startAlarmActivity(context: Context, alarm: NacAlarm?)
		{
			// Create the intent
			val intent = getStartIntent(context, alarm)

			// Start the activity
			context.startActivity(intent)
		}

		/**
		 * Start the alarm activity with the given alarm.
		 */
		fun startAlarmActivity(context: Context, intent: Intent, alarm: NacAlarm?)
		{
			// Create the intent
			val updatedIntent = getStartIntent(context, intent, alarm)

			// Start the activity
			context.startActivity(updatedIntent)
		}

		/**
		 * Stop the alarm activity
		 *
		 * TODO: Delete this if no longer necessary.
		 */
		fun stopAlarmActivity(context: Context)
		{
			// Create the intent and its flags
			val intent = Intent(context, NacActiveAlarmActivity::class.java)
			val flags = (Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)

			// Add the flags to the intent
			intent.addFlags(flags)
			intent.action = ACTION_STOP_ACTIVITY

			// Start the activity
			context.startActivity(intent)
		}

	}

}
