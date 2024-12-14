package com.nfcalarmclock.alarm.activealarm

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.nfc.NacNfc
import com.nfcalarmclock.alarm.options.nfc.NacNfcTagViewModel
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.triggers.shutdown.NacShutdownBroadcastReceiver
import com.nfcalarmclock.util.NacBundle
import com.nfcalarmclock.util.NacUtility.quickToast
import com.nfcalarmclock.util.addAlarm
import com.nfcalarmclock.util.enableActivityAlias
import com.nfcalarmclock.util.getAlarm
import com.nfcalarmclock.util.registerMyReceiver
import com.nfcalarmclock.util.unregisterMyReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


/**
 * Activity to dismiss/snooze the alarm.
 */
@AndroidEntryPoint
class NacActiveAlarmActivity
	: AppCompatActivity()
{

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
	 * Whether the alarm should use NFC or not.
	 */
	private val shouldUseNfc: Boolean
		get() = (alarm != null) && alarm!!.shouldUseNfc && sharedPreferences.shouldShowNfcButton

	/**
	 * NFC tag view model.
	 */
	private val nfcTagViewModel: NacNfcTagViewModel by viewModels()

	/**
	 * Shutdown broadcast receiver.
	 */
	private val shutdownBroadcastReceiver: NacShutdownBroadcastReceiver = NacShutdownBroadcastReceiver()

	/**
	 * Listener for an alarm action, such as snooze or dismiss.
	 */
	private val onAlarmActionListener: NacActiveAlarmLayoutHandler.OnAlarmActionListener =
		object: NacActiveAlarmLayoutHandler.OnAlarmActionListener
		{

			/**
			 * Called when the alarm should be snoozed.
			 */
			@OptIn(UnstableApi::class)
			override fun onSnooze(alarm: NacAlarm)
			{
				// Snooze the alarm service. Whether the alarm is actually
				// snoozed is determined in the service
				NacActiveAlarmService.snoozeAlarmService(
					this@NacActiveAlarmActivity, alarm)
			}

			/**
			 * Called when the alarm should be dismissed.
			 */
			@OptIn(UnstableApi::class)
			override fun onDismiss(alarm: NacAlarm)
			{
				// Dismiss the alarm service
				NacActiveAlarmService.dismissAlarmService(
					this@NacActiveAlarmActivity, alarm)
			}

		}

	/**
	 * Check if an alarm can be dismissed with NFC.
	 *
	 * @return True if an alarm can be dismissed with NFC, and False otherwise.
	 */
	private fun checkCanDismissWithNfc() : Boolean
	{
		// Get the NFC ID of the alarm and the intent
		val alarmNfcId = alarm!!.nfcTagId
		val intentNfcId = NacNfc.parseId(intent)

		// Compare the two NFC IDs. As long as nothing is null,
		// if the NFC button is not shown in the alarm card, or
		// if the alarm NFC ID is empty, this is good, or
		// if the two NFC IDs are equal, this is also good
		return if ((alarm != null)
			&& (intentNfcId != null)
			&& (!sharedPreferences.shouldShowNfcButton || alarmNfcId.isEmpty() || (alarmNfcId == intentNfcId)))
		{
			true
		}
		// Something went wrong when comparing the NFC IDs
		else
		{
			// Show toast
			quickToast(this, R.string.error_message_nfc_mismatch)
			false
		}
	}

	/**
	 * Create the activity.
	 */
	override fun onCreate(savedInstanceState: Bundle?)
	{
		// Super
		super.onCreate(savedInstanceState)

		// Setup the shared preferences
		sharedPreferences = NacSharedPreferences(this)

		// Set the alarm from the bundle
		setAlarm(savedInstanceState)

		// Setup the window
		requestWindowFeature(Window.FEATURE_NO_TITLE)

		// Check if the new alarm screen should be used
		if (sharedPreferences.shouldUseNewAlarmScreen)
		{
			// Set the screen view
			setContentView(R.layout.act_alarm_new)

			// Set the layout handler
			layoutHandler = NacSwipeLayoutHandler(this, alarm, onAlarmActionListener)
		}
		// Use the original alarm screen
		else
		{
			// Set the screen view
			setContentView(R.layout.act_alarm)

			// Set the layout handler
			layoutHandler = NacOriginalLayoutHandler(this, alarm, onAlarmActionListener)
		}

		// Setup
		setupScreenOn()

		// Handle when the back button is pressed
		onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true)
		{
			override fun handleOnBackPressed()
			{
			}
		})
	}

	/**
	 * NFC tag discovered so dismiss the dialog.
	 *
	 * Note: Parent method must be called last. Causes issues with
	 * setSnoozeCount.
	 */
	@OptIn(UnstableApi::class)
	override fun onNewIntent(intent: Intent)
	{
		// Super
		super.onNewIntent(intent)

		// Set the intent
		setIntent(intent)

		// Check if the alarm can be dismissed with NFC
		if (checkCanDismissWithNfc())
		{
			println("onNewIntent() Dismiss with NFC")
			// Dismiss the alarm service with NFC
			NacActiveAlarmService.dismissAlarmServiceWithNfc(this, alarm!!)
		}
	}

	/**
	 * Called when the activity is paused.
	 */
	public override fun onPause()
	{
		// Super
		super.onPause()

		// Stop scanning for NFC
		NacNfc.stop(this)

		// Re-enable the activity alias
		enableActivityAlias(this)

		// Unregister the shutdown receiver
		unregisterMyReceiver(this, shutdownBroadcastReceiver)
	}

	/**
	 * Called when the activity is resumed.
	 */
	@OptIn(UnstableApi::class)
	public override fun onResume()
	{
		// Super
		super.onResume()

		// NFC tag was scanned. Check if can dismiss
		if (NacNfc.wasScanned(intent) && checkCanDismissWithNfc())
		{
			println("onResume() Dismiss with NFC")
			// Dismiss the alarm service with NFC
			NacActiveAlarmService.dismissAlarmServiceWithNfc(this, alarm!!)
		}

		// Register the shutdown receiver
		val shutdownIntentFilter = IntentFilter()

		shutdownIntentFilter.addAction(Intent.ACTION_SHUTDOWN)
		shutdownIntentFilter.addAction(Intent.ACTION_REBOOT)
		registerMyReceiver(this, shutdownBroadcastReceiver, shutdownIntentFilter)

		// Setup
		setupNfc()
		layoutHandler.setup(this)

		// Check if NFC should be setup
		if (shouldUseNfc)
		{
			// Setup NFC tag
			lifecycleScope.launch {

				// Get the NFC tag that corresponds to this ID from the table
				val nfcTag = nfcTagViewModel.findNfcTag(alarm!!.nfcTagId)

				// Setup the NFC tag
				layoutHandler.setupNfcTag(this@NacActiveAlarmActivity, nfcTag)

			}
		}
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
	 * Setup NFC.
	 */
	private fun setupNfc()
	{
		// NFC is not enabled
		if (!NacNfc.isEnabled(this))
		{
			// NFC should be used
			if (NacNfc.exists(this) && shouldUseNfc)
			{
				// Prompt the user
				NacNfc.prompt(this)
			}
			// NFC does not need to be used
			else
			{
				return
			}
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
	 * Setup the screen and handle the case when the device is locked.
	 */
	@Suppress("deprecation")
	private fun setupScreenOn()
	{
		// Get whether the alarm should be shown when locked
		//val showWhenLocked = (alarm != null) && !alarm!!.shouldUseNfc
		val showWhenLocked = !shouldUseNfc

		// Use updated method calls to control screen for APK >= 27
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
		{
			setTurnScreenOn(true)
			setShowWhenLocked(showWhenLocked)
			//setShowWhenLocked(false);
		}
		else
		{
			// Turn on the screen
			window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

			// Add flag to show when locked
			if (showWhenLocked)
			{
				window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
			}
			// Do not show when locked
			else
			{
				window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
			}
		}

		// Keep screen on
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

		// Allow lock screen when screen is turned on
		//window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
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
