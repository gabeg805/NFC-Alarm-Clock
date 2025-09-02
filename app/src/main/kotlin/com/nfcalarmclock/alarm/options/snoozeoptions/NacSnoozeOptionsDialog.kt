package com.nfcalarmclock.alarm.options.snoozeoptions

import android.os.Bundle
import android.widget.AdapterView
import android.widget.RelativeLayout
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.util.addAlarm
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupInputLayoutColor
import com.nfcalarmclock.view.setupSwitchColor

/**
 * Snooze options.
 */
class NacSnoozeOptionsDialog
	: NacGenericAlarmOptionsDialog()
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId = R.layout.dlg_snooze_options

	/**
	 * Auto snooze switch.
	 */
	private lateinit var autoSnoozeSwitch: SwitchCompat

	/**
	 * Auto snooze minutes input layout.
	 */
	private lateinit var autoSnoozeMinutesInputLayout: TextInputLayout

	/**
	 * Auto snooze seconds input layout.
	 */
	private lateinit var autoSnoozeSecondsInputLayout: TextInputLayout

	/**
	 * Easy snooze switch.
	 */
	private lateinit var easySnoozeSwitch: SwitchCompat

	/**
	 * Volume snooze switch.
	 */
	private lateinit var volumeSnoozeSwitch: SwitchCompat

	/**
	 * Selected auto snooze time.
	 */
	private var selectedAutoSnoozeTime: Int = 0

	/**
	 * Selected max snooze time.
	 */
	private var selectedMaxSnoozeTime: Int = 0

	/**
	 * Selected snooze duration time.
	 */
	private var selectedSnoozeDurationTime: Int = 0

	/**
	 * Called when the Ok button is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// Update the alarm
		alarm?.shouldAutoSnooze = autoSnoozeSwitch.isChecked
		alarm?.autoSnoozeTime = selectedAutoSnoozeTime
		alarm?.maxSnooze = selectedMaxSnoozeTime
		alarm?.snoozeDuration = selectedSnoozeDurationTime
		alarm?.shouldEasySnooze = easySnoozeSwitch.isChecked
		alarm?.shouldVolumeSnooze = volumeSnoozeSwitch.isChecked
	}

	/**
	 * Called when the alarm should be saved.
	 */
	override fun onSaveAlarm(alarm: NacAlarm?)
	{
		onSaveAlarmListener?.onSaveAlarm(alarm!!)
	}

	/**
	 * Setup the usability of the auto snooze views.
	 */
	private fun setAutoSnoozeUsability()
	{
		// Get the state and calculate the alpha
		val state = autoSnoozeSwitch.isChecked
		val alpha = calcAlpha(state)

		// Setup the usablity of the dropdowns
		autoSnoozeMinutesInputLayout.alpha = alpha
		autoSnoozeSecondsInputLayout.alpha = alpha
		autoSnoozeMinutesInputLayout.isEnabled = state
		autoSnoozeSecondsInputLayout.isEnabled = state
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		// Get the default values
		val defaultShouldAutoSnooze = alarm?.shouldAutoSnooze ?: false
		val defaultAutoSnoozeTime = alarm?.autoSnoozeTime?.takeIf { it > 0 } ?: 300
		val defaultMaxSnooze = alarm?.maxSnooze ?: -1
		val defaultSnoozeDuration = alarm?.snoozeDuration?.takeIf { it > 0 } ?: 300
		val defaultShouldEasySnooze = alarm?.shouldEasySnooze ?: false
		val defaultShouldVolumeSnooze = alarm?.shouldVolumeSnooze ?: false
		selectedAutoSnoozeTime = defaultAutoSnoozeTime
		selectedMaxSnoozeTime = defaultMaxSnooze
		selectedSnoozeDurationTime = defaultSnoozeDuration

		// Setup the views
		setupAutoSnooze(defaultShouldAutoSnooze, defaultAutoSnoozeTime)
		setAutoSnoozeUsability()
		setupMaxSnooze(defaultMaxSnooze)
		setupSnoozeDuration(defaultSnoozeDuration)
		setupEasySnooze(defaultShouldEasySnooze)
		setupVolumeSnooze(defaultShouldVolumeSnooze)
	}

	/**
	 * Setup the auto snooze views.
	 */
	private fun setupAutoSnooze(defaultState: Boolean, defaultTime: Int)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.auto_snooze_container)
		val minutesAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.auto_snooze_minutes_dropdown_menu)
		val secondsAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.auto_snooze_seconds_dropdown_menu)
		autoSnoozeSwitch = dialog!!.findViewById(R.id.auto_snooze_switch)
		autoSnoozeMinutesInputLayout = dialog!!.findViewById(R.id.auto_snooze_minutes_input_layout)
		autoSnoozeSecondsInputLayout = dialog!!.findViewById(R.id.auto_snooze_seconds_input_layout)

		// Setup the switch
		autoSnoozeSwitch.isChecked = defaultState
		autoSnoozeSwitch.setupSwitchColor(sharedPreferences)

		// Setup the minutes and seconds
		setupMinutesAndSecondsOption(
			autoSnoozeMinutesInputLayout,
			autoSnoozeSecondsInputLayout,
			minutesAutoCompleteTextView,
			secondsAutoCompleteTextView,
			startIndices = NacAlarm.calcAutoSnoozeIndex(defaultTime),
			onTimeChanged = { minIndex, secIndex ->

				// Update the selected time
				val newMin = NacAlarm.calcAutoSnoozeFromMinutesIndex(minIndex)
				val newSec = NacAlarm.calcAutoSnoozeFromSecondsIndex(secIndex)
				selectedAutoSnoozeTime = newMin + newSec

			})

		// Set the layout click listener to handle changing the switch state
		relativeLayout.setOnClickListener {

			// Toggle the switch and set the usability of the views the switch controls
			autoSnoozeSwitch.toggle()
			setAutoSnoozeUsability()

		}
	}

	/**
	 * Setup the max snooze views.
	 */
	private fun setupMaxSnooze(default: Int)
	{
		// Get the views
		val inputLayout: TextInputLayout = dialog!!.findViewById(R.id.max_snooze_input_layout)
		val autoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.max_snooze_dropdown_menu)

		// Setup the input layouts
		inputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		val index = NacAlarm.calcMaxSnoozeIndex(default)
		autoCompleteTextView.setTextFromIndex(index)

		// Set the textview listeners
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedMaxSnoozeTime = NacAlarm.calcMaxSnooze(position)
		}
	}

	/**
	 * Setup the snooze duration views.
	 */
	private fun setupSnoozeDuration(default: Int)
	{
		// Get the views
		val minutesInputLayout: TextInputLayout = dialog!!.findViewById(R.id.snooze_duration_minutes_input_layout)
		val minutesAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.snooze_duration_minutes_dropdown_menu)
		val secondsInputLayout: TextInputLayout = dialog!!.findViewById(R.id.snooze_duration_seconds_input_layout)
		val secondsAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.snooze_duration_seconds_dropdown_menu)

		// Setup the minutes and seconds
		setupMinutesAndSecondsOption(
			minutesInputLayout,
			secondsInputLayout,
			minutesAutoCompleteTextView,
			secondsAutoCompleteTextView,
			startIndices = NacAlarm.calcSnoozeDurationIndex(default),
			onTimeChanged = { minIndex, secIndex ->

				// Update the selected time
				val newMin = NacAlarm.calcSnoozeDurationFromMinutesIndex(minIndex)
				val newSec = NacAlarm.calcSnoozeDurationFromSecondsIndex(secIndex)
				selectedSnoozeDurationTime = newMin + newSec

			})
	}

	/**
	 * Setup easy snooze.
	 */
	private fun setupEasySnooze(default: Boolean)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.easy_snooze_container)
		easySnoozeSwitch = dialog!!.findViewById(R.id.easy_snooze_switch)

		// Setup the checkbox
		easySnoozeSwitch.isChecked = default
		easySnoozeSwitch.setupSwitchColor(sharedPreferences)

		// Easy snooze listener
		relativeLayout.setOnClickListener {
			easySnoozeSwitch.isChecked = !easySnoozeSwitch.isChecked
		}
	}

	/**
	 * Setup volume snooze.
	 */
	private fun setupVolumeSnooze(default: Boolean)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.volume_snooze_container)
		volumeSnoozeSwitch = dialog!!.findViewById(R.id.volume_snooze_switch)

		// Setup the checkbox
		volumeSnoozeSwitch.isChecked = default
		volumeSnoozeSwitch.setupSwitchColor(sharedPreferences)

		// Volume snooze listener
		relativeLayout.setOnClickListener {
			volumeSnoozeSwitch.isChecked = !volumeSnoozeSwitch.isChecked
		}
	}

	companion object
	{

		/**
		 * Dialog name.
		 */
		const val TAG = "NacSnoozeOptionsDialog"

		/**
		 * Create a dialog that can be shown easily.
		 */
		fun create(
			alarm: NacAlarm,
			onSaveAlarmListener: (NacAlarm) -> Unit = {}
		): NacSnoozeOptionsDialog
		{
			// Create the dialog
			val dialog = NacSnoozeOptionsDialog()

			// Add the alarm to the dialog
			dialog.arguments = Bundle().addAlarm(alarm)

			// Set the listener to save the alarm
			dialog.onSaveAlarmListener = OnSaveAlarmListener { a ->
				onSaveAlarmListener(a)
			}

			return dialog
		}

	}

}