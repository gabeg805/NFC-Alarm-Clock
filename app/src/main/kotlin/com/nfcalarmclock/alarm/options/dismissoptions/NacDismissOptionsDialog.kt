package com.nfcalarmclock.alarm.options.dismissoptions

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
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.changeSimpleItemsOnZero
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupInputLayoutColor
import com.nfcalarmclock.view.setupSwitchColor

/**
 * Dismiss options.
 */
class NacDismissOptionsDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Auto dismiss switch.
	 */
	private lateinit var autoDismissSwitch: SwitchCompat

	/**
	 * Auto dismiss minutes input layout.
	 */
	private lateinit var autoDismissMinutesInputLayout: TextInputLayout

	/**
	 * Auto dismiss seconds input layout.
	 */
	private lateinit var autoDismissSecondsInputLayout: TextInputLayout

	/**
	 * Dismiss early switch.
	 */
	private lateinit var dismissEarlySwitch: SwitchCompat

	/**
	 * Dismiss early input layout.
	 */
	private lateinit var dismissEarlyInputLayout: TextInputLayout

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
		setupAutoDismissUsable()
		setupDismissEarly(defaultShouldDismissEarly, defaultDismissEarlyTime)
		setupDismissEarlyUsable()
		setupShouldDeleteAlarmAfterDismissed(defaultShouldDeleteAlarmAfterDismissed)

		// Setup the ok button
		setupPrimaryButton(okButton, listener = {

			// Update the alarm attributes
			alarm?.autoDismissTime = selectedAutoDismissTime
			alarm?.useDismissEarly = dismissEarlySwitch.isChecked
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
	 * Setup the auto dismiss views.
	 */
	private fun setupAutoDismiss(default: Int)
	{
		// Get the views
		val minutesAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.auto_dismiss_minutes_dropdown_menu)
		val secondsAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.auto_dismiss_seconds_dropdown_menu)
		autoDismissSwitch = dialog!!.findViewById(R.id.auto_dismiss_switch)
		autoDismissMinutesInputLayout = dialog!!.findViewById(R.id.auto_dismiss_minutes_input_layout)
		autoDismissSecondsInputLayout = dialog!!.findViewById(R.id.auto_dismiss_seconds_input_layout)

		// Get the list of items for minutes and seconds
		val minutes = resources.getStringArray(R.array.auto_dismiss_minute_summaries)
		val seconds = resources.getStringArray(R.array.general_seconds_summaries)

		// Setup the switch
		autoDismissSwitch.setupSwitchColor(sharedPreferences)

		// Setup the input layout
		autoDismissMinutesInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		autoDismissSecondsInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the switch listener
		autoDismissSwitch.setOnCheckedChangeListener { _, _ ->
			setupAutoDismissUsable()
		}

		// Set the default selected items in the text views
		// TODO
		//val index = NacAlarm.calcAutoDismissIndex(default)
		//autoCompleteTextView.setTextFromIndex(index)
		minutesAutoCompleteTextView.setTextFromIndex(1)
		secondsAutoCompleteTextView.setTextFromIndex(0)

		// Set the minutes textview listeners
		minutesAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			secondsAutoCompleteTextView.changeSimpleItemsOnZero(seconds, position)
			//	selectedAutoDismissTime = NacAlarm.calcAutoDismissTime(position)
		}

		// Set the seconds textview listeners
		secondsAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			minutesAutoCompleteTextView.changeSimpleItemsOnZero(minutes, position)
			//	selectedAutoDismissTime = NacAlarm.calcAutoDismissTime(position)
		}

		// TODO
		//autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
		//	selectedAutoDismissTime = NacAlarm.calcAutoDismissTime(position)
		//}
	}

	/**
	 * Setup whether the auto dismiss views should be usable or not.
	 */
	private fun setupAutoDismissUsable()
	{
		// Get the state and calculate the alpha
		val state = autoDismissSwitch.isChecked
		val alpha = calcAlpha(state)

		// Setup the usablity of the dropdowns
		autoDismissMinutesInputLayout.alpha = alpha
		autoDismissSecondsInputLayout.alpha = alpha
		autoDismissMinutesInputLayout.isEnabled = state
		autoDismissSecondsInputLayout.isEnabled = state
	}

	/**
	 * Setup the dismiss early views.
	 */
	private fun setupDismissEarly(defaultState: Boolean, defaultTime: Int)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.dismiss_early_container)
		val autoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.dismiss_early_dropdown_menu)
		dismissEarlySwitch = dialog!!.findViewById(R.id.dismiss_early_switch)
		dismissEarlyInputLayout = dialog!!.findViewById(R.id.dismiss_early_input_layout)

		// Setup the checkbox
		dismissEarlySwitch.isChecked = defaultState
		dismissEarlySwitch.setupSwitchColor(sharedPreferences)

		// Setup the dropdown
		val index = NacAlarm.calcDismissEarlyIndex(defaultTime)

		dismissEarlyInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		autoCompleteTextView.setTextFromIndex(index)

		// Set the listener for the parent to change the checkbox
		relativeLayout.setOnClickListener {

			// Toggle the checkbox
			dismissEarlySwitch.isChecked = !dismissEarlySwitch.isChecked

			// Set the usability of the how early to dismiss views
			setupDismissEarlyUsable()

		}

		// Set the dropdown listener
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedDismissEarlyTime = NacAlarm.calcDismissEarlyTime(position)
		}
	}

	/**
	 * Setup whether the dismiss early views should be usable or not.
	 */
	private fun setupDismissEarlyUsable()
	{
		// Get the state and calculate the alpha
		val state = dismissEarlySwitch.isChecked
		val alpha = calcAlpha(state)

		// Setup usability of the dropdown
		dismissEarlyInputLayout.alpha = alpha
		dismissEarlyInputLayout.isEnabled = state
	}

	/**
	 * Setup the should delete alarm after it is dismissed views.
	 */
	private fun setupShouldDeleteAlarmAfterDismissed(default: Boolean)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.delete_after_dismissed_container)
		val switch: SwitchCompat = dialog!!.findViewById(R.id.delete_after_dismissed_switch)

		// Setup the checkbox
		switch.isChecked = default
		switch.setupSwitchColor(sharedPreferences)

		// Set the parent click listener to change the checkbox
		relativeLayout.setOnClickListener {

			// Toggle the checkbox
			switch.toggle()
			selectedShouldDeleteAlarmAfterDismissed = switch.isChecked

		}
	}

}
