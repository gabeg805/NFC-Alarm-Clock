package com.nfcalarmclock.alarm;

import androidx.room.TypeConverter;
import java.util.Date;

/**
 * Type converters for when an object is retrieved from the database.
 */
public class NacAlarmCreatedStatisticTypeConverters
{

	/**
	 * Convert from a Long to a Date.
	 */
	@TypeConverter
	public static Date fromTimestamp(Long value)
	{
		return value == null ? null : new Date(value);
	}

	/**
	 * Convert from a Date to a Long.
	 */
	@TypeConverter
	public static Long dateToTimestamp(Date date)
	{
		return date == null ? null : date.getTime();
	}

}
