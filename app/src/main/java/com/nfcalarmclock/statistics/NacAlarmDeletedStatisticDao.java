package com.nfcalarmclock.statistics;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

/**
 * Data access object for storing when alarms were deleted.
 */
@Dao
public interface NacAlarmDeletedStatisticDao
	extends NacAlarmStatisticDao<NacAlarmDeletedStatistic>
{

	/**
	 * Delete all rows from the table.
	 */
	@Query("DELETE FROM alarm_deleted_statistic")
	int deleteAll();

	/**
	 * Get all instances when alarms were deleted.
	 *
	 * @return All instances when alarms were deleted.
	 */
	@Query("SELECT * FROM alarm_deleted_statistic")
	LiveData<List<NacAlarmDeletedStatistic>> getAll();

	/**
	 * Count the number of deleted alarm statistics.
	 *
	 * @return The number of deleted alarm statistics.
	 */
	@Query("SELECT COUNT(id) FROM alarm_deleted_statistic")
	long getCount();

}
