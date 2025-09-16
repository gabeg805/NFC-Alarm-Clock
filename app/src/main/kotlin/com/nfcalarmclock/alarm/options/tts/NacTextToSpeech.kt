package com.nfcalarmclock.alarm.options.tts

import android.content.Context
import android.media.AudioManager.OnAudioFocusChangeListener
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.nfcalarmclock.R
import com.nfcalarmclock.util.NacUtility
import com.nfcalarmclock.util.media.NacAudioAttributes
import com.nfcalarmclock.util.media.NacAudioManager
import com.nfcalarmclock.system.toBundle
import java.util.Locale

/**
 * Text to speech.
 */
class NacTextToSpeech(

	/**
	 * Context.
	 */
	private val context: Context,

	/**
	 * Listener for when TTS is speaking.
	 */
	listener: OnSpeakingListener? = null

	// Interface
) : TextToSpeech.OnInitListener
{

	/**
	 * On initialized listener.
	 */
	fun interface OnInitializedListener
	{
		fun onInitialized(tts: TextToSpeech, status: Int)
	}

	/**
	 * On speaking listener.
	 */
	interface OnSpeakingListener
	{

		/**
		 * Called when speech engine is done speaking.
		 */
		fun onDoneSpeaking()

		/**
		 * Called when speech engine has started speaking.
		 */
		fun onStartSpeaking()

	}

	/**
	 * Utterance listener.
	 */
	class NacUtteranceListener(

		/**
		 * Context.
		 */
		private val context: Context,

		/**
		 * Audio focus change listener.
		 */
		private val onAudioFocusChangeListener: OnAudioFocusChangeListener?

		// Constructor
	) : UtteranceProgressListener()
	{

		/**
		 * On speaking listener.
		 */
		var onSpeakingListener: OnSpeakingListener? = null

		/**
		 * Called when done speaking.
		 */
		override fun onDone(utteranceId: String)
		{
			// Call done speaking listener
			onSpeakingListener?.onDoneSpeaking()

			// Abandon audio focus
			NacAudioManager.abandonFocus(context, onAudioFocusChangeListener)
		}

		/**
		 * Called when starting to speak.
		 */
		override fun onStart(utteranceId: String)
		{
			// Call the start speaking listener
			onSpeakingListener?.onStartSpeaking()
		}

		/**
		 */
		@Deprecated("Deprecated in Java")
		override fun onError(utteranceId: String)
		{
			println("On speaking error : $utteranceId")
		}

		/**
		 */
		override fun onError(utteranceId: String, errorCode: Int)
		{
			println("On speaking error : $utteranceId | $errorCode")
		}

	}

	/**
	 * The speech engine.
	 */
	val textToSpeech: TextToSpeech = TextToSpeech(context, this)

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
	 * The utterance listener.
	 */
	private val utteranceListener: NacUtteranceListener =
		NacUtteranceListener(context, null)

	/**
	 * On initialized listener.
	 */
	var onInitializedListener: OnInitializedListener? = null
		set(value)
		{
			field = value

			// Call the listener if already initialized
			if (isInitialized)
			{
				onInitializedListener?.onInitialized(textToSpeech, TextToSpeech.SUCCESS)
			}
		}

	/**
	 * Constructor.
	 */
	init
	{
		// Set the speaking listener
		utteranceListener.onSpeakingListener = listener

		// Setup text to speech
		textToSpeech.setOnUtteranceProgressListener(utteranceListener)
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
		catch (_: IllegalArgumentException)
		{
			true
		}
	}

	/**
	 * The TextToSpeech engine is done being initialized.
	 */
	override fun onInit(status: Int)
	{
		// Set the initialization status
		isInitialized = (status == TextToSpeech.SUCCESS)

		// Initialization was a succes and there is a message in the buffer
		if (isInitialized && hasBuffer())
		{
			// Say what is in the buffer
			speak(bufferMessage, bufferAudioAttributes!!)
		}

		// Call the listener
		onInitializedListener?.onInitialized(textToSpeech, status)
	}

	/**
	 * Speak the given text.
	 */
	fun speak(message: String, attrs: NacAudioAttributes)
	{
		// TTS object is already initialized
		if (isInitialized)
		{
			// Gain transient audio focus
			if (!NacAudioManager.requestFocusGainTransient(context, null, attrs))
			{
				// Show toast
				NacUtility.quickToast(context, R.string.error_message_text_to_speech_audio_focus)

				// Clear the buffer, just in case
				clearBuffer()

				// Call listener that speaking is done
				utteranceListener.onSpeakingListener?.onDoneSpeaking()
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

			// Speak the message
			val androidAttrs = attrs.audioAttributes.audioAttributesV21.audioAttributes
			val bundle = attrs.toBundle()

			textToSpeech.setAudioAttributes(androidAttrs)
			textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, bundle,
				UTTERANCE_ID)

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