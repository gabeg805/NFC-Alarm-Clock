package com.nfcalarmclock.alarm.options.tts

import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.slider.Slider
import com.google.android.material.slider.Slider.OnSliderTouchListener
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
import com.nfcalarmclock.view.setupProgressAndThumbColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
	 * Requirement description for the text-to-speech voice.
	 */
	private lateinit var ttsVoiceRequirement: TextView

	/**
	 * Title for the text-to-speech speech rate.
	 */
	private lateinit var ttsSpeechRateTitle: TextView

	/**
	 * Description for the text-to-speech speech rate.
	 */
	private lateinit var ttsSpeechRateDescription: TextView

	/**
	 * Slider for the text-to-speech speech rate.
	 */
	private lateinit var ttsSpeechRateSlider: Slider

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
	 * Selected text-to-speech voice index.
	 */
	private var selectedTtsVoiceIndex: Int = 0

	/**
	 * Selected text-to-speech voice name.
	 */
	private val selectedTtsVoiceName: String
		get()
		{
			return allVoices.getOrNull(selectedTtsVoiceIndex)?.name ?: ""
		}

	/**
	 * Whether text-to-speech is being used or not.
	 */
	private val shouldUseTts: Boolean
		get() = currentTimeCheckBox.isChecked || alarmNameCheckBox.isChecked

	/**
	 * Called when the Cancel button is clicked.
	 */
	override fun onCancelClicked(alarm: NacAlarm?)
	{
		// Shutdown the text-to-speech resource
		ttsHelper.textToSpeech.shutdown()
	}

	/**
	 * Called when the Ok button is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// Update the alarm
		alarm?.shouldSayCurrentTime = currentTimeCheckBox.isChecked
		alarm?.shouldSayAlarmName = alarmNameCheckBox.isChecked
		alarm?.ttsFrequency = selectedTtsFreq
		alarm?.ttsSpeechRate = ttsSpeechRateSlider.value
		alarm?.ttsVoice = selectedTtsVoiceName

		// Shutdown the text-to-speech resource
		ttsHelper.textToSpeech.shutdown()
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
	 * Set whether the text-to-speech speech rate can be selected or not.
	 */
	private fun setTtsSpeechRateUsability()
	{
		// Get the state and alpha
		val state = shouldUseTts
		val alpha = calcAlpha(state)

		// Set the alpha of the views
		ttsSpeechRateTitle.alpha = alpha
		ttsSpeechRateDescription.alpha = alpha
		ttsSpeechRateSlider.alpha = alpha

		// Set whether the slider can be selected or not
		ttsSpeechRateSlider.isEnabled = state
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
		ttsVoiceRequirement.alpha = alpha

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
		setupTtsSpeechRate(a.ttsSpeechRate, a.name)
		setTtsSpeakFrequencyUsability()
		setTtsSpeechRateUsability()
		setTtsVoiceUsability()
	}

	/**
	 * Setup all the voices list.
	 */
	private fun setupAllVoices(tts: TextToSpeech)
	{
		// Get the default locale
		val locale = Locale.getDefault()

		// Filter out voices that do not match the current locale
		allVoices = tts.voices
			.filter { it.locale == locale }
			.partition { it == ttsHelper.textToSpeech.defaultVoice }
			.let { it.first + it.second }
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

				// Start TTS
				startTextToSpeech(name)
			}

		})
	}

	/**
	 * Setup the text-to-speech helper.
	 */
	private fun setupTtsHelper(default: String)
	{
		// Get the context and handler
		val context = requireContext()
		val handler = Handler(context.mainLooper)

		// Get the text-to-speech helper
		ttsHelper = NacTextToSpeech(context, object: NacTextToSpeech.OnSpeakingListener {

			/**
			 * Called when speech engine is done speaking.
			 */
			override fun onDoneSpeaking()
			{
				lifecycleScope.launch {
					withContext(Dispatchers.Main)
					{
						setPreviewText(true)
					}
				}
			}

			/**
			 * Called when speech engine has started speaking.
			 */
			override fun onStartSpeaking()
			{
			}

		})

		// Set the init listener on a delay so that the ok/cancel/preview buttons show up
		// immediately instead of after a noticeable lag
		handler.postDelayed({
			ttsHelper.onInitializedListener = NacTextToSpeech.OnInitializedListener { tts, _ ->
				setupAllVoices(tts)
				setupTtsVoiceDropdownItems(default)
			}
		}, 250)

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
	 * Setup the speech rate views.
	 */
	private fun setupTtsSpeechRate(speechRate: Float, name: String)
	{
		// Get the views
		ttsSpeechRateTitle = dialog!!.findViewById(R.id.tts_speech_rate_title)
		ttsSpeechRateDescription = dialog!!.findViewById(R.id.tts_speech_rate_description)
		ttsSpeechRateSlider = dialog!!.findViewById(R.id.tts_speech_rate_slider)

		// Setup the view
		ttsSpeechRateSlider.setupProgressAndThumbColor(sharedPreferences)
		ttsSpeechRateSlider.value = speechRate

		// Set the touch listener
		ttsSpeechRateSlider.addOnSliderTouchListener(object: OnSliderTouchListener
		{

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
				// Restart TTS if it is running
				if (ttsHelper.isSpeaking())
				{
					startTextToSpeech(name)
				}
			}

		})
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
		ttsVoiceRequirement = dialog!!.findViewById(R.id.tts_voice_requirement)
		val defaultVoice = requireContext().getString(R.string.message_text_to_speech_default_voice)

		// Setup the views
		ttsVoiceInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		ttsVoiceAutoCompleteTextView.setSimpleItems(listOf(defaultVoice).toTypedArray())
		ttsVoiceAutoCompleteTextView.setText(defaultVoice, false)

		// Set the item click listener
		ttsVoiceAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->

			// Set the selected index
			selectedTtsVoiceIndex = position

			// Set the visibility of the network requirement text, if voices were found initially
			if (allVoices.isNotEmpty())
			{
				ttsVoiceRequirement.visibility =
					if (allVoices[position].isNetworkConnectionRequired) View.VISIBLE else View.INVISIBLE
			}
		}
	}

	/**
	 * Setup the text-to-speech voice dropdown items.
	 */
	private fun setupTtsVoiceDropdownItems(default: String)
	{
		// Get the context
		val context = requireContext()

		// Set the currently selected voice
		val defaultVoice =
			default.ifEmpty { ttsHelper.textToSpeech.defaultVoice?.name ?: "" }
		selectedTtsVoiceIndex = allVoices
			.indexOfFirst { it.name == defaultVoice }
			.takeIf { it >= 0 } ?: 0

		// Get the items to use in the dropdown
		val simpleItems = if (allVoices.isNotEmpty())
		{
			// Create a list of all the voices
			List(allVoices.size) { i ->

				// Make the first index the default voice
				if (i == 0)
				{
					context.getString(R.string.message_text_to_speech_default_voice)
				}
				// Every other voice is "Voice #"
				else
				{
					context.getString(R.string.message_text_to_speech_voice, i + 1)
				}
			}
		}
		// Make a list with one item
		else
		{
			// Default voice
			if (defaultVoice.isNotEmpty())
			{
				listOf(context.getString(R.string.message_text_to_speech_default_voice))
			}
			// None
			else
			{
				listOf(context.getString(R.string.none))
			}
		}

		// Setup the voices
		ttsVoiceAutoCompleteTextView.setSimpleItems(simpleItems.toTypedArray())
		ttsVoiceAutoCompleteTextView.setText(simpleItems[selectedTtsVoiceIndex],
			false)

		// Set the visibility if network is required or not
		ttsVoiceRequirement.visibility =
			if (allVoices.isNotEmpty() && allVoices[selectedTtsVoiceIndex].isNetworkConnectionRequired)
			{
				View.VISIBLE
			}
			else
			{
				View.INVISIBLE
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
			setTtsSpeechRateUsability()
			setTtsVoiceUsability()
		}

		// Set the on click listener for the alarm name checkbox container
		alarmNameCheckBox.setOnClickListener {
			setTtsSpeakFrequencyUsability()
			setTtsSpeechRateUsability()
			setTtsVoiceUsability()
		}
	}

	/**
	 * Start the text-to-speech engine.
	 */
	private fun startTextToSpeech(name: String = "")
	{
		// Stop speaking if currently speaking
		if (ttsHelper.isSpeaking())
		{
			// Cleanup the preview
			ttsHelper.textToSpeech.stop()
		}

		// Get the text-to-speech phrase
		val context = requireContext()
		val phrase = NacTranslate.getTtsPhrase(context, currentTimeCheckBox.isChecked,
			alarmNameCheckBox.isChecked, name)

		// Get the audio attributes
		val attrs = NacAudioAttributes(context)
			.apply {
				speechRate = ttsSpeechRateSlider.value
				voice = selectedTtsVoiceName
			}

		// Start the preview
		ttsHelper.speak(phrase, attrs)
	}

}