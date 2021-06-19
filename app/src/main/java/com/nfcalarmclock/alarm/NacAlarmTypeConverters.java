package com.nfcalarmclock.alarm;

import androidx.room.TypeConverter;
import com.nfcalarmclock.system.NacCalendar;
import java.util.EnumSet;

/**
 * Type converters for when an object is retrieved from the database.
 */
public class NacAlarmTypeConverters
{

	/**
	 * Convert from an integer day value to an enumerated set of days.
	 */
	@TypeConverter
	public static EnumSet<NacCalendar.Day> dayValueToDays(int value)
	{
		return NacCalendar.Days.valueToDays(value);
	}

	/**
	 * Convert from an enumerated set of days to an integer day value.
	 */
	@TypeConverter
	public static int daysToDayValue(EnumSet<NacCalendar.Day> days)
	{
		return NacCalendar.Days.daysToValue(days);
	}

}
