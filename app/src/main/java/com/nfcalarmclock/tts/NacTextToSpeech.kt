package com.nfcalarmclock.tts

import android.content.Context
import android.media.AudioManager.OnAudioFocusChangeListener
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.nfcalarmclock.R
import com.nfcalarmclock.media.NacAudioAttributes
import com.nfcalarmclock.media.NacAudioManager
import com.nfcalarmclock.util.NacBundle
import com.nfcalarmclock.util.NacUtility
import java.util.Locale

/**
 * Text to speech.
 */
class NacTextToSpeech(context: Context, listener: OnSpeakingListener?) :
	TextToSpeech.OnInitListener
{

	companion object
	{

		/**
		 * Utterance ID when speaking through the TTS engine.
		 */
		const val UTTERANCE_ID = "NacAlarmTts"

	}

	/**
	 * On speaking listener.
	 */
	interface OnSpeakingListener
	{

		/**
		 * Called when speech engine is done speaking.
		 */
		@Suppress("unused")
		fun onDoneSpeaking(tts: NacTextToSpeech)

		/**
		 * Called when speech engine has started speaking.
		 */
		@Suppress("unused")
		fun onStartSpeaking(tts: NacTextToSpeech)

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
		 * Text-to-speech engine.
		 */
		private val textToSpeech: NacTextToSpeech,

		/**
		 * Audio focus change listener.
		 */
		private val onAudioFocusChangeListener: OnAudioFocusChangeListener?) :

		UtteranceProgressListener()
	{

		/**
		 * On speaking listener.
		 */
		var onSpeakingListener: OnSpeakingListener? = null

		/**
		 *  Call the OnSpeakingListener when speaking is done.
		 */
		private fun callOnDoneSpeakingListener()
		{
			onSpeakingListener?.onDoneSpeaking(textToSpeech)
		}

		/**
		 *  Call the OnSpeakingListener when speaking starts.
		 */
		private fun callOnStartSpeakingListener()
		{
			onSpeakingListener?.onStartSpeaking(textToSpeech)
		}

		/**
		 * Called when done speaking.
		 */
		override fun onDone(utteranceId: String)
		{
			// Call done speaking listener
			callOnDoneSpeakingListener()

			// Abandon audio focus
			NacAudioManager.abandonFocus(context, onAudioFocusChangeListener)
		}

		/**
		 * Called when starting to speak.
		 */
		override fun onStart(utteranceId: String)
		{
			// Call the start speaking listener
			callOnStartSpeakingListener()
		}

		/**
		 */
		@Deprecated("Deprecated in Java")
		override fun onError(utteranceId: String)
		{
		}

		/**
		 */
		override fun onError(utteranceId: String, errorCode: Int)
		{
		}

	}

	/**
	 * The context.
	 */
	private val context: Context

	/**
	 * The speech engine.
	 */
	private val textToSpeech: TextToSpeech

	/**
	 * Message to buffer and speak when ready.
	 */
	private var bufferMessage: String

	/**
	 * Audio attributes to buffer and use when ready.
	 */
	private var bufferAudioAttributes: NacAudioAttributes?

	/**
	 * Check if speech engine is initialized.
	 */
	private var isInitialized: Boolean

	/**
	 * The utterance listener.
	 */
	private val utteranceListener: NacUtteranceListener

	/**
	 * Constructor.
	 */
	init
	{
		this.context = context
		textToSpeech = TextToSpeech(context, this)
		bufferMessage = ""
		isInitialized = false
		utteranceListener = NacUtteranceListener(context, this, null)
		bufferAudioAttributes = null

		// Set the speaking listener
		utteranceListener.onSpeakingListener = listener

		// Setup text to speech
		textToSpeech.language = Locale.getDefault()
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
		val msg = bufferMessage

		return msg.isNotEmpty()
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
			true
		}
	}

	/*
	  Change media state when audio focus changes.
	 */
	//@Override
	//public void onAudioFocusChange(int focusChange)
	//{
	//	//String change = "UNKOWN";
	//	//if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
	//	//{
	//	//	change = "GAIN";
	//	//}
	//	//else if (focusChange == AudioManager.AUDIOFOCUS_LOSS)
	//	//{
	//	//	change = "LOSS";
	//	//}
	//	//else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
	//	//{
	//	//	change = "LOSS_TRANSIENT";
	//	//}
	//	//else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
	//	//{
	//	//	change = "LOSS_TRANSIENT_CAN_DUCK";
	//	//}
	//}

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
			speak(bufferMessage, bufferAudioAttributes)
		}
	}

	/**
	 * Shutdown the speech engine.
	 */
	fun shutdown()
	{
		textToSpeech.shutdown()
	}

	/**
	 * Speak the given text.
	 */
	fun speak(message: String, attrs: NacAudioAttributes?)
	{
		// TTS object is already initialized
		if (this.isInitialized)
		{
			// Gain transient audio focus
			if (!NacAudioManager.requestFocusGainTransient(context, null, attrs))
			{
				val errorMsg = context.getString(R.string.error_message_text_to_speech_audio_focus)

				NacUtility.quickToast(context, errorMsg)
				return
			}

			// Speak the message
			val androidAttrs = attrs!!.audioAttributes.audioAttributesV21.audioAttributes
			val bundle = NacBundle.toBundle(attrs)
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

}