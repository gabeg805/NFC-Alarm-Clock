package com.nfcalarmclock.activealarm

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.NacAlarmViewModel
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.nfc.NacNfc
import com.nfcalarmclock.nfc.NacNfcTag
import com.nfcalarmclock.scheduler.NacScheduler
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.statistics.NacAlarmStatisticViewModel
import com.nfcalarmclock.util.NacBundle
import com.nfcalarmclock.util.NacIntent
import com.nfcalarmclock.util.NacUtility.quickToast
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity to dismiss/snooze the alarm.
 *
 * TODO: Change to AppCompatActivity? This way AndroidEntryPoint can be used
 */
@AndroidEntryPoint
class NacActiveAlarmActivity
	: AppCompatActivity()
{

	companion object
	{

		/**
		 * Automatically dismiss the activity action.
		 */
		const val ACTION_AUTO_DISMISS_ACTIVITY = "com.nfcalarmclock.ACTION_AUTO_DISMISS_ALARM_ACTIVITY"

		/**
		 * Dismiss the activity action.
		 */
		const val ACTION_DISMISS_ACTIVITY = "com.nfcalarmclock.ACTION_DISMISS_ALARM_ACTIVITY"

		/**
		 * Stop the activity action.
		 */
		const val ACTION_STOP_ACTIVITY = "com.nfcalarmclock.ACTION_STOP_ALARM_ACTIVITY"

	}

	/**
	 * Alarm repository.
	 */
	private val alarmViewModel : NacAlarmViewModel by viewModels()

	/**
	 * Statistic view model.
	 */
	private val statisticViewModel: NacAlarmStatisticViewModel by viewModels()

	/**
	 * Shared preferences.
	 */
	private var sharedPreferences: NacSharedPreferences? = null

	/**
	 * Alarm.
	 */
	private var alarm: NacAlarm? = null

	/**
	 * Time that the service was started, in milliseconds.
	 */
	private var startTime: Long = 0

	/**
	 * Receiver to dismiss or stop the activity when the foreground service is done.
	 */
	private val activeAlarmActivityReceiver = object : BroadcastReceiver()
	{

		/**
		 * Called when the intent is received.
		 */
		override fun onReceive(context: Context, intent: Intent)
		{
			// Check the action of the intent
			when (intent.action)
			{
				// Stop the activity
				ACTION_AUTO_DISMISS_ACTIVITY ->
				{
					// Auto dismiss the alarm
					autoDismiss()

					// Finish the activity
					finish()
				}

				// Stop the activity
				ACTION_STOP_ACTIVITY -> finish()

				else -> {}
			}
		}

	}

	/**
	 * Auto dismiss the alarm.
	 */
	private fun autoDismiss()
	{
		// Dismiss the alarm
		dismiss()

		// Save the missed alarm into the statistics table
		statisticViewModel.insertMissed(alarm)
	}

	/**
	 * Check if the alarm can be snoozed and show a toast if it cannot.
	 *
	 * @return True if the alarm can be snoozed, and False otherwise.
	 */
	private fun checkCanSnooze(): Boolean
	{
		// Check if the alarm can be snoozed
		return if (alarm!!.canSnooze(sharedPreferences!!))
		{
			true
		}
		// Unable to snooze the alarm
		else
		{
			// Show a toast saying the alarm could not be snoozed
			quickToast(this, R.string.error_message_snooze)
			false
		}
	}

	/**
	 * Dismiss the alarm.
	 */
	fun dismiss(usedNfc: Boolean = false)
	{
		// TODO: Remove null alarm check here. Will this cause crashes for people?

		// Dismiss the alarm
		alarm!!.dismiss()

		// Update the alarm
		alarmViewModel.update(alarm!!)

		// Reschedule the alarm
		NacScheduler.update(this, alarm!!)

		// Save the dismissed alarm to the statistics table (used NFC)
		statisticViewModel.insertDismissed(alarm!!, usedNfc)

		// Set flag that the main activity needs to be refreshed
		sharedPreferences!!.editShouldRefreshMainActivity(true)

		// Show toast that the alarm was dismissed
		quickToast(this, R.string.message_alarm_dismiss)

		// Dismiss the service
		NacActiveAlarmService.dismissService(this, alarm)
	}

	/**
	 * Check if an alarm can be dismissed with NFC.
	 *
	 * @return True if an alarm can be dismissed with NFC, and False otherwise.
	 */
	private fun checkCanDismissWithNfc() : Boolean
	{
		// Build the NFC tag
		val tag = NacNfcTag(alarm, intent)

		// Compare the NFC tag ID between the alarm and the intent
		return tag.check(this)
	}

	/**
	 * Dismiss due to an NFC tag being scanned, and only if the NFC tag ID
	 * matches the saved alarm NFC tag ID. The exception to this is if the
	 * saved alarm NFC tag ID is empty.
	 *
	 * The finish() method is not called because if the ACTION_DISMISS_ALARM
	 * intent is sent to the foreground service, then the foreground service will
	 * finish this activity.
	 */
	private fun dismissWithNfc()
	{
		// Dismiss the alarm
		dismiss(usedNfc = true)

		// Dismiss the service with NFC
		NacActiveAlarmService.dismissServiceWithNfc(this, alarm)
	}

	/**
	 * Finish the activity.
	 */
	override fun finish()
	{
		// Set the alarm as no longer active
		alarm?.isActive = false

		// Check if the alarm is not null
		if (alarm != null)
		{
			println("Startime = $startTime")

			// Update the time that the alarm was active
			if (startTime > 0)
			{
				alarm!!.addToTimeActive(System.currentTimeMillis() - startTime)
			}
			else
			{
				println("HOW DID THIS HAPPEN? Startime = $startTime")
			}

			// Update the alarm
			alarmViewModel.update(alarm!!)

			// Reschedule the alarm
			NacScheduler.update(this, alarm!!)
		}

		// Finish
		super.finish()
	}

	/**
	 * Do not let the user back out of the activity.
	 */
	override fun onBackPressed()
	{
	}

	/**
	 * Create the activity.
	 */
	override fun onCreate(savedInstanceState: Bundle?)
	{
		// Super
		super.onCreate(savedInstanceState)

		// Setup the window
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		setContentView(R.layout.act_alarm)

		// Set the alarm from the bundle
		setAlarm(savedInstanceState)

		// Set the shared preferences
		sharedPreferences = NacSharedPreferences(this)

		// NFC tag was scanned. Check if can dismiss
		if (NacNfc.wasScanned(intent) && checkCanDismissWithNfc())
		{
			// Dismiss the alarm with NFC
			dismissWithNfc()

			// Finish the service
			// TODO: This is from the service
			finish()
		}

		// Check if alarm should be dismissed
		if (shouldDismissAlarm())
		{
			// Dismiss the alarm
			dismiss()

			// Finish the service
			// TODO: This is from the service
			finish()
		}

		// Setup
		setupScreenOn()
		setupAlarmButtons()
		setupAlarmInfo()

		// Setup the alarm
		if (alarm?.isActive == false)
		{
			// Set the active flag
			alarm!!.isActive = true

			// Update the alarm
			alarmViewModel.update(alarm!!)

			// Reschedule the alarm
			NacScheduler.update(this, alarm!!)
		}

		// Set the start time
		startTime = System.currentTimeMillis()
	}

	/**
	 * NFC tag discovered so dismiss the dialog.
	 *
	 * Note: Parent method must be called last. Causes issues with
	 * setSnoozeCount.
	 */
	override fun onNewIntent(intent: Intent)
	{
		// Super
		// TODO: Test this because this needed to be added for AppCompatActivity (which is a new change)
		super.onNewIntent(intent)

		// Set the intent
		setIntent(intent)

		// Check if the alarm can be dismissed with NFC
		if (checkCanDismissWithNfc())
		{
			// Dismiss the alarm with NFC
			dismissWithNfc()

			// Finish the service
			// TODO: This is from the service
			finish()
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

		// Unregister the receiver
		unregisterReceiver(activeAlarmActivityReceiver)
	}

	/**
	 * Called when the activity is resumed.
	 */
	public override fun onResume()
	{
		// Super
		super.onResume()

		// Setup
		setupNfc()
		setupAlarmInstructions()
		setupStopReceiver()
	}

	/**
	 * Save the alarm before the activity is killed.
	 */
	public override fun onSaveInstanceState(outState: Bundle)
	{
		super.onSaveInstanceState(outState)

		// Save the alarm to the save instance state
		if (alarm != null)
		{
			outState.putParcelable(NacBundle.ALARM_PARCEL_NAME, alarm)
		}
	}

	/**
	 * Set the alarm.
	 */
	private fun setAlarm(savedInstanceState: Bundle?)
	{
		// Attempt to get the alarm from the intent
		var intentAlarm = NacIntent.getAlarm(intent)

		// Unable to get the alarm from the intent
		if (intentAlarm == null)
		{
			// Attempt to get the alarm from the saved instance state
			intentAlarm = NacBundle.getAlarm(savedInstanceState)
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
	 * Setup the snooze and dismiss buttons.
	 */
	private fun setupAlarmButtons()
	{
		// Get the views
		val layout = findViewById<RelativeLayout>(R.id.act_alarm)
		val snoozeButton = findViewById<Button>(R.id.snooze)
		val dismissButton = findViewById<Button>(R.id.dismiss)

		// NFC should be used
		if (NacNfc.shouldUseNfc(this, alarm))
		{
			// Do not show the dismiss button
			dismissButton.visibility = View.GONE
		}
		// NFC does not need to be used
		else
		{
			// Show the dismiss button
			dismissButton.visibility = View.VISIBLE
		}

		// Setup the button color
		snoozeButton.setTextColor(sharedPreferences!!.themeColor)
		dismissButton.setTextColor(sharedPreferences!!.themeColor)

		// Setup the layout listener
		layout.setOnClickListener {

			// Check if easy snooze is allowed
			if (sharedPreferences!!.easySnooze)
			{
				// Check if the alarm can be snoozed
				if (checkCanSnooze())
				{
					// Snooze the alarm
					snooze()

					// Finish the service
					// TODO: This is from the service
					finish()
				}
			}

		}

		// Setup the snooze button listener
		snoozeButton.setOnClickListener {

			// Check if the alarm can be snoozed
			if (checkCanSnooze())
			{
				// Snooze the alarm
				snooze()

				// Finish the service
				// TODO: This is from the service
				finish()
			}

		}

		// Setup the dismiss button listener
		dismissButton.setOnClickListener {

			// Dismiss the alarm
			dismiss()

			// Finish the service
			// TODO: This is from the service
			finish()

		}
	}

	/**
	 * Setup the informational message at the bottom of the screen.
	 */
	private fun setupAlarmInfo()
	{
		// Alarm is present and the user wants to see alarm info
		if ((alarm != null) && sharedPreferences!!.showAlarmInfo)
		{
			// Get the text view to show the alarm name
			val nameTextView = findViewById<TextView>(R.id.name)

			// Show alarm info
			nameTextView.text = alarm!!.nameNormalized
			nameTextView.isSelected = true
		}
	}

	/**
	 * Setup the instruction message that appears just above the Snooze and
	 * Dismiss buttons.
	 */
	private fun setupAlarmInstructions()
	{
		// Show NFC instructions
		if (!NacNfc.isEnabled(this))
		{
			// Get the text view
			val instructionsTextView = findViewById<TextView>(R.id.instructions)

			// Do not show the instructions
			instructionsTextView.visibility = View.GONE
		}
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
			if (NacNfc.shouldUseNfc(this, alarm))
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
			val message = getString(R.string.error_message_nfc_unsupported)

			// Show a toast
			quickToast(this, message)
		}
	}

	/**
	 * Setup the screen and handle the case when the device is locked.
	 */
	@Suppress("deprecation")
	private fun setupScreenOn()
	{
		// Get whether the alarm should be shown when locked
		val showWhenLocked = (alarm != null) && !alarm!!.shouldUseNfc

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

	/**
	 * Setup the receiver for the Stop signal.
	 */
	@SuppressLint("UnspecifiedRegisterReceiverFlag")
	private fun setupStopReceiver()
	{
		// Create the intent filter
		val filter = IntentFilter(ACTION_STOP_ACTIVITY)

		// Check if app needs to set the exported flag in order to indicate
		// that this app does not expect broadcasts from other apps on the
		// device
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
		{
			// Register to listen for the STOP broadcast for the activity
			registerReceiver(activeAlarmActivityReceiver, filter, RECEIVER_NOT_EXPORTED)
		}
		else
		{
			// Register to listen for the STOP broadcast for the activity
			registerReceiver(activeAlarmActivityReceiver, filter)
		}
	}

	/**
	 * Check if the alarm should be dismissed.
	 *
	 * @return True if the alarm should be dismissed, and False otherwise.
	 */
	private fun shouldDismissAlarm(): Boolean
	{
		// Check to see if the alarm should be dismissed and NFC does not need to be used
		return intent.action == ACTION_DISMISS_ACTIVITY && !NacNfc.shouldUseNfc(this, alarm)
	}

	/**
	 * Snooze the alarm.
	 */
	fun snooze()
	{
		// Snooze the alarm and get the next time to run the alarm again
		val cal = alarm!!.snooze(sharedPreferences!!)

		// Update the time the alarm was active
		alarm!!.addToTimeActive(System.currentTimeMillis() - startTime)

		// Update the alarm
		alarmViewModel.update(alarm!!)

		// Reschedule the alarm
		NacScheduler.update(this, alarm!!, cal)

		// Set the flag that the main activity will need to be refreshed
		sharedPreferences!!.editShouldRefreshMainActivity(true)

		// Save this snooze duration to the statistics table
		statisticViewModel.insertSnoozed(alarm!!, 60L * sharedPreferences!!.snoozeDurationValue)

		// Show a toast saying the alarm was snoozed
		quickToast(this, R.string.message_alarm_snooze)

		// Snooze the alarm service
		NacActiveAlarmService.snoozeService(this, alarm)
	}

}