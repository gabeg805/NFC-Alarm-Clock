package com.nfcalarmclock.alarm.options.volume

import android.widget.AdapterView
import android.widget.RelativeLayout
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
 * Customize volume options for an alarm.
 */
class NacVolumeOptionsDialog
	: NacGenericAlarmOptionsDialog()
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId = R.layout.dlg_volume_options

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
	 * Called when the Ok button is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// Update the alarm
		alarm?.shouldGraduallyIncreaseVolume = graduallyIncreaseVolumeSwitch.isChecked
		alarm?.graduallyIncreaseVolumeWaitTime = selectedWaitTime
		alarm?.shouldRestrictVolume = restrictVolumeSwitch.isChecked
	}

	/**
	 * Setup whether the gradually increase volume wait time container can be
	 * used or not.
	 */
	private fun setGraduallyIncreaseVolumeUsability()
	{
		// Get the state and alpha
		val state = graduallyIncreaseVolumeSwitch.isChecked
		val alpha = calcAlpha(state)

		// Set the usability
		graduallyIncreaseVolumeInputLayout.alpha = alpha
		graduallyIncreaseVolumeInputLayout.isEnabled = state
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		// Get the default values
		val defaultShouldGraduallyIncreaseVolume = alarm?.shouldGraduallyIncreaseVolume ?: false
		val defaultGraduallyIncreaseVolumeWaitTime = alarm?.graduallyIncreaseVolumeWaitTime ?: 5
		val defaultRestrictVolume = alarm?.shouldRestrictVolume ?: false
		selectedWaitTime = defaultGraduallyIncreaseVolumeWaitTime

		// Setup the views
		setupGraduallyIncreaseVolume(defaultShouldGraduallyIncreaseVolume, defaultGraduallyIncreaseVolumeWaitTime)
		setGraduallyIncreaseVolumeUsability()
		setupRestrictVolume(defaultRestrictVolume)
	}

	/**
	 * Setup the gradually increase volume views.
	 */
	private fun setupGraduallyIncreaseVolume(defaultState: Boolean, defaultTime: Int)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.gradually_increase_volume_container)
		val autoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.gradually_increase_volume_dropdown_menu)
		graduallyIncreaseVolumeSwitch = dialog!!.findViewById(R.id.gradually_increase_volume_switch)
		graduallyIncreaseVolumeInputLayout = dialog!!.findViewById(R.id.gradually_increase_volume_input_layout)

		// Get the list of seconds, starting at the first index until the end
		// This will omit 0 seconds
		val seconds = resources.getStringArray(R.array.general_seconds_summaries).drop(1).toTypedArray()

		// Get the index of the default selected item in the textview
		val index = NacAlarm.calcGraduallyIncreaseVolumeIndex(defaultTime)

		// Setup the checkbox
		graduallyIncreaseVolumeSwitch.isChecked = defaultState
		graduallyIncreaseVolumeSwitch.setupSwitchColor(sharedPreferences)

		// Setup the input layout and textview
		graduallyIncreaseVolumeInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		autoCompleteTextView.setSimpleItems(seconds)
		autoCompleteTextView.setTextFromIndex(index)

		// Set the listener
		relativeLayout.setOnClickListener {

			// Toggle the checkbox and set the usability of the dropdown
			graduallyIncreaseVolumeSwitch.toggle()
			setGraduallyIncreaseVolumeUsability()

		}

		// Set the textview listener
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedWaitTime = NacAlarm.calcGraduallyIncreaseVolumeWaitTime(position)
		}
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