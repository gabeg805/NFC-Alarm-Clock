package com.nfcalarmclock.media;

import android.content.Context;
import android.media.AudioManager;

import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.C;

import com.nfcalarmclock.util.NacUtility;
import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;

import java.util.List;

/**
 * Audio attributes.
 */
@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "RedundantSuppression", "UnusedReturnValue"})
public class NacAudioAttributes
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
	 * Usage.
	 */
	private int mUsage;

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
	public NacAudioAttributes(Context context)
	{
		this(context, "");
	}

	/**
	 */
	public NacAudioAttributes(Context context, NacAlarm alarm)
	{
		this(context, "");
		this.merge(alarm);
	}

	/**
	 */
	public NacAudioAttributes(Context context, String source)
	{
		this.mContext = context;
		this.mShared = new NacSharedPreferences(context);

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
		int usage = this.getUsage();

		return new AudioAttributes.Builder()
			.setContentType(C.CONTENT_TYPE_MUSIC)
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
		AudioAttributes attr = this.getAudioAttributes();

		return attr.getAudioAttributesV21().getVolumeControlStream();
	}

	/**
	 * @return The current stream volume.
	 */
	private int getStreamVolume()
	{
		Context context = this.getContext();
		int stream = this.getStream();

		AudioManager am = (AudioManager) context.getSystemService(
			Context.AUDIO_SERVICE);

		return am.getStreamVolume(stream);
	}

	/**
	 * @return The maximum stream volume.
	 */
	private int getStreamMaxVolume()
	{
		Context context = this.getContext();
		int stream = this.getStream();

		AudioManager am = (AudioManager) context.getSystemService(
			Context.AUDIO_SERVICE);

		return am.getStreamMaxVolume(stream);
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
	public NacAudioAttributes merge(NacAlarm alarm)
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
		int usage = sourceToUsage(context, source);

		this.setUsage(usage);
	}

	/**
	 * Convert a source to a usage.
	 */
	public static int sourceToUsage(Context context, String source)
	{
		if ((source == null) || source.isEmpty())
		{
			return 0;
		}

		NacSharedConstants cons = new NacSharedConstants(context);
		List<String> audioSources = cons.getAudioSources();

		// Alarm
		if (source.equals(audioSources.get(0)))
		{
			return C.USAGE_ALARM;
		}
		// Media
		else if (source.equals(audioSources.get(1)))
		{
			return C.USAGE_MEDIA;
		}
		// Notification
		else if (source.equals(audioSources.get(2)))
		{
			return C.USAGE_NOTIFICATION;
		}
		// Ringtone
		else if (source.equals(audioSources.get(3)))
		{
			return C.USAGE_NOTIFICATION_RINGTONE;
		}
		// Call
		//else if (source.equals(audioSources.get(3)))
		//{
		//	stream = AudioManager.STREAM_RING;
		//	usage = C.USAGE_VOICE_COMMUNICATION;
		//	//usage = AudioAttributesCompat.USAGE_MEDIA;
		//}
		// System
		else if (source.equals(audioSources.get(4)))
		{
			return C.USAGE_MEDIA;
		}
		else
		{
			return C.USAGE_MEDIA;
		}
	}

	/**
	 * Set the volume of the stream.
	 */
	public void setStreamVolume(int volume)
	{
		Context context = this.getContext();
		int stream = this.getStream();

		AudioManager am = (AudioManager) context.getSystemService(
			Context.AUDIO_SERVICE);

		try
		{
			if (!am.isVolumeFixed())
			{
				am.setStreamVolume(stream, volume, 0);
			}
		}
		catch (SecurityException e)
		{
			NacUtility.printf("NacAudioAttributes : SecurityException : setStreamVolume");
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
