package com.nfcalarmclock.statistics.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import java.util.List;

/**
 * Data access object for storing when alarms were missed.
 */
@Dao
public interface NacAlarmMissedStatisticDao
	extends NacAlarmStatisticDao<NacAlarmMissedStatistic>
{

	/**
	 * Delete all rows from the table.
	 */
	@Query("DELETE FROM alarm_missed_statistic")
	int deleteAll();

	/**
	 * Get all instances when alarms were missed.
	 *
	 * @return All instances when alarms were missed.
	 */
	@Query("SELECT * FROM alarm_missed_statistic")
	LiveData<List<NacAlarmMissedStatistic>> getAll();

	/**
	 * Count the number of missed alarm statistics.
	 *
	 * @return The number of missed alarm statistics.
	 */
	@Query("SELECT COUNT(id) FROM alarm_missed_statistic")
	long getCount();

}
