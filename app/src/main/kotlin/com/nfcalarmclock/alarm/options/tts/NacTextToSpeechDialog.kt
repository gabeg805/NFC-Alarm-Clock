package com.nfcalarmclock.alarm.options.tts

import android.widget.AdapterView
import android.widget.TextView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupCheckBoxColor
import com.nfcalarmclock.view.setupInputLayoutColor

/**
 * Text to speech dialog asking the user what TTS settings they want.
 */
class NacTextToSpeechDialog
	: NacGenericAlarmOptionsDialog()
{

	/**
	 * Layout resourec ID.
	 */
	override val layoutId = R.layout.dlg_text_to_speech

	/**
	 * Say current time checkbox.
	 */
	private lateinit var currentTimeCheckBox: MaterialCheckBox

	/**
	 * Say alarm name checkbox.
	 */
	private lateinit var alarmNameCheckBox: MaterialCheckBox

	/**
	 * Title for the text-to-speech frequency.
	 */
	private lateinit var ttsFreqTitle: TextView

	/**
	 * Description for the text-to-speech frequency.
	 */
	private lateinit var ttsFreqDescription: TextView

	/**
	 * Input layout to select the text-to-speech frequency.
	 */
	private lateinit var ttsFreqInputLayout: TextInputLayout

	/**
	 * Selected text-to-speech frequency.
	 */
	private var selectedTtsFreq: Int = 0

	/**
	 * Whether text-to-speech is being used or not.
	 */
	private val shouldUseTts: Boolean
		get() = currentTimeCheckBox.isChecked || alarmNameCheckBox.isChecked

	/**
	 * Called when the Ok button is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// Update the alarm
		alarm?.sayCurrentTime = currentTimeCheckBox.isChecked
		alarm?.sayAlarmName = alarmNameCheckBox.isChecked
		alarm?.ttsFrequency = selectedTtsFreq
	}

	/**
	 * Set whether the text-to-speech frequency can be selected or not.
	 */
	private fun setTtsSpeakFrequencyUsability()
	{
		// Get the state and alpha
		val state = shouldUseTts
		val alpha = calcAlpha(state)

		// Set the alpha of the views
		ttsFreqTitle.alpha = alpha
		ttsFreqDescription.alpha = alpha
		ttsFreqInputLayout.alpha = alpha

		// Set whether the frequency can be selected or not
		ttsFreqInputLayout.isEnabled = state
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		// Get the default values
		val defaultSayCurrentTime = alarm?.sayCurrentTime ?: false
		val defaultSayAlarmName = alarm?.sayAlarmName ?: false
		val defaultTtsFreq = alarm?.ttsFrequency ?: 0
		selectedTtsFreq = defaultTtsFreq

		// Setup the views
		setupTtsWhatToSay(defaultSayCurrentTime, defaultSayAlarmName)
		setupTtsSpeakFrequency(defaultTtsFreq)
		setTtsSpeakFrequencyUsability()
	}

	/**
	 * Setup the what to say option views.
	 */
	private fun setupTtsWhatToSay(defaultSayCurrentTime: Boolean, defaultSayAlarmName: Boolean)
	{
		// Get the views
		currentTimeCheckBox = dialog!!.findViewById(R.id.tts_say_current_time)
		alarmNameCheckBox = dialog!!.findViewById(R.id.tts_say_alarm_name)

		// Setup the default status
		currentTimeCheckBox.isChecked = defaultSayCurrentTime
		alarmNameCheckBox.isChecked = defaultSayAlarmName

		// Setup the colors
		currentTimeCheckBox.setupCheckBoxColor(sharedPreferences)
		alarmNameCheckBox.setupCheckBoxColor(sharedPreferences)

		// Set the on click listener for the current time checkbox container
		currentTimeCheckBox.setOnClickListener {
			setTtsSpeakFrequencyUsability()
		}

		// Set the on click listener for the alarm name checkbox container
		alarmNameCheckBox.setOnClickListener {
			setTtsSpeakFrequencyUsability()
		}
	}

	/**
	 * Setup the speak frequency views.
	 */
	private fun setupTtsSpeakFrequency(default: Int)
	{
		// Set the member variables
		val autoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.tts_frequency_dropdown_menu)
		ttsFreqTitle = dialog!!.findViewById(R.id.tts_frequency_title)
		ttsFreqDescription = dialog!!.findViewById(R.id.tts_frequency_description)
		ttsFreqInputLayout = dialog!!.findViewById(R.id.tts_frequency_input_layout)

		// Setup the input layout
		ttsFreqInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		autoCompleteTextView.setTextFromIndex(default)

		// Set the textview listeners
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedTtsFreq = position
		}
	}

}