package com.nfcalarmclock.statistics.db

import androidx.room.Dao
import androidx.room.Query

/**
 * Data access object for storing when alarms were created.
 */
@Dao
interface NacAlarmCreatedStatisticDao
	: NacAlarmStatisticDao<NacAlarmCreatedStatistic>
{

	/**
	 * Count the number of created alarm statistics.
	 *
	 * @return The number of created alarm statistics.
	 */
	@Query("SELECT COUNT(id) FROM alarm_created_statistic")
	suspend fun count(): Long

	/**
	 * Delete all rows from the table.
	 */
	@Query("DELETE FROM alarm_created_statistic")
	suspend fun deleteAll(): Int

	/**
	 * Get the date when the first alarm was created.
	 *
	 * @return The date when the first alarm was created.
	 */
	@Query("SELECT MIN(timestamp) FROM alarm_created_statistic LIMIT 1")
	suspend fun firstCreatedTimestamp(): Long

	/**
	 * Get all instances when alarms were created.
	 *
	 * @return All instances when alarms were created.
	 */
	@Query("SELECT * FROM alarm_created_statistic")
	suspend fun getAll(): List<NacAlarmCreatedStatistic>

}