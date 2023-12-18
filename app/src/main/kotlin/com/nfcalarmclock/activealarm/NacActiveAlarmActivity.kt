package com.nfcalarmclock.activealarm

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.nfc.NacNfc
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacBundle
import com.nfcalarmclock.util.NacIntent
import com.nfcalarmclock.util.NacUtility.quickToast
import com.nfcalarmclock.util.enableActivityAlias
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.sqrt


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
	 * Alarm.
	 */
	private var alarm: NacAlarm? = null

	/**
	 * Original layout handler.
	 */
	private lateinit var originalLayoutHandler: NacOriginalLayoutHandler

	/**
	 * Swipe to dismiss layout handler.
	 */
	private lateinit var swipeLayoutHandler: NacSwipeLayoutHandler

	/**
	 * Listener for an alarm action, such as snooze or dismiss.
	 */
	val onAlarmActionListener: NacActiveAlarmLayoutHandler.OnAlarmActionListener =
		object: NacActiveAlarmLayoutHandler.OnAlarmActionListener
		{

			/**
			 * Called when the alarm should be snoozed.
			 */
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
		// if the alarm NFC ID is empty, this is good, or
		// if the two NFC IDs are equal, this is also good
		return if ((alarm != null)
			&& (intentNfcId != null)
			&& (alarmNfcId.isEmpty() || (alarmNfcId == intentNfcId)))
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

		// Setup the window
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		setContentView(R.layout.act_alarm_new)
		//setContentView(R.layout.act_alarm)

		// Set the alarm from the bundle
		setAlarm(savedInstanceState)

		// Set the member variables
		sharedPreferences = NacSharedPreferences(this)
		//originalLayoutHandler = NacOriginalLayoutHandler(this, alarm, onAlarmActionListener)
		swipeLayoutHandler = NacSwipeLayoutHandler(this, alarm, onAlarmActionListener)

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
	override fun onNewIntent(intent: Intent)
	{
		// Super
		super.onNewIntent(intent)

		// Set the intent
		setIntent(intent)

		// Check if the alarm can be dismissed with NFC
		if (checkCanDismissWithNfc())
		{
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

		// Reenable the activity alias
		enableActivityAlias(this)
	}

	/**
	 * Called when the activity is resumed.
	 */
	public override fun onResume()
	{
		// Super
		super.onResume()

		// NFC tag was scanned. Check if can dismiss
		if (NacNfc.wasScanned(intent) && checkCanDismissWithNfc())
		{
			// Dismiss the alarm service with NFC
			NacActiveAlarmService.dismissAlarmServiceWithNfc(this, alarm!!)
		}

		// Setup
		setupNfc()
		//val x = findViewById<TextView>(R.id.title)
		//x.isSelected = true
	}

	/**
	 * Called when the window focus has changed. This is the best indicator of
	 * whether the activity is visible to the user, or not.
	 */
	override fun onWindowFocusChanged(hasFocus: Boolean)
	{
		// Super
		super.onWindowFocusChanged(hasFocus)

		println("WINDOW FOCUS CHANGE : $hasFocus")

		if (hasFocus)
		{
			// Start the swipe layout handler
			//originalLayoutHandler.start()
			swipeLayoutHandler.start()
		}
		else
		{
			// Stop the swipe layout handler
			swipeLayoutHandler.stop()
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

}

/**
 * Calculate the radius of where a user is touching with respect to a view.
 */
fun View.calculateTouchRadius(motionEvent: MotionEvent): Int
{
	// Determine the X and Y from the reference of the origin, as
	// opposed to the top-left corner
	val xOrigin = this.layoutParams.width / 2
	val yOrigin = this.layoutParams.height / 2
	val newX = motionEvent.x - xOrigin
	val newY = motionEvent.y - yOrigin

	// Calculate the radius of where the user's finger is wrt the view
	return sqrt(newX*newX + newY*newY).toInt()
}

/**
 * Set the size of a view.
 */
fun View.setViewSize(size: Int)
{
	// Set the width and height
	this.layoutParams.width = size
	this.layoutParams.height = size

	// Force a layout refresh
	this.requestLayout()
}

/**
 * Set the size of a view from an animator.
 */
fun View.setViewSizeFromAnimator(valueAnimator: ValueAnimator)
{
	// Get the value from the animator
	val value = valueAnimator.animatedValue as Int

	// Set the size of the view
	this.setViewSize(value)
}

/**
 * Extension function to convert the values of a property holder to an Int.
 */
fun PropertyValuesHolder.getIntValues(): List<Int>
{
	return this.toString()
		.replace(":", "")
		.trim()
		.replace("\\s+".toRegex(), " ")
		.split("\\s".toRegex())
		.map { it.toInt() }
}
