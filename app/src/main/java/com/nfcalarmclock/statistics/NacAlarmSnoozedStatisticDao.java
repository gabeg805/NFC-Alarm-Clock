package com.nfcalarmclock.statistics;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

/**
 * Data access object for storing when alarms were snoozed.
 */
@Dao
public interface NacAlarmSnoozedStatisticDao
{

	/**
	 * Get all instances when alarms were snoozed.
	 *
	 * @return All instances when alarms were snoozed.
	 */
	@Query("SELECT * FROM alarm_snoozed_statistic")
	LiveData<List<NacAlarmSnoozedStatistic>> getAll();

	/**
	 * Get all instances when alarms were snoozed.
	 *
	 * This will wait until all alarms are selected.
	 *
	 * @return All instances when alarms were snoozed.
	 */
	@Query("SELECT * FROM alarm_snoozed_statistic")
	List<NacAlarmSnoozedStatistic> getAllNow();

	/**
	 * Insert an instance of an alarm being snoozed.
	 *
	 * @param  stat  Alarm snoozed statistic.
	 *
	 * @return The row ID of the row that was inserted.
	 */
	@Insert(onConflict=OnConflictStrategy.ABORT)
	long insert(NacAlarmSnoozedStatistic stat);

}
