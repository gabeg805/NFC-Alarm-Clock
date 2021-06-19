package com.nfcalarmclock.statistics;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

/**
 * Data access object for storing when alarms were missed.
 */
@Dao
public interface NacAlarmMissedStatisticDao
{

	/**
	 * Get all instances when alarms were missed.
	 *
	 * @return All instances when alarms were missed.
	 */
	@Query("SELECT * FROM alarm_missed_statistic")
	LiveData<List<NacAlarmMissedStatistic>> getAll();

	/**
	 * Get all instances when alarms were missed.
	 *
	 * This will wait until all alarms are selected.
	 *
	 * @return All instances when alarms were missed.
	 */
	@Query("SELECT * FROM alarm_missed_statistic")
	List<NacAlarmMissedStatistic> getAllNow();

	/**
	 * Insert an instance of an alarm being missed.
	 *
	 * @param  stat  Alarm missed statistic.
	 *
	 * @return The row ID of the row that was inserted.
	 */
	@Insert(onConflict=OnConflictStrategy.ABORT)
	long insert(NacAlarmMissedStatistic stat);

}
