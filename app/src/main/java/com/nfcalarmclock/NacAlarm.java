package com.nfcalarmclock;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;
import java.util.Calendar;
import java.util.EnumSet;

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
		 * Indicates whether the phone should vibrate when the alarm is run.
		 */
		private boolean mVibrate;

		/**
		 * The type of sound to play.
		 */
		private int mSoundType;

		/**
		 * The path to the sound to play.
		 */
		private String mSoundPath;

		/**
		 * The name of the sound to play.
		 */
		private String mSoundName;

		/**
		 * The name of the alarm.
		 */
		private String mName;

		/**
		 */
		public Builder()
		{
			this(Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
				Calendar.getInstance().get(Calendar.MINUTE));
		}

		/**
		 */
		public Builder(int hour, int min)
		{
			this(-1, hour, min);
		}

		/**
		 */
		public Builder(int id, int hour, int min)
		{
			this.mOnChangeListener = null;
			this.mId = id;
			this.mHour = hour;
			this.mMinute = min;
			this.mEnabled = true;
			this.mDays = EnumSet.noneOf(NacCalendar.Day.class);
			this.mRepeat = true;
			this.mVibrate = true;
			this.mSoundType = NacSound.TYPE_NONE;
			this.mSoundPath = "";
			this.mSoundName = "";
			this.mName = "";
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
		public String getSound()
		{
			return this.getSoundPath();
		}

		/**
		 * @return The path to the media file to play when the alarm goes off.
		 */
		public String getSoundName()
		{
			return this.mSoundName;
		}

		/**
		 * @return The path to the media file to play when the alarm goes off.
		 */
		public String getSoundPath()
		{
			return this.mSoundPath;
		}

		/**
		 * @return The path to the media file to play when the alarm goes off.
		 */
		public int getSoundType()
		{
			return this.mSoundType;
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
			return this.setDays(NacCalendar.valueToDays(value));
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
		 * Set the path, name, and type of the sound to play.
		 *
		 * @param  context  The application context.
		 * @param  path  The path to the sound to play.
		 *
		 * @return The Builder.
		 */
		public Builder setSound(Context context, String path)
		{
			String name = NacSound.getName(context, path);
			int type = NacSound.getType(path);

			this.setSoundPath(path);
			this.setSoundName(name);
			this.setSoundType(type);

			return this;
		}

		/**
		 * Set the sound name.
		 *
		 * @param  name  The name of the media file to play.
		 *
		 * @return The Builder.
		 */
		public Builder setSoundName(String name)
		{
			this.mSoundName = (name != null) ? name : "";

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
		public Builder setSoundPath(String path)
		{
			this.mSoundPath = (path != null) ? path : "";

			return this;
		}

		/**
		 * Set the type of sound to play.
		 *
		 * @param  type  The type of media file to play.
		 *
		 * @return The Builder.
		 */
		public Builder setSoundType(int type)
		{
			this.mSoundType = type;

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
	 * Indicates whether the phone should vibrate when the alarm is run.
	 */
	private boolean mVibrate;

	/**
	 * The type of sound to play.
	 */
	private int mSoundType;

	/**
	 * The path to the sound to play.
	 */
	private String mSoundPath;

	/**
	 * The name of the sound to play.
	 */
	private String mSoundName;

	/**
	 * The name of the alarm.
	 */
	private String mName;

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
		this.setVibrate(builder.getVibrate());
		this.setSoundType(builder.getSoundType());
		this.setSoundPath(builder.getSoundPath());
		this.setSoundName(builder.getSoundName());
		this.setName(builder.getName());
	}

	/**
	 * Populate values with input parcel.
	 */
	public NacAlarm(Parcel input)
	{
		this.setOnChangeListener(null);
		this.setId(input.readInt());
		this.setEnabled((input.readInt() != 0));
		this.setHour(input.readInt());
		this.setMinute(input.readInt());
		this.setDays(input.readInt());
		this.setRepeat((input.readInt() != 0));
		this.setVibrate((input.readInt() != 0));
		this.setSoundType(input.readInt());
		this.setSoundPath(input.readString());
		this.setSoundName(input.readString());
		this.setName(input.readString());
	}

	/**
	 * Check if any days are selected.
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
			.setVibrate(this.getVibrate())
			.setSoundType(this.getSoundType())
			.setSoundPath(this.getSoundPath())
			.setSoundName(this.getSoundName())
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
		return ((this.getId() == alarm.getId())
			&& (this.getEnabled() == alarm.getEnabled())
			&& (this.getHour() == alarm.getHour())
			&& (this.getMinute() == alarm.getMinute())
			&& (this.getDays() == alarm.getDays())
			&& (this.getRepeat() == alarm.getRepeat())
			&& (this.getVibrate() == alarm.getVibrate())
			&& (this.getSoundType() == alarm.getSoundType())
			&& (this.getSoundPath() == alarm.getSoundPath())
			&& (this.getSoundName() == alarm.getSoundName())
			&& (this.getName() == alarm.getName()));
	}

	/**
	 * @return The 24 hour format.
	 */
	public boolean is24HourFormat(Context context)
	{
		return DateFormat.is24HourFormat(context);
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
		int offset = NacCalendar.toIndex(day);

		return this.getId() + offset;
	}

	/**
	 * @return The meridian (AM or PM).
	 */
	public String getMeridian(Context context)
	{
		return NacCalendar.getMeridian(this.getHour(),
			this.is24HourFormat(context));
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
	 * @return The path to the media file to play when the alarm goes off.
	 */
	public String getSound()
	{
		return this.getSoundPath();
	}

	/**
	 * @return The path to the media file to play when the alarm goes off.
	 */
	public String getSoundName()
	{
		return this.mSoundName;
	}

	/**
	 * @return The path to the media file to play when the alarm goes off.
	 */
	public String getSoundPath()
	{
		return this.mSoundPath;
	}

	/**
	 * @return The path to the media file to play when the alarm goes off.
	 */
	public int getSoundType()
	{
		return this.mSoundType;
	}

	/**
	 * @return The time string.
	 */
	public String getTime(Context context)
	{
		return NacCalendar.getTime(this.getHour(), this.getMinute(),
			this.is24HourFormat(context));
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
	 * Check if alarm has a listener.
	 */
	public boolean hasListener()
	{
		return (this.getOnChangeListener() != null);
	}

	/**
	 * Check if the alarm is a one-time alarm.
	 */
	public boolean isOneTimeAlarm()
	{
		return (!this.getRepeat() || !this.areDaysSelected());
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
		NacUtility.printf("Days         : %s", NacCalendar.toString(this.getDays()));
		NacUtility.printf("Repeat       : %b", this.mRepeat);
		NacUtility.printf("Vibrate      : %b", this.mVibrate);
		NacUtility.printf("Sound Type   : %s", this.mSoundType);
		NacUtility.printf("Sound Path   : %s", this.mSoundPath);
		NacUtility.printf("Sound Name   : %s", this.mSoundName);
		NacUtility.printf("Name         : %s", this.mName);
	}

	/**
	 * Set the days to the run the alarm.
	 *
	 * @param  days  The set of days to run the alarm on.
	 */
	public void setDays(EnumSet<NacCalendar.Day> days)
	{
		this.mDays = days;
	}

	/**
	 * @see setDays
	 */
	public void setDays(int value)
	{
		this.setDays(NacCalendar.valueToDays(value));
	}

	/**
	 * Set whether the alarm is enabled or not.
	 *
	 * @param  enabled  True if the alarm is enabled and False otherwise.
	 */
	public void setEnabled(boolean enabled)
	{
		this.mEnabled = enabled;
	}

	/**
	 * Set the hour.
	 *
	 * @param  hour  The hour at which to run the alarm.
	 */
	public void setHour(int hour)
	{
		this.mHour = hour;
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
	}

	/**
	 * Set the sound to play when the alarm goes off.
	 *
	 * @param  context  The application context.
	 * @param  path  The path to sound to play.
	 */
	public void setSound(Context context, String path)
	{
		String name = NacSound.getName(context, path);
		int type = NacSound.getType(path);

		this.setSoundPath(path);
		this.setSoundName(name);
		this.setSoundType(type);
	}

	/**
	 * Set the sound name.
	 *
	 * @param  name  The name of the media file to play.
	 */
	public void setSoundName(String name)
	{
		this.mSoundName = (name != null) ? name : "";
	}

	/**
	 * Set the sound to play when the alarm goes off.
	 *
	 * @param  path  The path to the media file to play when the alarm goes
	 *               off.
	 */
	public void setSoundPath(String path)
	{
		this.mSoundPath = (path != null) ? path : "";
	}

	/**
	 * Set the type of sound to play.
	 *
	 * @param  type  The type of media file to play.
	 */
	public void setSoundType(int type)
	{
		this.mSoundType = type;
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
	}

	/**
	 * Toggle the day at the given index.
	 *
	 * @param  index  The index of the day.
	 */
	public void toggleIndex(int index)
	{
		NacCalendar.Day day = NacCalendar.fromIndex(index);

		this.toggleDay(day);
	}

	/**
	 * Toggle today.
	 */
	public void toggleToday()
	{
		NacCalendar.Day day = NacCalendar.getToday();

		this.toggleDay(day);
	}

	/**
	 * Toggle the day(s) with the given value.
	 *
	 * @param  value  The value of a day or multiple days.
	 */
	public void toggleValue(int value)
	{
		EnumSet<NacCalendar.Day> days = NacCalendar.valueToDays(value);

		for (NacCalendar.Day d : days)
		{
			this.toggleDay(d);
		}
	}

	/**
	 * Write data into parcel (required for Parcelable).
	 */
	@Override
	public void writeToParcel(Parcel output, int flags)
	{
		output.writeInt(this.getId());
		output.writeInt(this.getEnabled() ? 1 : 0);
		output.writeInt(this.getHour());
		output.writeInt(this.getMinute());
		output.writeInt(NacCalendar.daysToValue(this.getDays()));
		output.writeInt(this.getRepeat() ? 1 : 0);
		output.writeInt(this.getVibrate() ? 1 : 0);
		output.writeInt(this.getSoundType());
		output.writeString(this.getSoundPath());
		output.writeString(this.getSoundName());
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
