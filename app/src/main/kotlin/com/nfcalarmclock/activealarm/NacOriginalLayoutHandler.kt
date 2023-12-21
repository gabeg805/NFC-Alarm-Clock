package com.nfcalarmclock.activealarm

import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.nfc.NacNfc

/**
 * Original layout that the alarm activity had.
 */
class NacOriginalLayoutHandler(

	/**
	 * Activity.
	 */
	activity: AppCompatActivity,

	/**
	 * Alarm.
	 */
	alarm: NacAlarm?,

	/**
	 * Listener for an alarm action.
	 */
	onAlarmActionListener: OnAlarmActionListener

	// Constructor
) : NacActiveAlarmLayoutHandler(activity, alarm, onAlarmActionListener)
{

	/**
	 * Entire alarm activity layout.
	 */
	private val layout = activity.findViewById<RelativeLayout>(R.id.act_alarm)

	/**
	 * Snooze button.
	 */
	private val snoozeButton = activity.findViewById<Button>(R.id.snooze)

	/**
	 * Dismiss button.
	 */
	private val dismissButton = activity.findViewById<Button>(R.id.dismiss)

	/**
	 * The text view to show the instructions of how to interact with the
	 * alarm.
 	 */
	private val instructionsTextView = activity.findViewById<TextView>(R.id.instructions)

	/**
	 * The text view to show the alarm name.
	 */
	private val nameTextView = activity.findViewById<TextView>(R.id.music_title)

	/**
	 * Constructor.
	 */
	init
	{
		setupAlarmButtons(activity)
		setupAlarmInfo()
	}

	/**
	 * Setup the snooze and dismiss buttons.
	 */
	private fun setupAlarmButtons(activity: AppCompatActivity)
	{
		// NFC should be used
		if (NacNfc.shouldUseNfc(activity, alarm))
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
		snoozeButton.setTextColor(sharedPreferences.themeColor)
		dismissButton.setTextColor(sharedPreferences.themeColor)

		// Setup the layout listener
		layout.setOnClickListener {

			// Check if easy snooze is allowed
			if (sharedPreferences.easySnooze)
			{
				onAlarmActionListener.onSnooze(alarm!!)
			}

		}

		// Setup the snooze button listener
		snoozeButton.setOnClickListener {
			onAlarmActionListener.onSnooze(alarm!!)
		}

		// Setup the dismiss button listener
		dismissButton.setOnClickListener {
			onAlarmActionListener.onDismiss(alarm!!)
		}
	}

	/**
	 * Setup the informational message at the bottom of the screen.
	 */
	private fun setupAlarmInfo()
	{
		// Check if the alarm is not null and the user wants to see alarm name
		if ((alarm != null) && sharedPreferences.showAlarmName)
		{
			// Show the alarm name
			nameTextView.text = alarm.nameNormalized
			nameTextView.isSelected = true
		}
	}

	/**
	 * Setup the instruction message that appears just above the Snooze and
	 * Dismiss buttons.
	 */
	private fun setupAlarmInstructions()
	{
		// Set the visibility of the instructions based on if the alarm uses NFC
		instructionsTextView.visibility = if (alarm?.shouldUseNfc == true)
		{
			// Show NFC instructions
			View.VISIBLE
		}
		else
		{
			// Do not show the instructions
			View.GONE
		}
	}

	/**
	 * Start the handler.
	 */
	override fun start()
	{
		setupAlarmInstructions()
	}

	/**
	 * Stop the layout handler.
	 */
	override fun stop()
	{
	}

}