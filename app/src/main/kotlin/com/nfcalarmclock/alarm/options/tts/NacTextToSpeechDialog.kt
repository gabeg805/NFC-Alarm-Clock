package com.nfcalarmclock.alarm.options.tts

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.AdapterView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.util.media.NacAudioAttributes
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupCheckBoxColor
import com.nfcalarmclock.view.setupInputLayoutColor
import java.util.Locale

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
	 * Preview button.
	 */
	private lateinit var previewButton: MaterialButton

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
	 * Title for the text-to-speech voice.
	 */
	private lateinit var ttsVoiceTitle: TextView

	/**
	 * Description for the text-to-speech voice.
	 */
	private lateinit var ttsVoiceDescription: TextView

	/**
	 * Input layout to select the text-to-speech voice.
	 */
	private lateinit var ttsVoiceInputLayout: TextInputLayout

	/**
	 * Dropdown menu to select the text-to-speech voice.
	 */
	private lateinit var ttsVoiceAutoCompleteTextView: MaterialAutoCompleteTextView

	/**
	 * Text-to-speech helper.
	 */
	private lateinit var ttsHelper: NacTextToSpeech

	/**
	 * All text-to-speech voices.
	 */
	private lateinit var allVoices: List<Voice>

	/**
	 * Selected text-to-speech frequency.
	 */
	private var selectedTtsFreq: Int = 0

	/**
	 * Selected text-to-speech voice.
	 */
	private var selectedTtsVoice: Int = 0

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
		alarm?.shouldSayCurrentTime = currentTimeCheckBox.isChecked
		alarm?.shouldSayAlarmName = alarmNameCheckBox.isChecked
		alarm?.ttsFrequency = selectedTtsFreq
		alarm?.ttsVoice = allVoices[selectedTtsVoice].name
	}

	/**
	 * Set the preview button text.
	 */
	private fun setPreviewText(state: Boolean)
	{
		// Check if preview is running
		if (state)
		{
			// Change the text of the button back
			previewButton.text = resources.getString(R.string.action_preview)
		}
		// Preview not running
		else
		{
			// Change the text of the button to indicate that a preview is running
			previewButton.text = resources.getString(R.string.action_stop_preview)
		}
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
	 * Set whether the text-to-speech voice can be selected or not.
	 */
	private fun setTtsVoiceUsability()
	{
		// Get the state and alpha
		val state = shouldUseTts
		val alpha = calcAlpha(state)

		// Set the alpha of the views
		ttsVoiceTitle.alpha = alpha
		ttsVoiceDescription.alpha = alpha
		ttsVoiceInputLayout.alpha = alpha

		// Set whether the frequency can be selected or not
		ttsVoiceInputLayout.isEnabled = state
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		// Get the alarm, or build a new one, to get default values
		val a = alarm ?: NacAlarm.build()
		selectedTtsFreq = a.ttsFrequency

		// Setup the views
		setupTtsHelper(a.ttsVoice)
		setupTtsWhatToSay(a.shouldSayCurrentTime, a.shouldSayAlarmName)
		setupTtsSpeakFrequency(a.ttsFrequency)
		setupTtsVoice()
		setTtsSpeakFrequencyUsability()
		setTtsVoiceUsability()
	}

	/**
	 * Setup any extra buttons.
	 */
	override fun setupExtraButtons(alarm: NacAlarm?)
	{
		// Get the button
		previewButton = dialog!!.findViewById(R.id.preview_button)

		// Setup the button
		setupSecondaryButton(previewButton, listener = {

			val state = ttsHelper.isSpeaking()

			// Set the button text
			setPreviewText(state)

			// Check if preview is running
			if (state)
			{
				// Cleanup the preview
				ttsHelper.textToSpeech.stop()
			}
			// Preview not running
			else
			{
				// Get the alarm name
				val name = alarm?.name?.takeIf { it.isNotEmpty() } ?: ""

				// Get the text-to-speech phrase
				val context = requireContext()
				val phrase = NacTranslate.getTtsPhrase(context, currentTimeCheckBox.isChecked,
					alarmNameCheckBox.isChecked, name)

				// Get the audio attributes
				val attrs = NacAudioAttributes(context)
					.apply {
						voice = allVoices[selectedTtsVoice].name
					}

				// Start the preview
				ttsHelper.speak(phrase, attrs)
			}

		})
	}

	/**
	 * Setup the text-to-speech helper.
	 */
	private fun setupTtsHelper(default: String)
	{
		// Get the text-to-speech helper
		val context = requireContext()
		val locale = Locale.getDefault()
		ttsHelper = NacTextToSpeech(context, object: NacTextToSpeech.OnSpeakingListener {

			/**
			 * Called when speech engine is done speaking.
			 */
			override fun onDoneSpeaking()
			{
				setPreviewText(true)
			}

			/**
			 * Called when speech engine has started speaking.
			 */
			override fun onStartSpeaking()
			{
			}

		})

		// Set init listener
		ttsHelper.onInitializedListener = NacTextToSpeech.OnInitializedListener { tts, _ ->

			// Filter out voices that are not installed and do not match the current locale
			allVoices = tts.voices
				.filter { v ->
					(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in v.features)
						&& (v.locale == locale)
				}
				.partition { it == ttsHelper.textToSpeech.defaultVoice }
				.let { it.first + it.second }

			// Set the currently selected voice
			val defaultVoice = default.ifEmpty { ttsHelper.textToSpeech.defaultVoice.name }
			selectedTtsVoice = allVoices
				.indexOfFirst { it.name == defaultVoice }
				.takeIf { it >= 0 } ?: 0

			// Get the items to use in the dropdown
			val size = if (allVoices.isNotEmpty()) allVoices.size else 1
			val simpleItems = List(size) { i ->
				if (i == 0)
				{
					context.getString(R.string.message_text_to_speech_default_voice)
				}
				else
				{
					context.getString(R.string.message_text_to_speech_voice, i+1)
				}
			}.toTypedArray()

			// Setup the voices
			ttsVoiceAutoCompleteTextView.setSimpleItems(simpleItems)
			ttsVoiceAutoCompleteTextView.setText(simpleItems[selectedTtsVoice], false)

		}
	}

	/**
	 * Setup the speak frequency views.
	 */
	private fun setupTtsSpeakFrequency(default: Int)
	{
		// Get the views
		val autoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.tts_frequency_dropdown_menu)
		ttsFreqTitle = dialog!!.findViewById(R.id.tts_frequency_title)
		ttsFreqDescription = dialog!!.findViewById(R.id.tts_frequency_description)
		ttsFreqInputLayout = dialog!!.findViewById(R.id.tts_frequency_input_layout)

		// Setup the views
		ttsFreqInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		autoCompleteTextView.setTextFromIndex(default)

		// Set the textview listeners
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedTtsFreq = position
		}
	}

	/**
	 * Setup the voice views.
	 */
	private fun setupTtsVoice()
	{
		// Get the views
		ttsVoiceTitle = dialog!!.findViewById(R.id.tts_voice_title)
		ttsVoiceDescription = dialog!!.findViewById(R.id.tts_voice_description)
		ttsVoiceInputLayout = dialog!!.findViewById(R.id.tts_voice_input_layout)
		ttsVoiceAutoCompleteTextView = dialog!!.findViewById(R.id.tts_voice_dropdown_menu)

		// Setup the views
		ttsVoiceInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the textview listeners
		ttsVoiceAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedTtsVoice = position
		}
	}

	/**
	 * Setup the what to say option views.
	 */
	private fun setupTtsWhatToSay(defaultSayCurrentTime: Boolean, defaultSayAlarmName: Boolean)
	{
		// Get the views
		currentTimeCheckBox = dialog!!.findViewById(R.id.tts_say_current_time)
		alarmNameCheckBox = dialog!!.findViewById(R.id.tts_say_alarm_name)

		// Setup the views
		currentTimeCheckBox.isChecked = defaultSayCurrentTime
		alarmNameCheckBox.isChecked = defaultSayAlarmName

		currentTimeCheckBox.setupCheckBoxColor(sharedPreferences)
		alarmNameCheckBox.setupCheckBoxColor(sharedPreferences)

		// Set the on click listener for the current time checkbox container
		currentTimeCheckBox.setOnClickListener {
			setTtsSpeakFrequencyUsability()
			setTtsVoiceUsability()
		}

		// Set the on click listener for the alarm name checkbox container
		alarmNameCheckBox.setOnClickListener {
			setTtsSpeakFrequencyUsability()
			setTtsVoiceUsability()
		}
	}

}