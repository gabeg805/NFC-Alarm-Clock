package com.nfcalarmclock.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

import com.nfcalarmclock.NacUtility;
import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;

import java.util.List;

/**
 * Audio container.
 */
@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "RedundantSuppression", "UnusedReturnValue"})
public class NacAudio
{

	/**
	 * Audio attributes.
	 */
	public static class Attributes
	{

		/**
		 * Context.
		 */
		private final Context mContext;

		/**
		 * Shared preferences.
		 */
		private final NacSharedPreferences mShared;

		/**
		 * Stream.
		 */
		private int mStream;

		/**
		 * Usage.
		 */
		private int mUsage;

		/**
		 * Audio focus.
		 */
		private int mFocus;

		/**
		 * Volume level.
		 */
		private int mLevel;

		/**
		 * Ducking flag.
		 */
		private boolean mWasDucking;

		/**
		 * Repeat.
		 */
		private boolean mRepeat;

		/**
		 */
		public Attributes(Context context)
		{
			this(context, "");
		}

		/**
		 */
		public Attributes(Context context, NacAlarm alarm)
		{
			this(context, "");
			this.merge(alarm);
		}

		/**
		 */
		public Attributes(Context context, String source)
		{
			this.mContext = context;
			this.mShared = new NacSharedPreferences(context);

			this.setFocus(AudioManager.AUDIOFOCUS_GAIN);
			this.setSource(source);
			this.setVolumeLevel(-1);
			this.setRepeat(false);
			this.setWasDucking(false);
		}

		/**
		 * @return True if volume can be changed, and False otherwise.
		 */
		public boolean canVolumeChange()
		{
			return (this.getVolumeLevel() >= 0);
		}

		/**
		 * Duck the volume.
		 */
		public void duckVolume()
		{
			NacSharedPreferences shared = this.getSharedPreferences();
			int current = this.getStreamVolume();
			int duck = current / 2;

			this.setWasDucking(true);
			shared.editPreviousVolume(current);
			this.setStreamVolume(duck);
		}

		/**
		 * @return The audio attributes.
		 */
		public AudioAttributes getAudioAttributes()
		{
			int stream = this.getStream();
			int usage = this.getUsage();

			return new AudioAttributes.Builder()
				.setLegacyStreamType(stream)
				.setUsage(usage)
				.build();
		}

		/**
		 * @return The context.
		 */
		private Context getContext()
		{
			return this.mContext;
		}

		/**
		 * @return The audio focus.
		 */
		public int getFocus()
		{
			return this.mFocus;
		}

		/**
		 * @return True if the audio should be repeated, and False otherwise.
		 */
		public boolean getRepeat()
		{
			return this.mRepeat;
		}

		/**
		 * @return The shared preferences.
		 */
		private NacSharedPreferences getSharedPreferences()
		{
			return this.mShared;
		}

		/**
		 * @return The audio stream.
		 */
		public int getStream()
		{
			return this.mStream;
		}

		/**
		 * @return The current stream volume.
		 */
		private int getStreamVolume()
		{
			Context context = this.getContext();
			int stream = this.getStream();

			return NacAudio.getAudioManager(context).getStreamVolume(stream);
		}

		/**
		 * @return The maximum stream volume.
		 */
		private int getStreamMaxVolume()
		{
			Context context = this.getContext();
			int stream = this.getStream();

			return NacAudio.getAudioManager(context).getStreamMaxVolume(stream);
		}

		/**
		 * @return The audio usage.
		 */
		public int getUsage()
		{
			return this.mUsage;
		}

		/**
		 * @return The calculate volume level.
		 */
		public int getVolumeLevel()
		{
			return this.mLevel;
		}

		/**
		 * @return True if the stream volume is already set to the desired
		 *         volume level.
		 */
		public boolean isStreamVolumeAlreadySet()
		{
			int previous = this.getStreamVolume();
			int volume = this.toStreamVolume();
			return (previous == volume);
		}

		/**
		 * Merge the current audio attributes with that of the alarm.
		 */
		public Attributes merge(NacAlarm alarm)
		{
			if (alarm != null)
			{
				this.setSource(alarm.getAudioSource());
				this.setVolumeLevel(alarm.getVolume());
			}

			return this;
		}

		/**
		 * Revert the effects of ducking.
		 */
		public void revertDucking()
		{
			if (this.wasDucking())
			{
				this.setWasDucking(false);
			}
		}

		/**
		 * Revert the volume level.
		 */
		public void revertVolume()
		{
			if (!this.canVolumeChange())
			{
				return;
			}

			NacSharedPreferences shared = this.getSharedPreferences();
			int previous = shared.getPreviousVolume();

			this.setStreamVolume(previous);
		}

		/**
		 * Set the audio focus.
		 */
		public void setFocus(int focus)
		{
			this.mFocus = focus;
		}

		/**
		 * Set whether the audio should be repeated or not.
		 */
		public void setRepeat(boolean repeat)
		{
			this.mRepeat = repeat;
		}

		/**
		 * Set the audio stream and usage.
		 */
		public void setSource(String source)
		{
			Context context = this.getContext();
			NacSharedConstants cons = new NacSharedConstants(context);
			List<String> audioSources = cons.getAudioSources();
			int stream = AudioManager.STREAM_MUSIC;
			int usage = AudioAttributes.USAGE_MEDIA;

			if (source != null)
			{
				if (source.equals(audioSources.get(0)))
				{
					stream = AudioManager.STREAM_ALARM;
					usage = AudioAttributes.USAGE_ALARM;
				}
				else if (source.equals(audioSources.get(1)))
				{
					stream = AudioManager.STREAM_MUSIC;
					usage = AudioAttributes.USAGE_MEDIA;
				}
				else if (source.equals(audioSources.get(2)))
				{
					stream = AudioManager.STREAM_NOTIFICATION;
					usage = AudioAttributes.USAGE_NOTIFICATION;
				}
				else if (source.equals(audioSources.get(3)))
				{
					stream = AudioManager.STREAM_RING;
					usage = AudioAttributes.USAGE_MEDIA;
				}
				else if (source.equals(audioSources.get(4)))
				{
					stream = AudioManager.STREAM_SYSTEM;
					usage = AudioAttributes.USAGE_MEDIA;
				}
			}

			this.setStream(stream);
			this.setUsage(usage);
		}

		/**
		 * Set the audio stream.
		 */
		public void setStream(int stream)
		{
			this.mStream = stream;
		}

		/**
		 * Set the volume of the stream.
		 */
		public void setStreamVolume(int volume)
		{
			Context context = this.getContext();
			AudioManager manager = NacAudio.getAudioManager(context);
			int stream = this.getStream();

			try
			{
				if (!manager.isVolumeFixed())
				{
					manager.setStreamVolume(stream, volume, 0);
				}
			}
			catch (SecurityException e)
			{
				NacUtility.printf("NacAudio : SecurityException : setStreamVolume");
			}
		}

		/**
		 * Set the audio usage.
		 */
		public void setUsage(int usage)
		{
			this.mUsage = usage;
		}

		/**
		 * Set the volume.
		 */
		public void setVolume()
		{
			if (!this.canVolumeChange())
			{
				return;
			}

			NacSharedPreferences shared = this.getSharedPreferences();
			int previous = this.getStreamVolume();
			int volume = this.toStreamVolume();

			this.setStreamVolume(volume);
			shared.editPreviousVolume(previous);
		}

		/**
		 * Set the volume level.
		 */
		public void setVolumeLevel(int level)
		{
			this.mLevel = level;
		}

		/**
		 * Set whether the volume was ducked or not.
		 */
		public void setWasDucking(boolean wasDucking)
		{
			this.mWasDucking = wasDucking;
		}

		/**
		 * @see Attributes#toStreamVolume(int)
		 */
		public int toStreamVolume()
		{
			int level = this.getVolumeLevel();
			return this.toStreamVolume(level);
		}

		/**
		 * Convert the volume level to the stream volume format.
		 */
		public int toStreamVolume(int volumeLevel)
		{
			int max = this.getStreamMaxVolume();
			return (int) (max * volumeLevel / 100.0f);
		}

		/**
		 * @return True if the volume was ducked, and False otherwise.
		 */
		public boolean wasDucking()
		{
			return this.mWasDucking;
		}

	}

	/**
	 * Abandon audio focus.
	 */
	@SuppressWarnings("deprecation")
	public static int abandonAudioFocus(Context context,
		AudioManager.OnAudioFocusChangeListener listener)
	{
		AudioManager am = NacAudio.getAudioManager(context);
		int result;

		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		//{
		//	AudioFocusRequest request = this.getAudioFocusRequest();
		//	result = am.abandonAudioFocus(request);
		//}
		//else
		//{
			result = am.abandonAudioFocus(listener);
		//}

		return result;
	}

	/**
	 * @return The audio focus request.
	 */
	public static AudioFocusRequest getAudioFocusRequest(
		AudioManager.OnAudioFocusChangeListener listener,
		NacAudio.Attributes attrs)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			AudioAttributes audioAttributes = attrs.getAudioAttributes();
			int focus = attrs.getFocus();

			return new AudioFocusRequest.Builder(focus)
				.setAudioAttributes(audioAttributes)
				.setOnAudioFocusChangeListener(listener)
				.build();
		}
		else
		{
			return null;
		}
	}

	/**
	 * @return The audio manager.
	 */
	public static AudioManager getAudioManager(Context context)
	{
		return (AudioManager) context.getSystemService(
			Context.AUDIO_SERVICE);
	}

	/**
	 * Request audio focus.
	 */
	@SuppressWarnings("deprecation")
	public static boolean requestAudioFocus(Context context,
		AudioManager.OnAudioFocusChangeListener listener,
		NacAudio.Attributes attrs)
	{
		AudioManager am = NacAudio.getAudioManager(context);
		int result;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			AudioFocusRequest request = NacAudio.getAudioFocusRequest(listener,
				attrs);
			result = am.requestAudioFocus(request);
		}
		else
		{
			int stream = attrs.getStream();
			int focus = attrs.getFocus();
			result = am.requestAudioFocus(listener, stream, focus);
		}

		return (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
	}

	/**
	 * @see NacAudio#requestAudioFocus(Context,
	 *  AudioManager.OnAudioFocusChangeListener, NacAudio.Attributes)
	 */
	public static boolean requestAudioFocusGain(Context context,
		AudioManager.OnAudioFocusChangeListener listener,
		NacAudio.Attributes attrs)
	{
		attrs.setFocus(AudioManager.AUDIOFOCUS_GAIN);

		return NacAudio.requestAudioFocus(context, listener, attrs);
	}

	/**
	 * @see NacAudio#requestAudioFocus(Context,
	 *  AudioManager.OnAudioFocusChangeListener, NacAudio.Attributes)
	 */
	public static boolean requestAudioFocusTransient(Context context,
		AudioManager.OnAudioFocusChangeListener listener,
		NacAudio.Attributes attrs)
	{
		attrs.setFocus(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

		return NacAudio.requestAudioFocus(context, listener, attrs);
	}

}
