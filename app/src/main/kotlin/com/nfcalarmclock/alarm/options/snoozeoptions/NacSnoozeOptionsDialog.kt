package com.nfcalarmclock.alarm.options.snoozeoptions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.RelativeLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.util.NacBundle
import com.nfcalarmclock.view.changeSimpleItemsOnZero
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupInputLayoutColor
import com.nfcalarmclock.view.setupSwitchColor

/**
 * Snooze options.
 */
class NacSnoozeOptionsDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Easy snooze switch.
	 */
	private lateinit var easySnoozeSwitch: SwitchCompat

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
	 * Called when the creating the view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.dlg_snooze_options, container, false)
	}

	/**
	 * Called when the view has been created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the bundle
		val alarm = NacBundle.getAlarm(arguments)

		// Get the ok and cancel buttons
		val okButton: MaterialButton = dialog!!.findViewById(R.id.ok_button)
		val cancelButton: MaterialButton = dialog!!.findViewById(R.id.cancel_button)

		// Get the default values
		val defaultAutoSnoozeTime = alarm?.autoSnoozeTime ?: 0
		val defaultMaxSnooze = alarm?.maxSnooze ?: -1
		val defaultSnoozeDuration = alarm?.snoozeDuration ?: 5
		val defaultShouldEasySnooze = alarm?.useEasySnooze ?: false
		selectedAutoSnoozeTime = defaultAutoSnoozeTime
		selectedMaxSnoozeTime = defaultMaxSnooze
		selectedSnoozeDurationTime = defaultSnoozeDuration

		// Setup the views
		setupAutoSnooze(defaultAutoSnoozeTime)
		setupMaxSnooze(defaultMaxSnooze)
		setupSnoozeDuration(defaultSnoozeDuration)
		setupEasySnooze(defaultShouldEasySnooze)

		// Setup the ok button
		setupPrimaryButton(okButton, listener = {

			// Update the alarm attributes
			alarm?.autoSnoozeTime = selectedAutoSnoozeTime
			alarm?.maxSnooze = selectedMaxSnoozeTime
			alarm?.snoozeDuration = selectedSnoozeDurationTime
			alarm?.useEasySnooze = easySnoozeSwitch.isChecked

			// Save the change so that it is accessible in the previous dialog
			findNavController().previousBackStackEntry?.savedStateHandle?.set("YOYOYO", alarm)

			// Dismiss the dialog
			dismiss()

		})

		// Setup the cancel button
		setupSecondaryButton(cancelButton)
	}

	/**
	 * Setup the auto snooze views.
	 */
	private fun setupAutoSnooze(default: Int)
	{
		// Get the views
		val minutesInputLayout: TextInputLayout = dialog!!.findViewById(R.id.auto_snooze_minutes_input_layout)
		val minutesAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.auto_snooze_minutes_dropdown_menu)
		val secondsInputLayout: TextInputLayout = dialog!!.findViewById(R.id.auto_snooze_seconds_input_layout)
		val secondsAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.auto_snooze_seconds_dropdown_menu)

		// Get the list of items for minutes and seconds
		val minutes = resources.getStringArray(R.array.auto_snooze_minute_summaries)
		val seconds = resources.getStringArray(R.array.general_seconds_summaries)

		// Setup the input layout
		minutesInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		secondsInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		//val minutesIndex = NacAlarm.calcAutoSnoozeIndex(default)
		//val secondsIndex = NacAlarm.calcAutoSnoozeIndex(default)
		//minutesAutoCompleteTextView.setTextFromIndex(minutesIndex)
		//secondsAutoCompleteTextView.setTextFromIndex(secondsIndex)
		minutesAutoCompleteTextView.setTextFromIndex(1)
		secondsAutoCompleteTextView.setTextFromIndex(0)

		// Set the textview listeners
		minutesAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			//selectedAutoSnoozeTime = NacAlarm.calcAutoSnoozeTime(position)
			secondsAutoCompleteTextView.changeSimpleItemsOnZero(seconds, position)

			// Check if last minute item in dropdown was selected
			if (position == minutes.lastIndex)
			{
				// Change to 0 seconds
				secondsAutoCompleteTextView.setTextFromIndex(0)
			}
		}

		secondsAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			minutesAutoCompleteTextView.changeSimpleItemsOnZero(minutes, position)
			//selectedAutoSnoozeTime = NacAlarm.calcAutoSnoozeTime(position)
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

		// Get the list of items for minutes and seconds
		val minutes = resources.getStringArray(R.array.snooze_duration_minute_summaries)
		val seconds = resources.getStringArray(R.array.general_seconds_summaries)

		// Setup the input layouts
		minutesInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		secondsInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		//val minutesIndex = NacAlarm.calcSnoozeDurationIndex(default)
		//val secondsIndex = NacAlarm.calcSnoozeDurationIndex(default)
		//minutesAutoCompleteTextView.setTextFromIndex(minutesIndex)
		//secondsAutoCompleteTextView.setTextFromIndex(secondsIndex)
		minutesAutoCompleteTextView.setTextFromIndex(1)
		secondsAutoCompleteTextView.setTextFromIndex(0)

		// Set the textview listeners
		minutesAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			//selectedSnoozeDurationTime = NacAlarm.calcSnoozeDuration(position)
			secondsAutoCompleteTextView.changeSimpleItemsOnZero(seconds, position)

			// Check if last minute item in dropdown was selected
			if (position == minutes.lastIndex)
			{
				// Change to 0 seconds
				secondsAutoCompleteTextView.setTextFromIndex(0)
			}
		}

		secondsAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			//selectedSnoozeDurationTime = NacAlarm.calcSnoozeDuration(position)
			minutesAutoCompleteTextView.changeSimpleItemsOnZero(minutes, position)
		}
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

}