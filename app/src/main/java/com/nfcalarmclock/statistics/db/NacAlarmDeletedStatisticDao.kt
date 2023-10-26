package com.nfcalarmclock.statistics.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

/**
 * Data access object for storing when alarms were deleted.
 */
@Dao
interface NacAlarmDeletedStatisticDao : NacAlarmStatisticDao<NacAlarmDeletedStatistic>
{

	/**
	 * Delete all rows from the table.
	 */
	@Query("DELETE FROM alarm_deleted_statistic")
	fun deleteAll(): Int
	//TODO suspend fun deleteAll(): Int

	/**
	 * Get all instances when alarms were deleted.
	 *
	 * @return All instances when alarms were deleted.
	 */
	@get:Query("SELECT * FROM alarm_deleted_statistic")
	val all: LiveData<List<NacAlarmDeletedStatistic>>

	/**
	 * Count the number of deleted alarm statistics.
	 *
	 * @return The number of deleted alarm statistics.
	 */
	@get:Query("SELECT COUNT(id) FROM alarm_deleted_statistic")
	val count: Long

}