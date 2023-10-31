package com.nfcalarmclock.tts

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.material.checkbox.MaterialCheckBox
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment

/**
 * Text to speech dialog asking the user what TTS settings they want.
 */
class NacTextToSpeechDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when text-to-speech options are selected.
	 */
	fun interface OnTextToSpeechOptionsSelectedListener
	{
		fun onTextToSpeechOptionsSelected(useTts: Boolean, freq: Int)
	}

	/**
	 * Default flag indicating whether text-to-speech should be used or not.
	 */
	var defaultUseTts = false

	/**
	 * Default text-to-speech frequency.
	 */
	var defaultTtsFrequency = 0

	/**
	 * Get whether text-to-speech is being used or not.
	 *
	 * @return True if text-to-speech is being used, and False otherwise.
	 */
	private val shouldUseTts: Boolean
		get() = checkBox!!.isChecked

	/**
	 * Checkbox for setting whether text-to-speech should be used or not.
	 */
	private var checkBox: MaterialCheckBox? = null

	/**
	 * Scrollable picker to choose the text-to-speech frequency.
	 */
	private var frequencyPicker: NumberPicker? = null

	/**
	 * Listener for when a text-to-speech option and frequency is selected.
	 */
	var onTextToSpeechOptionsSelectedListener: OnTextToSpeechOptionsSelectedListener? = null

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(R.string.title_tts)
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Get the frequency to run TTS at
				val freq = frequencyPicker!!.value

				// Call the listener
				onTextToSpeechOptionsSelectedListener?.onTextToSpeechOptionsSelected(
					shouldUseTts, freq)

			}
			.setNegativeButton(R.string.action_cancel) { _, _ ->
			}
			.setView(R.layout.dlg_alarm_text_to_speech)
			.create()
	}

	/**
	 * Called when the dialog is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Initialize the widgets
		val container = dialog!!.findViewById<RelativeLayout>(R.id.should_use_tts)
		val textView: TextView = dialog!!.findViewById(R.id.should_use_tts_summary)

		// Set the member variables
		checkBox = dialog!!.findViewById(R.id.should_use_tts_checkbox)
		frequencyPicker = dialog!!.findViewById(R.id.tts_frequency_picker)

		// Set the default checked status
		checkBox!!.isChecked = defaultUseTts

		// Setup the views
		setupOnClickListener(container, textView)
		setupCheckBoxColor()
		setupTextView(textView)
		setupFrequencyPickerValues()
		setupFrequencyPickerUsable()
	}

	/**
	 * Setup the color of the use text-to-speech checkbox.
	 */
	private fun setupCheckBoxColor()
	{
		// Get the colors for the boolean states
		val colors = intArrayOf(sharedPreferences!!.themeColor, Color.GRAY)

		// Get the IDs of the two states
		val states = arrayOf(intArrayOf(android.R.attr.state_checked),
			intArrayOf(-android.R.attr.state_checked))

		// Set the state list of the checkbox
		checkBox!!.buttonTintList = ColorStateList(states, colors)
	}

	/**
	 * Setup whether the text-to-speech frequency picker can be used or not.
	 */
	private fun setupFrequencyPickerUsable()
	{
		// Set the alpha of the frequency picker
		frequencyPicker!!.alpha = if (shouldUseTts)
		{
			1.0f
		}
		else
		{
			0.25f
		}

		// Set whether the frequency picker should be accessible or not
		frequencyPicker!!.isEnabled = shouldUseTts
	}

	/**
	 * Setup the values of the text-to-speech frequency picker.
	 */
	private fun setupFrequencyPickerValues()
	{
		// Get the max frequency size
		val values = requireContext().resources.getStringArray(R.array.tts_frequency).toList()

		// Setup the frequency picker
		frequencyPicker!!.minValue = 0
		frequencyPicker!!.maxValue = values.size - 1
		frequencyPicker!!.displayedValues = values.toTypedArray()
		frequencyPicker!!.value = defaultTtsFrequency
	}

	/**
	 * Setup the on click listener.
	 */
	private fun setupOnClickListener(container: RelativeLayout, textView: TextView)
	{
		// Set the on click listener for the whole checkbox container
		container.setOnClickListener {

			// Toggle the checkbox
			checkBox!!.isChecked = !shouldUseTts

			// Summary
			setupTextView(textView)

			// Enable/disable the frequency picker
			setupFrequencyPickerUsable()

		}
	}

	/**
	 * Setup the summary text for whether text-to-speech should be used or not.
	 */
	private fun setupTextView(textView: TextView)
	{
		// Determine the text ID to use based on whether TTS will be used or not
		val textId = if (shouldUseTts)
		{
			R.string.speak_to_me_true
		}
		else
		{
			R.string.speak_to_me_false
		}

		// Set the text
		textView.setText(textId)
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacAlarmTextToSpeechDialog"

	}

}