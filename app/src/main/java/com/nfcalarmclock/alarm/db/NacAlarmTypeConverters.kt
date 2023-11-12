package com.nfcalarmclock.alarm.db

import androidx.room.TypeConverter
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.util.NacCalendar.Day
import java.util.EnumSet

/**
 * Type converters for when an object is retrieved from the database.
 */
object NacAlarmTypeConverters
{

	/**
	 * Convert from an integer day value to an enumerated set of days.
	 */
	@TypeConverter
	fun dayValueToDays(value: Int): EnumSet<Day>
	{
		return NacCalendar.Day.valueToDays(value)
	}

	/**
	 * Convert from an enumerated set of days to an integer day value.
	 */
	@TypeConverter
	fun daysToDayValue(days: EnumSet<Day>): Int
	{
		return NacCalendar.Day.daysToValue(days)
	}

}