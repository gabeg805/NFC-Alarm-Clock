package com.nfcalarmclock.statistics;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

/**
 * Data access object for storing when alarms were created.
 */
@Dao
public interface NacAlarmCreatedStatisticDao
{

	/**
	 * Get all instances when alarms were created.
	 *
	 * @return All instances when alarms were created.
	 */
	@Query("SELECT * FROM alarm_created_statistic")
	LiveData<List<NacAlarmCreatedStatistic>> getAll();

	/**
	 * Get all instances when alarms were created.
	 *
	 * This will wait until all alarms are selected.
	 *
	 * @return All instances when alarms were created.
	 */
	@Query("SELECT * FROM alarm_created_statistic")
	List<NacAlarmCreatedStatistic> getAllNow();

    /**
     * Insert an instance of an alarm being created.
     *
     * @param stat Alarm created statistic.
     * @return The row ID of the row that was inserted.
     */
    @Insert()
    long insert(NacAlarmCreatedStatistic stat);

}
