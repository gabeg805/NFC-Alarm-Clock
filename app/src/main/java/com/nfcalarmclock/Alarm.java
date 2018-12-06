package com.nfcalarmclock;

import android.app.AlarmManager;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * @brief Alarm.
 */
public class Alarm
{

	/**
	 * Definition for the change listener object.
	 */
	public interface OnChangedListener
	{
		public void onChanged(Alarm alarm);
	}

	/**
	 * Change listener.
	 */
	private OnChangedListener mChangeListener;

	/**
	 * A list of possible days the alarm can run on.
	 */
	public class Days
	{
		public static final byte NONE = 0;
		public static final byte SUNDAY = 1;
		public static final byte MONDAY = 2;
		public static final byte TUESDAY = 4;
		public static final byte WEDNESDAY = 8;
		public static final byte THURSDAY = 16;
		public static final byte FRIDAY = 32;
		public static final byte SATURDAY = 64;
	}

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
	private int mDays;

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
	 * Week days.
	 */
	private List<Byte> mWeekDays = new ArrayList<Byte>(Arrays.asList(
		Days.SUNDAY, Days.MONDAY, Days.TUESDAY, Days.WEDNESDAY, Days.THURSDAY,
		Days.FRIDAY, Days.SATURDAY));

	/**
	 * Calendar days.
	 */
	private List<Integer> mCalendarDays = new ArrayList<Integer>(Arrays.asList(
		Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
		Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY));

	/**
	 * Use the default set values for an alarm.
	 */
	public Alarm()
	{
		Calendar cal = Calendar.getInstance();

		this.setOnChangedListener(null);
		this.setId(-1);
		this.setEnabled(true);
		this.set24HourFormat(true);
		this.setHour(cal.get(Calendar.HOUR_OF_DAY));
		this.setMinute(cal.get(Calendar.MINUTE));
		this.setDays(Days.MONDAY|Days.TUESDAY|Days.WEDNESDAY|Days.THURSDAY|Days.FRIDAY);
		this.setRepeat(true);
		this.setVibrate(true);
		this.setSound("");
		this.setName("");

	}

	/**
	 * Set the 24 hour format.
	 */
	public Alarm(boolean state)
	{
		this();
		this.set24HourFormat(state);
	}

	/**
	 * Set the id and 24 hour format.
	 */
	public Alarm(boolean state, int id)
	{
		this(state);
		this.setId(id);
	}

	/**
	 * Set the days the alarm will run.
	 */
	public Alarm(int days)
	{
		this();
		this.setDays(days);
	}

	/**
	 * Set the hour and minute to run the alarm at.
	 */
	public Alarm(int hour, int minute)
	{
		this();
		this.setHour(hour);
		this.setMinute(minute);
	}

	/**
	 * Set the time and date to run the alarm at.
	 */
	public Alarm(int hour, int minute, int days)
	{
		this(hour, minute);
		this.setDays(days);
		this.setRepeat(true);
	}

	/**
	 * Set the name and the time to run the alarm at.
	 */
	public Alarm(String name, int hour, int minute)
	{
		this(hour, minute);
		this.setName(name);
	}

	/**
	 * Print all values in the alarm object.
	 */
	public void print()
	{
		NacUtility.printf("Id      : %d", this.mId);
		NacUtility.printf("Enabled : %b", this.mEnabled);
		NacUtility.printf("Hour    : %d", this.mHour);
		NacUtility.printf("Minute  : %d", this.mMinute);
		NacUtility.printf("Days    : %s (%d)", this.getDaysString(), this.getDays());
		NacUtility.printf("Repeat  : %b", this.mRepeat);
		NacUtility.printf("Vibrate : %b", this.mVibrate);
		NacUtility.printf("Sound   : %s", this.mSound);
		NacUtility.printf("Name    : %s", this.mName);
		NacUtility.printf("Format  : %s", this.m24HourFormat);
		NacUtility.printf("\n\n");
	}

	/**
	 * Call the listener when the alarm info has changed.
	 */
	public void changed()
	{
		if (this.hasListener())
		{
			this.mChangeListener.onChanged(this);
		}
	}

	/**
	 * Copy this alarm.
	 *
	 * @return A copy of this alarm.
	 */
	public Alarm copy()
	{
		Alarm copy = new Alarm();

		copy.setId(this.getId());
		copy.setEnabled(this.getEnabled());
		copy.set24HourFormat(this.get24HourFormat());
		copy.setHour(this.getHour());
		copy.setMinute(this.getMinute());
		copy.setDays(this.getDays());
		copy.setRepeat(this.getRepeat());
		copy.setVibrate(this.getVibrate());
		copy.setSound(this.getSound());
		copy.setName(this.getName());

		return copy;
	}

	/**
	 * Set a listener for when the alarm is changed.
	 * 
	 * @param  listener  The change listener.
	 */
	public void setOnChangedListener(OnChangedListener listener)
	{
		this.mChangeListener = listener;
	}

	/**
	 * Set the ID.
	 */
	public void setId(int id)
	{
		this.mId = id;
	}

	/**
	 * Set the enabled/disabled status of the alarm.
	 */
	public void setEnabled(boolean state)
	{
		this.mEnabled = state;
	}

	/**
	 * Set the 24 hour format indicator.
	 */
	public void set24HourFormat(boolean state)
	{
		this.m24HourFormat = state;
	}

	/**
	 * Set the hour at which to run the alarm.
	 */
	public void setHour(int hour)
	{
		this.mHour = hour;
	}

	/**
	 * Set the minute at which to run the alarm.
	 */
	public void setMinute(int minute)
	{
		this.mMinute = minute;
	}

	/**
	 * Set the days on which the alarm will be run.
	 */
	public void setDays(int days)
	{
		this.mDays = days;
	}

	/**
	 * Toggle a day.
	 */
	public void toggleDay(byte day)
	{
		this.setDays(mDays^day);
	}

	/**
	 * Toggle today.
	 */
	public void toggleToday()
	{
		byte day = this.getToday();

		this.toggleDay(day);
	}

	/**
	 * @return Today's day.
	 */
	public byte getToday()
	{
		Calendar c = Calendar.getInstance();
		int day = c.get(Calendar.DAY_OF_WEEK);
		int index = this.getCalendarDays().indexOf(day);

		return this.getWeekDays().get(index);
	}

	/**
	 * Set whether the alarm should be repeated or not.
	 */
	public void setRepeat(boolean state)
	{
		this.mRepeat = state;
	}

	/**
	 * Set whether or not the phone should vibrate when the alarm is activated.
	 */
	public void setVibrate(boolean state)
	{
		this.mVibrate = state;
	}

	/**
	 * Set the sound that will be played when the alarm is activated.
	 */
	public void setSound(String sound)
	{
		this.mSound = (sound != null) ? sound : "";
	}

	/**
	 * Set the name of the alarm.
	 */
	public void setName(String name)
	{
		this.mName = (name != null) ? name : "";
	}

	/**
	 * @return The ID.
	 */
	public int getId()
	{
		return this.mId;
	}

	/**
	 * @return The ID offset by the given day.
	 */
	public int getId(Calendar c)
	{
		int day = c.get(Calendar.DAY_OF_WEEK);
		int offset = this.getCalendarDays().indexOf(day);

		return this.getId()+offset;
	}

	/**
	 * @return The enabled flag for the alarm.
	 */
	public boolean getEnabled()
	{
		return this.mEnabled;
	}

	/**
	 * @return The 24 hour format indicator.
	 */
	public boolean get24HourFormat()
	{
		return this.m24HourFormat;
	}

	/**
	 * @return The hour at which to run the alarm.
	 */
	public int getHour()
	{
		return this.mHour;
	}

	/**
	 * @return The minutes at which to run the alarm.
	 */
	public int getMinute()
	{
		return this.mMinute;
	}

	/**
	 * @return The hour value in String format.
	 */
	public String getHourString()
	{
		int hour = this.getHour();

		if (!this.is24HourFormat())
		{
			if (hour > 12)
			{
				hour = (hour % 12);
			}
			else
			{
				if (hour == 0)
				{
					hour = 12;
				}
			}
		}

		return String.valueOf(hour);
	}

	/**
	 * @return The minutes value in String format.
	 */
	public String getMinuteString()
	{
		return String.format(Locale.getDefault(), "%02d", this.getMinute());
	}

	/**
	 * @return The time string.
	 */
	public String getTime()
	{
		return this.getHourString()+":"+this.getMinuteString();
	}

	/**
	 * @return The meridian (AM or PM).
	 */
	public String getMeridian()
	{
		if (this.is24HourFormat())
		{
			return "";
		}

		if (this.getHour() < 12)
		{
			return "AM";
		}
		else
		{
			return "PM";
		}
	}

	/**
	 * @return The days on which to run the alarm.
	 */
	public int getDays()
	{
		return this.mDays;
	}

	/**
	 * @return The default days on which to run the alarm.
	 */
	public static int getDaysDefault()
	{
		return Days.MONDAY | Days.TUESDAY | Days.WEDNESDAY | Days.THURSDAY | Days.FRIDAY;
	}

	/**
	 * @return All the days of week.
	 */
	public List<Byte> getWeekDays()
	{
		return this.mWeekDays;
	}

	/**
	 * @return All the days of week.
	 */
	public List<Integer> getCalendarDays()
	{
		return this.mCalendarDays;
	}

	/**
	 * @return The calendar instance with the specified alarm's time.
	 */
	public List<Calendar> getNextCalendars()
	{
		List<Calendar> calendars = getCalendars();
		List<Calendar> next = new ArrayList<>();
		int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

		for (Calendar c : calendars)
		{
			int nextdow = c.get(Calendar.DAY_OF_WEEK);

			if (!this.getRepeat() && (nextdow < dow))
			{
				continue;
			}

			next.add(c);
		}

		return next;
	}

	/**
	 * @return The calendar instances of all days the alarm is set to run.
	 */
	public List<Calendar> getCalendars()
	{
		Calendar today = Calendar.getInstance();
		List<Calendar> calendars = new ArrayList<>();
		List<Byte> weekdays = this.getWeekDays();
		List<Integer> caldays = this.getCalendarDays();

		for (int i=0; i < weekdays.size(); i++)
		{
			if (!this.isDay(weekdays.get(i)))
			{
				continue;
			}

			Calendar c = Calendar.getInstance();

			c.set(Calendar.DAY_OF_WEEK, caldays.get(i));
			c.set(Calendar.HOUR_OF_DAY, this.getHour());
			c.set(Calendar.MINUTE, this.getMinute());
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);

			if (c.before(today))
			{
				c.add(Calendar.DAY_OF_MONTH, 7);
			}

			calendars.add(c);
		}

		return calendars;
	}

	/**
	 * @return Comma separated string of days to repeat alarm on.
	 */
	public String getDaysString()
	{
		String string = "";
		String[] names = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
		List<Byte> dow = this.getWeekDays();

		for (int i=0; i < dow.size(); i++)
		{
			if (this.isDay(dow.get(i)))
			{
				if (!string.isEmpty())
				{
					string += ",";
				}

				string += names[i];
			}
		}

		return string;
		//return (string.isEmpty()) ? "Never" : conv;
	}

	/**
	 * @return The default days string. This should be used if getDaysString
	 *         is empty.
	 */
	public static String getDaysStringDefault()
	{
		return "None";
	}

	/**
	 * @return The default message for the days string.
	 */
	public static String getDaysStringMessage()
	{
		return "No day selected";
	}

	/**
	 * @return Whether the alarm should be repeated or not.
	 */
	public boolean getRepeat()
	{
		return this.mRepeat;
	}

	/**
	 * @return The default repeat state.
	 */
	public static boolean getRepeatDefault()
	{
		return true;
	}

	/**
	 * @return Whether or not the phone should vibrate when the alarm is run.
	 */
	public boolean getVibrate()
	{
		return this.mVibrate;
	}

	/**
	 * @return The default vibrate state.
	 */
	public static boolean getVibrateDefault()
	{
		return true;
	}

	/**
	 * @return The sound that will be played when the alarm is activated.
	 */
	public String getSound()
	{
		return this.mSound;
	}

	/**
	 * @return The default sound.
	 */
	public static String getSoundDefault()
	{
		return "";
	}

	/**
	 * @return The sound name.
	 */
	public String getSoundName(Context context)
	{
		String path = this.getSound();

		if (path.isEmpty())
		{
			NacUtility.printf("Path in alarm : %s", path);
			return "";
		}

		Uri uri = Uri.parse(path);
		Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
		String name = ringtone.getTitle(context);

		ringtone.stop();

		NacUtility.printf("Path name in alarm : %s", name);
		return name;
	}

	/**
	 * @return The default sound name string.
	 */
	public static String getSoundNameDefault()
	{
		return "None";
	}

	/**
	 * @return The default sound name message.
	 */
	public static String getSoundNameMessage()
	{
		return "Song or ringtone";
	}

	/**
	 * @return The name of the alarm.
	 */
	public String getName()
	{
		return this.mName;
	}

	/**
	 * @return The default name of the alarm.
	 */
	public static String getNameDefault()
	{
		return "None";
	}

	/**
	 * @return The default name message.
	 */
	public static String getNameMessage()
	{
		return "Alarm name";
	}

	/**
	 * Check if 24 hour format is enabled.
	 */
	public boolean is24HourFormat()
	{
		return this.m24HourFormat;
	}

	/**
	 * Check if the given index of day will run the alarm.
	 */
	public boolean isDay(int index)
	{
		return ((this.getDays() & this.getWeekDays().get(index)) != 0);
	}

	/**
	 * Check if the given day will run the alarm.
	 */
	public boolean isDay(byte d)
	{
		return ((this.getDays() & d) != 0);
	}

	/**
	 * Check if alarm has a listener.
	 */
	public boolean hasListener()
	{
		return (this.mChangeListener != null);
	}

}
