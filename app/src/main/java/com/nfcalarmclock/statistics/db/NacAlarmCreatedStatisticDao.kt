package com.nfcalarmclock.statistics.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

/**
 * Data access object for storing when alarms were created.
 */
@Dao
interface NacAlarmCreatedStatisticDao : NacAlarmStatisticDao<NacAlarmCreatedStatistic>
{

	/**
	 * Delete all rows from the table.
	 */
	@Query("DELETE FROM alarm_created_statistic")
	fun deleteAll(): Int
	//TODO suspend fun deleteAll(): Int

	/**
	 * Get all instances when alarms were created.
	 *
	 * @return All instances when alarms were created.
	 */
	@get:Query("SELECT * FROM alarm_created_statistic")
	val all: LiveData<List<NacAlarmCreatedStatistic>>

	/**
	 * Get the date when the first alarm was created.
	 *
	 * @return The date when the first alarm was created.
	 */
	@get:Query("SELECT MIN(timestamp) FROM alarm_created_statistic LIMIT 1")
	val firstCreatedTimestamp: Long

	/**
	 * Count the number of created alarm statistics.
	 *
	 * @return The number of created alarm statistics.
	 */
	@get:Query("SELECT COUNT(id) FROM alarm_created_statistic")
	val count: Long

}