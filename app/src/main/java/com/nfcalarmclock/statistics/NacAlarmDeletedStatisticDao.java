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
{

	/**
	 * Get all instances when alarms were deleted.
	 *
	 * @return All instances when alarms were deleted.
	 */
	@Query("SELECT * FROM alarm_deleted_statistic")
	LiveData<List<NacAlarmDeletedStatistic>> getAll();

	/**
	 * Get all instances when alarms were deleted.
	 *
	 * This will wait until all alarms are selected.
	 *
	 * @return All instances when alarms were deleted.
	 */
	@Query("SELECT * FROM alarm_deleted_statistic")
	List<NacAlarmDeletedStatistic> getAllNow();

	/**
	 * Insert an instance of an alarm being deleted.
	 *
	 * @param  stat  Alarm deleted statistic.
	 *
	 * @return The row ID of the row that was inserted.
	 */
	@Insert()
	long insert(NacAlarmDeletedStatistic stat);

}
