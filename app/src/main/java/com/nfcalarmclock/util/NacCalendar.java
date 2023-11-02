package com.nfcalarmclock.util;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateFormat;
import com.nfcalarmclock.R;
import com.nfcalarmclock.alarm.db.NacAlarm;
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
 * <p>
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
		Resources res = shared.resources;
		Calendar calendar = NacCalendar.getNextAlarmDay(alarm);
		Locale locale = Locale.getDefault();

		// No alarm scheduled
		if ((alarm == null) || (calendar == null))
		{
			String noAlarmsScheduled = res.getString(R.string.message_no_alarms_scheduled);

			return String.format(locale, "%1$s.", noAlarmsScheduled);
		}
		// Alarm is disabled
		else if (!alarm.isEnabled())
		{
			int length = res.getInteger(R.integer.max_message_name_length);
			String name = alarm.getNameNormalizedForMessage(length);
			String isDisabled = res.getString(R.string.is_disabled);
			String alarmPlural = res.getQuantityString(R.plurals.alarm, 1);
			String alarmWord = NacUtility.capitalize(alarmPlural);

			return name.isEmpty()
				? String.format(locale, "%1$s %2$s.", alarmWord, isDisabled)
				: String.format(locale, "\"%1$s\" %2$s.", name, isDisabled);
		}
		// Show when alarm will occur
		else
		{
			Context context = shared.getContext();
			int messageFormat = shared.getNextAlarmFormat();

			// e.g. Alarm in 12 hour 5 min
			if (messageFormat == 0)
			{
				return NacCalendar.getMessageTimeIn(context, calendar, prefix);
			}
			// e.g. Alarm on Mon at 8:05 AM
			else
			{
				return NacCalendar.getMessageTimeOn(context, calendar, prefix);
			}
		}
	}

	/**
	 * @return The message to display when the next alarm will occur.
	 */
	public static String getMessageNextAlarm(NacSharedPreferences shared, NacAlarm alarm)
	{
		// Get the prefix
		String prefix = shared.resources.getString(R.string.next_alarm);

		// Create the message
		return NacCalendar.getMessage(shared, alarm, prefix);
	}

	/**
	 * @return The message to display when an alarm will run IN some amount of
	 * time.
	 */
	public static String getMessageTimeIn(Context context, Calendar calendar,
		String prefix)
	{
		Resources res = context.getResources();
		long millis = calendar.getTimeInMillis();
		long time = (millis - System.currentTimeMillis()) / 1000;
		long day = (time / (60*60*24)) % 365;
		long hr = (time / (60*60)) % 24;
		long min = (time / 60) % 60;
		long sec = time % 60;
		String dayunit = res.getQuantityString(R.plurals.unit_day, (int)day);
		String hrunit  = res.getQuantityString(R.plurals.unit_hour, (int)hr);
		String minunit = res.getQuantityString(R.plurals.unit_minute, (int)min);
		String secunit = res.getQuantityString(R.plurals.unit_second, (int)sec);
		String timeRemaining;
		String format = "%1$d %2$s %3$d %4$s";
		Locale locale = Locale.getDefault();
		String timeIn = context.getString(R.string.time_in);

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

		// Build the message
		return String.format(locale, "%1$s %2$s %3$s", prefix, timeIn, timeRemaining);
	}

	/**
	 * @return The message to display when an alarm will run ON some date and
	 * time.
	 */
	public static String getMessageTimeOn(Context context, Calendar calendar,
		String prefix)
	{
		String time = NacCalendar.Time.getFullTime(context, calendar);
		Locale locale = Locale.getDefault();
		String timeOn = context.getString(R.string.time_on);

		return String.format(locale, "%1$s %2$s %3$s", prefix, timeOn, time);
	}

	/**
	 * @return The message to display when the alarm will run.
	 */
	public static String getMessageWillRun(NacSharedPreferences shared,
		NacAlarm alarm)
	{
		Locale locale = Locale.getDefault();
		Resources res = shared.resources;
		int length = res.getInteger(R.integer.max_message_name_length);
		String willRun = res.getString(R.string.will_run);
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

		// Alarm is null. Return an empty list
		if (alarm == null)
		{
			return calendars;
		}

		// No days are selected. This alarm will occur either today or tomorrow and
		// is a one-time alarm
		if (!alarm.getAreDaysSelected())
		{
			Calendar c = NacCalendar.toNextOneTimeCalendar(alarm);
			calendars.add(c);
		}
		// One or more days are selected. Add a calendar for each day that is
		// selected
		else
		{
			EnumSet<Day> days = alarm.getDays();
			long timeOfDismissEarlyAlarm = alarm.getTimeOfDismissEarlyAlarm();

			// Iterate over each selected day
			for (Day d : days)
			{
				// Get calendar and time of calendar
				Calendar c = NacCalendar.toNextCalendar(alarm, d);
				long t = c.getTimeInMillis();

				// Time matches the alarm that was dismissed early. Add a week to
				// this calendar
				if ((timeOfDismissEarlyAlarm > 0) && (t == timeOfDismissEarlyAlarm))
				{
					c.add(Calendar.DAY_OF_MONTH, 7);
				}

				// Add calendar to list of calendars
				calendars.add(c);
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

		// Alarm will occur in one week
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
		 * Convert an alarm to a string of days.
		 * <p>
		 * If no days are specified and the alarm is enable.
		 */
		public static String toString(Context context, NacAlarm alarm,
			int start)
		{
			// Get the days
			EnumSet<Day> days = alarm.getDays();
			String string = NacCalendar.Days.toString(context, days, start);

			// Check if unable to convert the days to a string or if there are no days selected
			if (string.isEmpty() || !alarm.getAreDaysSelected())
			{
				// Get the current day
				int now = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

				// Get the next time the alarm will ring
				int next = NacCalendar.toNextOneTimeCalendar(alarm)
					.get(Calendar.DAY_OF_MONTH);

				// Check if the two days are the same, that means that the name
				// should be today. Otherwise, it should show tomorrow
				return (now == next)
					? context.getString(R.string.dow_today)
					: context.getString(R.string.dow_tomorrow);
			}

			//
			return string;
		}

		/**
		 * Convert a set of days to a comma separate string of days.
		 *
		 * @return A string of the days.
		 *
		 * @param  context  Context.
		 * @param  daysToConvert  The set of days to convert.
		 * @param  start  The day to start the week on.
		 */
		public static String toString(Context context,
			EnumSet<Day> daysToConvert, int start)
		{

			// Every day
			if (NacCalendar.Days.isEveryday(daysToConvert))
			{
				return context.getString(R.string.dow_everyday);
			}
			// Weekdays
			else if (NacCalendar.Days.isWeekday(daysToConvert))
			{
				return context.getString(R.string.dow_weekdays);
			}
			// Weekends
			else if (NacCalendar.Days.isWeekend(daysToConvert))
			{
				return context.getString(R.string.dow_weekend);
			}

			// Get the days of week
			String[] daysOfWeek = context.getResources().getStringArray(R.array.days_of_week);
			List<String> dow = new ArrayList<>();

			// Abbreviate the days of week
			for (String s : daysOfWeek)
			{
				// Get the first 3 characters in the day of week
				String abbrv = s.substring(0, 3);

				// Add it to the list
				dow.add(abbrv);
			}

			//List<String> dow = cons.getDaysOfWeekAbbr();
			List<Day> days = Arrays.asList(Day.values());
			StringBuilder summary = new StringBuilder(32);

			// Iterate over each day in the week
			for (int count=0, i=start;
				count < WEEK_LENGTH;
				count++, i=(i+1) % WEEK_LENGTH)
			{

				// Check if the day is in the enum set
				if (daysToConvert.contains(days.get(i)))
				{
					// Add a dot in between each day
					if (summary.length() != 0)
					{
						summary.append(" \u2027 ");
					}

					// Append the day
					summary.append(dow.get(i));
				}

			}

			// Convert the string builder to a string
			return summary.toString();
		}

		/**
		 * @see #toString(Context, EnumSet, int)
		 */
		public static String toString(Context context, int value, int start)
		{
			// Convert value of days to enum set
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
			// Check if time is in 24 hour format
			boolean format = NacCalendar.Time.is24HourFormat(context);

			// Check the format
			if (format)
			{
				return "";
			}
			else
			{
				return (hour < 12)
					? context.getString(R.string.am)
					: context.getString(R.string.pm);
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
