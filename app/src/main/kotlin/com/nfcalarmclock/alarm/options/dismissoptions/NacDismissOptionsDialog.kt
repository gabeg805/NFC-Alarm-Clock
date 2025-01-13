package com.nfcalarmclock.alarm.options.dismissoptions

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
 * Dismiss options.
 */
class NacDismissOptionsDialog
	: NacGenericAlarmOptionsDialog()
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId = R.layout.dlg_dismiss_options

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
	 * Called when the Ok button is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// Update the alarm
		alarm?.shouldAutoDismiss = autoDismissSwitch.isChecked
		alarm?.autoDismissTime = selectedAutoDismissTime
		alarm?.useDismissEarly = dismissEarlySwitch.isChecked
		alarm?.dismissEarlyTime = selectedDismissEarlyTime
		alarm?.shouldDeleteAlarmAfterDismissed = selectedShouldDeleteAlarmAfterDismissed
	}

	/**
	 * Called when the alarm should be saved.
	 */
	override fun onSaveAlarm(alarm: NacAlarm?)
	{
		onSaveAlarmListener?.onSaveAlarm(alarm!!)
	}

	/**
	 * Setup whether the auto dismiss views should be usable or not.
	 */
	private fun setAutoDismissUsability()
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
	 * Setup whether the dismiss early views should be usable or not.
	 */
	private fun setDismissEarlyUsability()
	{
		// Get the state and calculate the alpha
		val state = dismissEarlySwitch.isChecked
		val alpha = calcAlpha(state)

		// Setup usability of the dropdown
		dismissEarlyInputLayout.alpha = alpha
		dismissEarlyInputLayout.isEnabled = state
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		// Get the default values
		val defaultShouldAutoDismiss = alarm?.shouldAutoDismiss ?: true
		val defaultAutoDismissTime = alarm?.autoDismissTime?.takeIf { it > 0 } ?: 900
		val defaultShouldDismissEarly = alarm?.useDismissEarly ?: false
		val defaultDismissEarlyTime = alarm?.dismissEarlyTime ?: 30
		val defaultShouldDeleteAlarmAfterDismissed = alarm?.shouldDeleteAlarmAfterDismissed ?: false
		selectedAutoDismissTime = defaultAutoDismissTime
		selectedDismissEarlyTime = defaultDismissEarlyTime

		// Setup the views
		setupAutoDismiss(defaultShouldAutoDismiss, defaultAutoDismissTime)
		setupDismissEarly(defaultShouldDismissEarly, defaultDismissEarlyTime)
		setupShouldDeleteAlarmAfterDismissed(defaultShouldDeleteAlarmAfterDismissed)
		setAutoDismissUsability()
		setDismissEarlyUsability()
	}

	/**
	 * Setup the auto dismiss views.
	 */
	private fun setupAutoDismiss(defaultState: Boolean, defaultTime: Int)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.auto_dismiss_container)
		val minutesAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.auto_dismiss_minutes_dropdown_menu)
		val secondsAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.auto_dismiss_seconds_dropdown_menu)
		autoDismissSwitch = dialog!!.findViewById(R.id.auto_dismiss_switch)
		autoDismissMinutesInputLayout = dialog!!.findViewById(R.id.auto_dismiss_minutes_input_layout)
		autoDismissSecondsInputLayout = dialog!!.findViewById(R.id.auto_dismiss_seconds_input_layout)

		// Setup the switch
		autoDismissSwitch.isChecked = defaultState
		autoDismissSwitch.setupSwitchColor(sharedPreferences)

		// Setup the minutes and seconds
		setupMinutesAndSecondsOption(
			autoDismissMinutesInputLayout,
			autoDismissSecondsInputLayout,
			minutesAutoCompleteTextView,
			secondsAutoCompleteTextView,
			startIndices = NacAlarm.calcAutoDismissIndex(defaultTime),
			onTimeChanged = { minIndex, secIndex ->

				// Update the selected time
				val newMin = NacAlarm.calcAutoDismissFromMinutesIndex(minIndex)
				val newSec = NacAlarm.calcAutoDismissFromSecondsIndex(secIndex)
				selectedAutoDismissTime = newMin + newSec

			})

		// Set the switch listener
		relativeLayout.setOnClickListener { _ ->

			// Toggle the switch and set the usability of the views the switch controls
			autoDismissSwitch.toggle()
			setAutoDismissUsability()

		}
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

			// Toggle the switch and set the usability of the views the switch controls
			dismissEarlySwitch.toggle()
			setDismissEarlyUsability()

		}

		// Set the dropdown listener
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

	companion object
	{

		/**
		 * Dialog name.
		 */
		const val TAG = "NacDismissOptionsDialog"

		/**
		 * Create a dialog that can be shown easily.
		 */
		fun create(
			alarm: NacAlarm,
			onSaveAlarmListener: (NacAlarm) -> Unit = {}
		): NacDismissOptionsDialog
		{
			// Create the dialog
			val dialog = NacDismissOptionsDialog()

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
