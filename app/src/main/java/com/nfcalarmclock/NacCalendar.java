package com.nfcalarmclock;

import java.util.ArrayList;
import java.util.Calendar;
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
	 * @return Today's day.
	 */
	public static Day getToday()
	{
		return NacCalendar.toWeekDay(Calendar.getInstance()
			.get(Calendar.DAY_OF_WEEK));
	}

	/**
	 * Convert the upcoming days in an alarm to Calendars.
	 *
	 * @param  alarm  The alarm.
	 *
	 * @return The list of upcoming Calendars.
	 */
	public static List<Calendar> nextCalendars(NacAlarm alarm)
	{
		EnumSet<Day> days = alarm.getDays();
		int hour = alarm.getHour();
		int minute = alarm.getMinute();
		boolean repeat = alarm.getRepeat();
		List<Calendar> calendars = NacCalendar.toCalendars(alarm);
		List<Calendar> nextCalendars = new ArrayList<>();
		int calendarDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

		for (Calendar c : calendars)
		{
			int nextCalendarDay = c.get(Calendar.DAY_OF_WEEK);

			// Should this include repeat? I should check this before
			// anyways right? If repeat is false why would I get next
			// calendars?
			if (!repeat && (nextCalendarDay < calendarDay))
			{
				continue;
			}

			nextCalendars.add(c);
		}

		return nextCalendars;
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
		int hour = alarm.getHour();
		int minute = alarm.getMinute();
		Calendar calendar = Calendar.getInstance();

		calendar.set(Calendar.DAY_OF_WEEK, NacCalendar.toCalendarDay(day));
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar;
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
	 * Convert all the days an alarm is scheduled to go off, to Calendars.
	 *
	 * @param  alarm  The alarm.
	 *
	 * @return A list of Calendars.
	 */
	public static List<Calendar> toCalendars(NacAlarm alarm)
	{
		EnumSet<Day> days = alarm.getDays();
		Calendar today = Calendar.getInstance();
		List<Calendar> calendars = new ArrayList<>();

		for (Day d : days)
		{
			Calendar c = NacCalendar.toCalendar(alarm, d);

			if (c.before(today))
			{
				c.add(Calendar.DAY_OF_MONTH, 7);
			}

			calendars.add(c);
		}

		return calendars;
	}

	/**
	 * Convert the alarm to a calendar corresponding to today or tomorrow.
	 *
	 * @param  alarm  The alarm.
	 *
	 * @return A calendar.
	 */
	public static Calendar toCalendarTodayOrTomorrow(NacAlarm alarm)
	{
		Calendar now = Calendar.getInstance();
		Calendar todayOrTomorrow = NacCalendar.toCalendar(alarm,
			NacCalendar.getToday());

		if (todayOrTomorrow.before(now))
		{
			todayOrTomorrow.add(Calendar.DAY_OF_MONTH, 1);
		}

		return todayOrTomorrow;
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
		Day weekday = NacCalendar.toWeekDay(day);

		return NacCalendar.toIndex(weekday);
	}

	/**
	 * Convert an alarm to a string of days.
	 *
	 * If no days are specified and the alarm is enable.
	 */
	public static String toString(NacAlarm alarm)
	{
		String string = NacCalendar.toString(alarm.getDays());

		if (string.isEmpty())
		{
			int now = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
			int todayOrTomorrow = NacCalendar.toCalendarTodayOrTomorrow(alarm)
				.get(Calendar.DAY_OF_MONTH);

			return (now == todayOrTomorrow) ? "Today" : "Tomorrow";
		}

		return string;
	}

	/**
	 * Convert a set of days to a comma separate string of days.
	 *
	 * @param  days  The set of days.
	 *
	 * @return A string of the days.
	 */
	public static String toString(EnumSet<Day> days)
	{
		String string = "";
		String[] names = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
		int index = 0;

		for (Day d : WEEK)
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
		EnumSet<Day> days = NacCalendar.valueToDays(value);

		return NacCalendar.toString(days);
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
