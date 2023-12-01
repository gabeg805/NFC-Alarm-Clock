package com.nfcalarmclock.statistics.db

import androidx.room.Dao
import androidx.room.Query

/**
 * Data access object for storing when alarms were snoozed.
 */
@Dao
interface NacAlarmSnoozedStatisticDao
	: NacAlarmStatisticDao<NacAlarmSnoozedStatistic>
{

	/**
	 * Count the number of snoozed alarm statistics.
	 *
	 * @return The number of snoozed alarm statistics.
	 */
	@Query("SELECT COUNT(id) FROM alarm_snoozed_statistic")
	suspend fun count(): Long

	/**
	 * Delete all rows from the table.
	 */
	@Query("DELETE FROM alarm_snoozed_statistic")
	suspend fun deleteAll(): Int

	/**
	 * Get the total snooze duration.
	 *
	 * @return The total snooze duration.
	 */
	@Query("SELECT SUM(duration) FROM alarm_snoozed_statistic")
	suspend fun totalDuration(): Long

	/**
	 * Get all instances when alarms were snoozed.
	 *
	 * @return All instances when alarms were snoozed.
	 */
	@Query("SELECT * FROM alarm_snoozed_statistic")
	suspend fun getAll(): List<NacAlarmSnoozedStatistic>

}