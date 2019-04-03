package com.nfcalarmclock;

import java.util.Calendar;
import java.util.EnumSet;

/**
 * Alarm object.
 */
public class NacAlarm
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
		 * Indicator if using a 24 hour format to display time or not.
		 */
		private boolean m24HourFormat;

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
		 * The sound to play when the alarm is run.
		 */
		private String mSound;

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
			this.m24HourFormat = false;
			this.mDays = EnumSet.noneOf(NacCalendar.Day.class);
			this.mRepeat = true;
			this.mVibrate = true;
			this.mSound = "";
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
		 * @return True if using 24 time format, and False if using 12 hour
		 *         format.
		 */
		public boolean get24HourFormat()
		{
			return this.m24HourFormat;
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
			return this.mSound;
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
		 * Set the time format to 12 or 24 hour.
		 *
		 * @param  format  True indicates using 24 hour format, and False
		 *                 indicates 12 hour format.
		 *
		 * @return The Builder.
		 */
		public Builder set24HourFormat(boolean format)
		{
			this.m24HourFormat = format;

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
		 * Set the sound to play when the alarm goes off.
		 *
		 * @param  path  The path to the media file to play when the alarm goes
		 *               off.
		 *
		 * @return The Builder.
		 */
		public Builder setSound(String path)
		{
			this.mSound = (path != null) ? path : "";

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
	 * The sound to play when the alarm is run.
	 */
	private String mSound;

	/**
	 * The name of the alarm.
	 */
	private String mName;

	/**
	 * Indicates whether the alarm is enabled or not.
	 */
	private boolean mEnabled;

	/**
	 * Indicator if using a 24 hour format to display time or not.
	 */
	private boolean m24HourFormat;

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
		this.setHour(builder.getHour());
		this.setMinute(builder.getMinute());
		this.setDays(builder.getDays());
		this.setRepeat(builder.getRepeat());
		this.setVibrate(builder.getVibrate());
		this.setSound(builder.getSound());
		this.setName(builder.getName());
		this.setEnabled(builder.getEnabled());
		this.set24HourFormat(builder.get24HourFormat());
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
			.setHour(this.getHour())
			.setMinute(this.getMinute())
			.setDays(this.getDays())
			.setRepeat(this.getRepeat())
			.setVibrate(this.getVibrate())
			.setSound(this.getSound())
			.setName(this.getName())
			.setEnabled(this.getEnabled())
			.set24HourFormat(this.get24HourFormat())
			.build();
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
			&& (this.getHour() == alarm.getHour())
			&& (this.getMinute() == alarm.getMinute())
			&& (this.getDays() == alarm.getDays())
			&& (this.getRepeat() == alarm.getRepeat())
			&& (this.getVibrate() == alarm.getVibrate())
			&& (this.getSound() == alarm.getSound())
			&& (this.getName() == alarm.getName())
			&& (this.getEnabled() == alarm.getEnabled())
			&& (this.get24HourFormat() == alarm.get24HourFormat()));
	}

	/**
	 * @return True if using 24 time format, and False if using 12 hour
	 *         format.
	 */
	public boolean get24HourFormat()
	{
		return this.m24HourFormat;
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
	public String getMeridian()
	{
		return NacCalendar.getMeridian(this.getHour(), this.get24HourFormat());
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
		return this.mSound;
	}

	/**
	 * @return The time string.
	 */
	public String getTime()
	{
		return NacCalendar.getTime(this.getHour(), this.getMinute(),
			this.get24HourFormat());
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
	 * Print all values in the alarm object.
	 */
	public void print()
	{
		NacUtility.printf("Alarm Information");
		NacUtility.printf("Id           : %d", this.mId);
		NacUtility.printf("Hour         : %d", this.mHour);
		NacUtility.printf("Minute       : %d", this.mMinute);
		NacUtility.printf("Days         : %s", NacCalendar.toString(this.getDays()));
		NacUtility.printf("Repeat       : %b", this.mRepeat);
		NacUtility.printf("Vibrate      : %b", this.mVibrate);
		NacUtility.printf("Sound        : %s", this.mSound);
		NacUtility.printf("Name         : %s", this.mName);
		NacUtility.printf("Enabled      : %b", this.mEnabled);
		NacUtility.printf("24 hr Format : %s", this.m24HourFormat);
	}

	/**
	 * Set the time format to 12 or 24 hour.
	 *
	 * @param  format  True indicates using 24 hour format, and False
	 *                 indicates 12 hour format.
	 */
	public void set24HourFormat(boolean format)
	{
		this.m24HourFormat = format;
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
	 * @param  path  The path to the media file to play when the alarm goes
	 *               off.
	 */
	public void setSound(String path)
	{
		this.mSound = (path != null) ? path : "";
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

}
