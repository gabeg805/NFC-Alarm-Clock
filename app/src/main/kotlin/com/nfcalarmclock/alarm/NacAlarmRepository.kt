package com.nfcalarmclock.alarm

import androidx.lifecycle.LiveData
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.db.NacAlarmDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

/**
 * Alarm repository.
 */
class NacAlarmRepository @Inject constructor(

	/**
	 * Data access object for an alarm.
	 */
	private val alarmDao: NacAlarmDao

)
{

	/**
	 * Live data list of all alarms.
	 */
	val allAlarms: LiveData<List<NacAlarm>>
		get() = alarmDao.allAlarmsLive

	/**
	 * Count the number of alarms.
	 *
	 * @return The number of alarms in the table.
	 */
	suspend fun count(): Int = alarmDao.count()

	/**
	 * Delete an alarm, asynchronously, from the database.
	 *
	 * @return The number of rows deleted.
	 */
	suspend fun delete(alarm: NacAlarm): Int = alarmDao.delete(alarm)

	/**
	 * Get an alarm with the given ID.
	 *
	 * @return An alarm with the given ID.
	 */
	suspend fun findAlarm(id: Long): NacAlarm? = alarmDao.findAlarm(id)

	/**
	 * An active alarm.
	 */
	suspend fun getActiveAlarm(): NacAlarm? = alarmDao.getActiveAlarm()

	/**
	 * The list of active alarms.
	 */
	suspend fun getActiveAlarms(): List<NacAlarm> = alarmDao.getActiveAlarms()

	/**
	 * All alarms in the database.
	 */
	suspend fun getAllAlarms(): List<NacAlarm> = alarmDao.getAllAlarms()

	/**
	 * Insert an alarm, asynchronously, into the database.
	 *
	 * @param alarm Alarm to insert.
	 *
	 * @return The row ID of the inserted alarm.
	 */
	suspend fun insert(alarm: NacAlarm): Long = alarmDao.insert(alarm)

	/**
	 * Update an alarm, asynchronously, in the database.
	 *
	 * @param alarm Alarm to update.
	 *
	 * @return The number of alarms updated.
	 */
	suspend fun update(alarm: NacAlarm): Int = alarmDao.update(alarm)

}

/**
 * Hilt module to provide an instance of the repository.
 */
@InstallIn(SingletonComponent::class)
@Module
class NacAlarmRepositoryModule
{

	/**
	 * Provide an instance of the repository.
	 */
	@Provides
	fun provideAlarmRepository(alarmDao: NacAlarmDao) : NacAlarmRepository
	{
		return NacAlarmRepository(alarmDao)
	}

}
