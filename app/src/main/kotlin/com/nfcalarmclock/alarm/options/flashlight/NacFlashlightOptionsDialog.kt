package com.nfcalarmclock.alarm.options.flashlight

import android.os.Build
import android.view.View
import android.widget.AdapterView
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.slider.Slider.OnSliderTouchListener
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupInputLayoutColor
import com.nfcalarmclock.view.setupProgressAndThumbColor
import com.nfcalarmclock.view.setupSwitchColor

/**
 * Flashlight options for an alarm.
 */
open class NacFlashlightOptionsDialog
	: NacGenericAlarmOptionsDialog()
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId = R.layout.dlg_flashlight

	/**
	 * Flashlight.
	 */
	private lateinit var flashlight: NacFlashlight

	/**
	 * Seekbar for the flashlight strength level.
	 */
	private lateinit var brightnessSlider: Slider

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
	 * Called when the cancel button is clicked.
	 */
	override fun onCancelClicked(alarm: NacAlarm?)
	{
		// Cleanup the flashlight
		flashlight.cleanup()
	}

	/**
	 * Update the alarm with selected options.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// Cleanup the preview if it is running
		flashlight.cleanup()

		// Update the alarm
		alarm?.flashlightStrengthLevel = brightnessSlider.value.toInt()
		alarm?.shouldBlinkFlashlight = blinkSwitch.isChecked
		alarm?.flashlightOnDuration = selectedBlinkOnDuration
		alarm?.flashlightOffDuration = selectedBlinkOffDuration
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
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		// Create the flashlight
		flashlight = NacFlashlight(requireContext())

		// Get the alarm, or build a new one, to get default values
		val a = alarm ?: NacAlarm.build(sharedPreferences)

		// Set the default selected values
		selectedBlinkOnDuration = if (a.flashlightOnDuration == "0") "1.0" else a.flashlightOnDuration
		selectedBlinkOffDuration = if (a.flashlightOffDuration == "0") "1.0" else a.flashlightOffDuration

		// Setup the views
		setupBrightnessLevel(a.flashlightStrengthLevel)
		setupBlinkFlashlight(a.shouldBlinkFlashlight, a.flashlightOnDuration, a.flashlightOffDuration)
		setBlinkFlashlightUsability()
	}

	/**
	 * Setup the blink flashlight views.
	 */
	private fun setupBlinkFlashlight(defaultState: Boolean, defaultOn: String, defaultOff: String)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.flashlight_blink_container)
		val onDurationAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.flashlight_on_duration_dropdown_menu)
		val offDurationAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.flashlight_off_duration_dropdown_menu)
		blinkSwitch = dialog!!.findViewById(R.id.flashlight_blink_switch)
		onOffDurationRelativeLayout = dialog!!.findViewById(R.id.flashlight_on_off_duration)
		onDurationInputLayout = dialog!!.findViewById(R.id.flashlight_on_duration_input_layout)
		offDurationInputLayout = dialog!!.findViewById(R.id.flashlight_off_duration_input_layout)

		// Setup the switch
		blinkSwitch.isChecked = defaultState
		blinkSwitch.setupSwitchColor(sharedPreferences)

		// Setup the input layout
		onDurationInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		offDurationInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		val onIndex = NacAlarm.calcFlashlightOnOffDurationIndex(defaultOn)
		val offIndex = NacAlarm.calcFlashlightOnOffDurationIndex(defaultOff)
		onDurationAutoCompleteTextView.setTextFromIndex(onIndex)
		offDurationAutoCompleteTextView.setTextFromIndex(offIndex)

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
	 * Setup the strength level.
	 */
	private fun setupBrightnessLevel(default: Int)
	{
		// Get the views
		val title: TextView = dialog!!.findViewById(R.id.flashlight_brightness_title)
		val description: TextView = dialog!!.findViewById(R.id.flashlight_brightness_description)
		val space: Space = dialog!!.findViewById(R.id.flashlight_brightness_space)
		brightnessSlider = dialog!!.findViewById(R.id.flashlight_brightness_slider)

		// Check if this version does not support changing the flashlight level
		if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) || (flashlight.maxLevel == 1))
		{
			// Get the gone visibility
			val vis = View.GONE

			// Set the visibility to everything
			title.visibility = vis
			description.visibility = vis
			space.visibility = vis
			brightnessSlider.visibility = vis
			return
		}

		// Setup the color
		brightnessSlider.setupProgressAndThumbColor(sharedPreferences)

		// Set the min and max value
		brightnessSlider.valueFrom = flashlight.minLevel.toFloat()
		brightnessSlider.valueTo = flashlight.maxLevel.toFloat()

		// Set the brightness level
		brightnessSlider.value = if (default == 0)
		{
			flashlight.maxLevel.toFloat()
		}
		else
		{
			default.toFloat()
		}

		// Set the change listener
		brightnessSlider.addOnChangeListener { _, value, _ ->

			// Change the flashlight strength
			flashlight.strengthLevel = value.toInt()

		}

		// Set the touch listener
		brightnessSlider.addOnSliderTouchListener(object: OnSliderTouchListener {

			/**
			 * Seek bar was touched.
			 */
			override fun onStartTrackingTouch(slider: Slider)
			{
			}

			/**
			 * Seek bar stopped being touched.
			 */
			override fun onStopTrackingTouch(slider: Slider)
			{
				// Check if the flashlight is running
				if (flashlight.isRunning)
				{
					// Restart the flashlight
					startFlashlight()
				}
			}

		})
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