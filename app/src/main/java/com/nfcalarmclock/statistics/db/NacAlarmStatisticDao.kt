package com.nfcalarmclock.statistics.db

import androidx.room.Insert

/**
 * Data access object for storing when alarms were created.
 */
interface NacAlarmStatisticDao<T>
{

	/**
	 * Insert an instance of an alarm statistic.
	 */
	@Insert
	suspend fun insert(stat: T): Long

}