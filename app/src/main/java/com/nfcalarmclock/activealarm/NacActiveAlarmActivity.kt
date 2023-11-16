package com.nfcalarmclock.activealarm

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.nfc.NacNfc
import com.nfcalarmclock.nfc.NacNfcTag
import com.nfcalarmclock.shared.NacSharedPreferences
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
			// Create the intent and its flags
			val intent = Intent(context, NacActiveAlarmActivity::class.java)
			val flags = (Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)

			// Add the flags to the intent
			intent.addFlags(flags)

			return NacIntent.addAlarm(intent, alarm)
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

	/**
	 * Shared preferences.
	 */
	private var sharedPreferences: NacSharedPreferences? = null

	/**
	 * Alarm.
	 */
	private var alarm: NacAlarm? = null

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
	 * Create the activity.
	 */
	override fun onCreate(savedInstanceState: Bundle?)
	{
		// Super
		super.onCreate(savedInstanceState)
		println("onCreate() ALARM ACTIVITY : ${intent.action}")

		// Setup the window
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		setContentView(R.layout.act_alarm)

		// Set the alarm from the bundle
		setAlarm(savedInstanceState)

		// Set the shared preferences
		sharedPreferences = NacSharedPreferences(this)

		// Setup
		setupScreenOn()
		setupAlarmButtons()
		setupAlarmInfo()

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
	override fun onNewIntent(intent: Intent)
	{
		// Super
		// TODO: Test this because this needed to be added for AppCompatActivity (which is a new change)
		super.onNewIntent(intent)
		println("ALARM ACTIVITY on new intent son")

		// Set the intent
		setIntent(intent)

		// Check if the alarm can be dismissed with NFC
		if (checkCanDismissWithNfc())
		{
			println("ALARM ACTIVITY check can dismiss with NFC")
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
		println("ALARM ACTIVITY onPause()")

		// Stop scanning for NFC
		NacNfc.stop(this)
	}

	/**
	 * Called when the activity is resumed.
	 */
	public override fun onResume()
	{
		// Super
		super.onResume()
		println("onResume() ALARM ACTIVITY : ${intent.action}")

		// NFC tag was scanned. Check if can dismiss
		if (NacNfc.wasScanned(intent) && checkCanDismissWithNfc())
		{
			println("ALARM ACTIVITY was scanned and can dismiss with NFC")
			// Dismiss the alarm service with NFC
			NacActiveAlarmService.dismissAlarmServiceWithNfc(this, alarm!!)
		}

		// Setup
		setupNfc()
		setupAlarmInstructions()
	}

	/**
	 * Save the alarm before the activity is killed.
	 */
	public override fun onSaveInstanceState(outState: Bundle)
	{
		// Super
		super.onSaveInstanceState(outState)
		println("ALARM ACTIVITY onSaveInstanceState()")

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

			// Check if easy snooze is allowed and the alarm can be snoozed
			if (sharedPreferences!!.easySnooze
				&& alarm!!.canSnooze(sharedPreferences!!))
			{
				// Snooze the alarm service
				println("SNOOZE/STOP active activitiy service")
				NacActiveAlarmService.snoozeAlarmService(this, alarm!!)
			}

		}

		// Setup the snooze button listener
		snoozeButton.setOnClickListener {

			// Check if the alarm can be snoozed
			if (alarm!!.canSnooze(sharedPreferences!!))
			{
				// Snooze the alarm service
				println("SNOOZE/STOP active activitiy service")
				NacActiveAlarmService.snoozeAlarmService(this, alarm!!)
			}

		}

		// Setup the dismiss button listener
		dismissButton.setOnClickListener {

			// Dismiss the alarm service
			println("Dismiss/STOP active activitiy service")
			NacActiveAlarmService.dismissAlarmService(this, alarm!!)

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

}