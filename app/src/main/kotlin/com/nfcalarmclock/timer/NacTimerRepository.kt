package com.nfcalarmclock.timer

import androidx.lifecycle.LiveData
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.timer.db.NacTimerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

/**
 * Timer repository.
 */
class NacTimerRepository @Inject constructor(

	/**
	 * Data access object for a timer.
	 */
	private val timerDao: NacTimerDao

)
{

	/**
	 * Live data list of all timers.
	 */
	val allTimers: LiveData<List<NacTimer>>
		get() = timerDao.allTimersLive

	/**
	 * Count the number of timers.
	 *
	 * @return The number of timers in the table.
	 */
	suspend fun count(): Int = timerDao.count()

	/**
	 * Count the number of active timers.
	 *
	 * @return The number of active timers in the table.
	 */
	suspend fun countActive(): Int = timerDao.countActive()

	/**
	 * Delete a timer, asynchronously, from the database.
	 *
	 * @return The number of rows deleted.
	 */
	suspend fun delete(timer: NacTimer): Int = timerDao.delete(timer)

	/**
	 * All active timer.
	 */
	suspend fun getAllActiveTimers(): List<NacTimer> = timerDao.getAllActiveTimers() ?: emptyList()

	/**
	 * All timers in the database.
	 */
	suspend fun getAllTimers(): List<NacTimer> = timerDao.getAllTimers()

	/**
	 * Whether the table has a matching timer or not.
	 *
	 * @param id The ID of the timer to find.
	 *
	 * @return The number of timers that match the ID. Should be either 0 or 1.
	 */
	suspend fun hasTimer(id: Long): Boolean = timerDao.hasTimer(id)

	/**
	 * Insert a timer, asynchronously, into the database.
	 *
	 * @param timer Timer to insert.
	 *
	 * @return The row ID of the inserted timer.
	 */
	suspend fun insert(timer: NacTimer): Long = timerDao.insert(timer)

	/**
	 * Update a timer, asynchronously, in the database.
	 *
	 * @param timer Timer to update.
	 *
	 * @return The number of timers updated.
	 */
	suspend fun update(timer: NacTimer): Int = timerDao.update(timer)

}

/**
 * Hilt module to provide an instance of the repository.
 */
@InstallIn(SingletonComponent::class)
@Module
class NacTimerRepositoryModule
{

	/**
	 * Provide an instance of the repository.
	 */
	@Provides
	fun provideTimerRepository(timerDao: NacTimerDao) : NacTimerRepository
	{
		return NacTimerRepository(timerDao)
	}


}