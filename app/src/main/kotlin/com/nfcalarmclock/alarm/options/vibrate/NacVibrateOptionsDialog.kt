package com.nfcalarmclock.alarm.options.vibrate

import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.setupProgressAndThumbColor
import com.nfcalarmclock.view.setupSwitchColor

/**
 * Vibrate options for an alarm.
 */
open class NacVibrateOptionsDialog
	: NacGenericAlarmOptionsDialog()
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId = R.layout.dlg_vibrate

	/**
	 * Slider for the vibrate on duration times.
	 */
	private lateinit var vibrateOnSlider: Slider

	/**
	 * Slider for the vibrate off (wait) duration times.
	 */
	private lateinit var vibrateOffSlider: Slider

	/**
	 * Custom pattern switch.
	 */
	private lateinit var customPatternSwitch: SwitchCompat

	/**
	 * Relative layout containing all the on/off duration views.
	 */
	private lateinit var customPatternOnOffRelativeLayout: RelativeLayout

	/**
	 * Slider for how many times to repeat the pattern.
	 */
	private lateinit var customPatternRepeatSlider: Slider

	/**
	 * Slider for the pattern wait duration times.
	 */
	private lateinit var customPatternWaitSlider: Slider

	/**
	 * Vibrate the device.
	 */
	private lateinit var vibrator: NacVibrator

	/**
	 * Set the label formatter for a slider that has values of milliseconds.
	 */
	private fun Slider.setMillisecondLabelFormatter(textView: TextView)
	{
		this.setLabelFormatter {

			// Get the label
			val label = resources.getString(R.string.message_milliseconds, it.toInt())

			// Set the label
			textView.text = label

			return@setLabelFormatter label
		}
	}

	/**
	 * Set the label formatter for a slider that repeats something a number of times.
	 */
	private fun Slider.setRepeatLabelFormatter(textView: TextView)
	{
		this.setLabelFormatter {

			// Get the label
			val value = it.toInt()
			val label = resources.getQuantityString(R.plurals.unit_number_of_times, value, value)

			// Set the label
			textView.text = label

			return@setLabelFormatter label
		}
	}

	/**
	 * Called when the cancel button is clicked.
	 */
	override fun onCancelClicked(alarm: NacAlarm?)
	{
		// Cleanup the preview if it is running
		vibrator.cleanup()
	}

	/**
	 * Update the alarm with selected options.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// Cleanup the preview if it is running
		vibrator.cleanup()

		// Update the alarm
		alarm?.vibrateDuration = vibrateOnSlider.value.toLong()
		alarm?.vibrateWaitTime = vibrateOffSlider.value.toLong()
		alarm?.shouldVibratePattern = customPatternSwitch.isChecked
		alarm?.vibrateRepeatPattern = customPatternRepeatSlider.value.toInt()
		alarm?.vibrateWaitTimeAfterPattern = customPatternWaitSlider.value.toLong()
	}

	/**
	 * Setup whether the custom pattern repeat/wait container can be used or not.
	 */
	private fun setCustomPatternUsability()
	{
		// Get the state and alpha
		val state = customPatternSwitch.isChecked
		val alpha = calcAlpha(state)

		// Set the usability
		customPatternOnOffRelativeLayout.alpha = alpha
		customPatternRepeatSlider.isEnabled = state
		customPatternWaitSlider.isEnabled = state
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		// Get the vibrator
		val context = requireContext()
		vibrator = NacVibrator(context)

		// Get the alarm, or build a new one, to get default values
		val a = alarm ?: NacAlarm.build(sharedPreferences)

		// Setup the views
		setupVibrationDuration(a.vibrateDuration, a.vibrateWaitTime)
		setupCustomPattern(a.shouldVibratePattern, a.vibrateRepeatPattern, a.vibrateWaitTimeAfterPattern)
		setCustomPatternUsability()

	}

	/**
	 * Setup the vibration duration views.
	 */
	private fun setupVibrationDuration(defaultVibrate: Long, defaultWait: Long)
	{
		// Get the views
		vibrateOnSlider = dialog!!.findViewById(R.id.vibrate_on_slider)
		vibrateOffSlider = dialog!!.findViewById(R.id.vibrate_off_slider)
		val vibrateOnValue: TextView = dialog!!.findViewById(R.id.message_vibrate_on_value)
		val vibrateOffValue: TextView = dialog!!.findViewById(R.id.message_vibrate_off_value)

		// Set the default values
		vibrateOnSlider.value = defaultVibrate.toFloat()
		vibrateOffSlider.value = defaultWait.toFloat()
		vibrateOnValue.text = resources.getString(R.string.message_milliseconds, defaultVibrate)
		vibrateOffValue.text = resources.getString(R.string.message_milliseconds, defaultWait)

		// Setup the views
		vibrateOnSlider.setupProgressAndThumbColor(sharedPreferences)
		vibrateOffSlider.setupProgressAndThumbColor(sharedPreferences)
		vibrateOnSlider.setMillisecondLabelFormatter(vibrateOnValue)
		vibrateOffSlider.setMillisecondLabelFormatter(vibrateOffValue)

		// Set the on duration listeners
		vibrateOnSlider.addOnSliderTouchListener(object: Slider.OnSliderTouchListener {

			/**
			 * Called when the slider is first touched.
			 */
			override fun onStartTrackingTouch(slider: Slider)
			{
			}

			/**
			 * Called when the slider is no longer touched.
			 */
			override fun onStopTrackingTouch(slider: Slider)
			{
				// Restart the vibrator if it is running
				if (vibrator.isRunning)
				{
					startVibrator()
				}
			}

		})

		// Set the off duration listeners
		vibrateOffSlider.addOnSliderTouchListener(object: Slider.OnSliderTouchListener {

			/**
			 * Called when the slider is first touched.
			 */
			override fun onStartTrackingTouch(slider: Slider)
			{
			}

			/**
			 * Called when the slider is no longer touched.
			 */
			override fun onStopTrackingTouch(slider: Slider)
			{
				// Restart the vibrator if it is running
				if (vibrator.isRunning)
				{
					startVibrator()
				}
			}

		})
	}

	/**
	 * Setup the custom pattern views.
	 */
	private fun setupCustomPattern(defaultState: Boolean, defaultRepeat: Int, defaultWait: Long)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.custom_pattern_container)
		customPatternSwitch = dialog!!.findViewById(R.id.custom_pattern_switch)
		customPatternOnOffRelativeLayout = dialog!!.findViewById(R.id.custom_pattern_repeat_wait_layout)
		customPatternRepeatSlider = dialog!!.findViewById(R.id.custom_pattern_repeat_slider)
		customPatternWaitSlider = dialog!!.findViewById(R.id.custom_pattern_wait_slider)
		val customPatternRepeatValue: TextView = dialog!!.findViewById(R.id.essage_custom_pattern_repeat2)
		val customPatternWaitValue: TextView = dialog!!.findViewById(R.id.message_custom_pattern_wait_value)

		// Set the default values
		customPatternSwitch.isChecked = defaultState
		customPatternRepeatSlider.value = defaultRepeat.toFloat()
		customPatternWaitSlider.value = defaultWait.toFloat()
		customPatternRepeatValue.text = resources.getQuantityString(R.plurals.unit_number_of_times, defaultRepeat, defaultRepeat)
		customPatternWaitValue.text = resources.getString(R.string.message_milliseconds, defaultWait)

		// Setup the views
		customPatternSwitch.setupSwitchColor(sharedPreferences)
		customPatternRepeatSlider.setupProgressAndThumbColor(sharedPreferences)
		customPatternWaitSlider.setupProgressAndThumbColor(sharedPreferences)
		customPatternRepeatSlider.setRepeatLabelFormatter(customPatternRepeatValue)
		customPatternWaitSlider.setMillisecondLabelFormatter(customPatternWaitValue)

		// Set the listener
		relativeLayout.setOnClickListener {

			// Toggle the switch
			customPatternSwitch.toggle()

			// Set the usability of the blink on/off duration views
			setCustomPatternUsability()

			// Restart the vibrator if it is running
			if (vibrator.isRunning)
			{
				startVibrator()
			}

		}

		// Set the repeat slider listener
		customPatternRepeatSlider.addOnSliderTouchListener(object: Slider.OnSliderTouchListener {

			/**
			 * Called when the slider is first touched.
			 */
			override fun onStartTrackingTouch(slider: Slider)
			{
			}

			/**
			 * Called when the slider is no longer touched.
			 */
			override fun onStopTrackingTouch(slider: Slider)
			{
				// Restart the vibrator if it is running
				if (vibrator.isRunning)
				{
					startVibrator()
				}
			}

		})

		// Set the wait slider listener
		customPatternWaitSlider.addOnSliderTouchListener(object: Slider.OnSliderTouchListener {

			/**
			 * Called when the slider is first touched.
			 */
			override fun onStartTrackingTouch(slider: Slider)
			{
			}

			/**
			 * Called when the slider is no longer touched.
			 */
			override fun onStopTrackingTouch(slider: Slider)
			{
				// Restart the vibrator if it is running
				if (vibrator.isRunning)
				{
					startVibrator()
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
			if (vibrator.isRunning)
			{
				// Change the text of the button back
				previewButton.text = resources.getString(R.string.action_preview)

				// Cleanup the preview
				vibrator.cleanup()
			}
			// Preview not running
			else
			{
				// Change the text of the button to indicate that a preview is running
				previewButton.text = resources.getString(R.string.action_stop_preview)

				// Start the vibrator
				startVibrator()
			}

		})
	}

	/**
	 * Start the vibrator.
	 */
	private fun startVibrator()
	{
		// Cleanup the vibrator
		vibrator.cleanup()

		// Get the values
		val duration = vibrateOnSlider.value.toLong()
		val wait = vibrateOffSlider.value.toLong()

		// Vibrate with a pattern
		if (customPatternSwitch.isChecked)
		{
			vibrator.vibrate(
				duration,
				wait,
				customPatternRepeatSlider.value.toInt(),
				customPatternWaitSlider.value.toLong())
		}
		// Vibrate normally
		else
		{
			vibrator.vibrate(duration, wait)
		}
	}

}