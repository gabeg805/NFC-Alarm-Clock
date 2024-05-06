package com.nfcalarmclock.alarm.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Data access object for alarms.
 */
@Dao
interface NacAlarmDao
{

	/**
	 * Get all alarms.
	 *
	 * @return All alarms.
	 */
	@get:Query("SELECT * FROM alarm")
	val allAlarms: LiveData<List<NacAlarm>>

	/**
	 * Delete an alarm.
	 *
	 * @param  alarm  Alarm to delete.
	 *
	 * @return The number of rows deleted.
	 */
	@Delete
	suspend fun delete(alarm: NacAlarm): Int

	/**
	 * Delete all alarms.
	 *
	 * @return The number of rows deleted.
	 */
	@Query("DELETE FROM alarm")
	suspend fun deleteAll(): Int

	/**
	 * Find an alarm.
	 *
	 * @param  id  The ID of the alarm to find.
	 *
	 * @return The alarm with the ID.
	 */
	@Query("SELECT * FROM alarm WHERE id=:id")
	suspend fun findAlarm(id: Long): NacAlarm?

	/**
	 * Get an active alarm, the alarm that has been active the longest.
	 *
	 * @return An active alarm, the alarm that has been active the longest.
	 */
	@Query("SELECT * FROM alarm WHERE is_active=1 ORDER BY time_active DESC LIMIT 1")
	suspend fun getActiveAlarm(): NacAlarm?

	/**
	 * Get a list of all active alarms.
	 * <p>
	 * This will wait until all alarms are selected.
	 *
	 * @return List of all active alarms.
	 */
	@Query("SELECT * FROM alarm WHERE is_active=1")
	suspend fun getActiveAlarms(): List<NacAlarm>

	/**
	 * Get all alarms.
	 * <p>
	 * This will wait until all alarms are selected.
	 *
	 * @return All alarms.
	 */
	@Query("SELECT * FROM alarm")
	suspend fun getAllAlarms(): List<NacAlarm>

	/**
	 * Insert an alarm.
	 *
	 * @param  alarm  The alarm to insert.
	 *
	 * @return The row ID of the alarm that was inserted.
	 */
	@Insert
	suspend fun insert(alarm: NacAlarm): Long

	/**
	 * Update an existing alarm.
	 *
	 * @param  alarm  The alarm to update.
	 *
	 * @return The number of alarms updated.
	 */
	@Update
	suspend fun update(alarm: NacAlarm): Int

}