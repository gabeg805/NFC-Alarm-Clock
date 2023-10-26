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
	 * Delete an alarm.
	 *
	 * @param  alarm  Alarm to delete.
	 *
	 * @return The number of rows deleted.
	 */
	@Delete
	fun delete(alarm: NacAlarm?): Int
	//TODO suspend fun delete(alarm: NacAlarm?): Int

	/**
	 * Delete all alarms.
	 *
	 * @return The number of rows deleted.
	 */
	@Query("DELETE FROM alarm")
	fun deleteAll(): Int
	//TODO suspend fun deleteAll(): Int

	/**
	 * Find an alarm.
	 *
	 * @param  id  The ID of the alarm to find.
	 *
	 * @return The alarm with the ID.
	 */
	@Query("SELECT * FROM alarm WHERE id=:id")
	fun findAlarm(id: Long): NacAlarm?

	/**
	 * Get an active alarm.
	 *
	 * @return An active alarm.
	 */
	@get:Query("SELECT * FROM alarm WHERE is_active=1 LIMIT 1")
	val activeAlarm: LiveData<NacAlarm>

	/**
	 * Get a list of all active alarms.
	 *
	 * @return List of all active alarms.
	 */
	@get:Query("SELECT * FROM alarm WHERE is_active=1")
	val activeAlarms: LiveData<List<NacAlarm>>

	/**
	 * Get a list of all active alarms.
	 * <p>
	 * This will wait until all alarms are selected.
	 *
	 * @return List of all active alarms.
	 */
	@get:Query("SELECT * FROM alarm WHERE is_active=1")
	val activeAlarmsNow: List<NacAlarm>

	/**
	 * Get all alarms.
	 *
	 * @return All alarms.
	 */
	@get:Query("SELECT * FROM alarm")
	val allAlarms: LiveData<List<NacAlarm>>

	/**
	 * Get all alarms.
	 * <p>
	 * This will wait until all alarms are selected.
	 *
	 * @return All alarms.
	 */
	@get:Query("SELECT * FROM alarm")
	val allAlarmsNow: List<NacAlarm>

	/**
	 * Insert an alarm.
	 *
	 * @param  alarm  The alarm to insert.
	 *
	 * @return The row ID of the alarm that was inserted.
	 */
	@Insert
	fun insert(alarm: NacAlarm?): Long
	//TODO suspend fun insert(alarm: NacAlarm?): Long

	/**
	 * Update an existing alarm
	 *
	 * @param  alarm  The alarm to update.
	 *
	 * @return The number of alarms updated.
	 */
	@Update
	fun update(alarm: NacAlarm?): Int
	//TODO suspend fun update(alarm: NacAlarm?): Int

}