package com.nfcalarmclock.alarm.vibrate

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.AdapterView
import android.widget.RelativeLayout
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.alarm.options.flashlight.NacFlashlight
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupInputLayoutColor
import com.nfcalarmclock.view.setupSwitchColor

class NacVibrateOptionsDialog
	: NacGenericAlarmOptionsDialog()
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId = R.layout.dlg_vibrate

	/**
	 * Blink flashlight switch.
	 */
	private lateinit var blinkSwitch: SwitchCompat

	/**
	 * Relative layout containing all the on/off duration views.
	 */
	private lateinit var onOffDurationRelativeLayout: RelativeLayout

	/**
	 * Input layout for the flashlight blink on duration times.
	 */
	private lateinit var onDurationInputLayout: TextInputLayout

	/**
	 * Input layout for the flashlight blink off duration times.
	 */
	private lateinit var offDurationInputLayout: TextInputLayout

	/**
	 * Selected blink on duration.
	 */
	private var selectedBlinkOnDuration: String = ""

	/**
	 * Selected blink off duration.
	 */
	private var selectedBlinkOffDuration: String = ""

	/**
	 * Vibrate handler to vibrate the phone at periodic intervals.
	 */
	private lateinit var vibrateHandler: Handler

	/**
	 * Vibrator object to vibrate the phone.
	 */
	private lateinit var vibrator: Vibrator

	/**
	 * Called when the cancel button is clicked.
	 */
	override fun onCancelClicked(alarm: NacAlarm?)
	{
		// Cleanup the vibrator
		//flashlight.cleanup()
	}

	/**
	 * Update the alarm with selected options.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// Cleanup the preview if it is running
		//flashlight.cleanup()

		// Update the alarm
		//alarm?.flashlightStrengthLevel = brightnessSlider.value.toInt()
		//alarm?.shouldBlinkFlashlight = blinkSwitch.isChecked
		//alarm?.flashlightOnDuration = selectedBlinkOnDuration
		//alarm?.flashlightOffDuration = selectedBlinkOffDuration
	}

	/**
	 * Setup whether the blink flashlight on/off container can be used or not.
	 */
	private fun setBlinkFlashlightUsability()
	{
		// Get the state and alpha
		val state = blinkSwitch.isChecked
		val alpha = calcAlpha(state)

		// Set the usability
		onOffDurationRelativeLayout.alpha = alpha
		onDurationInputLayout.isEnabled = state
		offDurationInputLayout.isEnabled = state
	}

	/**
	 * Setup all alarm options.
	 */
	@Suppress("deprecation")
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		// Get the context
		val context = requireContext()

		// Get the vibrator
		vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
		{
			// Get the manager
			val manager = context.getSystemService(
				Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager

			// Return the vibrator
			manager.defaultVibrator
		}
		// Use the old API
		else
		{
			context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
		}

		// Get the vibrate handler
		vibrateHandler = Handler(context.mainLooper)

		//// Get the default values
		//val defaultStrength = alarm?.flashlightStrengthLevel ?: 0
		//val defaultShouldBlink = alarm?.shouldBlinkFlashlight ?: false
		//val defaultOnDuration = alarm?.flashlightOnDuration ?: "0"
		//val defaultOffDuration = alarm?.flashlightOffDuration ?: "0"
		//selectedBlinkOnDuration = if (defaultOnDuration == "0") "1.0" else defaultOnDuration
		//selectedBlinkOffDuration = if (defaultOffDuration == "0") "1.0" else defaultOffDuration

		// Setup the views
		setupVibrationDuration("", "")
		setBlinkFlashlightUsability()
	}

	/**
	 * Setup the vibration duration views.
	 */
	private fun setupVibrationDuration(defaultOn: String, defaultOff: String)
	{
		// Get the views
		val onDurationAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.vibration_on_duration_dropdown_menu)
		val offDurationAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.vibration_off_duration_dropdown_menu)
		onDurationInputLayout = dialog!!.findViewById(R.id.vibration_on_duration_input_layout)
		offDurationInputLayout = dialog!!.findViewById(R.id.vibration_off_duration_input_layout)

		// Setup the input layout
		onDurationInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		offDurationInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		//val onIndex = NacAlarm.calcFlashlightOnOffDurationIndex(defaultOn)
		//val offIndex = NacAlarm.calcFlashlightOnOffDurationIndex(defaultOff)
		//onDurationAutoCompleteTextView.setTextFromIndex(onIndex)
		//offDurationAutoCompleteTextView.setTextFromIndex(offIndex)

		// Set the on duration textview listeners
		onDurationAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->

			// Set the duration
			selectedBlinkOnDuration = NacAlarm.calcFlashlightOnOffDuration(position)

			// Check if the flashlight is running
			if (vibrateHandler)
			{
				// Restart the flashlight
				startFlashlight()
			}

		}

		// Set the off duration textview listeners
		offDurationAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->

			// Set the duration
			selectedBlinkOffDuration = NacAlarm.calcFlashlightOnOffDuration(position)

			// Check if the flashlight is running
			if (vibrateHandler)
			{
				// Restart the flashlight
				startFlashlight()
			}

		}
	}

	/**
	 * Setup the custom pattern views.
	 */
	private fun setupCustomPattern(defaultState: Boolean, defaultOn: String, defaultOff: String)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.custom_pattern_container)
		val onDurationAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.custom_pattern_on_duration_dropdown_menu)
		val offDurationAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.custom_pattern_off_duration_dropdown_menu)
		blinkSwitch = dialog!!.findViewById(R.id.custom_pattern_switch)
		onOffDurationRelativeLayout = dialog!!.findViewById(R.id.custom_pattern_on_off_duration)
		onDurationInputLayout = dialog!!.findViewById(R.id.custom_pattern_on_duration_input_layout)
		offDurationInputLayout = dialog!!.findViewById(R.id.custom_pattern_off_duration_input_layout)

		// Setup the switch
		blinkSwitch.isChecked = defaultState
		blinkSwitch.setupSwitchColor(sharedPreferences)

		// Setup the input layout
		onDurationInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		offDurationInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		//val onIndex = NacAlarm.calcFlashlightOnOffDurationIndex(defaultOn)
		//val offIndex = NacAlarm.calcFlashlightOnOffDurationIndex(defaultOff)
		//onDurationAutoCompleteTextView.setTextFromIndex(onIndex)
		//offDurationAutoCompleteTextView.setTextFromIndex(offIndex)

		// Set the listener
		relativeLayout.setOnClickListener {

			// Toggle the switch
			blinkSwitch.toggle()

			// Set the usability of the blink on/off duration views
			setBlinkFlashlightUsability()

			// Check if the flashlight is running
			if (flashlight.isRunning)
			{
				// Restart the flashlight
				startFlashlight()
			}

		}

		// Set the on duration textview listeners
		onDurationAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->

			// Set the duration
			selectedBlinkOnDuration = NacAlarm.calcFlashlightOnOffDuration(position)

			// Check if the flashlight is running
			if (flashlight.isRunning)
			{
				// Restart the flashlight
				startFlashlight()
			}

		}

		// Set the off duration textview listeners
		offDurationAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->

			// Set the duration
			selectedBlinkOffDuration = NacAlarm.calcFlashlightOnOffDuration(position)

			// Check if the flashlight is running
			if (flashlight.isRunning)
			{
				// Restart the flashlight
				startFlashlight()
			}

		}
	}

	/**
	 * Setup any extra buttons.
	 */
	override fun setupExtraButtons(alarm: NacAlarm?)
	{
		// Get the button
		val previewButton: MaterialButton = dialog!!.findViewById(R.id.preview_button)

		// Setup the button
		setupSecondaryButton(previewButton, listener = {

			// Check if preview is running
			if (flashlight.isRunning)
			{
				// Change the text of the button back
				previewButton.text = resources.getString(R.string.action_preview)

				// Cleanup the preview
				flashlight.cleanup()
			}
			// Preview not running
			else
			{
				// Change the text of the button to indicate that a preview is running
				previewButton.text = resources.getString(R.string.action_stop_preview)

				// Start the flashlight
				startFlashlight()
			}

		})
	}

	/**
	 * Start the flashlight.
	 */
	private fun startFlashlight()
	{
		// Cleanup the flashlight
		flashlight.cleanup()

		// TODO: Create a NacVibrator object
		// Check if blink handlers need to be setup
		if (blinkSwitch.isChecked)
		{
			// Blink the flashlight
			flashlight.blink(selectedBlinkOnDuration, selectedBlinkOffDuration)
		}
		// Turn on the flashlight normally
		else
		{
			flashlight.turnOn()
		}
	}

}