package com.nfcalarmclock.alarm.options.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.nfcalarmclock.R
import com.nfcalarmclock.log.NacLog
import com.nfcalarmclock.system.media.NacAudioAttributes
import com.nfcalarmclock.system.media.requestFocusGainTransient
import com.nfcalarmclock.system.toBundle
import com.nfcalarmclock.view.quickToast
import java.util.Locale

/**
 * Text to speech.
 *
 * @param context Context.
 * @param onStartSpeaking Function to call when starting to speak.
 * @param onDoneSpeaking Function to call when done speaking.
 * @param onErrorSpeaking Function to call when an error occurred trying to speak.
 */
class NacTextToSpeech(
	context: Context,
	onInit: (TextToSpeech, Int) -> Unit = { _, _ -> },
	onStartSpeaking: () -> Unit = {},
	onDoneSpeaking: () -> Unit = {},
	onErrorSpeaking: () -> Unit = {
		quickToast(context, R.string.error_message_text_to_speech_audio_focus)
	},
	private val utteranceId: String = UTTERANCE_ID
)
{

	/**
	 * Utterance listener.
	 */
	class NacUtteranceListener(
		val onStartSpeaking: () -> Unit = {},
		val onDoneSpeaking: () -> Unit = {},
		val onErrorSpeaking: () -> Unit = {}
	) : UtteranceProgressListener()
	{

		/**
		 * Done speaking.
		 */
		override fun onDone(utteranceId: String)
		{
			onDoneSpeaking()
		}

		/**
		 * Starting to speak.
		 */
		override fun onStart(utteranceId: String)
		{
			onStartSpeaking()
		}

		/**
		 * Error speaking.
		 */
		@Deprecated("Deprecated in Java")
		override fun onError(utteranceId: String)
		{
			NacLog.e("On speaking error. utteranceId=$utteranceId")
			onErrorSpeaking()
		}

		/**
		 * Error speaking.
		 */
		override fun onError(utteranceId: String, errorCode: Int)
		{
			NacLog.e("On speaking error. utteranceId=$utteranceId | errorCode=$errorCode")
			onErrorSpeaking()
		}

	}

	/**
	 * Audio manager.
	 */
	val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

	/**
	 * The speech engine.
	 *
	 * The TextToSpeech.OnInitListener is the lambda.
	 */
	val textToSpeech: TextToSpeech = TextToSpeech(context) { status ->

		// Cleanup has been called before init is finished
		if (isCleanedUp)
		{
			return@TextToSpeech
		}

		// Set the initialization status
		isInitialized = (status == TextToSpeech.SUCCESS)

		// Initialization was a succes and there is a message in the buffer
		if (isInitialized && hasBuffer())
		{
			// Say what is in the buffer
			speak(bufferMessage, bufferAudioAttributes!!)
		}

		// Call the listener
		onInit(textToSpeech, status)

	}

	/**
	 * Message to buffer and speak when ready.
	 */
	private var bufferMessage: String = ""

	/**
	 * Audio attributes to buffer and use when ready.
	 */
	private var bufferAudioAttributes: NacAudioAttributes? = null

	/**
	 * Check if speech engine is initialized.
	 */
	var isInitialized: Boolean = false

	/**
	 * Whether the TTS resources have been cleaned up or not.
	 */
	var isCleanedUp: Boolean = false

	/**
	 * The utterance listener.
	 */
	private val utteranceListener: NacUtteranceListener = NacUtteranceListener(
		onStartSpeaking = onStartSpeaking,
		onDoneSpeaking = onDoneSpeaking,
		onErrorSpeaking = onErrorSpeaking
	)

	/**
	 * Constructor.
	 */
	init
	{
		// Setup text to speech listener
		textToSpeech.setOnUtteranceProgressListener(utteranceListener)
	}

	/**
	 * Cleanup any used resources.
	 */
	fun cleanup()
	{
		// Shutdown the TTS engine
		textToSpeech.shutdown()

		// Set the cleanup flag
		isCleanedUp = true
	}

	/**
	 * Clear the buffered message and audio attributes.
	 */
	private fun clearBuffer()
	{
		bufferMessage = ""
		bufferAudioAttributes = null
	}

	/**
	 * Check if there is a buffer present.
	 *
	 * @return True if there is a buffer present, and False otherwise.
	 */
	fun hasBuffer() : Boolean
	{
		return bufferMessage.isNotEmpty()
	}

	/**
	 * Check if the speech engine is running.
	 *
	 * @return True if the speech engine is running, and False otherwise. Will
	 *         also return True if the IllegalArgumentException is raised.
	 */
	fun isSpeaking() : Boolean
	{
		return try
		{
			this.isInitialized && textToSpeech.isSpeaking
		}
		catch (e: IllegalArgumentException)
		{
			NacLog.e("Unable to check whether TTS is speaking", throwable = e)
			true
		}
	}

	/**
	 * Speak the given text.
	 */
	fun speak(message: String, attrs: NacAudioAttributes)
	{
		// Cleanup has been called before init is finished
		if (isCleanedUp)
		{
			return
		}

		// TTS object is already initialized
		if (isInitialized)
		{
			// Gain transient audio focus
			if (!audioManager.requestFocusGainTransient(null, attrs))
			{
				// Clear the buffer, just in case
				clearBuffer()

				// Call error and done listeners
				utteranceListener.onErrorSpeaking()
				utteranceListener.onDoneSpeaking()
				return
			}

			// Set the language
			textToSpeech.language = Locale.getDefault()

			// Set the speech rate
			if (attrs.speechRate != 0f)
			{
				textToSpeech.setSpeechRate(attrs.speechRate)
			}

			// Set the voice
			if (attrs.voice.isNotEmpty())
			{
				textToSpeech.voices
					.find { it.name == attrs.voice }
					?.let { textToSpeech.voice = it }
			}

			// Create TTS audio attributes and bundle any TTS params
			val audioAttributes = AudioAttributes.Builder()
				.setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
				.setUsage(attrs.audioUsage)
				.build()
			val bundle = attrs.toBundle()

			// Speak the message
			textToSpeech.setAudioAttributes(audioAttributes)
			textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, bundle, utteranceId)

			// Clear the buffer
			clearBuffer()
		}
		// Buffer the message and audio attributes
		else
		{
			bufferMessage = message
			bufferAudioAttributes = attrs
		}
	}

	companion object
	{

		/**
		 * Utterance ID when speaking through the TTS engine.
		 */
		const val UTTERANCE_ID = "NacAlarmTts"

	}

}