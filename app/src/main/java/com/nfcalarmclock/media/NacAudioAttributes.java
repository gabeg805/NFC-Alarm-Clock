package com.nfcalarmclock.media;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.C;

import com.nfcalarmclock.util.NacUtility;
import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.shared.NacSharedPreferences;

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

		this.setUsageFromSource(source);
		this.setVolumeLevel(-1);
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
	 * @return The audio attributes.
	 */
	public android.media.AudioAttributes getAudioAttributesV21()
	{
		AudioAttributes attrs = this.getAudioAttributes();

		return attrs.getAudioAttributesV21().audioAttributes;
	}

	/**
	 * @return The audio manager.
	 */
	private AudioManager getAudioManager()
	{
		Context context = this.getContext();

		return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			return this.getAudioAttributesV21().getVolumeControlStream();
		}
		else
		{
			int usage = this.getUsage();

			return NacAudioManager.usageToStream(usage);
		}
	}

	/**
	 * @return The current stream volume.
	 */
	public int getStreamVolume()
	{
		AudioManager am = this.getAudioManager();
		int stream = this.getStream();

		// Get the stream volume
		if (stream != AudioManager.USE_DEFAULT_STREAM_TYPE)
		{
			return am.getStreamVolume(stream);
		}
		// Unable to get stream volume
		else
		{
			return 0;
		}
	}

	/**
	 * @return The maximum stream volume.
	 */
	public int getStreamMaxVolume()
	{
		AudioManager am = this.getAudioManager();
		int stream = this.getStream();

		// Get the stream volume
		if (stream != AudioManager.USE_DEFAULT_STREAM_TYPE)
		{
			return am.getStreamMaxVolume(stream);
		}
		// Unable to get stream volume
		else
		{
			return 0;
		}
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
			this.setUsageFromSource(alarm.getAudioSource());
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
	 * Set the audio usage from the source name.
	 */
	public void setUsageFromSource(String source)
	{
		Context context = this.getContext();
		int usage = NacAudioManager.sourceToUsage(context, source);

		this.setUsage(usage);
	}

	/**
	 * Set the volume of the stream.
	 */
	public void setStreamVolume(int volume)
	{
		AudioManager am = this.getAudioManager();
		int stream = this.getStream();

		// Unable to change the volume because the volume is fixed or because the
		// stream is invalid
		if (am.isVolumeFixed() || (stream == AudioManager.USE_DEFAULT_STREAM_TYPE))
		{
			return;
		}

		// Set the stream volume
		try
		{
			am.setStreamVolume(stream, volume, 0);
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
