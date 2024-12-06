package com.nfcalarmclock.alarm.options.volume

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.util.NacBundle
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupCheckBoxColor
import com.nfcalarmclock.view.setupInputLayoutColor
import com.nfcalarmclock.view.setupSwitchColor

class NacVolumeOptionsDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Gradually increase volume switch.
	 */
	private lateinit var graduallyIncreaseVolumeSwitch: SwitchCompat

	/**
	 * Input layout to select the gradually increase volume wait time.
	 */
	private lateinit var graduallyIncreaseVolumeInputLayout: TextInputLayout

	/**
	 * Restrict volume switch.
	 */
	private lateinit var restrictVolumeSwitch: SwitchCompat

	/**
	 * Selected gradually increase volume wait time.
	 */
	private var selectedWaitTime: Int = 0

	/**
	 * Called when the creating the view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.dlg_volume_options, container, false)
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
		val defaultShouldGraduallyIncreaseVolume = alarm?.shouldGraduallyIncreaseVolume ?: false
		val defaultGraduallyIncreaseVolumeWaitTime = alarm?.graduallyIncreaseVolumeWaitTime ?: 5
		val defaultRestrictVolume = alarm?.shouldRestrictVolume ?: false
		selectedWaitTime = defaultGraduallyIncreaseVolumeWaitTime

		// Setup the views
		setupGraduallyIncreaseVolume(defaultShouldGraduallyIncreaseVolume, defaultGraduallyIncreaseVolumeWaitTime)
		setupGraduallyIncreaseVolumeUsable()
		setupRestrictVolume(defaultRestrictVolume)

		// Setup the ok button
		setupPrimaryButton(okButton, listener = {

			// Update the alarm attributes
			alarm?.shouldGraduallyIncreaseVolume = graduallyIncreaseVolumeSwitch.isChecked
			alarm?.graduallyIncreaseVolumeWaitTime = selectedWaitTime
			alarm?.shouldRestrictVolume = restrictVolumeSwitch.isChecked

			// Save the change so that it is accessible in the previous dialog
			findNavController().previousBackStackEntry?.savedStateHandle?.set("YOYOYO", alarm)

			// Dismiss the dialog
			dismiss()

		})

		// Setup the cancel button
		setupSecondaryButton(cancelButton)
	}

	/**
	 * Setup the gradually increase volume views.
	 */
	private fun setupGraduallyIncreaseVolume(defaultState: Boolean, defaultTime: Int)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.gradually_increase_volume_container)
		val switch: SwitchCompat = dialog!!.findViewById(R.id.gradually_increase_volume_switch)
		val autoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.gradually_increase_volume_dropdown_menu)
		graduallyIncreaseVolumeInputLayout = dialog!!.findViewById(R.id.gradually_increase_volume_input_layout)

		// Setup the checkbox
		switch.isChecked = defaultState
		switch.setupSwitchColor(sharedPreferences)

		// Setup the input layout
		graduallyIncreaseVolumeInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		val index = NacAlarm.calcGraduallyIncreaseVolumeIndex(defaultTime)
		autoCompleteTextView.setTextFromIndex(index)

		// Set the listener
		relativeLayout.setOnClickListener {

			// Toggle the checkbox
			switch.isChecked = !switch.isChecked

			// Set the usability of the dropdown
			setupGraduallyIncreaseVolumeUsable()

		}

		// Set the textview listener
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedWaitTime = NacAlarm.calcGraduallyIncreaseVolumeWaitTime(position)
		}
	}

	/**
	 * Setup whether the gradually increase volume wait time container can be
	 * used or not.
	 */
	private fun setupGraduallyIncreaseVolumeUsable()
	{
		// Get the state and alpha
		val state = graduallyIncreaseVolumeSwitch.isChecked
		val alpha = calcAlpha(state)

		// Set the usability
		graduallyIncreaseVolumeInputLayout.alpha = alpha
		graduallyIncreaseVolumeInputLayout.isEnabled = state
	}

	/**
	 * Setup whether to restrict volume or not.
	 */
	private fun setupRestrictVolume(default: Boolean)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.restrict_volume_container)
		restrictVolumeSwitch = dialog!!.findViewById(R.id.restrict_volume_switch)

		// Setup the switch
		restrictVolumeSwitch.isChecked = default
		restrictVolumeSwitch.setupSwitchColor(sharedPreferences)

		// Set the listener
		relativeLayout.setOnClickListener {

			// Toggle the checkbox
			restrictVolumeSwitch.isChecked = !restrictVolumeSwitch.isChecked

		}
	}

}