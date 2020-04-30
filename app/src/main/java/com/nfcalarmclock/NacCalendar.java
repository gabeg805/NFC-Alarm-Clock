package com.nfcalarmclock;

import android.content.Context;
import android.text.format.DateFormat;
import java.lang.System;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

/**
 * A list of possible days the alarm can run on.
 *
 * TODO Fix getNextMessage and getMessage so that they're language compliant.
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
	 * Every day of week.
	 */
	public static final EnumSet<Day> WEEK = EnumSet.allOf(Day.class);

	/**
	 * All weekday days.
	 */
	public static final EnumSet<Day> WEEKDAY = EnumSet.of(Day.MONDAY,
		Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY);

	/**
	 * All weekend days.
	 */
	public static final EnumSet<Day> WEEKEND = EnumSet.of(Day.SUNDAY,
		Day.SATURDAY);

	/**
	 * Length of week.
	 */
	public static final int WEEK_LENGTH = WEEK.size();

	/**
	 * @see getMessage
	 */
	public static String getMessage(String prefix, Calendar next)
	{
		return prefix + " on " + NacCalendar.toString(next, "EEE h:mm a");
		//return "Next alarm on " + NacCalendar.toString(next, "EEE h:mm a");
	}

	/**
	 * @see getMessage
	 */
	public static String getMessage(String prefix, long millis)
	{
		long time = (millis - System.currentTimeMillis())
			/ 1000;
		long day = (time / (60*60*24)) % 365;
		long hr = (time / (60*60)) % 24;
		long min = (time / 60) % 60;
		long sec = time % 60;
		String dayunit = (day != 1) ? "days"    : "day";
		String hrunit  = (hr  != 1) ? "hours"   : "hour";
		String minunit = (min != 1) ? "minutes" : "minute";
		String secunit = (sec != 1) ? "seconds" : "second";
		String msg = prefix + " in ";
		String format = "%1$d %2$s %3$d %4$s";
		Locale locale = Locale.getDefault();

		if (day > 0)
		{
			msg += String.format(locale, format, day, dayunit, hr, hrunit);
		}
		else
		{
			if (hr > 0)
			{
				msg += String.format(locale, format, hr, hrunit, min, minunit);
			}
			else
			{
				if (min > 0)
				{
					msg += String.format(locale, format, min, minunit, sec,
						secunit);
				}
				else
				{
					msg += String.format(locale, format.substring(0, 9), sec,
						secunit);
				}
			}
		}

		return msg;
	}

	/**
	 * @see getMessage
	 */
	public static String getMessage(String prefix, NacSharedPreferences shared,
		NacAlarm alarm)
	{
		Calendar calendar = NacCalendar.getNext(alarm);

		if ((shared == null) || (alarm == null) || (calendar == null))
		{
			return "No scheduled alarms.";
		}
		else if (!alarm.getEnabled())
		{
			String name = alarm.getNameNormalizedForMessage();

			return String.format("\"%1$s\" is disabled.", name);
		}

		int nextAlarmFormat = shared.getNextAlarmFormat();
		long millis = calendar.getTimeInMillis();

		return NacCalendar.getMessage(prefix, millis, nextAlarmFormat);
	}

	/**
	 * @return The message to display.
	 */
	public static String getMessage(String prefix, long millis, int format)
	{
		if (format == 0)
		{
			return NacCalendar.getMessage(prefix, millis);
		}
		else
		{
			Calendar calendar = Calendar.getInstance();

			calendar.setTimeInMillis(millis);

			return NacCalendar.getMessage(prefix, calendar);
		}
	}

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
	 * @see getNext
	 */
	public static Calendar getNext(NacAlarm alarm)
	{
		List<Calendar> calendars = NacCalendar.toCalendars(alarm);

		return NacCalendar.getNext(calendars);
	}

	/**
	 * @return The next alarm from the given list of alarms.
	 */
	public static NacAlarm getNextAlarm(List<NacAlarm> alarms)
	{
		Calendar nextCalendar = null;
		NacAlarm nextAlarm = null;

		for (NacAlarm a : alarms)
		{
			if (!a.getEnabled())
			{
				continue;
			}

			Calendar calendar = NacCalendar.getNext(a);

			if (calendar == null)
			{
				continue;
			}
			else if ((nextCalendar == null) || calendar.before(nextCalendar))
			{
				nextCalendar = calendar;
				nextAlarm = a;
			}
		}

		return nextAlarm;
	}

	/**
	 * @return The message to display when the next action will occur.
	 */
	public static String getNextMessage(Calendar next)
	{
		return NacCalendar.getMessage("Next alarm", next);
	}

	/**
	 * @return The message to display when the next action will occur.
	 */
	public static String getNextMessage(long millis)
	{
		return NacCalendar.getMessage("Next alarm", millis);
	}

	/**
	 * @see getNextMessage
	 */
	public static String getNextMessage(NacSharedPreferences shared,
		NacAlarm alarm)
	{
		return NacCalendar.getMessage("Next alarm", shared, alarm);
	}

	/**
	 * @return The next message in the desired format.
	 */
	public static String getNextMessage(long millis, int format)
	{
		return NacCalendar.getMessage("Next alarm", millis, format);
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

		if (alarm == null)
		{
			;
		}
		else if (!alarm.areDaysSelected())
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
		//boolean repeat = alarm.getRepeat();

		if (calendar.before(now))
		{
			calendar.add(Calendar.DAY_OF_MONTH, 7);
		}

		return calendar;
	}

	/**
	 * Convert the calendar to a string in the given format.
	 */
	public static String toString(Calendar calendar, String format)
	{
		SimpleDateFormat formatter = new SimpleDateFormat(format,
			Locale.getDefault());
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
		 * @return True if every day is in the set, and False otherwise.
		 */
		public static boolean isEveryday(EnumSet<Day> days)
		{
			return days.equals(NacCalendar.WEEK);
		}

		/**
		 * @return True if all weekdays are in the set, and False otherwise.
		 */
		public static boolean isWeekday(EnumSet<Day> days)
		{
			return days.equals(NacCalendar.WEEKDAY);
		}

		/**
		 * @return True if all weekend days are in the set, and False otherwise.
		 */
		public static boolean isWeekend(EnumSet<Day> days)
		{
			return days.equals(NacCalendar.WEEKEND);
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
		 * Convert an alarm to a string of days.
		 *
		 * If no days are specified and the alarm is enable.
		 */
		public static String toString(Context context, NacAlarm alarm,
			int start)
		{
			EnumSet<Day> days = alarm.getDays();
			String string = NacCalendar.Days.toString(context, days, start);

			if (string.isEmpty() || !alarm.areDaysSelected())
			{
				NacSharedConstants cons = new NacSharedConstants(context);
				int now = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
				int next = NacCalendar.toNextOneTimeCalendar(alarm)
					.get(Calendar.DAY_OF_MONTH);

				return (now == next) ? cons.getToday() : cons.getTomorrow();
			}

			return string;
		}

		/**
		 * Convert a set of days to a comma separate string of days.
		 *
		 * @param  convertDays  The set of days to convert.
		 * @param  start  The day to start the week on.
		 *
		 * @return A string of the days.
		 */
		public static String toString(Context context,
			EnumSet<Day> daysToConvert, int start)
		{
			NacSharedConstants cons = new NacSharedConstants(context);

			if (NacCalendar.Days.isEveryday(daysToConvert))
			{
				return cons.getEveryday();
			}
			else if (NacCalendar.Days.isWeekday(daysToConvert))
			{
				return cons.getWeekdays();
			}
			else if (NacCalendar.Days.isWeekend(daysToConvert))
			{
				return cons.getWeekend();
			}

			List<String> dow = cons.getDaysOfWeekAbbr();
			List<Day> days = Arrays.asList(Day.values());
			String string = "";

			for (int count=0, i=start;
				count < WEEK_LENGTH;
				count++, i=(i+1) % WEEK_LENGTH)
			{
				if (daysToConvert.contains(days.get(i)))
				{
					if (!string.isEmpty())
					{
						string += " \u2027 ";
					}

					string += dow.get(i);
				}
			}

			return string;
		}

		/**
		 * @see toString
		 */
		public static String toString(Context context, int value, int start)
		{
			EnumSet<Day> days = NacCalendar.Days.valueToDays(value);
			return NacCalendar.Days.toString(context, days, start);
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
		 * @see getClockTime
		 */
		public static String getClockTime(Context context)
		{
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);

			return NacCalendar.Time.getClockTime(context, hour, minute);
		}

		/**
		 * @param  context  The application context.
		 * @param  hour  The hour.
		 * @param  minute  The minutes.
		 *
		 * @return The time.
		 */
		public static String getClockTime(Context context, int hour, int minute)
		{
			boolean format = NacCalendar.Time.is24HourFormat(context);
			return NacCalendar.Time.getClockTime(hour, minute, format);
		}

		/**
		 * @param  hour  The hour.
		 * @param  minute  The minutes.
		 * @param  format  The 24 hour format, to determine how to interpret the
		 *                 hour.
		 *
		 * @return The time.
		 */
		public static String getClockTime(int hour, int minute, boolean format)
		{
			if (!format)
			{
				hour = NacCalendar.Time.to12HourFormat(hour);
			}

			String hourString = String.valueOf(hour);
			String minuteString = String.format(Locale.getDefault(), "%02d",
				minute);

			return hourString + ":" + minuteString;
		}

		/**
		 * @return The full time string, EEE, HH:MM AM/PM.
		 */
		public static String getFullTime(Context context, Calendar calendar)
		{
			String date = NacCalendar.toString(calendar, "EEE h:mm");
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			String meridian = NacCalendar.Time.getMeridian(context, hour);

			return (!meridian.isEmpty()) ? date+" "+meridian : date;
		}

		/**
		 * @see getMeridian
		 */
		public static String getMeridian(Context context, int hour)
		{
			boolean format = NacCalendar.Time.is24HourFormat(context);
			return NacCalendar.Time.getMeridian(hour, format);
		}

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
			else
			{
				return (hour < 12) ? "AM" : "PM";
			}
		}

		/**
		 * @see getTime
		 */
		public static String getTime(Context context)
		{
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);

			return NacCalendar.Time.getTime(context, hour, minute);
		}

		/**
		 * @return The full time string, HH:MM AM/PM.
		 */
		public static String getTime(Context context, int hour, int minute)
		{
			String time = NacCalendar.Time.getClockTime(context, hour, minute);
			String meridian = NacCalendar.Time.getMeridian(context, hour);

			return !meridian.isEmpty() ? time+" "+meridian : time;
		}

		/**
		 * @return True if the locale is in 24 hour time format, and False
		 *         otherwise.
		 */
		public static boolean is24HourFormat(Context context)
		{
			return DateFormat.is24HourFormat(context);
		}

		/**
		 * Convert an hour to 12 hour format.
		 */
		public static int to12HourFormat(int hour)
		{
			if (hour > 12)
			{
				return hour % 12;
			}
			else
			{
				return (hour == 0) ? 12 : hour;
			}
		}

	}

}
