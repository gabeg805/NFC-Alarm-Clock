package com.nfcalarmclock;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Locale;

/**
 * Alarm object.
 */
public class NacAlarm
	implements Parcelable
{

	/**
	 * Definition for the change listener object.
	 */
	public interface OnChangeListener
	{
		public void onChange(NacAlarm alarm);
	}

	/**
	 * Helper to build an alarm.
	 */
	public static class Builder
	{

		/**
		 * Listener for when the alarm is changed.
		 */
		private OnChangeListener mOnChangeListener;

		/**
		 * The alarm ID.
		 */
		private int mId;

		/**
		 * Indicates whether the alarm is enabled or not.
		 */
		private boolean mEnabled;

		/**
		 * The hour at which to run the alarm.
		 */
		private int mHour;

		/**
		 * The minute at which to run the alarm.
		 */
		private int mMinute;

		/**
		 * The days on which to run the alarm.
		 */
		private EnumSet<NacCalendar.Day> mDays;

		/**
		 * Indicates whether the alarm should be repeated or not.
		 */
		private boolean mRepeat;

		/**
		 * Indicates whether the alarm should use NFC or not.
		 */
		private boolean mNfc;

		/**
		 * Indicates whether the phone should vibrate when the alarm is run.
		 */
		private boolean mVibrate;

		/**
		 * Volume level.
		 */
		private int mVolume;

		/**
		 * Audio source.
		 */
		private String mAudioSource;

		/**
		 * Type of media.
		 */
		private int mMediaType;

		/**
		 * Path of the media.
		 */
		private String mMediaPath;

		/**
		 * Title of the media.
		 */
		private String mMediaTitle;

		/**
		 * Name of the alarm.
		 */
		private String mName;

		/**
		 * Tag.
		 */
		private Object mTag;

		/**
		 */
		public Builder()
		{
			this(null);
		}

		/**
		 */
		public Builder(Context context)
		{
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);

			this.mOnChangeListener = null;
			this.mId = -1;
			this.mHour = hour;
			this.mMinute = minute;
			this.mEnabled = true;
			this.mMediaType = NacMedia.TYPE_NONE;
			this.mMediaPath = "";
			this.mMediaTitle = "";
			this.mName = "";
			this.mTag = null;

			if (context != null)
			{
				NacSharedConstants cons = new NacSharedConstants(context);
				NacSharedDefaults defaults = new NacSharedDefaults(context);
				this.mDays = NacCalendar.Days.valueToDays(defaults.getDays());
				this.mRepeat = defaults.getRepeat();
				this.mNfc = defaults.getUseNfc();
				this.mVibrate = defaults.getVibrate();
				this.mVolume = defaults.getVolume();
				this.mAudioSource = cons.getAudioSources().get(1);
			}
		}

		/**
		 * Build the alarm.
		 */
		public NacAlarm build()
		{
			return new NacAlarm(this);
		}

		/**
		 * @return The days on which to run the alarm.
		 */
		public EnumSet<NacCalendar.Day> getDays()
		{
			return this.mDays;
		}

		/**
		 * @return The audio source.
		 */
		public String getAudioSource()
		{
			return this.mAudioSource;
		}

		/**
		 * @return True if the alarm is enabled and false otherwise.
		 */
		public boolean getEnabled()
		{
			return this.mEnabled;
		}

		/**
		 * @return The hour.
		 */
		public int getHour()
		{
			return this.mHour;
		}

		/**
		 * @return The alarm ID.
		 */
		public int getId()
		{
			return this.mId;
		}

		/**
		 * @return The minute.
		 */
		public int getMinute()
		{
			return this.mMinute;
		}

		/**
		 * @return The alarm name.
		 */
		public String getName()
		{
			return this.mName;
		}

		/**
		 * @return The OnChangeListener.
		 */
		public OnChangeListener getOnChangeListener()
		{
			return this.mOnChangeListener;
		}

		/**
		 * @return True if repeating the alarm after it runs and false
		 *         otherwise.
		 */
		public boolean getRepeat()
		{
			return this.mRepeat;
		}

		/**
		 * @return The path to the media file to play when the alarm goes off.
		 */
		public String getMediaPath()
		{
			return this.mMediaPath;
		}

		/**
		 * @return The title of the media file.
		 */
		public String getMediaTitle()
		{
			return this.mMediaTitle;
		}

		/**
		 * @return The path to the media file to play when the alarm goes off.
		 */
		public int getMediaType()
		{
			return this.mMediaType;
		}

		/**
		 * @return The tag.
		 */
		public Object getTag()
		{
			return this.mTag;
		}

		/**
		 * @return True if using NFC, and False otherwise.
		 */
		public boolean getUseNfc()
		{
			return this.mNfc;
		}

		/**
		 * @return True if the phone should vibrate when the alarm is going off
		 *         and false otherwise.
		 */
		public boolean getVibrate()
		{
			return this.mVibrate;
		}

		/**
		 * @return The volume level.
		 */
		public int getVolume()
		{
			return this.mVolume;
		}

		/**
		 * Set the audio source.
		 *
		 * @param  source  The audio source.
		 *
		 * @return The Builder.
		 */
		public Builder setAudioSource(String source)
		{
			this.mAudioSource = source;
			return this;
		}

		/**
		 * Set the days to the run the alarm.
		 *
		 * @param  days  The set of days to run the alarm on.
		 *
		 * @return The Builder.
		 */
		public Builder setDays(EnumSet<NacCalendar.Day> days)
		{
			this.mDays = days;
			return this;
		}

		/**
		 * @see setDays
		 */
		public Builder setDays(int value)
		{
			return this.setDays(NacCalendar.Days.valueToDays(value));
		}

		/**
		 * Set whether the alarm is enabled or not.
		 *
		 * @param  enabled  True if the alarm is enabled and False otherwise.
		 *
		 * @return The Builder.
		 */
		public Builder setEnabled(boolean enabled)
		{
			this.mEnabled = enabled;
			return this;
		}

		/**
		 * Set the hour.
		 *
		 * @param  hour  The hour at which to run the alarm.
		 *
		 * @return The Builder.
		 */
		public Builder setHour(int hour)
		{
			this.mHour = hour;
			return this;
		}

		/**
		 * Set the alarm ID.
		 *
		 * @param  id  The unique ID of the alarm.
		 *
		 * @return The Builder.
		 */
		public Builder setId(int id)
		{
			this.mId = id;
			return this;
		}

		/**
		 * Set the path, name, and type of the sound to play.
		 *
		 * @param  context  The application context.
		 * @param  path  The path to the sound to play.
		 *
		 * @return The Builder.
		 */
		public Builder setMedia(Context context, String path)
		{
			int type = NacMedia.getType(context, path);
			String title = NacMedia.getTitle(context, path);

			this.setMediaType(type);
			this.setMediaPath(path);
			this.setMediaTitle(title);
			return this;
		}

		/**
		 * Set the media title.
		 *
		 * @param  title  The title of the media file to play.
		 *
		 * @return The Builder.
		 */
		public Builder setMediaTitle(String title)
		{
			this.mMediaTitle = (title != null) ? title : "";
			return this;
		}

		/**
		 * Set the sound to play when the alarm goes off.
		 *
		 * @param  path  The path to the media file to play when the alarm goes
		 *               off.
		 *
		 * @return The Builder.
		 */
		public Builder setMediaPath(String path)
		{
			this.mMediaPath = (path != null) ? path : "";
			return this;
		}

		/**
		 * Set the type of sound to play.
		 *
		 * @param  type  The type of media file to play.
		 *
		 * @return The Builder.
		 */
		public Builder setMediaType(int type)
		{
			this.mMediaType = type;
			return this;
		}

		/**
		 * Set the minute.
		 *
		 * @param  minute  The minute at which to run the alarm.
		 *
		 * @return The Builder.
		 */
		public Builder setMinute(int minute)
		{
			this.mMinute = minute;
			return this;
		}

		/**
		 * Set the name of the alarm.
		 *
		 * @param  name  The alarm name.
		 *
		 * @return The Builder.
		 */
		public Builder setName(String name)
		{
			this.mName = (name != null) ? name : "";

			return this;
		}

		/**
		 * Set the listener for when the alarm is changed.
		 *
		 * @param  listener  The OnChangeListener.
		 *
		 * @return The Builder.
		 */
		public Builder setOnChangeListener(OnChangeListener listener)
		{
			this.mOnChangeListener = listener;
			return this;
		}

		/**
		 * Set whether the alarm should repeat every week or not.
		 *
		 * @param  repeat  True if repeating the alarm after it runs, and False
		 *                 otherwise.
		 *
		 * @return The Builder.
		 */
		public Builder setRepeat(boolean repeat)
		{
			this.mRepeat = repeat;
			return this;
		}

		/**
		 * Set the tag.
		 */
		public Builder setTag(Object tag)
		{
			this.mTag = tag;
			return this;
		}

		/**
		 * Set whether the alarm should use NFC to dismiss or not.
		 *
		 * @param  nfc  True if the phone should use NFC to dismiss, and False
		 *              otherwise.
		 *
		 * @return The Builder.
		 */
		public Builder setUseNfc(boolean nfc)
		{
			this.mNfc = nfc;
			return this;
		}

		/**
		 * Set whether the alarm should vibrate the phone or not.
		 *
		 * @param  vibrate  True if the phone should vibrate when the alarm is
		 *                  going off and false otherwise.
		 *
		 * @return The Builder.
		 */
		public Builder setVibrate(boolean vibrate)
		{
			this.mVibrate = vibrate;
			return this;
		}

		/**
		 * Set the volume level.
		 *
		 * @param  volume  The volume level.
		 *
		 * @return The Builder.
		 */
		public Builder setVolume(int volume)
		{
			this.mVolume = volume;
			return this;
		}

	}

	/**
	 * Track changes that were made to the alarm.
	 */
	public enum ChangeTracker
	{
		NONE,
		STATE,
		ENABLE,
		TIME,
		REPEAT,
		DAY
	}

	/**
	 * Listener for when the alarm is changed.
	 */
	private OnChangeListener mOnChangeListener;

	/**
	 * The alarm ID.
	 */
	private int mId;

	/**
	 * Indicates whether the alarm is enabled or not.
	 */
	private boolean mEnabled;

	/**
	 * The hour at which to run the alarm.
	 */
	private int mHour;

	/**
	 * The minute at which to run the alarm.
	 */
	private int mMinute;

	/**
	 * The days on which to run the alarm.
	 */
	private EnumSet<NacCalendar.Day> mDays;

	/**
	 * Indicates whether the alarm should be repeated or not.
	 */
	private boolean mRepeat;

	/**
	 * Indicates whether the alarm should use NFC or not.
	 */
	private boolean mNfc;

	/**
	 * Indicates whether the phone should vibrate when the alarm is run.
	 */
	private boolean mVibrate;

	/**
	 * Volume level.
	 */
	private int mVolume;

	/**
	 * Audio source.
	 */
	private String mAudioSource;

	/**
	 * Type of media.
	 */
	private int mMediaType;

	/**
	 * Path of the media file.
	 */
	private String mMediaPath;

	/**
	 * Title of the media file.
	 */
	private String mMediaTitle;

	/**
	 * Name of the alarm.
	 */
	private String mName;

	/**
	 * Tag.
	 */
	private Object mTag;

	/**
	 * Was the alarm enabled.
	 */
	private ChangeTracker mTracker;

	/**
	 * Latch for the change tracker.
	 */
	private boolean mLatch;

	/**
	 */
	public NacAlarm()
	{
		this(new Builder());
	}

	/**
	 */
	public NacAlarm(Builder builder)
	{
		this.setOnChangeListener(builder.getOnChangeListener());
		this.setId(builder.getId());
		this.setEnabled(builder.getEnabled());
		this.setHour(builder.getHour());
		this.setMinute(builder.getMinute());
		this.setDays(builder.getDays());
		this.setRepeat(builder.getRepeat());
		this.setUseNfc(builder.getUseNfc());
		this.setVibrate(builder.getVibrate());
		this.setVolume(builder.getVolume());
		this.setAudioSource(builder.getAudioSource());
		this.setMediaTitle(builder.getMediaTitle());
		this.setMediaPath(builder.getMediaPath());
		this.setMediaType(builder.getMediaType());
		this.setName(builder.getName());
		this.setTag(builder.getTag());
		this.setChangeTracker(ChangeTracker.NONE);
		this.unlatchChangeTracker();
	}

	/**
	 * Populate values with input parcel.
	 *
	 * Update this when adding/removing an element.
	 */
	public NacAlarm(Parcel input)
	{
		this(new Builder()
			.setOnChangeListener(null)
			.setId(input.readInt())
			.setEnabled((input.readInt() != 0))
			.setHour(input.readInt())
			.setMinute(input.readInt())
			.setDays(input.readInt())
			.setRepeat((input.readInt() != 0))
			.setUseNfc((input.readInt() != 0))
			.setVibrate((input.readInt() != 0))
			.setVolume(input.readInt())
			.setAudioSource(input.readString())
			.setMediaType(input.readInt())
			.setMediaPath(input.readString())
			.setMediaTitle(input.readString())
			.setName(input.readString())
			.setTag(null));
	}

	/**
	 * @return True if any days are selected, and False otherwise.
	 */
	public boolean areDaysSelected()
	{
		return !this.getDays().isEmpty();
	}

	/**
	 * Call the listener when the alarm info has changed.
	 */
	public void changed()
	{
		if (this.hasListener())
		{
			this.getOnChangeListener().onChange(this);
		}

		if (!this.isChangeTrackerLatched())
		{
			this.resetChangeTracker();
		}
	}

	/**
	 * Copy this alarm.
	 *
	 * @return A copy of this alarm.
	 */
	public NacAlarm copy()
	{
		return this.copy(this.getId());
	}

	/**
	 * @see copy
	 */
	public NacAlarm copy(int id)
	{
		return new NacAlarm.Builder()
			.setId(id)
			.setEnabled(this.getEnabled())
			.setHour(this.getHour())
			.setMinute(this.getMinute())
			.setDays(this.getDays())
			.setRepeat(this.getRepeat())
			.setUseNfc(this.getUseNfc())
			.setVibrate(this.getVibrate())
			.setVolume(this.getVolume())
			.setAudioSource(this.getAudioSource())
			.setMediaTitle(this.getMediaTitle())
			.setMediaPath(this.getMediaPath())
			.setMediaType(this.getMediaType())
			.setName(this.getName())
			.build();
	}

	/**
	 * Describe contents (required for Parcelable).
	 */
	@Override
	public int describeContents()
	{
		return 0;
	}

	/**
	 * Check if this alarm equals the given alarm.
	 *
	 * @param  alarm  The alarm to compare against.
	 *
	 * @return True if both alarms are the same, and false otherwise.
	 */
	public boolean equals(NacAlarm alarm)
	{
		return ((alarm != null)
			&& (this.getId() == alarm.getId())
			&& (this.getEnabled() == alarm.getEnabled())
			&& (this.getHour() == alarm.getHour())
			&& (this.getMinute() == alarm.getMinute())
			&& (this.getDays() == alarm.getDays())
			&& (this.getRepeat() == alarm.getRepeat())
			&& (this.getVibrate() == alarm.getVibrate())
			&& (this.getMediaType() == alarm.getMediaType())
			&& (this.getMediaPath() == alarm.getMediaPath())
			&& (this.getMediaTitle() == alarm.getMediaTitle())
			&& (this.getName() == alarm.getName()));
	}

	/**
	 * @return The audio source.
	 */
	public String getAudioSource()
	{
		return this.mAudioSource;
	}

	/**
	 * @return The change tracker.
	 */
	public ChangeTracker getChangeTracker()
	{
		return this.mTracker;
	}

	/**
	 * @return The time string.
	 */
	public String getClockTime(Context context)
	{
		return NacCalendar.Time.getClockTime(context, this.getHour(),
			this.getMinute());
	}

	/**
	 * @return The days on which to run the alarm.
	 */
	public EnumSet<NacCalendar.Day> getDays()
	{
		return this.mDays;
	}

	/**
	 * @return True if the alarm is enabled and false otherwise.
	 */
	public boolean getEnabled()
	{
		return this.mEnabled;
	}

	/**
	 * @return The full time string.
	 */
	public String getFullTime(Context context)
	{
		Calendar next = NacCalendar.getNext(this);
		return NacCalendar.Time.getFullTime(context, next);
	}

	/**
	 * @return The hour.
	 */
	public int getHour()
	{
		return this.mHour;
	}

	/**
	 * @return The alarm ID.
	 */
	public int getId()
	{
		return this.mId;
	}

	/**
	 * @param  c  The calendar instance.
	 *
	 * @return The alarm ID, offset by the given day to make it unique to that
	 *         day.
	 */
	public int getId(Calendar c)
	{
		int day = c.get(Calendar.DAY_OF_WEEK);
		int offset = NacCalendar.Days.toIndex(day);

		return this.getId() + offset;
	}

	/**
	 * @return The meridian (AM or PM).
	 */
	public String getMeridian(Context context)
	{
		return NacCalendar.Time.getMeridian(this.getHour(),
			NacCalendar.Time.is24HourFormat(context));
	}

	/**
	 * @return The minute.
	 */
	public int getMinute()
	{
		return this.mMinute;
	}

	/**
	 * @return The alarm name.
	 */
	public String getName()
	{
		String name = this.mName;
		return (name != null) ? name : "";
	}

	/**
	 * @return The normalized alarm name (with newlines replaced with spaces).
	 */
	public String getNameNormalized()
	{
		String name = this.getName();
		return !name.isEmpty() ? name.replace("\n", " ") : name;
	}

	/**
	 * @see getNameNormalized
	 */
	public String getNameNormalizedForMessage(int max)
	{
		String name = this.getNameNormalized();
		Locale locale = Locale.getDefault();
		return (name.length() > max) ?
			String.format(locale, "%1$s...", name.substring(0, max-3)) : name;
	}

	/**
	 * @return The OnChangeListener.
	 */
	protected OnChangeListener getOnChangeListener()
	{
		return this.mOnChangeListener;
	}

	/**
	 * @return True if repeating the alarm after it runs and false
	 *         otherwise.
	 */
	public boolean getRepeat()
	{
		return this.mRepeat;
	}

	/**
	 * @return The title of the media file.
	 */
	public String getMediaTitle()
	{
		return this.mMediaTitle;
	}

	/**
	 * @return The path to the media file to play when the alarm goes off.
	 */
	public String getMediaPath()
	{
		return this.mMediaPath;
	}

	/**
	 * @return The path to the media file to play when the alarm goes off.
	 */
	public int getMediaType()
	{
		return this.mMediaType;
	}

	/**
	 * @return The tag.
	 */
	public Object getTag()
	{
		return this.mTag;
	}

	/**
	 * @return True if using NFC, and False otherwise.
	 */
	public boolean getUseNfc()
	{
		return this.mNfc;
	}

	/**
	 * @return True if the phone should vibrate when the alarm is going off
	 *         and false otherwise.
	 */
	public boolean getVibrate()
	{
		return this.mVibrate;
	}

	/**
	 * @return The volume level.
	 */
	public int getVolume()
	{
		return this.mVolume;
	}

	/**
	 * Check if alarm has a listener.
	 */
	public boolean hasListener()
	{
		return this.getOnChangeListener() != null;
	}

	/**
	 * @return True if the alarm has a sound that will be played when it goes
	 *         off, and False otherwise.
	 */
	public boolean hasMedia()
	{
		String sound = this.getMediaPath();
		return ((sound != null) && !sound.isEmpty());
	}

	/**
	 * @return True if the change tracker is latched, and False otherwise.
	 */
	public boolean isChangeTrackerLatched()
	{
		return this.mLatch;
	}

	/**
	 * @return True if the alarm is snoozed and False otherwise.
	 */
	public boolean isSnoozed(NacSharedPreferences shared)
	{
		int id = this.getId();
		return (shared.getSnoozeCount(id) > 0);
		//return (shared.getPreventAppFromClosing()
		//	&& (shared.getSnoozeCount(id) > 0));
	}

	/**
	 * Latch the change tracker, so that if a change is made, it does not get
	 * reset.
	 */
	public void latchChangeTracker()
	{
		NacUtility.printf("Something latched the change tracker T_T!");
		this.mLatch = true;
	}

	/**
	 * Print all values in the alarm object.
	 */
	public void print()
	{
		NacUtility.printf("Alarm Information");
		NacUtility.printf("Id           : %d", this.mId);
		NacUtility.printf("Enabled      : %b", this.mEnabled);
		NacUtility.printf("Hour         : %d", this.mHour);
		NacUtility.printf("Minute       : %d", this.mMinute);
		//NacUtility.printf("Days         : %s", NacCalendar.Days.toString(this.getDays()));
		NacUtility.printf("Days         : %s", this.getDays());
		NacUtility.printf("Repeat       : %b", this.mRepeat);
		NacUtility.printf("Use NFC      : %b", this.mNfc);
		NacUtility.printf("Vibrate      : %b", this.mVibrate);
		NacUtility.printf("Volume       : %d", this.mVolume);
		NacUtility.printf("Audio Source : %s", this.mAudioSource);
		NacUtility.printf("Media Type   : %s", this.mMediaType);
		NacUtility.printf("Media Path   : %s", this.mMediaPath);
		NacUtility.printf("Media Name   : %s", this.mMediaTitle);
		NacUtility.printf("Name         : %s", this.mName);
		NacUtility.printf("Change Track : %s", this.mTracker.toString());
		NacUtility.printf("Latch        : %b", this.mLatch);
	}

	/**
	 * Reset the change tracker.
	 */
	public void resetChangeTracker()
	{
		this.setChangeTracker(ChangeTracker.NONE);
	}

	/**
	 * Set the audio source.
	 *
	 * @param  source  The audio source.
	 */
	public void setAudioSource(String source)
	{
		this.mAudioSource = source;
	}

	/**
	 * Set the change tracker.
	 */
	public void setChangeTracker(ChangeTracker change)
	{
		this.mTracker = change;
	}

	/**
	 * Set the days to the run the alarm.
	 *
	 * @param  days  The set of days to run the alarm on.
	 */
	public void setDays(EnumSet<NacCalendar.Day> days)
	{
		this.mDays = days;
		this.setChangeTracker(ChangeTracker.DAY);
	}

	/**
	 * @see setDays
	 */
	public void setDays(int value)
	{
		this.setDays(NacCalendar.Days.valueToDays(value));
	}

	/**
	 * Set whether the alarm is enabled or not.
	 *
	 * @param  enabled  True if the alarm is enabled and False otherwise.
	 */
	public void setEnabled(boolean enabled)
	{
		this.mEnabled = enabled;
		this.setChangeTracker(ChangeTracker.ENABLE);
	}

	/**
	 * Set the hour.
	 *
	 * @param  hour  The hour at which to run the alarm.
	 */
	public void setHour(int hour)
	{
		this.mHour = hour;
		this.setChangeTracker(ChangeTracker.TIME);
	}

	/**
	 * Set the alarm ID.
	 *
	 * @param  id  The unique ID of the alarm.
	 */
	public void setId(int id)
	{
		this.mId = id;
	}

	/**
	 * Set the minute.
	 *
	 * @param  minute  The minute at which to run the alarm.
	 */
	public void setMinute(int minute)
	{
		this.mMinute = minute;
		this.setChangeTracker(ChangeTracker.TIME);
	}

	/**
	 * Set the name of the alarm.
	 *
	 * @param  name  The alarm name.
	 */
	public void setName(String name)
	{
		this.mName = (name != null) ? name : "";
	}

	/**
	 * Set a listener for when the alarm is changed.
	 * 
	 * @param  listener  The change listener.
	 */
	public void setOnChangeListener(OnChangeListener listener)
	{
		this.mOnChangeListener = listener;
	}

	/**
	 * Set whether the alarm should repeat every week or not.
	 *
	 * @param  repeat  True if repeating the alarm after it runs, and False
	 *                 otherwise.
	 */
	public void setRepeat(boolean repeat)
	{
		this.mRepeat = repeat;
		this.setChangeTracker(ChangeTracker.REPEAT);
	}

	/**
	 * Set the sound to play when the alarm goes off.
	 *
	 * @param  context  The application context.
	 * @param  path  The path to sound to play.
	 */
	public void setMedia(Context context, String path)
	{
		int type = NacMedia.getType(context, path);
		String title = NacMedia.getTitle(context, path);

		this.setMediaType(type);
		this.setMediaPath(path);
		this.setMediaTitle(title);
	}

	/**
	 * Set the media title.
	 *
	 * @param  title  The title of the media file to play.
	 */
	public void setMediaTitle(String title)
	{
		this.mMediaTitle = (title != null) ? title : "";
	}

	/**
	 * Set the sound to play when the alarm goes off.
	 *
	 * @param  path  The path to the media file to play when the alarm goes
	 *               off.
	 */
	public void setMediaPath(String path)
	{
		this.mMediaPath = (path != null) ? path : "";
	}

	/**
	 * Set the type of sound to play.
	 *
	 * @param  type  The type of media file to play.
	 */
	public void setMediaType(int type)
	{
		this.mMediaType = type;
	}

	/**
	 * Set the tag.
	 */
	public void setTag(Object tag)
	{
		this.mTag = tag;
	}

	/**
	 * Set whether the alarm should use NFC to dismiss or not.
	 *
	 * @param  nfc  True if the phone should use NFC to dismiss, and False
	 *              otherwise.
	 */
	public void setUseNfc(boolean nfc)
	{
		this.mNfc = nfc;
	}

	/**
	 * Set whether the alarm should vibrate the phone or not.
	 *
	 * @param  vibrate  True if the phone should vibrate when the alarm is
	 *                  going off and false otherwise.
	 */
	public void setVibrate(boolean vibrate)
	{
		this.mVibrate = vibrate;
	}

	/**
	 * Set the volume level.
	 *
	 * @param  volume  The volume level.
	 */
	public void setVolume(int volume)
	{
		this.mVolume = volume;
	}

	/**
	 * Toggle a day.
	 */
	public void toggleDay(NacCalendar.Day day)
	{
		if (this.getDays().contains(day))
		{
			this.getDays().remove(day);
		}
		else
		{
			this.getDays().add(day);
		}

		this.setChangeTracker(ChangeTracker.DAY);
	}

	/**
	 * Toggle the day at the given index.
	 *
	 * @param  index  The index of the day.
	 */
	public void toggleIndex(int index)
	{
		NacCalendar.Day day = NacCalendar.Days.fromIndex(index);
		this.toggleDay(day);
	}

	/**
	 * Toggle repeat.
	 */
	public void toggleRepeat()
	{
		boolean repeat = this.getRepeat();
		this.setRepeat(!repeat);
	}

	/**
	 * Toggle use NFC.
	 */
	public void toggleUseNfc()
	{
		boolean useNfc = this.getUseNfc();
		this.setUseNfc(!useNfc);
	}

	/**
	 * Toggle vibrate.
	 */
	public void toggleVibrate()
	{
		boolean vibrate = this.getVibrate();
		this.setVibrate(!vibrate);
	}

	/**
	 * Toggle today.
	 */
	public void toggleToday()
	{
		NacCalendar.Day day = NacCalendar.Days.getToday();

		this.toggleDay(day);
	}

	/**
	 * Toggle the day(s) with the given value.
	 *
	 * @param  value  The value of a day or multiple days.
	 */
	public void toggleValue(int value)
	{
		EnumSet<NacCalendar.Day> days = NacCalendar.Days.valueToDays(value);

		for (NacCalendar.Day d : days)
		{
			this.toggleDay(d);
		}
	}

	/**
	 * Unlatch the change tracker, so that if a change is made, it will get
	 * reset.
	 */
	public void unlatchChangeTracker()
	{
		this.mLatch = false;
	}

	/**
	 * @return True if the alarm was enabled before the changed listener was
	 *         called, and False otherwise.
	 */
	public boolean wasChanged()
	{
		return (this.getChangeTracker() != ChangeTracker.NONE);
	}

	/**
	 * Write data into parcel (required for Parcelable).
	 *
	 * Update this when adding/removing an element.
	 */
	@Override
	public void writeToParcel(Parcel output, int flags)
	{
		output.writeInt(this.getId());
		output.writeInt(this.getEnabled() ? 1 : 0);
		output.writeInt(this.getHour());
		output.writeInt(this.getMinute());
		output.writeInt(NacCalendar.Days.daysToValue(this.getDays()));
		output.writeInt(this.getRepeat() ? 1 : 0);
		output.writeInt(this.getUseNfc() ? 1 : 0);
		output.writeInt(this.getVibrate() ? 1 : 0);
		output.writeInt(this.getVolume());
		output.writeString(this.getAudioSource());
		output.writeInt(this.getMediaType());
		output.writeString(this.getMediaPath());
		output.writeString(this.getMediaTitle());
		output.writeString(this.getName());
	}

	/**
	 * Generate parcel (required for Parcelable).
	 */
	public static final Parcelable.Creator<NacAlarm> CREATOR = new
		Parcelable.Creator<NacAlarm>()
	{
		public NacAlarm createFromParcel(Parcel input)
		{
			return new NacAlarm(input);
		}

		public NacAlarm[] newArray(int size)
		{
			return new NacAlarm[size];
		}
	};

}
