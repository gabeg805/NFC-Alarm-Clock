package com.nfcalarmclock.alarm.activealarm

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm

/**
 * Original layout that the alarm activity had.
 *
 * @param activity Activity.
 * @param alarm Alarm.
 * @param onAlarmActionListener Listener for an alarm action.
 */
class NacOriginalLayoutHandler(
	activity: AppCompatActivity,
	alarm: NacAlarm?,
	onAlarmActionListener: OnAlarmActionListener
) : NacActiveAlarmLayoutHandler(activity, alarm, onAlarmActionListener)
{

	/**
	 * Parent view for entire alarm activity.
	 */
	private val parent: RelativeLayout = activity.findViewById(R.id.act_alarm)

	/**
	 * Snooze button.
	 */
	private val snoozeButton: Button = activity.findViewById(R.id.snooze)

	/**
	 * Dismiss button.
	 */
	private val dismissButton: Button = activity.findViewById(R.id.dismiss)

	/**
	 * The text view to show the instructions of how to interact with the
	 * alarm.
 	 */
	private val instructionsTextView: TextView = activity.findViewById(R.id.instructions)

	/**
	 * The text view to show the alarm name.
	 */
	private val nameTextView: TextView = activity.findViewById(R.id.music_title)

	/**
	 * Run any setup steps.
	 */
	override fun setup(context: Context)
	{
		setupAlarmButtons()
		setupAlarmInfo()
	}

	/**
	 * Setup the snooze and dismiss buttons.
	 */
	private fun setupAlarmButtons()
	{
		// NFC should be used
		if (shouldUseNfc)
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

		// Setup easy snooze click listeners
		if (alarm?.shouldEasySnooze == true)
		{
			// Parent click listener
			parent.setOnClickListener {
				onAlarmActionListener.onSnooze(alarm)
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
		if ((alarm != null) && sharedPreferences.shouldShowAlarmName)
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
		instructionsTextView.visibility = if (shouldUseNfc)
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
	override fun start(context: Context)
	{
		setupAlarmInstructions()
	}

	/**
	 * Stop the layout handler.
	 */
	override fun stop(context: Context)
	{
	}

}