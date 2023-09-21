package com.nfcalarmclock.statistics.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import java.util.List;

/**
 * Data access object for storing when alarms were dismissed.
 */
@Dao
public interface NacAlarmDismissedStatisticDao
	extends NacAlarmStatisticDao<NacAlarmDismissedStatistic>
{

	/**
	 * Delete all rows from the table.
	 */
	@Query("DELETE FROM alarm_dismissed_statistic")
	int deleteAll();

	/**
	 * Get all instances when alarms were dismissed.
	 *
	 * @return All instances when alarms were dismissed.
	 */
	@Query("SELECT * FROM alarm_dismissed_statistic")
	LiveData<List<NacAlarmDismissedStatistic>> getAll();

	/**
	 * Count the number of dismissed alarm statistics.
	 *
	 * @return The number of dismissed alarm statistics.
	 */
	@Query("SELECT COUNT(id) FROM alarm_dismissed_statistic")
	long getCount();

	/**
	 * Count the number of dismissed with NFC alarm statistics.
	 *
	 * @return The number of dismissed with NFC alarm statistics.
	 */
	@Query("SELECT COUNT(id) FROM alarm_dismissed_statistic WHERE used_nfc=1")
	long getNfcCount();

}
