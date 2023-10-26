package com.nfcalarmclock.statistics.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

/**
 * Data access object for storing when alarms were snoozed.
 */
@Dao
interface NacAlarmSnoozedStatisticDao : NacAlarmStatisticDao<NacAlarmSnoozedStatistic>
{

	/**
	 * Delete all rows from the table.
	 */
	@Query("DELETE FROM alarm_snoozed_statistic")
	fun deleteAll(): Int
	//TODO suspend fun deleteAll(): Int

	/**
	 * Get all instances when alarms were snoozed.
	 *
	 * @return All instances when alarms were snoozed.
	 */
	@get:Query("SELECT * FROM alarm_snoozed_statistic")
	val all: LiveData<List<NacAlarmSnoozedStatistic>>

	/**
	 * Count the number of snoozed alarm statistics.
	 *
	 * @return The number of snoozed alarm statistics.
	 */
	@get:Query("SELECT COUNT(id) FROM alarm_snoozed_statistic")
	val count: Long

	/**
	 * Get the total snooze duration.
	 *
	 * @return The total snooze duration.
	 */
	@get:Query("SELECT SUM(duration) FROM alarm_snoozed_statistic")
	val totalDuration: Long

}