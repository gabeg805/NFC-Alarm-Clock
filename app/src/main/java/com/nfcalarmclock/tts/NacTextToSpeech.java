package com.nfcalarmclock.tts;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.nfcalarmclock.media.NacAudioAttributes;
import com.nfcalarmclock.media.NacAudioManager;
import com.nfcalarmclock.system.NacBundle;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.util.NacUtility;

import java.util.Locale;

/**
 * Text to speech.
 */
@SuppressWarnings({"RedundantSuppression", "UnnecessaryInterfaceModifier"})
public class NacTextToSpeech
	implements TextToSpeech.OnInitListener
		//AudioManager.OnAudioFocusChangeListener
{

	/**
	 * On speaking listener.
	 */
	public interface OnSpeakingListener
	{

		/**
		 * Called when speech engine is done speaking.
		 */
		@SuppressWarnings("unused")
		public void onDoneSpeaking(NacTextToSpeech tts);

		/**
		 * Called when speech engine has started speaking.
		 */
		@SuppressWarnings("unused")
		public void onStartSpeaking(NacTextToSpeech tts);
	}

	/**
	 */
	public static class NacUtteranceListener
		extends UtteranceProgressListener
	{

		/**
		 * Context.
		 */
		private final Context mContext;

		/**
		 * Text-to-speech engine.
		 */
		private final NacTextToSpeech mTextToSpeech;

		/**
		 * Audio focus change listener.
		 */
		private final AudioManager.OnAudioFocusChangeListener
			mOnAudioFocusChangeListener;

		/**
		 * On speaking listener.
		 */
		private OnSpeakingListener mOnSpeakingListener;

		/**
		 */
		public NacUtteranceListener(Context context, NacTextToSpeech tts,
			AudioManager.OnAudioFocusChangeListener listener)
		{
			this.mContext = context;
			this.mTextToSpeech = tts;
			this.mOnAudioFocusChangeListener = listener;
		}

		/** Call the OnSpeakingListener when speaking is done.
		 */
		private void callOnDoneSpeakingListener()
		{
			OnSpeakingListener listener = this.getOnSpeakingListener();
			NacTextToSpeech tts = this.getTextToSpeech();

			if (listener != null)
			{
				listener.onDoneSpeaking(tts);
			}
		}

		/** Call the OnSpeakingListener when speaking starts.
		 */
		private void callOnStartSpeakingListener()
		{
			OnSpeakingListener listener = this.getOnSpeakingListener();
			NacTextToSpeech tts = this.getTextToSpeech();

			// Call the listener
			if (listener != null)
			{
				listener.onStartSpeaking(tts);
			}
		}

		/**
		 * @return The context.
		 */
		private Context getContext()
		{
			return this.mContext;
		}

		/**
		 * @return This audio focus change listener.
		 */
		private AudioManager.OnAudioFocusChangeListener
			getOnAudioFocusChangeListener()
		{
			return this.mOnAudioFocusChangeListener;
		}

		/**
		 * @return The on speaking listener.
		 */
		private OnSpeakingListener getOnSpeakingListener()
		{
			return this.mOnSpeakingListener;
		}

		/**
		 * @return The text-to-speech engine.
		 */
		private NacTextToSpeech getTextToSpeech()
		{
			return this.mTextToSpeech;
		}

		/**
		 */
		@Override
		public void onDone(String utteranceId)
		{
			this.callOnDoneSpeakingListener();

			NacAudioManager.abandonFocus(this.getContext(),
				this.getOnAudioFocusChangeListener());
		}

		/**
		 */
		@Override
		public void onStart(String utteranceId)
		{
			this.callOnStartSpeakingListener();
		}

		/**
		 */
		@SuppressWarnings("deprecation")
		@Override
		public void onError(String utteranceId)
		{
			NacUtility.printf("onError! %s", utteranceId);
		}

		/**
		 */
		@Override
		public void onError(String utteranceId, int errorCode)
		{
			NacUtility.printf("onError! %s", utteranceId);
		}

		/**
		 * Set on speaking listener.
		 */
		public void setOnSpeakingListener(OnSpeakingListener listener)
		{
			this.mOnSpeakingListener = listener;
		}

	}

	/**
	 * Utterance ID when speaking through the TTS engine.
	 */
	public static final String UTTERANCE_ID = "NacAlarmTts";

	/**
	 * The context.
	 */
	private final Context mContext;

	/**
	 * The speech engine.
	 */
	private final TextToSpeech mSpeech;

	/**
	 * Message to buffer and speak when ready.
	 */
	private String mBufferMessage;

	/**
	 * Audio attributes to buffer and use when ready.
	 */
	private NacAudioAttributes mBufferAudioAttributes;

	/**
	 * Check if speech engine is initialized.
	 */
	private boolean mInitialized;

	/**
	 * The utterance listener.
	 */
	private final NacUtteranceListener mUtterance;

	/**
	 */
	public NacTextToSpeech(Context context, OnSpeakingListener listener)
	{
		NacSharedPreferences shared = new NacSharedPreferences(context);
		this.mContext = context;
		this.mSpeech = new TextToSpeech(context, this);
		this.mBufferMessage = "";
		this.mInitialized = false;
		this.mUtterance = new NacUtteranceListener(context, this, null);
		//this.mUtterance = new NacUtteranceListener(context, this, this);
		this.mBufferAudioAttributes = null;

		this.setOnSpeakingListener(listener);
		this.getTextToSpeech().setLanguage(Locale.getDefault());
		this.getTextToSpeech().setOnUtteranceProgressListener(this.getUtteranceListener());
	}

	/**
	 * Clear the buffered message and audio attributes.
	 */
	public void clearBuffer()
	{
		this.setBufferMessage("");
		this.setBufferAudioAttributes(null);
	}

	/**
	 * @return The audio attributes.
	 */
	public NacAudioAttributes getBufferAudioAttributes()
	{
		return this.mBufferAudioAttributes;
	}

	/**
	 * @return The speech buffer.
	 */
	private String getBufferMessage()
	{
		return this.mBufferMessage;
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The speech engine.
	 */
	private TextToSpeech getTextToSpeech()
	{
		return this.mSpeech;
	}

	/**
	 * @return The utterance listener.
	 */
	private NacUtteranceListener getUtteranceListener()
	{
		return this.mUtterance;
	}

	/**
	 * @return True if there is a buffer present and False otherwise.
	 */
	public boolean hasBuffer()
	{
		String msg = this.getBufferMessage();

		return (msg != null) && !msg.isEmpty();
	}

	/**
	 * @return True if the speech engine is initialized and False otherwise.
	 */
	public boolean isInitialized()
	{
		return (this.mInitialized && (this.mSpeech != null));
	}

	/**
	 * @return True if the speech engine is running, and False otherwise. Will
	 *     also return True if the IllegalArgumentException is raised.
	 */
	public boolean isSpeaking()
	{
		TextToSpeech speech = this.getTextToSpeech();

		try
		{
			return this.isInitialized() && speech.isSpeaking();
		}
		catch (IllegalArgumentException e)
		{
			return true;
		}
	}

	/**
	 * Change media state when audio focus changes.
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
	 */
	@Override
	public void onInit(int status)
	{
		this.mInitialized = (status == TextToSpeech.SUCCESS);

		// Initialization was a succes and there is a message in the buffer
		if (this.isInitialized() && this.hasBuffer())
		{
			String msg = this.getBufferMessage();
			NacAudioAttributes attrs = this.getBufferAudioAttributes();

			// Say what is in the buffer
			this.speak(msg, attrs);
		}
	}

	/**
	 * Set the audio attributes to buffer and use when ready.
	 *
	 * @param  attrs  The audio attributes.
	 */
	public void setBufferAudioAttributes(NacAudioAttributes attrs)
	{
		this.mBufferAudioAttributes = attrs;
	}

	/**
	 * Set the speech message buffer.
	 */
	private void setBufferMessage(String buffer)
	{
		this.mBufferMessage = buffer;
	}

	/**
	 * Set on speaking listener.
	 */
	public void setOnSpeakingListener(OnSpeakingListener listener)
	{
		this.getUtteranceListener().setOnSpeakingListener(listener);
	}

	/**
	 * Shutdown the speech engine.
	 */
	public void shutdown()
	{
		TextToSpeech speech = this.getTextToSpeech();

		if (speech != null)
		{
			speech.shutdown();
			//this.mSpeech = null;
		}
	}

	/**
	 * Speak the given text.
	 */
	public void speak(String message, NacAudioAttributes attrs)
	{
		Context context = this.getContext();
		TextToSpeech speech = this.getTextToSpeech();

		// TTS object is already initialized
		if (this.isInitialized())
		{
			// Gain transient audio focus
			if(!NacAudioManager.requestFocusGainTransient(context, null, attrs))
			{
				NacUtility.printf("Audio Focus TRANSIENT NOT Granted!");
				return;
			}

			// Speak
			AudioAttributes androidAttrs = attrs.getAudioAttributes().getAudioAttributesV21();
			Bundle bundle = NacBundle.toBundle(attrs);

			speech.setAudioAttributes(androidAttrs);
			speech.speak(message, TextToSpeech.QUEUE_FLUSH, bundle,
				NacTextToSpeech.UTTERANCE_ID);

			// Clear the buffer
			this.clearBuffer();
		}
		// Buffer the message and audio attributes until the TTS object is
		// initialized
		else
		{
			this.setBufferMessage(message);
			this.setBufferAudioAttributes(attrs);
		}
	}

}
