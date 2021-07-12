package com.nfcalarmclock.system;

import android.content.Context;
import android.text.format.DateFormat;

import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.util.NacUtility;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;

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
	@SuppressWarnings("UnnecessaryEnumModifier")
	public enum Day
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

		/**
		 * @return A set of no days.
		 */
		public static EnumSet<Day> none()
		{
			return EnumSet.noneOf(Day.class);
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
	 * @return A message to display.
	 */
	public static String getMessage(NacSharedPreferences shared,
                                    NacAlarm alarm, String prefix)
	{
		NacSharedConstants cons = shared.getConstants();
		Calendar calendar = NacCalendar.getNextAlarmDay(alarm);
		Locale locale = Locale.getDefault();

		if ((shared == null) || (alarm == null) || (calendar == null))
		{
			return String.format(locale, "%1$s.",
				cons.getMessageNoAlarmsScheduled());
		}
		else if (!alarm.isEnabled())
		{
			int length = cons.getMessageNameLength();
			String name = alarm.getNameNormalizedForMessage(length);
			String isDisabled = cons.getIsDisabled();
			String alarmWord = NacUtility.capitalize(cons.getAlarm(1));

			return name.isEmpty()
				? String.format(locale, "%1$s %2$s.", alarmWord, isDisabled)
				: String.format(locale, "\"%1$s\" %2$s.", name, isDisabled);
		}
		else
		{
			Context context = shared.getContext();
			int messageFormat = shared.getNextAlarmFormat();

			if (messageFormat == 0)
			{
				return NacCalendar.getMessageTimeIn(context, calendar, prefix);
			}
			else
			{
				return NacCalendar.getMessageTimeOn(context, calendar, prefix);
			}
		}
	}

	/**
	 * @return The message to display when the next alarm will occur.
	 */
	public static String getMessageNextAlarm(NacSharedPreferences shared,
                                             NacAlarm alarm)
	{
		NacSharedConstants cons = shared.getConstants();
		String prefix = cons.getNextAlarm();
		return NacCalendar.getMessage(shared, alarm, prefix);
	}

	/**
	 * @return The message to display when an alarm will run IN some amount of
	 * time.
	 */
	public static String getMessageTimeIn(Context context, Calendar calendar,
		String prefix)
	{
		NacSharedConstants cons = new NacSharedConstants(context);
		long millis = calendar.getTimeInMillis();
		long time = (millis - System.currentTimeMillis()) / 1000;
		long day = (time / (60*60*24)) % 365;
		long hr = (time / (60*60)) % 24;
		long min = (time / 60) % 60;
		long sec = time % 60;
		String dayunit = cons.getUnitDay((int)day);
		String hrunit  = cons.getUnitHour((int)hr);
		String minunit = cons.getUnitMinute((int)min);
		String secunit = cons.getUnitSecond((int)sec);
		String timeRemaining;
		String format = "%1$d %2$s %3$d %4$s";
		Locale locale = Locale.getDefault();

		if (day > 0)
		{
			timeRemaining = String.format(locale, format, day, dayunit, hr,
				hrunit);
		}
		else
		{
			if (hr > 0)
			{
				timeRemaining = String.format(locale, format, hr, hrunit, min,
					minunit);
			}
			else
			{
				format = (min > 0) ? format : format.substring(10, 19);
				timeRemaining = String.format(locale, format, min, minunit,
					sec, secunit);
			}
		}

		return String.format(locale, "%1$s %2$s %3$s", prefix,
			cons.getTimeIn(), timeRemaining);
	}

	/**
	 * @return The message to display when an alarm will run ON some date and
	 * time.
	 */
	public static String getMessageTimeOn(Context context, Calendar calendar,
		String prefix)
	{
		NacSharedConstants cons = new NacSharedConstants(context);
		String time = NacCalendar.Time.getFullTime(context, calendar);
		Locale locale = Locale.getDefault();

		return String.format(locale, "%1$s %2$s %3$s", prefix,
			cons.getTimeOn(), time);
	}

	/**
	 * @return The message to display when the alarm will run.
	 */
	public static String getMessageWillRun(NacSharedPreferences shared,
		NacAlarm alarm)
	{
		NacSharedConstants cons = shared.getConstants();
		int length = cons.getMessageNameLength();
		Locale locale = Locale.getDefault();
		String willRun = cons.getWillRun();
		String name = alarm.getNameNormalizedForMessage(length);
		String prefix = name.isEmpty() ? willRun
			: String.format(locale, "\"%1$s\" %2$s", name,
				willRun.toLowerCase(locale));

		return NacCalendar.getMessage(shared, alarm, prefix);
	}

	/**
	 * @return The alarm that will run next.
	 *
	 * @param  alarms  List of alarms to check.
	 */
	public static NacAlarm getNextAlarm(List<NacAlarm> alarms)
	{
		Calendar nextCalendar = null;
		NacAlarm nextAlarm = null;

		for (NacAlarm a : alarms)
		{
			if (!a.isEnabled())
			{
				continue;
			}

			Calendar calendar = NacCalendar.getNextAlarmDay(a);

			if ((nextCalendar == null)
				|| ((calendar != null) && calendar.before(nextCalendar)))
			{
				nextCalendar = calendar;
				nextAlarm = a;
			}
		}

		return nextAlarm;
	}

	/**
	 * @return The Calendar day on which the given alarm will run next.
	 *
	 * @param  alarm  The alarm who's days to check.
	 */
	public static Calendar getNextAlarmDay(NacAlarm alarm)
	{
		List<Calendar> calendars = NacCalendar.toCalendars(alarm);
		return NacCalendar.getNextDay(calendars);
	}

	/**
	 * @return The Calendary day that represents the next upcoming day.
	 */
	public static Calendar getNextDay(List<Calendar> calendars)
	{
		Calendar next = null;

		for (Calendar c : calendars)
		{
			next = ((next == null) || next.after(c)) ? c : next;
		}

		return next;
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

		if (alarm != null)
		{
			if (!alarm.areDaysSelected())
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
		}

		return calendars;
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
		//boolean repeat = alarm.shouldRepeat();

		if (calendar.before(now))
		{
			calendar.add(Calendar.DAY_OF_MONTH, 7);
		}

		return calendar;
	}

	/**
	 * Convert the one time alarm to the next calendar.
	 */
	public static Calendar toNextOneTimeCalendar(NacAlarm alarm)
	{
		Calendar today = NacCalendar.getToday(alarm);
		Calendar now = Calendar.getInstance();

		if (today.before(now))
		{
			today.add(Calendar.DAY_OF_MONTH, 1);
		}

		return today;
		//return today.after(now) ? today : NacCalendar.getTomorrow(alarm);
	}

	/**
	 * Convert the calendar to a string in the given format.
	 */
	public static String toString(Calendar calendar, String format)
	{
		Locale locale = Locale.getDefault();
		SimpleDateFormat formatter = new SimpleDateFormat(format, locale);
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
		 * @return The Day today.
		 */
		public static Day getToday()
		{
			Calendar today = Calendar.getInstance();
			int dow = today.get(Calendar.DAY_OF_WEEK);

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

			for (Day d : NacCalendar.WEEK)
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
		 * @see #toIndex(NacCalendar.Day)
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
		public static String toString(NacSharedConstants cons, NacAlarm alarm,
			int start)
		{
			EnumSet<Day> days = alarm.getDays();
			String string = NacCalendar.Days.toString(cons, days, start);

			if (string.isEmpty() || !alarm.areDaysSelected())
			{
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
		 * @return A string of the days.
		 *
		 * @param  cons  Shared constants.
		 * @param  daysToConvert  The set of days to convert.
		 * @param  start  The day to start the week on.
		 */
		public static String toString(NacSharedConstants cons,
			EnumSet<Day> daysToConvert, int start)
		{
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
			StringBuilder summary = new StringBuilder(32);

			for (int count=0, i=start;
				count < WEEK_LENGTH;
				count++, i=(i+1) % WEEK_LENGTH)
			{
				if (daysToConvert.contains(days.get(i)))
				{
					if (summary.length() != 0)
					{
						summary.append(" \u2027 ");
					}

					summary.append(dow.get(i));
				}
			}

			return summary.toString();
		}

		/**
		 * @see #toString(NacSharedConstants, EnumSet, int)
		 */
		public static String toString(NacSharedConstants cons, int value, int start)
		{
			EnumSet<Day> days = NacCalendar.Days.valueToDays(value);
			return NacCalendar.Days.toString(cons, days, start);
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
			EnumSet<Day> days = NacCalendar.Day.none();

			for (Day d : NacCalendar.WEEK)
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
		 * @return The time.
		 *
		 * @param  hour    The hour.
		 * @param  minute  The minutes.
		 * @param  format  The 24 hour format, to determine how to interpret the
		 *                 hour.
		 */
		public static String getClockTime(int hour, int minute, boolean format)
		{
			if (!format)
			{
				hour = NacCalendar.Time.to12HourFormat(hour);
			}

			Locale locale = Locale.getDefault();
			return String.format(locale, "%1$d:%2$02d", hour, minute);
		}

		/**
		 * @return The full time string, EEE, HH:MM AM/PM.
		 */
		public static String getFullTime(Context context, Calendar calendar)
		{
			String format = NacCalendar.Time.is24HourFormat(context)
				? "EEE HH:mm" : "EEE hh:mm a";
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int convertedHour = NacCalendar.Time.to12HourFormat(hour);

			if (convertedHour < 10)
			{
				format = format.replaceFirst("h", " ");
			}

			return NacCalendar.toString(calendar, format);
		}

		/**
		 * @return The time meridian.
		 *
		 * @param  hour  The hour.
		 */
		public static String getMeridian(Context context, int hour)
		{
			NacSharedConstants cons = new NacSharedConstants(context);
			boolean format = NacCalendar.Time.is24HourFormat(context);
			if (format)
			{
				return "";
			}
			else
			{
				return (hour < 12) ? cons.getAm() : cons.getPm();
			}
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
