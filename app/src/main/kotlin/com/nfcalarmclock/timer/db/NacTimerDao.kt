package com.nfcalarmclock.timer.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

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