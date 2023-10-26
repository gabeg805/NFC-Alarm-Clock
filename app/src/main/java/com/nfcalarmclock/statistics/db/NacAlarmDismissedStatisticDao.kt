package com.nfcalarmclock.statistics.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

/**
 * Data access object for storing when alarms were dismissed.
 */
@Dao
interface NacAlarmDismissedStatisticDao : NacAlarmStatisticDao<NacAlarmDismissedStatistic>
{

	/**
	 * Delete all rows from the table.
	 */
	@Query("DELETE FROM alarm_dismissed_statistic")
	fun deleteAll(): Int
	//TODO suspend fun deleteAll(): Int

	/**
	 * Get all instances when alarms were dismissed.
	 *
	 * @return All instances when alarms were dismissed.
	 */
	@get:Query("SELECT * FROM alarm_dismissed_statistic")
	val all: LiveData<List<NacAlarmDismissedStatistic>>

	/**
	 * Cound the number of dismissed alarm statistics.
	 *
	 * @return The number of dismissed alarm statistics.
	 */
	@get:Query("SELECT COUNT(id) FROM alarm_dismissed_statistic")
	val count: Long

	/**
	 * Count the number of dismissed with NFC alarm statistics.
	 *
	 * @return The number of dismissed with NFC alarm statistics.
	 */
	@get:Query("SELECT COUNT(id) FROM alarm_dismissed_statistic WHERE used_nfc=1")
	val nfcCount: Long

}