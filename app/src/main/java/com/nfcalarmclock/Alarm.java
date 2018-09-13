package com.nfcalarmclock;

import android.app.AlarmManager;
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
	 * @brief Definition for the change listener object.
	 */
	public interface OnChangedListener
	{
		public void onChanged(Alarm alarm);
	}

	/**
	 * @brief Change listener.
	 */
	private OnChangedListener mChangeListener = null;

	/**
	 * @brief A list of possible days the alarm can run on.
	 */
	public class Days
	{
		public final static byte NONE = 0;
		public final static byte SUNDAY = 1;
		public final static byte MONDAY = 2;
		public final static byte TUESDAY = 4;
		public final static byte WEDNESDAY = 8;
		public final static byte THURSDAY = 16;
		public final static byte FRIDAY = 32;
		public final static byte SATURDAY = 64;
	}

	/**
	 * @brief The alarm ID.
	 */
	private int mId = -1;

	/**
	 * @brief Indicates whether the alarm is enabled or not.
	 */
	private boolean mEnabled = true;

	/**
	 * @brief Indicator if using a 24 hour format to display time or not.
	 */
	private boolean m24HourFormat = true;

	/**
	 * @brief The hour at which to run the alarm.
	 */
	private int mHour = 0;

	/**
	 * @brief The minute at which to run the alarm.
	 */
	private int mMinute = 0;

	/**
	 * @brief The days on which to run the alarm.
	 */
	private int mDays = Days.MONDAY|Days.TUESDAY|Days.WEDNESDAY|Days.THURSDAY|Days.FRIDAY;

	/**
	 * @brief Indicates whether the alarm should be repeated or not.
	 */
	private boolean mRepeat = true;

	/**
	 * @brief Indicates whether the phone should vibrate when the alarm is run.
	 */
	private boolean mVibrate = true;

	/**
	 * @brief The sound to play when the alarm is run.
	 */
	private String mSound = "";

	/**
	 * @brief The name of the alarm.
	 */
	private String mName = "";

	/**
	 * @brief Week days.
	 */
	private List<Byte> mWeekDays = new ArrayList<Byte>(Arrays.asList(
		Days.SUNDAY, Days.MONDAY, Days.TUESDAY, Days.WEDNESDAY, Days.THURSDAY,
		Days.FRIDAY, Days.SATURDAY));

	/**
	 * @brief Calendar days.
	 */
	private List<Integer> mCalendarDays = new ArrayList<Integer>(Arrays.asList(
		Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
		Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY));

	/**
	 * @brief Use the default set values for an alarm.
	 */
	public Alarm()
	{
		Calendar cal = Calendar.getInstance();
		this.mChangeListener = null;

		this.setHour(cal.get(Calendar.HOUR_OF_DAY));
		this.setMinute(cal.get(Calendar.MINUTE));
	}

	/**
	 * @brief Set the id and 24 hour format.
	 */
	public Alarm(boolean state, int id)
	{
		this(state);
		this.setId(id);
	}

	/**
	 * @brief Set the 24 hour format.
	 */
	public Alarm(boolean state)
	{
		this();
		this.set24HourFormat(state);
	}

	/**
	 * @brief Set the hour and minute to run the alarm at.
	 */
	public Alarm(int hour, int minute)
	{
		this.mChangeListener = null;

		this.setHour(hour);
		this.setMinute(minute);
	}

	/**
	 * @brief Set the time and date to run the alarm at.
	 */
	public Alarm(int hour, int minute, int days)
	{
		this(hour, minute);
		this.setDays(days);
	}

	/**
	 * @brief Set the name and the time to run the alarm at.
	 */
	public Alarm(String name, int hour, int minute)
	{
		this(hour, minute);
		this.setName(name);
	}

	/**
	 * @brief Print all values in the alarm object.
	 */
	public void print()
	{
		NacUtility.printf("Id	   : %d", this.mId);
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
	 * @brief Call the listener when the alarm info has changed.
	 */
	public void changed()
	{
		if (this.mChangeListener != null)
		{
			NacUtility.print("Calling ALARM Changed().");
			this.mChangeListener.onChanged(this);
		}
	}

	/**
	 * @brief Set a listener for when the alarm is changed.
	 * 
	 * @param  listener  The change listener.
	 */
	public void setOnChangedListener(OnChangedListener listener)
	{
		this.mChangeListener = listener;
	}

	/**
	 * @brief Set the ID.
	 */
	public void setId(int id)
	{
		this.mId = id;
	}

	/**
	 * @brief Set the enabled/disabled status of the alarm.
	 */
	public void setEnabled(boolean state)
	{
		this.mEnabled = state;
	}

	/**
	 * @brief Set the 24 hour format indicator.
	 */
	public void set24HourFormat(boolean state)
	{
		this.m24HourFormat = state;
	}

	/**
	 * @brief Set the hour at which to run the alarm.
	 */
	public void setHour(int hour)
	{
		this.mHour = hour;
	}

	/**
	 * @brief Set the minute at which to run the alarm.
	 */
	public void setMinute(int minute)
	{
		this.mMinute = minute;
	}

	/**
	 * @brief Set the days on which the alarm will be run.
	 */
	public void setDays(int days)
	{
		this.mDays = days;
		this.setRepeat(true);
	}

	/**
	 * @brief Toggle a day.
	 */
	public void toggleDay(int day)
	{
		this.setDays(mDays^day);
	}

	/**
	 * @brief Set whether the alarm should be repeated or not.
	 */
	public void setRepeat(boolean state)
	{
		this.mRepeat = state;
	}

	/**
	 * @brief Set whether or not the phone should vibrate when the alarm is
	 *		  activated.
	 */
	public void setVibrate(boolean state)
	{
		this.mVibrate = state;
	}

	/**
	 * @brief Set the sound that will be played when the alarm is activated.
	 */
	public void setSound(String sound)
	{
		this.mSound = sound;
	}

	/**
	 * @brief Set the name of the alarm.
	 */
	public void setName(String name)
	{
		this.mName = name;
	}

	/**
	 * @brief Return the ID.
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
	 * @brief Return the enabled flag for the alarm.
	 */
	public boolean getEnabled()
	{
		return this.mEnabled;
	}

	/**
	 * @brief Return the 24 hour format indicator.
	 */
	public boolean get24HourFormat()
	{
		return this.m24HourFormat;
	}

	/**
	 * @brief Return the hour at which to run the alarm.
	 */
	public int getHour()
	{
		return this.mHour;
	}

	/**
	 * @brief Return the minutes at which to run the alarm.
	 */
	public int getMinute()
	{
		return this.mMinute;
	}

	/**
	 * @brief Return the hour value in String format.
	 */
	public String getHourString()
	{
		int hour = this.getHour();

		if (!this.m24HourFormat)
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
	 * @brief Return the minutes value in String format.
	 */
	public String getMinuteString()
	{
		return String.format(Locale.getDefault(), "%02d", this.mMinute);
	}

	/**
	 * @brief Return the time string.
	 */
	public String getTime()
	{
		return this.getHourString()+":"+this.getMinuteString();
	}

	/**
	 * @brief Return the meridian (AM or PM).
	 */
	public String getMeridian()
	{
		if (this.m24HourFormat)
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
	 * @brief Return the days on which to run the alarm.
	 */
	public int getDays()
	{
		return this.mDays;
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
	 * @brief Convert days to comma separated string.
	 * 
	 * @return Comma separated string of days to repeat alarm on.
	 */
	public String getDaysString()
	{
		String conv = "";
		String[] names = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
		List<Byte> dow = this.getWeekDays();

		for (int i=0; i < dow.size(); i++)
		{
			if (this.isDay(dow.get(i)))
			{
				if (!conv.isEmpty())
				{
					conv += ",";
				}

				conv += names[i];
			}
		}

		return conv;
	}

	/**
	 * @brief Return whether the alarm should be repeated or not.
	 */
	public boolean getRepeat()
	{
		return this.mRepeat;
	}

	/**
	 * @brief Return whether or not the phone should vibrate when the alarm is
	 *		  activated.
	 */
	public boolean getVibrate()
	{
		return this.mVibrate;
	}

	/**
	 * @brief Return the sound that will be played when the alarm is activated.
	 */
	public String getSound()
	{
		return this.mSound;
	}

	/**
	 * @brief Return the name of the alarm.
	 */
	public String getName()
	{
		return this.mName;
	}

	/**
	 * @brief Check if the given day will run the alarm.
	 */
	public boolean isDay(byte d)
	{
		return ((this.getDays() & d) != 0);
	}

}
