package com.nfcalarmclock.alarm.options.upcomingreminder

import android.widget.AdapterView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupInputLayoutColor
import com.nfcalarmclock.view.setupSwitchColor

/**
 * Upcoming reminder dialog.
 */
class NacUpcomingReminderDialog
	: NacGenericAlarmOptionsDialog()
{

	/**
	 * Layout resourec ID.
	 */
	override val layoutId = R.layout.dlg_upcoming_reminder

	/**
	 * Show reminder switch.
	 */
	private lateinit var showReminderSwitch: SwitchCompat

	/**
	 * How early title textview.
	 */
	private lateinit var howEarlyTitle: TextView

	/**
	 * How early description textview.
	 */
	private lateinit var howEarlyDescription: TextView

	/**
	 * Input layout for how early to show the reminder.
	 */
	private lateinit var howEarlyInputLayout: TextInputLayout

	/**
	 * How frequent title textview.
	 */
	private lateinit var howFrequentTitle: TextView

	/**
	 * How frequent description textview.
	 */
	private lateinit var howFrequentDescription: TextView

	/**
	 * How frequent input layout.
	 */
	private lateinit var howFrequentInputLayout: TextInputLayout

	/**
	 * Use text-to-speech parent container.
	 */
	private lateinit var useTtsRelativeLayout: RelativeLayout

	/**
	 * Use text-to-speech switch.
	 */
	private lateinit var useTtsSwitch: SwitchCompat

	/**
	 * Selected how early time.
	 */
	private var selectedHowEarlyTime: Int = 0

	/**
	 * Selected how frequent time.
	 */
	private var selectedHowFreqTime: Int = 0

	/**
	 * Called when the Ok button is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// Update the alarm
		alarm?.shouldShowReminder = showReminderSwitch.isChecked
		alarm?.timeToShowReminder = selectedHowEarlyTime
		alarm?.reminderFrequency = selectedHowFreqTime
		alarm?.shouldUseTtsForReminder = useTtsSwitch.isChecked
	}

	/**
	 * Setup the usability of the how early views.
	 */
	private fun setHowEarlyUsability()
	{
		// Get the state and alpha
		val state = showReminderSwitch.isChecked
		val alpha = calcAlpha(state)

		// Set the usability
		howEarlyTitle.alpha = alpha
		howEarlyDescription.alpha = alpha
		howEarlyInputLayout.alpha = alpha
		howEarlyInputLayout.isEnabled = state
	}

	/**
	 * Setup the usability of the how frequent views.
	 */
	private fun setHowFrequentUsability()
	{
		// Get the state and alpha
		val state = showReminderSwitch.isChecked
		val alpha = calcAlpha(state)

		// Set the usability
		howFrequentTitle.alpha = alpha
		howFrequentDescription.alpha = alpha
		howFrequentInputLayout.alpha = alpha
		howFrequentInputLayout.isEnabled = state
	}

	/**
	 * Setup the usability of the use text-to-speech views.
	 */
	private fun setUseTtsUsability()
	{
		// Get the state and alpha
		val state = showReminderSwitch.isChecked
		val alpha = calcAlpha(state)

		// Set the usability
		useTtsRelativeLayout.alpha = alpha
		useTtsSwitch.isEnabled = state
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		// Get the alarm, or build a new one, to get default values
		val a = alarm ?: NacAlarm.build()
		selectedHowEarlyTime = a.timeToShowReminder
		selectedHowFreqTime = a.reminderFrequency

		// Setup the views
		setupShowReminder(a.shouldShowReminder)
		setupHowEarly(a.timeToShowReminder)
		setupHowFrequent(a.reminderFrequency)
		setupUseTts(a.shouldUseTts && a.shouldUseTtsForReminder)
		setHowEarlyUsability()
		setHowFrequentUsability()
		setUseTtsUsability()
	}

	/**
	 * Setup the how early views.
	 */
	private fun setupHowEarly(default: Int)
	{
		// Get the views
		val autoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.reminder_how_early_dropdown_menu)
		howEarlyTitle = dialog!!.findViewById(R.id.reminder_how_early_title)
		howEarlyDescription = dialog!!.findViewById(R.id.reminder_how_early_description)
		howEarlyInputLayout = dialog!!.findViewById(R.id.reminder_how_early_input_layout)

		// Setup the input layout
		howEarlyInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		val index = NacAlarm.calcUpcomingReminderTimeToShowIndex(default)
		autoCompleteTextView.setTextFromIndex(index)

		// Set the textview listeners
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedHowEarlyTime = NacAlarm.calcUpcomingReminderTimeToShow(position)
		}
	}

	/**
	 * Setup the how frequent views.
	 */
	private fun setupHowFrequent(default: Int)
	{
		// Get the views
		val autoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.reminder_how_frequent_dropdown_menu)
		howFrequentTitle = dialog!!.findViewById(R.id.reminder_how_frequent_title)
		howFrequentDescription = dialog!!.findViewById(R.id.reminder_how_frequent_description)
		howFrequentInputLayout = dialog!!.findViewById(R.id.reminder_how_frequent_input_layout)

		// Setup the input layout
		howFrequentInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		autoCompleteTextView.setTextFromIndex(default)

		// Set the textview listeners
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedHowFreqTime = position
		}
	}

	/**
	 * Setup the show reminder views.
	 */
	private fun setupShowReminder(default: Boolean)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.reminder_show_container)
		showReminderSwitch = dialog!!.findViewById(R.id.reminder_show_switch)

		// Setup the switch
		showReminderSwitch.isChecked = default
		showReminderSwitch.setupSwitchColor(sharedPreferences)

		// Set the listener
		relativeLayout.setOnClickListener {

			// Toggle the switch
			showReminderSwitch.toggle()

			// Setup the views
			setHowEarlyUsability()
			setHowFrequentUsability()
			setUseTtsUsability()

		}
	}

	/**
	 * Setup the use text-to-speech views.
	 */
	private fun setupUseTts(default: Boolean)
	{
		// Get the views
		useTtsRelativeLayout = dialog!!.findViewById(R.id.reminder_use_tts_container)
		useTtsSwitch = dialog!!.findViewById(R.id.reminder_use_tts_switch)

		// Setup the switch
		useTtsSwitch.isChecked = default
		useTtsSwitch.setupSwitchColor(sharedPreferences)

		// Set the listener
		useTtsRelativeLayout.setOnClickListener {
			useTtsSwitch.toggle()
		}
	}

}