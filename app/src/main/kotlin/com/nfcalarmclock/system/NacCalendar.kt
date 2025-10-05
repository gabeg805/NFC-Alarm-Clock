package com.nfcalarmclock.system

import android.content.Context
import android.content.res.Resources
import android.text.format.DateFormat
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.db.NacNextAlarm
import com.nfcalarmclock.system.NacCalendar.Day
import com.nfcalarmclock.system.NacCalendar.Day.Companion.WEEK
import com.nfcalarmclock.system.NacCalendar.Day.Companion.WEEKDAY
import com.nfcalarmclock.system.NacCalendar.Day.Companion.WEEKEND
import com.nfcalarmclock.system.NacCalendar.Day.Companion.WEEK_LENGTH
import com.nfcalarmclock.system.NacCalendar.Day.FRIDAY
import com.nfcalarmclock.system.NacCalendar.Day.MONDAY
import com.nfcalarmclock.system.NacCalendar.Day.SATURDAY
import com.nfcalarmclock.system.NacCalendar.Day.SUNDAY
import com.nfcalarmclock.system.NacCalendar.Day.THURSDAY
import com.nfcalarmclock.system.NacCalendar.Day.TUESDAY
import com.nfcalarmclock.system.NacCalendar.Day.WEDNESDAY
import com.nfcalarmclock.system.NacCalendar.Day.entries
import com.nfcalarmclock.system.NacCalendar.alarmToCalendar
import com.nfcalarmclock.system.NacCalendar.alarmToNextOneTimeCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.EnumSet
import java.util.Locale
import java.util.TimeZone

/**
 * Convert a set of days to a value.
 *
 * @return The computed value of all enum days added together.
 */
fun EnumSet<Day>.daysToValue(): Int
{
	// Initialize the value
	var value = 0

	// Iterate over each day in the set
	for (d in this)
	{
		value += d.value
	}

	return value
}

/**
 * Remove today if it is present in the set.
 */
fun EnumSet<Day>.removeToday()
{
	// Get today
	val today = Day.TODAY

	// Remove today if it is in the set
	if (today in this)
	{
		this.remove(today)
	}
}

/**
 * Convert a set of days to a comma separate string of days.
 *
 * @param  context  Context.
 * @param  start  The day to start the week on.
 *
 * @return A string of the days.
 */
fun EnumSet<Day>.toDayString(
	context: Context,
	start: Int
): String
{
	// Get the abbreviated days of week
	val daysOfWeek = context.resources.getStringArray(R.array.days_of_week_abbr)

	// Iterate over each day in the week
	val days = entries
	val summary = StringBuilder(32)
	var count = 0
	var i = start

	while (count < WEEK_LENGTH)
	{

		// Check if the day is in the enum set
		if (this.contains(days[i]))
		{
			// Add a dot in between each day
			if (summary.isNotEmpty())
			{
				summary.append(" \u2027 ")
			}

			// Append the day
			summary.append(daysOfWeek[i])
		}

		count++
		i = (i + 1) % WEEK_LENGTH
	}

	// Convert the string builder to a string
	return summary.toString()
}

/**
 * Convert from a Day to a Calendar day.
 *
 * @return A Calendar day.
 */
fun Day.toCalendarDay(): Int
{
	return when (this)
	{
		SUNDAY    -> Calendar.SUNDAY
		MONDAY    -> Calendar.MONDAY
		TUESDAY   -> Calendar.TUESDAY
		WEDNESDAY -> Calendar.WEDNESDAY
		THURSDAY  -> Calendar.THURSDAY
		FRIDAY    -> Calendar.FRIDAY
		SATURDAY  -> Calendar.SATURDAY
	}
}

/**
 * Convert a repeat frequency unit to a calendar field.
 */
fun Int.toCalendarField(): Int
{
	return when (this)
	{
		1 -> Calendar.MINUTE
		2 -> Calendar.HOUR_OF_DAY
		3 -> Calendar.DAY_OF_MONTH
		4 -> Calendar.WEEK_OF_YEAR
		5 -> Calendar.MONTH
		else -> Calendar.WEEK_OF_YEAR
	}
}

/**
 * Convert from a Calendar day to a Day.
 *
 * @return A Day.
 */
fun Int.toDay(): Day
{
	return when (this)
	{
		Calendar.SUNDAY    -> SUNDAY
		Calendar.MONDAY    -> MONDAY
		Calendar.TUESDAY   -> TUESDAY
		Calendar.WEDNESDAY -> WEDNESDAY
		Calendar.THURSDAY  -> THURSDAY
		Calendar.FRIDAY    -> FRIDAY
		Calendar.SATURDAY  -> SATURDAY
		else               -> SUNDAY
	}
}

/**
 * Convert a value to a set of days.
 *
 * The value of a set of days. Each day has a unique, 2^n,
 * value so only one bit is set. Doing a bitwise-and of a
 * day should compute to 1 if you have the correct day.
 *
 * @return A set of enum days.
 */
fun Int.toDays(): EnumSet<Day>
{
	// Initialize the set
	val days = Day.NONE

	// Iterate over each day in the week
	for (d in WEEK)
	{
		// Check the value
		if (d.value and this != 0)
		{
			days.add(d)
		}
	}

	// Return the set
	return days
}

/**
 * Check if a calendar's time matches the dismiss early time of an alarm.
 *
 * @return True if a calendar's time matches the dismiss early time of an alarm, and
 * False otherwise.
 */
fun Calendar.equalsDismissEarlyTime(alarm: NacAlarm): Boolean
{
	return (alarm.timeOfDismissEarlyAlarm > 0) && (this.timeInMillis == alarm.timeOfDismissEarlyAlarm)
}

/**
 * Convert an alarm to a string of days.
 *
 * If no days are specified and the alarm is enable.
 */
fun NacAlarm.toDayString(
	context: Context,
	start: Int
): String
{
	// Date
	if (this.date.isNotEmpty())
	{
		// Build a calendar with the date
		val cal = alarmToCalendar(this)

		// Convert date to string
		val date = DateFormat.format("MMM d", cal).toString()

		// Alarm will repeat
		if (this.shouldRepeat)
		{
			// Get the repeat frequency string
			val repeatFrequency = this.toRepeatFrequencyString(context)

			// Combine the date and repeat frequency string
			return "$date \u2027 $repeatFrequency"
		}
		// Do not repeat alarm
		else
		{
			// Simply return the date
			return date
		}
	}
	// No days
	else if (this.days.isEmpty())
	{
		// Today or tomorrow
		val oneTime = this.toOneTimeString(context)
		println("No days : $oneTime")

		// Alarm will not repeat
		if (!this.shouldRepeat)
		{
			// Only show the one time alarm
			return oneTime
		}

		// Repeat frequency string
		val repeatFrequency = this.toRepeatFrequencyString(context)
		println("No days repeat : $repeatFrequency")

		// Check the repeat frequency units
		return when (this.repeatFrequencyUnits)
		{
			// Minutes or Hours
			1, 2 ->
			{
				// Tomorrow string
				val tomorrow = context.getString(R.string.dow_tomorrow)

				// One time alarm will occur tomorrow
				if (oneTime == tomorrow)
				{
					// Tomorrow * Every X <min/hour>
					"$oneTime \u2027 $repeatFrequency"
				}
				// Alarm will occur today
				else
				{
					// Every X <min/hour>
					repeatFrequency
				}
			}

			// Days
			3 ->
			{
				// Every day alarm
				if (this.repeatFrequency == 1)
				{
					// Today/tomorrow
					oneTime
				}
				// Every X days
				else
				{
					// Today/tomorrow * Every X days
					"$oneTime \u2027 $repeatFrequency"
				}
			}

			// Months. Show: Today/tomorrow * Every X months
			5 -> "$oneTime \u2027 $repeatFrequency"

			// Unknown or weeks, but don't think this is possible? Since
			// no day = current day so a day would always be selected
			else -> repeatFrequency
		}
	}
	// Combination of days
	else
	{
		// Build string
		val days = when (this.days)
		{
			// Every day
			WEEK ->
			{
				context.getString(R.string.dow_everyday)
			}
			// Weekdays
			WEEKDAY ->
			{
				context.getString(R.string.dow_weekdays)
			}
			// Weekend
			WEEKEND ->
			{
				context.getString(R.string.dow_weekend)
			}
			// Other combination of days
			else ->
			{
				this.days.toDayString(context, start)
			}
		}

		println("Days : $days | Repeat? ${this.shouldRepeat} | ${this.repeatFrequencyUnits} | ${this.repeatFrequency}")
		// Alarm should be repeat at a frequency that is NOT just every 1 week (which is the norm)
		if (this.shouldRepeat && !((this.repeatFrequencyUnits == 4) && (this.repeatFrequency == 1)))
		{
			// Get the repeat frequency string
			val repeatFrequency = this.toRepeatFrequencyString(context)
			println("Jank : $repeatFrequency")

			// Combine the date and repeat frequency string
			return "$days \u2027 $repeatFrequency"
		}
		// Alarm will not be repeated or if it is, it will be every 1 week (the norm)
		else
		{
			return days
		}
	}
}

/**
 * Convert a one time alarm to a string indicating today or tomorrow.
 *
 * @param context Context.
 *
 * @return A one time alarm to a string indicating today or tomorrow.
 */
fun NacAlarm.toOneTimeString(context: Context): String
{
	// Get the current day
	val now = Calendar.getInstance()[Calendar.DAY_OF_MONTH]

	// Get the next time the alarm will ring
	val next = alarmToNextOneTimeCalendar(this)[Calendar.DAY_OF_MONTH]

	// Check if the two days are the same, that means that the name
	// should be today. Otherwise, it should show tomorrow
	return if (now == next)
	{
		context.getString(R.string.dow_today)
	}
	else
	{
		context.getString(R.string.dow_tomorrow)
	}
}

/**
 * Convert a repeat frequency to a string.
 *
 * @return A repeat frequency to a string.
 */
fun NacAlarm.toRepeatFrequencyString(context: Context): String
{
	// Check the repeat frequency units
	// Note: The "Every X <unit>" for Minutes, Hours, and Months reflects
	//       what is in alarmToNextOneTimeCalendar()
	return when (this.repeatFrequencyUnits)
	{
		// Minutes
		1 ->
		{
			// Every X minutes
			context.resources.getQuantityString(R.plurals.repeat_minutely,
				this.repeatFrequency, this.repeatFrequency)
		}

		// Hours
		2 ->
		{
			// Every X hours
			context.resources.getQuantityString(R.plurals.repeat_hourly,
				this.repeatFrequency, this.repeatFrequency)
		}

		// Days
		3 ->
		{
			// Check if repeating every day
			if (this.repeatFrequency == 1)
			{
				// Today or tomorrow
				this.toOneTimeString(context)
			}
			// Repeating every X days
			else
			{
				// Every X days
				context.resources.getQuantityString(R.plurals.repeat_daily,
					this.repeatFrequency, this.repeatFrequency)
			}
		}

		// Weeks
		4 ->
		{
			// Every X weeks
			context.resources.getQuantityString(R.plurals.repeat_weekly,
				this.repeatFrequency, this.repeatFrequency)
		}

		// Months
		5 ->
		{
			// Every X months
			context.resources.getQuantityString(R.plurals.repeat_monthly,
				this.repeatFrequency, this.repeatFrequency)
		}

		// Unknown
		else -> this.toOneTimeString(context)
	}
}

/**
 * A list of possible days the alarm can run on.
 */
object NacCalendar
{

	/**
	 * Convert the alarm on the given day to a Calendar.
	 *
	 * @param  alarm  The alarm.
	 * @param  day  The day to convert.
	 *
	 * @return A Calendar.
	 */
	private fun alarmDayToCalendar(alarm: NacAlarm, day: Day): Calendar
	{
		// Get the current calendar instance
		val now = Calendar.getInstance()

		// Build the alarm calendar instance. Setting the day of week could set the
		// calendar time to be in the past
		val alarmCalendar = alarmToCalendar(alarm)
		alarmCalendar[Calendar.DAY_OF_WEEK] = day.toCalendarDay()

		// Repeat alarm every X weeks (whatever the repeat frequency is) but
		// this day is not included to run early.
		//
		// Note: This check is before the calendar check below because if the calendar
		// check runs first, then the calendar could be ahead by 1 week
		if ((alarm.repeatFrequencyUnits == 4) && (alarm.repeatFrequency != 1) && !alarm.repeatFrequencyDaysToRunBeforeStarting.contains(day))
		{
			println("Hello alarmDayToCalendar() : ${alarmCalendar.before(now)} | ${alarm.repeatFrequencyUnits} | ${alarm.repeatFrequency} | $day")
			// Alarm will occur in X weeks
			alarmCalendar.add(alarm.repeatFrequencyUnits.toCalendarField(), alarm.repeatFrequency)
		}
		// Day has already past, so add a week to the next time in the future the day
		// will occur
		else if (alarmCalendar.before(now))
		{
			alarmCalendar.add(Calendar.WEEK_OF_YEAR, 1)
		}

		return alarmCalendar
	}

	/**
	 * Get a calendar with the alarm hour and minute.
	 *
	 * If a date is set, then the calendar will have the alarm's date. Otherwise, it will
	 * have today's date.
	 *
	 * @return A calendar with the alarm hour, minute, and date, if present.
	 */
	fun alarmToCalendar(alarm: NacAlarm): Calendar
	{
		// Get the current calendar instance
		val cal = Calendar.getInstance()

		// Set the calendar instance with attributes from the alarm
		cal[Calendar.HOUR_OF_DAY] = alarm.hour
		cal[Calendar.MINUTE] = alarm.minute
		cal[Calendar.SECOND] = 0
		cal[Calendar.MILLISECOND] = 0

		// Alarm date was set
		if (alarm.date.isNotEmpty())
		{
			// Get the year/month/day
			val (year, month, day) = alarm.date.split("-")

			// Build a calendar with that date
			cal[Calendar.YEAR] = year.toInt()
			cal[Calendar.MONTH] = month.toInt()-1
			cal[Calendar.DAY_OF_MONTH] = day.toInt()
		}

		return cal
	}

	/**
	 * Convert all the days an alarm is scheduled to go off, to Calendars.
	 *
	 * @param  alarm  The alarm.
	 *
	 * @return A list of Calendars.
	 */
	private fun alarmToCalendars(alarm: NacAlarm): List<Calendar>
	{
		val calendars: MutableList<Calendar> = ArrayList()

		// Date is set
		if (alarm.date.isNotEmpty())
		{
			val c = alarmToCalendar(alarm)

			// Alarm was dismissed early at the same time matching this calendar
			if (c.equalsDismissEarlyTime(alarm))
			{
				// Add the repeat frequency to this calendar
				c.add(alarm.repeatFrequencyUnits.toCalendarField(), alarm.repeatFrequency)
			}

			calendars.add(c)
		}
		// Days are selected
		else if (alarm.days.isNotEmpty())
		{
			// Iterate over each selected day
			for (d in alarm.days)
			{
				// Get calendar and time of calendar
				val c = alarmDayToCalendar(alarm, d)

				// Alarm was dismissed early at the same time matching this calendar
				if (c.equalsDismissEarlyTime(alarm))
				{
					// Add the repeat frequency to this calendar
					c.add(alarm.repeatFrequencyUnits.toCalendarField(), alarm.repeatFrequency)
				}

				// Add calendar to list of calendars
				calendars.add(c)
			}
		}
		// No days are selected. This alarm will occur either today or tomorrow and
		// is a one-time alarm
		else
		{
			val c = alarmToNextOneTimeCalendar(alarm)
			calendars.add(c)
		}

		return calendars
	}

	/**
	 * Convert the one time alarm to the next calendar.
	 */
	fun alarmToNextOneTimeCalendar(alarm: NacAlarm): Calendar
	{
		// Get the current calendar instance
		val now = Calendar.getInstance()

		// Build the alarm calendar instance
		val alarmCalendar = alarmToCalendar(alarm)

		// Alarm calendar occurs in the past or was dismissed early at the same time
		// matching this calendar
		if (alarmCalendar.before(now) || alarmCalendar.equalsDismissEarlyTime(alarm))
		{
			// Start the alarm the next day
			alarmCalendar.add(Calendar.DAY_OF_MONTH, 1)
		}

		// Return the alarm calendar
		return alarmCalendar
	}

	/**
	 * Convert the calendar to a string in the given format.
	 * TODO: Change back to private
	 */
	fun calendarToString(calendar: Calendar, format: String?): String
	{
		// Create the date format
		val locale = Locale.getDefault()
		val formatter = SimpleDateFormat(format, locale)

		// Format the calendar time
		return formatter.format(calendar.time)
	}

	/**
	 * Format hours.
	 */
	fun formatHour(hour: Int, is24HourFormat: Boolean): Int
	{
		return if (is24HourFormat)
		{
			// Use 24 hour format
			hour
		}
		else
		{
			// Convert to 12 hour format
			to12HourFormat(hour)
		}
	}

	/**
	 * Get the time.
	 *
	 * @param  context  The application context.
	 * @param  hour  The hour.
	 * @param  minute  The minutes.
	 *
	 * @return The time.
	 */
	fun getClockTime(context: Context, hour: Int, minute: Int): String
	{
		// Check if using 24 hour format
		val format = DateFormat.is24HourFormat(context)

		// Get the time
		return getClockTime(hour, minute, format)
	}

	/**
	 * Get the time.
	 *
	 * @param  hour    The hour.
	 * @param  minute  The minutes.
	 * @param  is24HourFormat  The 24 hour format, to determine how to interpret the hour.
	 *
	 * @return The time.
	 */
	private fun getClockTime(hour: Int, minute: Int, is24HourFormat: Boolean): String
	{
		// Format the hour
		val clockHour = formatHour(hour, is24HourFormat)

		// Zero pad the minutes and make it 2 digits long
		val clockMinute = minute.toString().padStart(2, '0')

		// Format the time
		return "$clockHour:$clockMinute"
	}

	/**
	 * Get the current date.
	 */
	fun getDate(calendar: Calendar): String
	{
		// Get the date
		val locale = Locale.getDefault()
		val skeleton = DateFormat.getBestDateTimePattern(locale, "E MMM d")
		val dateFormat = SimpleDateFormat(skeleton, locale)

		// Format the date
		dateFormat.timeZone = TimeZone.getDefault()
		dateFormat.applyLocalizedPattern(skeleton)

		return dateFormat.format(calendar.time)
	}

	/**
	 * Get the Calendar day on which the first upcoming reminder for the
	 * alarm will run.
	 *
	 * @param  alarm  The alarm.
	 *
	 * @return The Calendar day on which the first upcoming reminder for the
	 *         alarm will run.
	 */
	fun getFirstAlarmUpcomingReminder(alarm: NacAlarm, cal: Calendar): Calendar
	{
		// Get the current calendar and a copy of the calendar passed in
		val nowCal = Calendar.getInstance()
		val calCopy = cal.clone() as Calendar

		// Compute the number of minutes to subtract to show the upcoming
		// reminder at the correct time
		val minutes = -1 * alarm.timeToShowReminder

		// Subtract the number of minutes from when the alarm will run
		calCopy.add(Calendar.MINUTE, minutes)

		// Check if the calendar corresponds to a time after right now. It needs to be
		// after right now so that the reminder is shown
		if (calCopy.after(nowCal))
		{
			return calCopy
		}
		// Reminder will not be shown because the time has already passed, so need to
		// change the calendar that will be returned
		else
		{
			// Round up to the nearest minute and then add a minute
			nowCal.add(Calendar.MINUTE, 1)
			nowCal.add(Calendar.SECOND, 30)
			nowCal.set(Calendar.SECOND, 0)

			return nowCal
		}
	}

	/**
	 * The full time until the timer rings string, #h #m #s.
	 *
	 * @return The full time until the timer rings string, #h #m #s.
	 */
	fun getFullTimeUntilTimer(context: Context, secsUntilFinished: Long): String
	{
		// Get the hour, minute, and seconds values
		val hour = secsUntilFinished / 3600
		val minute = secsUntilFinished / 60
		val seconds = secsUntilFinished % 60

		// Get the hour, minute, and seconds letters
		val h = context.resources.getString(R.string.letter_h)
		val m = context.resources.getString(R.string.letter_m)
		val s = context.resources.getString(R.string.letter_s)

		var text = ""

		text  = if (hour > 0) "$hour$h " else text
		text += if (minute > 0) "$minute$m " else text
		text += "$seconds$s"

		// Get the full time until the timer rings
		return text
	}

	/**
	 * The full time string, EEE, HH:MM AM/PM.
	 *
	 * @return The full time string, EEE, HH:MM AM/PM.
	 */
	fun getFullTime(calendar: Calendar, is24HourFormat: Boolean): String
	{
		val hour = calendar[Calendar.HOUR_OF_DAY]
		val convertedHour = to12HourFormat(hour)

		// Get the time format
		var format = if (is24HourFormat)
		{
			"EEE HH:mm"
		}
		else
		{
			"EEE hh:mm a"
		}

		// Add some formatting thing for the hours that are less than 10
		if (convertedHour < 10)
		{
			format = format.replaceFirst("h".toRegex(), " ")
		}

		// Convert to a string
		return calendarToString(calendar, format)
	}

	/**
	 * The full time string, EEE, HH:MM AM/PM.
	 *
	 * @return The full time string, EEE, HH:MM AM/PM.
	 */
	fun getFullTime(context: Context, calendar: Calendar): String
	{
		// Get if 24 hour format is used or not
		val is24HourFormat = DateFormat.is24HourFormat(context)

		// Get the full time
		return getFullTime(calendar, is24HourFormat)
	}

	/**
	 * Get the time meridian.
	 *
	 * @param  hour  The hour.
	 *
	 * @return The time meridian.
	 */
	fun getMeridian(context: Context, hour: Int): String
	{
		// Check if time is in 24 hour format
		return if (DateFormat.is24HourFormat(context))
		{
			""
		}
		else
		{
			// Check that the hour is in the morning
			if (hour < 12)
			{
				context.getString(R.string.am)
			}
			// Check that the hour is in the evening
			else
			{
				context.getString(R.string.pm)
			}
		}
	}

	/**
	 * Get the alarm that will run next.
	 *
	 * @param  alarms  List of alarms to check.
	 *
	 * @return The alarm that will run next.
	 */
	fun getNextAlarm(alarms: List<NacAlarm>): NacNextAlarm?
	{
		var nextCalendar: Calendar? = null
		var nextAlarm: NacAlarm? = null

		// Iterate over each alarm
		for (a in alarms)
		{
			// Alarm is disabled
			if (!a.isEnabled)
			{
				continue
			}
			// Next alarm will be skipped, it only has one day set, and repeat is disabled
			else if (a.isNextSkippedAndFinal)
			{
				continue
			}

			// Get the calendar instance for the next time the alarm will run
			val calendar = getNextAlarmDay(a)!!

			// Check if this is either the first calendar being checked,
			// or if this calendar corresponds to an earlier time than the
			// "nextCalendar"
			if ((nextCalendar == null) || (calendar.before(nextCalendar)))
			{
				nextCalendar = calendar
				nextAlarm = a
			}
		}

		// Check that the alarm and calendar were set
		return if ((nextAlarm != null) && (nextCalendar != null))
		{
			// Create object that contains alarm and calendar
			NacNextAlarm(nextAlarm, nextCalendar)
		}
		// Unable to find the next alarm and calendar
		else
		{
			null
		}
	}

	/**
	 * Get the Calendar day on which the given alarm will run next.
	 *
	 * @param  alarm  The alarm who's days to check.
	 * @param  ignoreSkip  Whether the "shouldSkipNextAlarm" flag should be
	 *                     ignored or not.
	 *
	 * @return The Calendar day on which the given alarm will run next.
	 */
	fun getNextAlarmDay(alarm: NacAlarm, ignoreSkip: Boolean = false): Calendar?
	{
		// Convert the alarm to a list of calendar instances
		val calendars = alarmToCalendars(alarm)

		// Get the calendar day that is the soonest
		val nextDay = getNextDay(calendars)!!

		// Check if the next alarm should be skipped
		return if (alarm.shouldSkipNextAlarm && !ignoreSkip)
		{
			// Check if there was only one calendar in the list, but the alarm is scheduled to be repeated
			if ((calendars.size == 1) && alarm.shouldRepeat)
			{
				println("SKIPPIO. Should skip alarm but do not ignore skip and only one calendar in list: ${alarm.repeatFrequency} | ${alarm.repeatFrequencyUnits} | ${alarm.repeatFrequencyDaysToRunBeforeStarting}")
				// Add the repeat frequency to the calendar
				nextDay.add(alarm.repeatFrequencyUnits.toCalendarField(), alarm.repeatFrequency)
				nextDay
			}
			else
			{
				// Get another calendar day ignoring the one that was previously
				// determined
				getNextDay(calendars, skipDay = nextDay)
			}
		}
		else
		{
			// Return the next day
			nextDay
		}
	}

	/**
	 * Get the Calendary day that represents the next upcoming day.
	 *
	 * @return The Calendary day that represents the next upcoming day.
	 */
	private fun getNextDay(calendars: List<Calendar>, skipDay: Calendar? = null): Calendar?
	{
		var next: Calendar? = null

		// Iterate over each calendar instance
		for (c in calendars)
		{
			// Check if either the "next" calendar has not been set yet,
			// or occurs after the current calendar item.
			//
			// If "skipDay" is set, it will make sure to ignore that day since,
			// as the name suggests, it should be skipped
			next = if (((next == null) || next.after(c)) && (c != skipDay))
			{
				c
			}
			else
			{
				next
			}
		}

		// Get the calendar
		return next
	}

	/**
	 * Get the Calendar day on which the next upcoming reminder for the
	 * alarm will run.
	 *
	 * @param  alarm  The alarm.
	 *
	 * @return The Calendar day on which the next upcoming reminder for the
	 *         alarm will run.
	 */
	fun getNextAlarmUpcomingReminder(alarm: NacAlarm): Calendar
	{
		// Get right now
		val cal = Calendar.getInstance()

		// Add the reminder frequency and clear all fields smaller than MINUTE
		cal.add(Calendar.MINUTE, alarm.reminderFrequency)
		cal.set(Calendar.SECOND, 0)
		cal.set(Calendar.MILLISECOND, 0)

		return cal
	}

	/**
	 * Get the current timestamp in a desired format.
	 */
	fun getTimestamp(format: String): String
	{
		// Get the current time
		val locale = Locale.getDefault()
		val now = Calendar.getInstance().time

		// Create a date formatter
		val dateTimeFormat = SimpleDateFormat(format, locale)

		// Format the time
		return dateTimeFormat.format(now)
	}

	/**
	 * Convert an hour to 12 hour format.
	 */
	fun to12HourFormat(hour: Int): Int
	{
		// Check if hour is past noon
		return if (hour > 12)
		{
			hour % 12
		}
		else
		{
			// Check if hour is zero
			if (hour == 0)
			{
				12
			}
			// Return the hour
			else
			{
				hour
			}
		}
	}

	/**
	 * Day of week.
	 */
	enum class Day(

		/**
		 * The value associated with an enum.
		 */
		val value: Int

	)
	{

		/**
		 * Enums.
		 */
		SUNDAY(1), MONDAY(2), TUESDAY(4), WEDNESDAY(8), THURSDAY(16), FRIDAY(32), SATURDAY(64);

		companion object
		{

			/*
			 * A set of no days.
			 */
			val NONE: EnumSet<Day>
				get() = EnumSet.noneOf(Day::class.java)

			/**
			 * The Day today.
			 */
			val TODAY: Day
				get()
				{
					// Get the day of week from the calendar instance
					val today = Calendar.getInstance()
					val dow = today[Calendar.DAY_OF_WEEK]

					// Convert the day of week to a day
					return dow.toDay()
				}

			/**
			 * Every day of week.
			 */
			val WEEK: EnumSet<Day>
				get() = EnumSet.allOf(Day::class.java)

			/**
			 * All weekday days.
			 */
			val WEEKDAY: EnumSet<Day>
				get() = EnumSet.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)

			/**
			 * All weekend days.
			 */
			@Suppress("MemberVisibilityCanBePrivate")
			val WEEKEND: EnumSet<Day>
				get() = EnumSet.of(SUNDAY, SATURDAY)

			/**
			 * Length of week.
			 */
			@Suppress("MemberVisibilityCanBePrivate")
			val WEEK_LENGTH = WEEK.size

		}

	}

	/**
	 * Format calendar messages.
	 */
	object Message
	{

		/**
		 * Maximum message length.
		 */
		private const val MAXIMUM_LENGTH = 32

		/**
		 * Get the message to display when the next alarm will occur.
		 *
		 * @return The message to display when the next alarm will occur.
		 */
		fun getNext(
			context: Context,
			calendar: Calendar?,
			nextAlarmFormat: Int
		): String
		{
			// No calendar provided
			return if (calendar == null)
			{
				context.resources.getString(R.string.message_no_alarms_scheduled)
			}
			else
			{
				// e.g. Alarm in 12 hour 5 min
				if (nextAlarmFormat == 0)
				{
					// Get the remaining time until the alarm runs
					val timeRemaining = getTimeRemaining(context.resources, calendar)

					// Return
					context.resources.getString(R.string.message_next_alarm_in, timeRemaining)
				}
				// e.g. Alarm on ...
				else
				{
					// Get the time at which the alarm will run
					val is24HourFormat = DateFormat.is24HourFormat(context)
					val time = getFullTime(calendar, is24HourFormat)

					// Return
					context.resources.getString(R.string.message_next_alarm_on, time)
				}
			}
		}

		/**
		 * Get the message to display when an alarm will run IN some amount of time.
		 *
		 * @return The message to display when an alarm will run IN some amount of
		 *         time.
		 */
		private fun getTimeRemaining(
			resources: Resources,
			calendar: Calendar
		): String
		{
			// Get the time remaining
			val time = (calendar.timeInMillis - System.currentTimeMillis()) / 1000

			// Get the time components
			val day = (time / (60 * 60 * 24) % 365).toInt()
			val hr = (time / (60 * 60) % 24).toInt()
			val min = (time / 60 % 60).toInt()
			val sec = (time % 60).toInt()

			// Get the phrase for the different units of time
			val dayPhrase = resources.getQuantityString(R.plurals.unit_day, day, day)
			val hrPhrase = resources.getQuantityString(R.plurals.unit_hour, hr, hr)
			val minPhrase = resources.getQuantityString(R.plurals.unit_minute, min, min)
			val secPhrase = resources.getQuantityString(R.plurals.unit_second, sec, sec)

			// Format the time remaining message
			return if (day > 0)
			{
				// Days
				"$dayPhrase $hrPhrase"
			}
			else
			{
				if (hr > 0)
				{
					// Hours
					"$hrPhrase $minPhrase"
				}
				else
				{
					// Check if minutes is 0
					if (min == 0)
					{
						// Only show seconds since there are no minutes
						secPhrase
					}
					else
					{
						// Minutes and seconds
						"$minPhrase $secPhrase"
					}
				}
			}
		}

		/**
		 * Get the message to display when the alarm will run.
		 *
		 * @return The message to display when the alarm will run.
		 */
		fun getWillRun(
			context: Context,
			alarm: NacAlarm,
			nextAlarmFormat: Int
		): String
		{
			// Get the alarm name
			val name = alarm.getNameNormalizedForMessage(MAXIMUM_LENGTH)

			// Alarm is not enabled
			return if (!alarm.isEnabled)
			{
				// Alarm has a name
				if (name.isNotEmpty())
				{
					context.resources.getString(R.string.message_disabled_named_alarm, name)
				}
				// Alarm does not have a name
				else
				{
					context.resources.getString(R.string.message_disabled_general_alarm)
				}
			}
			else
			{
				// Get the next alarm day
				val calendar = getNextAlarmDay(alarm)

				// No alarm scheduled, possibly because the next alarm is skipped
				if (calendar == null)
				{
					// Alarm has a name
					if (name.isNotEmpty())
					{
						context.resources.getString(R.string.message_skip_named_alarm, name)
					}
					// Alarm does not have a name
					else
					{
						context.resources.getString(R.string.message_skip_general_alarm)
					}
				}
				// e.g. Alarm in 12 hour 5 min
				else if (nextAlarmFormat == 0)
				{
					// Get the remaining time until the alarm runs
					val timeRemaining = getTimeRemaining(context.resources, calendar)

					// Alarm has a name
					if (name.isNotEmpty())
					{
						context.resources.getString(R.string.message_will_run_in_named_alarm, name, timeRemaining)
					}
					// Alarm does not have a name
					else
					{
						context.resources.getString(R.string.message_will_run_in_general_alarm, "", timeRemaining)
					}
				}
				// e.g. Alarm on ...
				else
				{
					// Get the time at which the alarm will run
					val is24HourFormat = DateFormat.is24HourFormat(context)
					val time = getFullTime(calendar, is24HourFormat)

					// Alarm has a name
					if (name.isNotEmpty())
					{
						context.resources.getString(R.string.message_will_run_on_named_alarm, name, time)
					}
					// Alarm does not have a name
					else
					{
						context.resources.getString(R.string.message_will_run_on_general_alarm, "", time)
					}
				}
			}
		}

	}

}
