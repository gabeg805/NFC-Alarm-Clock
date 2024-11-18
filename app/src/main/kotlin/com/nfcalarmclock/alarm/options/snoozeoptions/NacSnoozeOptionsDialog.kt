package com.nfcalarmclock.alarm.options.snoozeoptions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.util.NacBundle
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupCheckBoxColor
import com.nfcalarmclock.view.setupInputLayoutColor

/**
 * Snooze options.
 */
class NacSnoozeOptionsDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Checkbox for whether the alarm can be snoozed easily or not.
	 */
	private lateinit var easySnoozeCheckBox: MaterialCheckBox

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
		val okButton = dialog!!.findViewById(R.id.ok_button) as MaterialButton
		val cancelButton = dialog!!.findViewById(R.id.cancel_button) as MaterialButton

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
			alarm?.useEasySnooze = easySnoozeCheckBox.isChecked

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
		val inputLayout = dialog!!.findViewById(R.id.auto_snooze_input_layout) as TextInputLayout
		val autoCompleteTextView = dialog!!.findViewById(R.id.auto_snooze_dropdown_menu) as MaterialAutoCompleteTextView

		// Setup the input layout
		inputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		val index = NacAlarm.calcAutoSnoozeIndex(default)
		autoCompleteTextView.setTextFromIndex(index)

		// Set the textview listeners
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedAutoSnoozeTime = NacAlarm.calcAutoSnoozeTime(position)
		}
	}

	/**
	 * Setup the max snooze views.
	 */
	private fun setupMaxSnooze(default: Int)
	{
		// Get the views
		val inputLayout = dialog!!.findViewById(R.id.max_snooze_input_layout) as TextInputLayout
		val autoCompleteTextView = dialog!!.findViewById(R.id.max_snooze_dropdown_menu) as MaterialAutoCompleteTextView

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
		val inputLayout = dialog!!.findViewById(R.id.snooze_duration_input_layout) as TextInputLayout
		val autoCompleteTextView = dialog!!.findViewById(R.id.snooze_duration_dropdown_menu) as MaterialAutoCompleteTextView

		// Setup the input layouts
		inputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		val index = NacAlarm.calcSnoozeDurationIndex(default)
		autoCompleteTextView.setTextFromIndex(index)

		// Set the textview listeners
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedSnoozeDurationTime = NacAlarm.calcSnoozeDuration(position)
		}
	}

	/**
	 * Set the description for what type of snoozing should take place.
	 */
	private fun setEasySnoozeDescription(textView: TextView)
	{
		// Determine the text ID to use based on whether easy snooze will be
		// used or not
		val textId = if (easySnoozeCheckBox.isChecked)
		{
			R.string.easy_snooze_true
		}
		else
		{
			R.string.easy_snooze_false
		}

		// Set the text
		textView.setText(textId)
	}

	/**
	 * Setup easy snooze.
	 */
	private fun setupEasySnooze(default: Boolean)
	{
		// Get the views
		val relativeLayout = dialog!!.findViewById(R.id.should_easy_snooze) as RelativeLayout
		val description = dialog!!.findViewById(R.id.should_easy_snooze_summary) as TextView
		easySnoozeCheckBox = dialog!!.findViewById(R.id.should_easy_snooze_checkbox) as MaterialCheckBox

		// Set the default checkbox value
		easySnoozeCheckBox.isChecked = default

		// Setup the checkbox
		easySnoozeCheckBox.setupCheckBoxColor(sharedPreferences)

		// Easy snooze description
		setEasySnoozeDescription(description)

		// Easy snooze listener
		relativeLayout.setOnClickListener {

			// Toggle the checkbox
			easySnoozeCheckBox.isChecked = !easySnoozeCheckBox.isChecked

			// Setup the description
			setEasySnoozeDescription(description)

		}
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacSnoozeOptionsDialog"

	}

}