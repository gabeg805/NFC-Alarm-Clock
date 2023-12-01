package com.nfcalarmclock.statistics.db

import androidx.room.Dao
import androidx.room.Query

/**
 * Data access object for storing when alarms were dismissed.
 */
@Dao
interface NacAlarmDismissedStatisticDao
	: NacAlarmStatisticDao<NacAlarmDismissedStatistic>
{

	/**
	 * Cound the number of dismissed alarm statistics.
	 *
	 * @return The number of dismissed alarm statistics.
	 */
	@Query("SELECT COUNT(id) FROM alarm_dismissed_statistic")
	suspend fun count(): Long

	/**
	 * Delete all rows from the table.
	 */
	@Query("DELETE FROM alarm_dismissed_statistic")
	suspend fun deleteAll(): Int

	/**
	 * Count the number of dismissed with NFC alarm statistics.
	 *
	 * @return The number of dismissed with NFC alarm statistics.
	 */
	@Query("SELECT COUNT(id) FROM alarm_dismissed_statistic WHERE used_nfc=1")
	suspend fun nfcCount(): Long

	/**
	 * Get all instances when alarms were dismissed.
	 *
	 * @return All instances when alarms were dismissed.
	 */
	@Query("SELECT * FROM alarm_dismissed_statistic")
	suspend fun getAll(): List<NacAlarmDismissedStatistic>

}