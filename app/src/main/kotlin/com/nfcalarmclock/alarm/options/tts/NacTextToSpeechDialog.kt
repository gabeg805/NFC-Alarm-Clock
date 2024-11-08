package com.nfcalarmclock.alarm.options.tts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.util.NacBundle
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupCheckBoxColor
import com.nfcalarmclock.view.setupInputLayoutColor

/**
 * Text to speech dialog asking the user what TTS settings they want.
 */
class NacTextToSpeechDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Checkbox for setting whether the time should be said or not.
	 */
	private lateinit var currentTimeCheckBox: MaterialCheckBox

	/**
	 * Checkbox for setting whether the alarm name should be said or not.
	 */
	private lateinit var alarmNameCheckBox: MaterialCheckBox

	/**
	 * Question above the text-to-speech frequency input layout.
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
	 * The alpha that views should have based on the should use text-to-speech
	 * flag.
	 */
	private val alpha: Float
		get() = if (shouldUseTts) 1.0f else 0.2f

	/**
	 * Called when the creating the view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.dlg_text_to_speech, container, false)
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

		// Get the views
		val okButton = dialog!!.findViewById(R.id.ok_button) as MaterialButton
		val cancelButton = dialog!!.findViewById(R.id.cancel_button) as MaterialButton

		// Get the default values
		val defaultSayCurrentTime = alarm?.sayCurrentTime ?: false
		val defaultSayAlarmName = alarm?.sayAlarmName ?: false
		val defaultTtsFreq = alarm?.ttsFrequency ?: 0
		selectedTtsFreq = defaultTtsFreq

		// Setup the views
		setupCurrentTimeCheckBox(defaultSayCurrentTime)
		setupAlarmNameCheckBox(defaultSayAlarmName)
		setupTtsFrequency(defaultTtsFreq)
		setupTtsFrequencyUsable()

		// Setup the ok button
		setupPrimaryButton(okButton, listener = {

			// Update the alarm attributes
			alarm?.sayCurrentTime = currentTimeCheckBox.isChecked
			alarm?.sayAlarmName = alarmNameCheckBox.isChecked
			alarm?.ttsFrequency = selectedTtsFreq

			// Save the change so that it is accessible in the previous dialog
			findNavController().previousBackStackEntry?.savedStateHandle?.set("YOYOYO", alarm)

			// Dismiss the dialog
			dismiss()

		})

		// Setup the cancel button
		setupSecondaryButton(cancelButton)
	}

	/**
	 * Setup the name of the alarm checkbox.
	 */
	private fun setupAlarmNameCheckBox(default: Boolean)
	{
		// Get the view
		alarmNameCheckBox = dialog!!.findViewById(R.id.say_alarm_name)

		// Set the default checked status
		alarmNameCheckBox.isChecked = default

		// Setup the views
		alarmNameCheckBox.setupCheckBoxColor(sharedPreferences)

		// Set the on click listener for the alarm name checkbox container
		alarmNameCheckBox.setOnClickListener {

			// Enable/disable the text-to-speech frequency input layout
			setupTtsFrequencyUsable()

		}
	}

	/**
	 * Setup the current time checkbox.
	 */
	private fun setupCurrentTimeCheckBox(default: Boolean)
	{
		// Get the view
		currentTimeCheckBox = dialog!!.findViewById(R.id.say_current_time)

		// Set the default checked status
		currentTimeCheckBox.isChecked = default

		// Setup the color
		currentTimeCheckBox.setupCheckBoxColor(sharedPreferences)

		// Set the on click listener for the current time checkbox container
		currentTimeCheckBox.setOnClickListener {

			// Enable/disable the text-to-speech frequency input layout
			setupTtsFrequencyUsable()

		}
	}

	/**
	 * Setup the text-to-speech frequency views.
	 */
	private fun setupTtsFrequency(default: Int)
	{
		// Set the member variables
		ttsFreqDescription = dialog!!.findViewById(R.id.title_tts_how_often_to_say)
		ttsFreqInputLayout = dialog!!.findViewById(R.id.tts_frequency_input_layout)
		val autoCompleteTextView = dialog!!.findViewById(R.id.tts_frequency_dropdown_menu) as MaterialAutoCompleteTextView

		// Setup the input layout
		ttsFreqInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		autoCompleteTextView.setTextFromIndex(default)

		// Set the textview listeners
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedTtsFreq = position
		}
	}

	/**
	 * Setup whether the text-to-speech frequency can be selected or not.
	 */
	private fun setupTtsFrequencyUsable()
	{
		// Set the alpha of the views
		ttsFreqDescription.alpha = alpha
		ttsFreqInputLayout.alpha = alpha

		// Set whether the frequency can be selected or not
		ttsFreqInputLayout.isEnabled = shouldUseTts
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacAlarmTextToSpeechDialog"

	}

}