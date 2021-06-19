package com.nfcalarmclock.audio.tts;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.nfcalarmclock.audio.NacAudio;
import com.nfcalarmclock.system.NacBundle;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.NacUtility;

import java.util.Locale;

/**
 * Text to speech.
 */
@SuppressWarnings({"RedundantSuppression", "UnnecessaryInterfaceModifier"})
public class NacTextToSpeech
	implements TextToSpeech.OnInitListener,
		AudioManager.OnAudioFocusChangeListener
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
		public void onDoneSpeaking(NacTextToSpeech tts,
			NacAudio.Attributes attrs);

		/**
		 * Called when speech engine has started speaking.
		 */
		@SuppressWarnings("unused")
		public void onStartSpeaking(NacTextToSpeech tts,
			NacAudio.Attributes attrs);
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
			OnSpeakingListener listener = getOnSpeakingListener();
			NacTextToSpeech tts = getTextToSpeech();
			NacAudio.Attributes attrs = (tts != null) ? tts.getAudioAttributes()
				: null;

			if (listener != null)
			{
				listener.onDoneSpeaking(tts, attrs);
			}

			NacAudio.abandonAudioFocus(this.getContext(),
				this.getOnAudioFocusChangeListener());
		}

		/**
		 */
		@Override
		public void onStart(String utteranceId)
		{
			OnSpeakingListener listener = getOnSpeakingListener();
			NacTextToSpeech tts = getTextToSpeech();
			NacAudio.Attributes attrs = (tts != null) ? tts.getAudioAttributes()
				: null;

			if (listener != null)
			{
				listener.onStartSpeaking(tts, attrs);
			}
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
	 * The context.
	 */
	private final Context mContext;

	/**
	 * The speech engine.
	 */
	private TextToSpeech mSpeech;

	/**
	 * Message buffer to speak.
	 */
	private String mBuffer;

	/**
	 * Check if speech engine is initialized.
	 */
	private boolean mInitialized;

	/**
	 * The utterance listener.
	 */
	private final NacUtteranceListener mUtterance;

	/**
	 * Audio attributes.
	 */
	private NacAudio.Attributes mAudioAttributes;

	/**
	 * Utterance ID when speaking through the TTS engine.
	 */
	public static final String UTTERANCE_ID = "NacAlarmTts";

	/**
	 */
	public NacTextToSpeech(Context context, OnSpeakingListener listener)
	{
		NacSharedPreferences shared = new NacSharedPreferences(context);
		this.mContext = context;
		this.mSpeech = null;
		this.mBuffer = "";
		this.mInitialized = false;
		this.mUtterance = new NacUtteranceListener(context, this, this);
		this.mAudioAttributes = new NacAudio.Attributes(context,
			shared.getAudioSource());
		int defaultFocus = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;

		this.getAudioAttributes().setFocus(defaultFocus);
		this.setOnSpeakingListener(listener);
	}

	/**
	 * @return The audio attributes.
	 */
	public NacAudio.Attributes getAudioAttributes()
	{
		return this.mAudioAttributes;
	}

	/**
	 * @return The speech buffer.
	 */
	private String getBuffer()
	{
		return this.mBuffer;
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
		String buffer = this.getBuffer();

		return ((buffer != null) && (!buffer.isEmpty()));
	}

	/**
	 * @return True if the speech engine is initialized and False otherwise.
	 */
	public boolean isInitialized()
	{
		return (this.mInitialized && (this.mSpeech != null));
	}

	/**
	 * @return True if the speech engine is running, and False otherwise.
	 */
	public boolean isSpeaking()
	{
		TextToSpeech speech = this.getTextToSpeech();

		return (this.isInitialized() && speech.isSpeaking());
	}

	/**
	 * Change media state when audio focus changes.
	 */
	@Override
	public void onAudioFocusChange(int focusChange)
	{
		//String change = "UNKOWN";

		//if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
		//{
		//	change = "GAIN";
		//}
		//else if (focusChange == AudioManager.AUDIOFOCUS_LOSS)
		//{
		//	change = "LOSS";
		//}
		//else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
		//{
		//	change = "LOSS_TRANSIENT";
		//}
		//else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
		//{
		//	change = "LOSS_TRANSIENT_CAN_DUCK";
		//}
	}

	/**
	 */
	@Override
	public void onInit(int status)
	{
		this.mInitialized = (status == TextToSpeech.SUCCESS);

		if (this.isInitialized())
		{
			TextToSpeech speech = this.getTextToSpeech();
			speech.setLanguage(Locale.getDefault());

			if (this.hasBuffer())
			{
				String buffer = this.getBuffer();
				this.speak(buffer);
				this.setBuffer("");
			}
		}
	}

	/**
	 * Set the speech message buffer.
	 */
	private void setBuffer(String buffer)
	{
		this.mBuffer = buffer;
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
			this.mSpeech = null;
		}
	}

	/**
	 * @see #speak(String, NacAudio.Attributes)
	 */
	public void speak(String message)
	{
		this.speak(message, this.getAudioAttributes());
	}

	/**
	 * Speak the given text.
	 */
	public void speak(String message, NacAudio.Attributes attrs)
	{
		Context context = this.getContext();
		TextToSpeech speech = this.getTextToSpeech();
		this.mAudioAttributes = attrs;

		if (speech == null)
		{
			speech = new TextToSpeech(context, this);
			this.mSpeech = speech;
			speech.setOnUtteranceProgressListener(this.mUtterance);
		}

		if (this.isInitialized())
		{
			if(!NacAudio.requestAudioFocus(context, this, attrs))
			{
				NacUtility.printf("Audio Focus TRANSIENT NOT Granted!");
				return;
			}

			Bundle bundle = NacBundle.toBundle(attrs);
			speech.setAudioAttributes(attrs.getAudioAttributes());
			speech.speak(message, TextToSpeech.QUEUE_FLUSH, bundle,
				UTTERANCE_ID);
		}
		else
		{
			this.setBuffer(message);
		}
	}

}
