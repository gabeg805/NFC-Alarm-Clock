package com.nfcalarmclock.statistics.db

import androidx.room.Dao
import androidx.room.Query

/**
 * Data access object for storing when alarms were missed.
 */
@Dao
interface NacAlarmMissedStatisticDao
	: NacAlarmStatisticDao<NacAlarmMissedStatistic>
{

	/**
	 * Count the number of missed alarm statistics.
	 *
	 * @return The number of missed alarm statistics.
	 */
	@Query("SELECT COUNT(id) FROM alarm_missed_statistic")
	suspend fun count(): Long

	/**
	 * Delete all rows from the table.
	 */
	@Query("DELETE FROM alarm_missed_statistic")
	suspend fun deleteAll(): Int

	/**
	 * Get all instances when alarms were missed.
	 *
	 * @return All instances when alarms were missed.
	 */
	@Query("SELECT * FROM alarm_missed_statistic")
	suspend fun getAll(): List<NacAlarmMissedStatistic>

}