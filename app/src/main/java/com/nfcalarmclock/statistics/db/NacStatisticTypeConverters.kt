package com.nfcalarmclock.statistics.db

import androidx.room.TypeConverter
import java.util.Date

/**
 * Type converters for when an object is retrieved from the database.
 */
object NacStatisticTypeConverters
{

	/**
	 * Convert from a Long to a Date.
	 */
	@TypeConverter
	fun fromTimestamp(value: Long): Date
	{
		return Date(value)
	}

	/**
	 * Convert from a Date to a Long.
	 */
	@TypeConverter
	fun dateToTimestamp(date: Date): Long
	{
		return date.time
	}

}