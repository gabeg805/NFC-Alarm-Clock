package com.nfcalarmclock;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

/**
 * Audio container.
 */
public class NacAudio
{

	/**
	 * Audio attributes.
	 */
	public static class Attributes
	{

		/**
		 * Shared preferences.
		 */
		private NacSharedPreferences mShared;

		/**
		 * Context.
		 */
		private Context mContext;

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
		 * Stream volume.
		 */
		private int mVolume;

		/**
		 * Previously set stream volume.
		 */
		//private int mPreviousVolume;

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
			this(context, (alarm != null) ? alarm.getAudioSource() : "");

			if (alarm != null)
			{
				this.setVolumeLevel(alarm.getVolume());
			}
		}

		/**
		 */
		public Attributes(Context context, String source)
		{
			//int focus = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			//	? AudioManager.AUDIOFOCUS_NONE : 0;
			this.mContext = context;
			this.mShared = new NacSharedPreferences(context);

			this.setFocus(0);
			this.setSource(source);
			this.setVolumeLevel(-1);
			this.setRepeat(false);
			this.setWasDucking(false);

			//this.mWasDucking = false;
		}

		/**
		 * Duck the volume.
		 */
		public void duckVolume()
		{
			NacSharedPreferences shared = this.getSharedPreferences();
			int current = this.getStreamVolume();
			int duck = current / 2;
			this.mVolume = duck;
			//this.mWasDucking = true;

			this.setWasDucking(true);
			shared.editPreviousVolume(current);
			this.setStreamVolume(duck);

			//int current = this.getStreamVolume();
			//this.mVolume = current / 2;
			//this.mPreviousVolume = current;
			//this.mWasDucking = true;
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
		 * @return The volume.
		 */
		public int getVolume()
		{
			return this.mVolume;
		}

		/**
		 * @return The calculate volume level.
		 */
		public int getVolumeLevel()
		{
			return this.mLevel;
		}

		/**
		 * Revert the effects of ducking.
		 */
		public void revertDucking()
		{
			if (this.wasDucking())
			{
				//this.mWasDucking = false;
				this.setWasDucking(false);
			}
		}

		/**
		 * Revert the volume level.
		 */
		public void revertVolume()
		{
			int level = this.getVolumeLevel();

			if (level < 0)
			{
				return;
			}

			//int temp = this.mPreviousVolume;
			//this.mPreviousVolume = this.mVolume;
			//this.mVolume = temp;

			NacSharedPreferences shared = this.getSharedPreferences();
			//int current = this.mVolume;
			int current = this.getStreamVolume();
			int previous = shared.getPreviousVolume();
			this.mVolume = previous;

			shared.editPreviousVolume(previous);
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
			int stream = AudioManager.STREAM_MUSIC;
			int usage = AudioAttributes.USAGE_MEDIA;

			if ((source == null) || source.isEmpty() || source.equals("Media"))
			{
			}
			else if (source.equals("Alarm"))
			{
				stream = AudioManager.STREAM_ALARM;
				usage = AudioAttributes.USAGE_ALARM;
			}
			else if (source.equals("Notification"))
			{
				stream = AudioManager.STREAM_NOTIFICATION;
				usage = AudioAttributes.USAGE_NOTIFICATION;
			}
			else if (source.equals("Ringtone"))
			{
				stream = AudioManager.STREAM_RING;
				usage = AudioAttributes.USAGE_MEDIA; // Might need to be different
			}
			else if (source.equals("System"))
			{
				stream = AudioManager.STREAM_SYSTEM;
				usage = AudioAttributes.USAGE_MEDIA;
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
				NacUtility.printf("NacAudio : SecurityException : Unable to setStreamVolume");
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
			int level = this.getVolumeLevel();

			if (level < 0)
			{
				return;
			}

			NacSharedPreferences shared = this.getSharedPreferences();
			int previous = this.getStreamVolume();
			int max = this.getStreamMaxVolume();
			int volume = (int) (max * level / 100.0f);

			this.setStreamVolume(volume);

			//this.mPreviousVolume = previous;
			shared.editPreviousVolume(previous);
			this.mVolume = volume;
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
		int result = 0;

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
		int result = 0;

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
	 * @see requestAudioFocus
	 */
	public static boolean requestAudioFocusGain(Context context,
		AudioManager.OnAudioFocusChangeListener listener,
		NacAudio.Attributes attrs)
	{
		attrs.setFocus(AudioManager.AUDIOFOCUS_GAIN);

		return NacAudio.requestAudioFocus(context, listener, attrs);
	}

	/**
	 * @see requestAudioFocus
	 */
	public static boolean requestAudioFocusTransient(Context context,
		AudioManager.OnAudioFocusChangeListener listener,
		NacAudio.Attributes attrs)
	{
		attrs.setFocus(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

		return NacAudio.requestAudioFocus(context, listener, attrs);
	}

}
