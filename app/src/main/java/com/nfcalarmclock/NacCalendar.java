package com.nfcalarmclock;

import android.content.Context;
import android.text.format.DateFormat;
import java.lang.System;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

/**
 * A list of possible days the alarm can run on.
 */
public class NacCalendar
{

	/**
	 * Day of week.
	 */
	enum Day
	{

		SUNDAY(1), MONDAY(2), TUESDAY(4), WEDNESDAY(8), THURSDAY(16),
		FRIDAY(32), SATURDAY(64);

		/**
		 * The value associated with an enum.
		 */
		private final int mValue;

		/**
		 */
		private Day(int value)
		{
			this.mValue = value;
		}

		/**
		 * @return The value of an enum.
		 */
		public int getValue()
		{
			return this.mValue;
		}

	}

	/**
	 * Every day of week enum.
	 */
	public static final EnumSet<Day> WEEK = EnumSet.allOf(Day.class);

	/**
	 * @return The next calendar in the list.
	 */
	public static Calendar getNext(List<Calendar> calendars)
	{
		Calendar next = null;

		for (Calendar c : calendars)
		{
			next = ((next == null) || !next.before(c)) ? c : next;
		}

		return next;
	}

	/**
	 * @return The message to display when the next action will occur.
	 */
	public static String getNextMessage(Calendar next)
	{
		return "Next alarm on " + NacCalendar.toString(next, "EEE, h:mm a");
	}

	/**
	 * @return The message to display when the next action will occur.
	 */
	public static String getNextMessage(long millis)
	{
		long time = (millis - System.currentTimeMillis())
			/ 1000;
		long day = (time / (60*60*24)) % 365;
		long hr = (time / (60*60)) % 24;
		long min = (time / 60) % 60;
		long sec = time % 60;
		String dayunit = (day != 1) ? " days " : " day ";
		String hrunit = (hr != 1) ? " hours " : " hour ";
		String minunit = (min != 1) ? " minutes " : " minute ";
		String secunit = (sec != 1) ? " seconds " : " second ";
		String msg = "Next alarm in ";

		if (day > 0)
		{
			msg += String.valueOf(day)+dayunit+String.valueOf(hr)+hrunit;
		}
		else
		{
			if (hr > 0)
			{
				msg += String.valueOf(hr)+hrunit+String.valueOf(min)+minunit;
			}
			else
			{
				msg += String.valueOf(min)+minunit+String.valueOf(sec)+secunit;
			}
		}

		return msg;
	}

	/**
	 * @return The next message in the desired format.
	 */
	public static String getNextMessage(long millis, boolean timeRemaining)
	{
		if (timeRemaining)
		{
			return NacCalendar.getNextMessage(millis);
		}
		else
		{
			Calendar calendar = Calendar.getInstance();

			calendar.setTimeInMillis(millis);

			return NacCalendar.getNextMessage(calendar);
		}
	}

	/**
	 * @see getToday
	 */
	public static Calendar getToday()
	{
		return NacCalendar.getToday(null);
	}

	/**
	 * @return Today's day, with the alarm hour and minute, if supplied.
	 */
	public static Calendar getToday(NacAlarm alarm)
	{
		Calendar today = Calendar.getInstance();

		if (alarm != null)
		{
			today.set(Calendar.HOUR_OF_DAY, alarm.getHour());
			today.set(Calendar.MINUTE, alarm.getMinute());
			today.set(Calendar.SECOND, 0);
			today.set(Calendar.MILLISECOND, 0);
		}

		return today;
	}

	/**
	 * @see getTomorrow
	 */
	public static Calendar getTomorrow()
	{
		return NacCalendar.getTomorrow(null);
	}

	/**
	 * @return Tomorrow's day, with the alarm hour and minute, if supplied.
	 */
	public static Calendar getTomorrow(NacAlarm alarm)
	{
		Calendar tomorrow = NacCalendar.getToday(alarm);

		tomorrow.add(Calendar.DAY_OF_MONTH, 1);

		return tomorrow;
	}

	/**
	 * Convert the alarm on the given day to a Calendar.
	 *
	 * @param  alarm  The alarm.
	 * @param  day  The day to convert.
	 *
	 * @return A Calendar.
	 */
	public static Calendar toCalendar(NacAlarm alarm, Day day)
	{
		int dow = NacCalendar.Days.toCalendarDay(day);
		int hour = alarm.getHour();
		int minute = alarm.getMinute();
		Calendar calendar = Calendar.getInstance();

		calendar.set(Calendar.DAY_OF_WEEK, dow);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar;
	}

	/**
	 * Convert all the days an alarm is scheduled to go off, to Calendars.
	 *
	 * @param  alarm  The alarm.
	 *
	 * @return A list of Calendars.
	 */
	public static List<Calendar> toCalendars(NacAlarm alarm)
	{
		List<Calendar> calendars = new ArrayList<>();

		if (alarm.isOneTimeAlarm())
		{
			Calendar c = NacCalendar.toNextOneTimeCalendar(alarm);

			calendars.add(c);
		}
		else
		{
			EnumSet<Day> days = alarm.getDays();

			for (Day d : days)
			{
				Calendar c = NacCalendar.toNextCalendar(alarm, d);

				if (c == null)
				{
					continue;
				}

				calendars.add(c);
			}
		}

		return calendars;
	}

	/**
	 * Convert the one time alarm to the next calendar.
	 */
	public static Calendar toNextOneTimeCalendar(NacAlarm alarm)
	{
		Calendar today = NacCalendar.getToday(alarm);
		Calendar now = Calendar.getInstance();

		return (today.after(now)) ? today : NacCalendar.getTomorrow(alarm);
	}

	/**
	 * Convert the alarm on the given day to a Calendar.
	 *
	 * @param  alarm  The alarm.
	 * @param  day  The day to convert.
	 *
	 * @return A Calendar.
	 */
	public static Calendar toNextCalendar(NacAlarm alarm, Day day)
	{
		Calendar calendar = NacCalendar.toCalendar(alarm, day);
		Calendar now = Calendar.getInstance();
		boolean repeat = alarm.getRepeat();

		if (calendar.before(now))
		{
			if (!repeat)
			{
				return null;
			}

			calendar.add(Calendar.DAY_OF_MONTH, 7);
		}

		return calendar;
	}

	/**
	 * Convert the calendar to a string in the given format.
	 */
	public static String toString(Calendar calendar, String format)
	{
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		Date date = calendar.getTime();

		return formatter.format(date);
	}



	/**
	 * Days static class.
	 */
	public static class Days
	{

		/**
		 * Convert a set of days to a value.
		 *
		 * @param  days  A set of days.
		 *
		 * @return The computed value of all enum days added together.
		 */
		public static int daysToValue(EnumSet<Day> days)
		{
			int value = 0;

			for (Day d : days)
			{
				value += d.getValue();
			}

			return value;
		}

		/**
		 * Convert from an index to a Day.
		 *
		 * @param  index  A day index from 0-6, corresponding to the days of the
		 *                week, starting on Sunday.
		 *
		 * @return A Day.
		 */
		public static Day fromIndex(int index)
		{
			int i = 0;

			for (Day d : WEEK)
			{
				if (i == index)
				{
					return d;
				}

				i++;
			}

			return Day.SUNDAY;
		}

		/**
		 * @return The Day today.
		 */
		public static Day getToday()
		{
			Calendar today = Calendar.getInstance();
			int dow = today.get(Calendar.DAY_OF_WEEK);

			return NacCalendar.Days.toWeekDay(dow);
		}

		/**
		 * @return The tomorrow Day.
		 */
		public static Day getTomorrow()
		{
			Calendar tomorrow = Calendar.getInstance();

			tomorrow.add(Calendar.DAY_OF_MONTH, 1);

			int dow = tomorrow.get(Calendar.DAY_OF_WEEK);

			return NacCalendar.Days.toWeekDay(dow);
		}

		/**
		 * Convert from a Day to a Calendar day.
		 *
		 * @param  day  A Day.
		 *
		 * @return A Calendar day.
		 */
		public static int toCalendarDay(Day day)
		{
			if (day == Day.SUNDAY)
			{
				return Calendar.SUNDAY;
			}
			else if (day == Day.MONDAY)
			{
				return Calendar.MONDAY;
			}
			else if (day == Day.TUESDAY)
			{
				return Calendar.TUESDAY;
			}
			else if (day == Day.WEDNESDAY)
			{
				return Calendar.WEDNESDAY;
			}
			else if (day == Day.THURSDAY)
			{
				return Calendar.THURSDAY;
			}
			else if (day == Day.FRIDAY)
			{
				return Calendar.FRIDAY;
			}
			else if (day == Day.SATURDAY)
			{
				return Calendar.SATURDAY;
			}
			else
			{
				return -1;
			}
		}

		/**
		 * Convert from a Calendar day to an index.
		 *
		 * @param  day  A Calendar day.
		 *
		 * @return The index corresponding to the given day.
		 */
		public static int toIndex(Day day)
		{
			int index = 0;

			for (Day d : WEEK)
			{
				if (d == day)
				{
					return index;
				}

				index++;
			}

			return index;
		}

		/**
		 * @see toIndex
		 */
		public static int toIndex(int day)
		{
			Day weekday = NacCalendar.Days.toWeekDay(day);

			return NacCalendar.Days.toIndex(weekday);
		}

		/**
		 * @see toString
		 */
		public static String toString(NacAlarm alarm)
		{
			return NacCalendar.Days.toString(alarm, false);
		}

		/**
		 * Convert an alarm to a string of days.
		 *
		 * If no days are specified and the alarm is enable.
		 */
		public static String toString(NacAlarm alarm, boolean mondayFirst)
		{
			String string = NacCalendar.Days.toString(alarm.getDays(), mondayFirst);

			if (string.isEmpty() || !alarm.getRepeat())
			{
				int now = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
				int next = NacCalendar.toNextOneTimeCalendar(alarm)
					.get(Calendar.DAY_OF_MONTH);

				return (now == next) ? "Today" : "Tomorrow";
			}

			return string;
		}

		/**
		 * @see toString
		 */
		public static String toString(EnumSet<Day> days)
		{
			return NacCalendar.Days.toString(days, false);
		}

		/**
		 * Convert a set of days to a comma separate string of days.
		 *
		 * @param  days  The set of days.
		 *
		 * @return A string of the days.
		 */
		public static String toString(EnumSet<Day> days, boolean mondayFirst)
		{
			String string = "";
			int index = 0;
			String[] names;
			Day[] set;

			if (mondayFirst)
			{
				names = new String[] { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
					"Sun" };
				set = new Day[] { Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY,
					Day.THURSDAY, Day.FRIDAY, Day.SATURDAY, Day.SUNDAY };
			}
			else
			{
				names = new String[] { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri",
					"Sat" };
				set = new Day[] { Day.SUNDAY, Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY,
					Day.THURSDAY, Day.FRIDAY, Day.SATURDAY };
			}

			for (Day d : set)
			{
				if (days.contains(d))
				{
					if (!string.isEmpty())
					{
						string += ",";
					}

					string += names[index];
				}

				index++;
			}

			return string;
		}

		/**
		 * @see toString
		 */
		public static String toString(int value)
		{
			return NacCalendar.Days.toString(value, false);
		}

		/**
		 * @see toString
		 */
		public static String toString(int value, boolean mondayFirst)
		{
			EnumSet<Day> days = NacCalendar.Days.valueToDays(value);

			return NacCalendar.Days.toString(days, mondayFirst);
		}

		/**
		 * Convert from a Calendar day to a Day.
		 *
		 * @param  day  A Calendar day.
		 *
		 * @return A Day.
		 */
		public static Day toWeekDay(int day)
		{
			if (day == Calendar.SUNDAY)
			{
				return Day.SUNDAY;
			}
			else if (day == Calendar.MONDAY)
			{
				return Day.MONDAY;
			}
			else if (day == Calendar.TUESDAY)
			{
				return Day.TUESDAY;
			}
			else if (day == Calendar.WEDNESDAY)
			{
				return Day.WEDNESDAY;
			}
			else if (day == Calendar.THURSDAY)
			{
				return Day.THURSDAY;
			}
			else if (day == Calendar.FRIDAY)
			{
				return Day.FRIDAY;
			}
			else if (day == Calendar.SATURDAY)
			{
				return Day.SATURDAY;
			}
			else
			{
				return Day.SUNDAY;
			}
		}

		/**
		 * Convert a value to a set of days.
		 *
		 * @param  value  The value of a set of days. Each day has a unique, 2^n,
		 *                value so only one bit is set. Doing a bitwise-and of a
		 *                day should compute to 1 if you have the correct day.
		 *
		 * @return A set of enum days.
		 */
		public static EnumSet<Day> valueToDays(int value)
		{
			EnumSet<Day> days = EnumSet.noneOf(Day.class);

			for (Day d : WEEK)
			{
				if ((d.getValue() & value) != 0)
				{
					days.add(d);
				}
			}

			return days;
		}

	}

	/**
	 * Time static class.
	 */
	public static class Time
	{

		/**
		 * @param  hour  The hour.
		 * @param  format  The 24 hour format to determine how to interpret the
		 *                 hour.
		 *
		 * @return The time meridian.
		 */
		public static String getMeridian(int hour, boolean format)
		{
			if (format)
			{
				return "";
			}

			return (hour < 12) ? "AM" : "PM";
		}

		/**
		 * @param  context  The application context.
		 * @param  hour  The hour.
		 * @param  minute  The minutes.
		 *
		 * @return The time.
		 */
		public static String getTime(Context context, int hour, int minute)
		{
			boolean format = NacCalendar.Time.is24HourFormat(context);

			return NacCalendar.Time.getTime(hour, minute, format);
		}

		/**
		 * @param  hour  The hour.
		 * @param  minute  The minutes.
		 * @param  format  The 24 hour format, to determine how to interpret the
		 *                 hour.
		 *
		 * @return The time.
		 */
		public static String getTime(int hour, int minute, boolean format)
		{
			if (!format)
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

			String hourString = String.valueOf(hour);
			String minuteString = String.format(Locale.getDefault(), "%02d",
				minute);

			return hourString + ":" + minuteString;
		}

		/**
		 * @return True if the locale is in 24 hour time format, and False
		 *         otherwise.
		 */
		public static boolean is24HourFormat(Context context)
		{
			return DateFormat.is24HourFormat(context);
		}

	}

}
