package com.nfcalarmclock.alarmoptions.flashlight

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Space
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
 * Dialog to prompt user what flashlight options they want.
 */
class NacFlashlightOptionsDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Flashlight.
	 */
	private lateinit var flashlight: NacFlashlight

	/**
	 * Seekbar for the flashlight strength level.
	 */
	private lateinit var strengthSeekBar: SeekBar

	/**
	 * Checkbox indicating whether flashlight should blink or not.
	 */
	private lateinit var blinkCheckBox: MaterialCheckBox

	/**
	 * Question asking how long the flashlight should stay on/off when blinking.
	 */
	private lateinit var onOffDurationQuestion: TextView

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
	 * The alpha that views should have based on the should use text-to-speech
	 * flag.
	 */
	private val alpha: Float
		get() = if (blinkCheckBox.isChecked) 1.0f else 0.2f

	/**
	 * Called when the creating the view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?)
		: View?
	{
		return inflater.inflate(R.layout.dlg_flashlight, container, false)
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

		// Get the flashlight
		flashlight = NacFlashlight(requireContext())

		// Get the done, preview, and cancel buttons
		val doneButton = dialog!!.findViewById(R.id.ok_button) as MaterialButton
		val previewButton = dialog!!.findViewById(R.id.preview_button) as MaterialButton
		val cancelButton = dialog!!.findViewById(R.id.cancel_button) as MaterialButton

		// Get the default values
		val defaultStrength = alarm?.flashlightStrengthLevel ?: 0
		val defaultShouldBlink = alarm?.shouldBlinkFlashlight ?: false
		val defaultOnDuration = alarm?.flashlightOnDuration ?: "0"
		val defaultOffDuration = alarm?.flashlightOffDuration ?: "0"
		selectedBlinkOnDuration = if (defaultOnDuration == "0") "1.0" else defaultOnDuration
		selectedBlinkOffDuration = if (defaultOffDuration == "0") "1.0" else defaultOffDuration

		// Setup the views
		setupStrengthLevel(defaultStrength)
		setupBlinkCheckBox(defaultShouldBlink)
		setupBlinkOnOffDuration(defaultOnDuration, defaultOffDuration)
		setupBlinkOnOffDurationUsable()

		// Setup the ok button
		setupPrimaryButton(doneButton, listener = {

			// Cleanup the preview if it is running
			flashlight.cleanup()

			// Set the on/off duration based on if the flashlight should blink
			val onDuration = if (blinkCheckBox.isChecked) selectedBlinkOnDuration else "0"
			val offDuration = if (blinkCheckBox.isChecked) selectedBlinkOffDuration else "0"

			// Set the alarm attributes
			alarm?.flashlightStrengthLevel = strengthSeekBar.progress
			alarm?.shouldBlinkFlashlight = blinkCheckBox.isChecked
			alarm?.flashlightOnDuration = onDuration
			alarm?.flashlightOffDuration = offDuration

			// Save the change so that it is accessible in the previous dialog
			findNavController().previousBackStackEntry?.savedStateHandle?.set("YOYOYO", alarm)

			// Dismiss the dialog
			dismiss()

		})

		// Setup the preview button
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

		// Setup the cancel button
		setupSecondaryButton(cancelButton, listener = {

			// Cleanup the flashlight
			flashlight.cleanup()

			// Dismiss the dialog
			dismiss()

		})
	}

	/**
	 * Setup the flashlight blink checkbox.
	 */
	private fun setupBlinkCheckBox(default: Boolean)
	{
		// Get the views
		val relativeLayout = dialog!!.findViewById(R.id.should_flashlight_blink) as RelativeLayout
		val description = dialog!!.findViewById(R.id.description_should_flashlight_blink) as TextView
		blinkCheckBox = dialog!!.findViewById(R.id.should_flashlight_blink_checkbox) as MaterialCheckBox

		// Set the status of the checkbox
		blinkCheckBox.isChecked = default

		// Setup the checkbox
		blinkCheckBox.setupCheckBoxColor(sharedPreferences)

		// Setup the description
		setupBlinkDescription(description)

		// Set the listener
		relativeLayout.setOnClickListener {

			// Toggle the checkbox
			blinkCheckBox.isChecked = !blinkCheckBox.isChecked

			// Set the description
			setupBlinkDescription(description)

			// Set the usability of the blink on/off duration views
			setupBlinkOnOffDurationUsable()

			// Check if the flashlight is running
			if (flashlight.isRunning)
			{
				// Restart the flashlight
				startFlashlight()
			}
		}
	}

	/**
	 * Setup the description for whether flashlight should blink or not.
	 */
	private fun setupBlinkDescription(textView: TextView)
	{
		// Determine the text ID to use based on whether restrict volume will
		// be used or not
		val textId = if (blinkCheckBox.isChecked)
		{
			R.string.flashlight_blink_true
		}
		else
		{
			R.string.flashlight_blink_false
		}

		// Set the text
		textView.setText(textId)
	}

	/**
	 * Setup the flashlight blink on/off duration.
	 */
	private fun setupBlinkOnOffDuration(defaultOn: String, defaultOff: String)
	{
		// Get the views
		onOffDurationQuestion = dialog!!.findViewById(R.id.question_flashlight_on_off_duration)
		onOffDurationRelativeLayout = dialog!!.findViewById(R.id.flashlight_on_off_duration)
		onDurationInputLayout = dialog!!.findViewById(R.id.flashlight_on_duration_input_layout)
		offDurationInputLayout = dialog!!.findViewById(R.id.flashlight_off_duration_input_layout)
		val onDurationAutoCompleteTextView= dialog!!.findViewById(R.id.flashlight_on_duration_dropdown_menu) as MaterialAutoCompleteTextView
		val offDurationAutoCompleteTextView= dialog!!.findViewById(R.id.flashlight_off_duration_dropdown_menu) as MaterialAutoCompleteTextView

		// Setup the input layout
		onDurationInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		offDurationInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		val onIndex = NacAlarm.calcFlashlightOnOffDurationIndex(defaultOn)
		val offIndex = NacAlarm.calcFlashlightOnOffDurationIndex(defaultOff)
		onDurationAutoCompleteTextView.setTextFromIndex(onIndex)
		offDurationAutoCompleteTextView.setTextFromIndex(offIndex)

		// Set the textview listeners
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
	 * Setup whether the blink flashlight on/off container can be used or not.
	 */
	private fun setupBlinkOnOffDurationUsable()
	{
		// Set the alpha for the views
		onOffDurationQuestion.alpha = alpha
		onOffDurationRelativeLayout.alpha = alpha

		// Set whether it can be used or not
		onOffDurationRelativeLayout.isEnabled = blinkCheckBox.isChecked
		onDurationInputLayout.isEnabled = blinkCheckBox.isChecked
		offDurationInputLayout.isEnabled = blinkCheckBox.isChecked
	}

	/**
	 * Setup the strength level.
	 */
	private fun setupStrengthLevel(default: Int)
	{
		// Get the views
		val question = dialog!!.findViewById(R.id.question_flashlight_strength) as TextView
		val space = dialog!!.findViewById(R.id.space_flashlight_strength) as Space
		strengthSeekBar = dialog!!.findViewById(R.id.seekbar_flashlight_strength)

		// Check if this version does not support changing the flashlight level
		if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) || (flashlight.maxLevel == 1))
		{
			question.visibility = View.GONE
			space.visibility = View.GONE
			strengthSeekBar.visibility = View.GONE

			return
		}

		// Set the min and max seekbar value
		strengthSeekBar.min = flashlight.minLevel
		strengthSeekBar.max = flashlight.maxLevel

		// Set the strength level
		strengthSeekBar.progress = if (default == 0)
		{
			flashlight.maxLevel
		}
		else
		{
			default
		}

		// Set the listener
		strengthSeekBar.setOnSeekBarChangeListener(object: OnSeekBarChangeListener {

			/**
			 * Seek bar was changed.
			 */
			override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean)
			{
				// Change the flashlight strength
				flashlight.strengthLevel = progress
			}

			/**
			 * Seek bar was touched.
			 */
			override fun onStartTrackingTouch(seekBar: SeekBar) {}

			/**
			 * Seek bar stopped being touched.
			 */
			override fun onStopTrackingTouch(seekBar: SeekBar)
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
	 * Start the flashlight.
	 */
	private fun startFlashlight()
	{
		// Cleanup the flashlight
		flashlight.cleanup()

		// Check if blink handlers need to be setup
		if (blinkCheckBox.isChecked)
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

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacFlashlightOptionsDialog"

	}

}