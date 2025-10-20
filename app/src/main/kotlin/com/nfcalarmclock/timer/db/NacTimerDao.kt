package com.nfcalarmclock.timer.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nfcalarmclock.alarm.db.NacAlarm

/**
 * Data access object for timers.
 */
@Dao
interface NacTimerDao
{

	/**
	 * Get all timers.
	 *
	 * @return All timers.
	 */
	@get:Query("SELECT * FROM timer")
	val allTimersLive: LiveData<List<NacTimer>>

	/**
	 * Count the number of timers.
	 *
	 * @return The number of timers in the table.
	 */
	@Query("SELECT COUNT(id) FROM timer")
	suspend fun count(): Int

	/**
	 * Delete a timer.
	 *
	 * @param timer Timer to delete.
	 *
	 * @return The number of rows deleted.
	 */
	@Delete
	suspend fun delete(timer: NacTimer): Int

	/**
	 * Get an active timer.
	 *
	 * @return An active timer.
	 */
	@Query("SELECT * FROM timer WHERE is_active=1 ORDER BY id ASC LIMIT 1")
	suspend fun getActiveTimer(): NacTimer?

	/**
	 * Get all timers.
	 * <p>
	 * This will wait until all timers are selected.
	 *
	 * @return All timers.
	 */
	@Query("SELECT * FROM timer")
	suspend fun getAllTimers(): List<NacTimer>

	/**
	 * Whether the table has a matching timer or not.
	 *
	 * @param id The ID of the timer to find.
	 *
	 * @return The number of timers that match the ID. Should be either 0 or 1.
	 */
	@Query("SELECT COUNT(id) FROM timer WHERE id=:id")
	suspend fun hasTimer(id: Long): Boolean

	/**
	 * Insert a timer.
	 *
	 * @param timer The timer to insert.
	 *
	 * @return The row ID of the timer that was inserted.
	 */
	@Insert
	suspend fun insert(timer: NacTimer): Long

	/**
	 * Update an existing timer.
	 *
	 * @param timer The timer to update.
	 *
	 * @return The number of timers updated.
	 */
	@Update
	suspend fun update(timer: NacTimer): Int

}