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
		get() = shouldUseTtsCheckBox!!.isChecked

	/**
	 * Checkbox for setting whether text-to-speech should be used or not.
	 */
	private var shouldUseTtsCheckBox: MaterialCheckBox? = null

	/**
	 * Summary text for whether text-to-speech should be used or not.
	 */
	private var shouldUseTtsSummary: TextView? = null

	/**
	 * Scrollable picker to choose the text-to-speech frequency.
	 */
	private var ttsFrequencyPicker: NumberPicker? = null

	/**
	 * Listener for when a text-to-speech option and frequency is selected.
	 */
	var onTextToSpeechOptionsSelectedListener: OnTextToSpeechOptionsSelectedListener? = null

	/**
	 * Call the OnTextToSpeechOptionsSelectedListener object, if it has been set.
	 */
	private fun callOnTextToSpeechOptionsSelectedListener()
	{
		// Get the frequency to run TTS at
		val freq = ttsFrequencyPicker!!.value

		// Call the listener
		onTextToSpeechOptionsSelectedListener?.onTextToSpeechOptionsSelected(
			shouldUseTts, freq)
	}

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Get the name of the title
		val title = getString(R.string.title_tts)

		// Get the name of the actions
		val ok = getString(R.string.action_ok)
		val cancel = getString(R.string.action_cancel)

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(title)
			.setPositiveButton(ok) { _, _ ->

				// Call the listener
				callOnTextToSpeechOptionsSelectedListener()

			}
			.setNegativeButton(cancel) { _, _ ->
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
		//val dialog = dialog as AlertDialog?
		val useTtsContainer = dialog!!.findViewById<RelativeLayout>(R.id.should_use_tts)

		// Set the member variable widgets
		shouldUseTtsCheckBox = dialog!!.findViewById(R.id.should_use_tts_checkbox)
		shouldUseTtsSummary = dialog!!.findViewById(R.id.should_use_tts_summary)
		ttsFrequencyPicker = dialog!!.findViewById(R.id.tts_frequency_picker)

		// Set the on click listener for the whole checkbox container
		useTtsContainer.setOnClickListener {
			toggleShouldUseTts()
			setupShouldUseTtsSummary()
			setupTtsFrequencyEnabled()
		}

		// Setup the dialog and widgets
		setupShouldUseTts()
		setupTtsFrequencyPicker()
		setupTtsFrequencyEnabled()
		setupShouldUseTtsColor()
	}

	/**
	 * Setup the summary text for whether text-to-speech should be used or not.
	 */
	private fun setupShouldUseTts()
	{
		// Set whether the check box is checked or not
		shouldUseTtsCheckBox!!.isChecked = defaultUseTts

		// Setup the summary based on the checkbox status
		setupShouldUseTtsSummary()
	}

	/**
	 * Setup the summary text for whether text-to-speech should be used or not.
	 */
	private fun setupShouldUseTtsColor()
	{
		// Get the colors for the boolean states
		val colors = intArrayOf(sharedPreferences!!.themeColor, Color.GRAY)

		// Get the IDs of the two states
		val states = arrayOf(intArrayOf(android.R.attr.state_checked),
			intArrayOf(-android.R.attr.state_checked))

		// Set the state list of the checkbox
		shouldUseTtsCheckBox!!.buttonTintList = ColorStateList(states, colors)
	}

	/**
	 * Setup the summary text for whether text-to-speech should be used or not.
	 */
	private fun setupShouldUseTtsSummary()
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
		shouldUseTtsSummary!!.setText(textId)
	}

	/**
	 * Setup whether the text-to-speech frequency container can be used or not.
	 */
	private fun setupTtsFrequencyEnabled()
	{
		// Set the alpha of the frequency picker
		ttsFrequencyPicker!!.alpha = if (shouldUseTts)
		{
			1.0f
		}
		else
		{
			0.25f
		}

		// Set whether the frequency picker should be accessible or not
		ttsFrequencyPicker!!.isEnabled = shouldUseTts
	}

	/**
	 * Setup scrollable picker for the text-to-speech frequency.
	 */
	private fun setupTtsFrequencyPicker()
	{
		// Get the max frequency size
		val cons = sharedConstants
		val values = cons.textToSpeechFrequency

		// Setup the frequency picker
		ttsFrequencyPicker!!.minValue = 0
		ttsFrequencyPicker!!.maxValue = values.size - 1
		ttsFrequencyPicker!!.displayedValues = values.toTypedArray()
		ttsFrequencyPicker!!.value = defaultTtsFrequency
	}

	/**
	 * Toggle whether text-to-speech is being used or not.
	 */
	private fun toggleShouldUseTts()
	{
		shouldUseTtsCheckBox!!.isChecked = !shouldUseTts
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacAlarmTextToSpeechDialog"

	}

}