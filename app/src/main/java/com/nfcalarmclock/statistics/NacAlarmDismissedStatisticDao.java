package com.nfcalarmclock.statistics;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

/**
 * Data access object for storing when alarms were dismissed.
 */
@Dao
public interface NacAlarmDismissedStatisticDao
{

	/**
	 * Get all instances when alarms were dismissed.
	 *
	 * @return All instances when alarms were dismissed.
	 */
	@Query("SELECT * FROM alarm_dismissed_statistic")
	LiveData<List<NacAlarmDismissedStatistic>> getAll();

	/**
	 * Get all instances when alarms were dismissed.
	 *
	 * This will wait until all alarms are selected.
	 *
	 * @return All instances when alarms were dismissed.
	 */
	@Query("SELECT * FROM alarm_dismissed_statistic")
	List<NacAlarmDismissedStatistic> getAllNow();

	/**
	 * Insert an instance of an alarm being dismissed.
	 *
	 * @param  stat  Alarm dismissed statistic.
	 *
	 * @return The row ID of the row that was inserted.
	 */
	@Insert()
	long insert(NacAlarmDismissedStatistic stat);

}
