package com.nfcalarmclock.statistics.db

import androidx.room.Dao
import androidx.room.Query

/**
 * Data access object for storing when alarms were deleted.
 */
@Dao
interface NacAlarmDeletedStatisticDao
	: NacAlarmStatisticDao<NacAlarmDeletedStatistic>
{

	/**
	 * Count the number of deleted alarm statistics.
	 *
	 * @return The number of deleted alarm statistics.
	 */
	@Query("SELECT COUNT(id) FROM alarm_deleted_statistic")
	suspend fun count(): Long

	/**
	 * Delete all rows from the table.
	 */
	@Query("DELETE FROM alarm_deleted_statistic")
	suspend fun deleteAll(): Int

	/**
	 * Get all instances when alarms were deleted.
	 *
	 * @return All instances when alarms were deleted.
	 */
	@Query("SELECT * FROM alarm_deleted_statistic")
	suspend fun getAll(): List<NacAlarmDeletedStatistic>

}