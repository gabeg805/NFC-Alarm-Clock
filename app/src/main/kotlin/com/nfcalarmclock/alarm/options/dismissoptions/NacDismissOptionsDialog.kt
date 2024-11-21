package com.nfcalarmclock.alarm.options.dismissoptions

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
 * Dismiss options.
 */
class NacDismissOptionsDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Question above the frequency picker.
	 */
	private lateinit var dismissEarlyQuestion: TextView

	/**
	 * Input layout to select the dismiss early wait time.
	 */
	private lateinit var dismissEarlyInputLayout: TextInputLayout

	/**
	 * Checkbox for whether the alarm can be dismissed early or not.
	 */
	private lateinit var dismissEarlyCheckBox: MaterialCheckBox

	/**
	 * Selected auto dismiss time.
	 */
	private var selectedAutoDismissTime: Int = 0

	/**
	 * Selected dismiss early time.
	 */
	private var selectedDismissEarlyTime: Int = 0

	/**
	 * Selected should delete the alarm after it is dismissed option.
	 */
	private var selectedShouldDeleteAlarmAfterDismissed: Boolean = false

	/**
	 * The alpha that views should have based on the should use text-to-speech
	 * flag.
	 */
	private val alpha: Float
		get() = if (dismissEarlyCheckBox.isChecked) 1.0f else 0.2f

	/**
	 * Called when the creating the view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.dlg_dismiss_options, container, false)
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
		val defaultAutoDismissTime = alarm?.autoDismissTime ?: 15
		val defaultShouldDismissEarly = alarm?.useDismissEarly ?: false
		val defaultDismissEarlyTime = alarm?.dismissEarlyTime ?: 30
		val defaultShouldDeleteAlarmAfterDismissed = alarm?.shouldDeleteAlarmAfterDismissed ?: false
		selectedAutoDismissTime = defaultAutoDismissTime
		selectedDismissEarlyTime = defaultDismissEarlyTime

		// Setup the views
		setupAutoDismiss(defaultAutoDismissTime)
		setupShouldDismissEarly(defaultShouldDismissEarly)
		setupHowEarlyToDismiss(defaultDismissEarlyTime)
		setHowEarlyToDismissUsable()
		setupShouldDeleteAlarmAfterDismissed(defaultShouldDeleteAlarmAfterDismissed)

		// Setup the ok button
		setupPrimaryButton(okButton, listener = {

			// Update the alarm attributes
			alarm?.autoDismissTime = selectedAutoDismissTime
			alarm?.useDismissEarly = dismissEarlyCheckBox.isChecked
			alarm?.dismissEarlyTime = selectedDismissEarlyTime
			alarm?.shouldDeleteAlarmAfterDismissed = selectedShouldDeleteAlarmAfterDismissed

			// Save the change so that it is accessible in the previous dialog
			findNavController().previousBackStackEntry?.savedStateHandle?.set("YOYOYO", alarm)

			// Dismiss the dialog
			dismiss()

		})

		// Setup the cancel button
		setupSecondaryButton(cancelButton)
	}

	/**
	 * Set if the how early to dismiss views should be usable or not.
	 */
	private fun setHowEarlyToDismissUsable()
	{
		// Set the alpha for the views
		dismissEarlyQuestion.alpha = alpha
		dismissEarlyInputLayout.alpha = alpha

		// Set whether it can be used or not
		dismissEarlyInputLayout.isEnabled = dismissEarlyCheckBox.isChecked
	}

	/**
	 * Setup the description for if the alarm should be deleted after it is dismissed or not.
	 */
	private fun setShouldDeleteAlarmAfterDismissedDescription(checkBox: MaterialCheckBox, textView: TextView)
	{
		// Determine the text ID to use based on whether easy snooze will be
		// used or not
		val textId = if (checkBox.isChecked)
		{
			R.string.delete_alarm_after_dismissed_true
		}
		else
		{
			R.string.delete_alarm_after_dismissed_false
		}

		// Set the text
		textView.setText(textId)
	}

	/**
	 * Setup the description for what type of snoozing should take place.
	 */
	private fun setShouldDismissEarlyDescription(textView: TextView)
	{
		// Determine the text ID to use based on whether easy snooze will be
		// used or not
		val textId = if (dismissEarlyCheckBox.isChecked)
		{
			R.string.dismiss_early_true
		}
		else
		{
			R.string.dismiss_early_false
		}

		// Set the text
		textView.setText(textId)
	}

	/**
	 * Setup the auto dismiss views.
	 */
	private fun setupAutoDismiss(default: Int)
	{
		// Get the views
		val inputLayout: TextInputLayout = dialog!!.findViewById(R.id.auto_dismiss_input_layout)
		val autoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.auto_dismiss_dropdown_menu)

		// Setup the input layout
		inputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		val index = NacAlarm.calcAutoDismissIndex(default)
		autoCompleteTextView.setTextFromIndex(index)

		// Set the textview listeners
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedAutoDismissTime = NacAlarm.calcAutoDismissTime(position)
		}
	}

	/**
	 * Setup the how early to dismiss views.
	 */
	private fun setupHowEarlyToDismiss(default: Int)
	{
		// Get the views
		dismissEarlyQuestion = dialog!!.findViewById(R.id.how_early_to_dismiss_question)
		dismissEarlyInputLayout = dialog!!.findViewById(R.id.how_early_to_dismiss_input_layout)
		val autoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.how_early_to_dismiss_dropdown_menu)

		// Setup the input layout
		dismissEarlyInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		val index = NacAlarm.calcDismissEarlyIndex(default)
		autoCompleteTextView.setTextFromIndex(index)

		// Set the textview listeners
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedDismissEarlyTime = NacAlarm.calcDismissEarlyTime(position)
		}
	}

	/**
	 * Setup the should delete alarm after it is dismissed views.
	 */
	private fun setupShouldDeleteAlarmAfterDismissed(default: Boolean)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.should_delete_alarm_after_dismissed)
		val description: TextView = dialog!!.findViewById(R.id.should_delete_alarm_after_dismissed_summary)
		val checkBox: MaterialCheckBox = dialog!!.findViewById(R.id.should_delete_alarm_after_dismissed_checkbox)

		// Set the default checkbox value
		checkBox.isChecked = default

		// Setup the checkbox
		checkBox.setupCheckBoxColor(sharedPreferences)

		// Set the description
		setShouldDeleteAlarmAfterDismissedDescription(checkBox, description)

		// Layout click listener to change the checkbox
		relativeLayout.setOnClickListener {

			// Toggle the checkbox
			checkBox.toggle()
			selectedShouldDeleteAlarmAfterDismissed = checkBox.isChecked

			// Set the description
			setShouldDeleteAlarmAfterDismissedDescription(checkBox, description)

		}
	}

	/**
	 * Setup the should dismiss early views.
	 */
	private fun setupShouldDismissEarly(default: Boolean)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.should_use_dismiss_early)
		val description: TextView = dialog!!.findViewById(R.id.should_use_dismiss_early_summary)
		dismissEarlyCheckBox = dialog!!.findViewById(R.id.should_use_dismiss_early_checkbox)!!

		// Set the default checkbox value
		dismissEarlyCheckBox.isChecked = default

		// Setup the checkbox
		dismissEarlyCheckBox.setupCheckBoxColor(sharedPreferences)

		// Set the description
		setShouldDismissEarlyDescription(description)

		// Layout click listener to change the checkbox
		relativeLayout.setOnClickListener {

			// Toggle the checkbox
			dismissEarlyCheckBox.isChecked = !dismissEarlyCheckBox.isChecked

			// Set the description
			setShouldDismissEarlyDescription(description)

			// Set the usability of the how early to dismiss views
			setHowEarlyToDismissUsable()

		}
	}

}