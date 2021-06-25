package com.nfcalarmclock.statistics;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

/**
 * Data access object for storing when alarms were snoozed.
 */
@Dao
public interface NacAlarmSnoozedStatisticDao
	extends NacAlarmStatisticDao<NacAlarmSnoozedStatistic>
{

	/**
	 * Get all instances when alarms were snoozed.
	 *
	 * @return All instances when alarms were snoozed.
	 */
	@Query("SELECT * FROM alarm_snoozed_statistic")
	LiveData<List<NacAlarmSnoozedStatistic>> getAll();

	/**
	 * Count the number of snoozed alarm statistics.
	 *
	 * @return The number of snoozed alarm statistics.
	 */
	@Query("SELECT COUNT(id) FROM alarm_snoozed_statistic")
	long getCount();

}
