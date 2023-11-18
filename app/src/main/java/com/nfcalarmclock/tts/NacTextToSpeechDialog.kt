package com.nfcalarmclock.tts

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.TextView
import com.google.android.material.checkbox.MaterialCheckBox
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment
import org.w3c.dom.Text

/**
 * Text to speech dialog asking the user what TTS settings they want.
 */
class NacTextToSpeechDialog
	: NacDialogFragment()
{

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacAlarmTextToSpeechDialog"

	}

	/**
	 * Listener for when text-to-speech options are selected.
	 */
	fun interface OnTextToSpeechOptionsSelectedListener
	{
		fun onTextToSpeechOptionsSelected(
			shouldSayCurrentTime: Boolean,
			shouldSayAlarmName: Boolean,
			ttsFreq: Int)
	}

	/**
	 * Default flag indicating whether the current time should be said or not.
	 */
	var defaultSayCurrentTime = false

	/**
	 * Default flag indicating whether the alarm name should be said or not.
	 */
	var defaultSayAlarmName = false

	/**
	 * Default text-to-speech frequency.
	 */
	var defaultTtsFrequency = 0

	/**
	 * Textview for the section title for the frequency.
	 */
	private var howOftenToSayTextView: TextView? = null

	/**
	 * Checkbox for setting whether the time should be said or not.
	 */
	private var currentTimeCheckBox: MaterialCheckBox? = null

	/**
	 * Checkbox for setting whether the alarm name should be said or not.
	 */
	private var alarmNameCheckBox: MaterialCheckBox? = null

	/**
	 * Scrollable picker to choose the text-to-speech frequency.
	 */
	private var frequencyPicker: NumberPicker? = null

	/**
	 * Listener for when a text-to-speech option and frequency is selected.
	 */
	var onTextToSpeechOptionsSelectedListener: OnTextToSpeechOptionsSelectedListener? = null

	/**
	 * Whether text-to-speech is being used or not.
	 */
	private val shouldUseTts: Boolean
		get() = currentTimeCheckBox!!.isChecked || alarmNameCheckBox!!.isChecked

	/**
	 * The alpha that views should have based on the should use text-to-speech
	 * flag.
	 */
	private val alpha: Float
		get() = if (shouldUseTts) 1.0f else 0.25f

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

				// Call the listener
				onTextToSpeechOptionsSelectedListener?.onTextToSpeechOptionsSelected(
					currentTimeCheckBox!!.isChecked,
					alarmNameCheckBox!!.isChecked,
					frequencyPicker!!.value)

			}
			.setNegativeButton(R.string.action_cancel, null)
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
		//val whatToSay = dialog!!.findViewById<TextView>(R.id.title_tts_what_to_say)
		howOftenToSayTextView = dialog!!.findViewById(R.id.title_tts_how_often_to_say)

		// Set the member variables
		currentTimeCheckBox = dialog!!.findViewById(R.id.say_current_time)
		alarmNameCheckBox = dialog!!.findViewById(R.id.say_alarm_name)
		frequencyPicker = dialog!!.findViewById(R.id.tts_frequency_picker)

		// Set the default checked status
		currentTimeCheckBox!!.isChecked = defaultSayCurrentTime
		alarmNameCheckBox!!.isChecked = defaultSayAlarmName

		// Setup the views
		//whatToSay.setTextColor(sharedPreferences!!.nameColor)
		//howOftenToSay.setTextColor(sharedPreferences!!.nameColor)
		setupCheckBoxColor()
		setupOnClickListener()
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

		// Set the state list of the checkboxes
		currentTimeCheckBox!!.buttonTintList = ColorStateList(states, colors)
		alarmNameCheckBox!!.buttonTintList = ColorStateList(states, colors)
	}

	/**
	 * Setup whether the text-to-speech frequency picker can be used or not.
	 */
	private fun setupFrequencyPickerUsable()
	{
		// Set the alpha of the frequency title
		howOftenToSayTextView!!.alpha = alpha

		// Set the alpha of the frequency picker
		frequencyPicker!!.alpha = alpha

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
	 * Setup the on click listeners.
	 */
	private fun setupOnClickListener()
	{
		// Set the on click listener for the current time checkbox container
		currentTimeCheckBox!!.setOnClickListener {

			// Enable/disable the frequency picker
			setupFrequencyPickerUsable()

		}

		// Set the on click listener for the current time checkbox container
		alarmNameCheckBox!!.setOnClickListener {

			// Enable/disable the frequency picker
			setupFrequencyPickerUsable()

		}
	}

}