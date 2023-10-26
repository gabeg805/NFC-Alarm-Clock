package com.nfcalarmclock.alarm

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.db.NacAlarmDao
import com.nfcalarmclock.db.NacAlarmDatabase
import com.nfcalarmclock.db.NacRepository
import java.util.concurrent.Future

/**
 * Alarm repository.
 */
class NacAlarmRepository(app: Application) : NacRepository()
{

	/**
	 * Data access object for an alarm.
	 */
	private val alarmDao: NacAlarmDao

	/**
	 * Live data list of all alarms.
	 */
	val allAlarms: LiveData<List<NacAlarm>>

	/**
	 * Constructor.
	 */
	init
	{
		val db = NacAlarmDatabase.getInstance(app)
		val dao = db.alarmDao()
		alarmDao = dao
		allAlarms = dao.allAlarms
	}

	/**
	 * Constructor.
	 */
	constructor(context: Context) : this(context.applicationContext as Application)

	/**
	 * Copy an alarm, asynchronously, into the database.
	 *
	 * TODO: Be sure to test this when swiping.
	 *
	 * @param  copiedAlarm  Alarm that has been copied.
	 *
	 * @return The row ID of the inserted alarm.
	 */
	fun copy(copiedAlarm: NacAlarm?): Long
	{
		// Check if the copied alarm is null
		if (copiedAlarm == null)
		{
			return -1
		}

		// Insert the copcied alarm into the database
		val id = insert(copiedAlarm)

		// Set the alarm ID of the copied alarm
		copiedAlarm.id = id

		return id
	}

	/**
	 * Delete an alarm, asynchronously, from the database.
	 *
	 * @return The number of rows deleted.
	 */
	fun delete(alarm: NacAlarm?): Int
	{
		// Check if the alarm is null
		if (alarm == null)
		{
			return -1
		}

		// Delete the alarm
		val future = doDelete(alarm)

		return getIntegerFromFuture(future)
	}

	/**
	 * Delete an alarm, asynchronously, from the database.
	 */
	private fun doDelete(alarm: NacAlarm?): Future<*>?
	{
		// Check if the alarm is null
		if (alarm == null)
		{
			return null
		}

		return NacAlarmDatabase.executor.submit<Int> { alarmDao.delete(alarm) }
	}

	/**
	 * Find an alarm with the given ID.
	 */
	private fun doFindAlarm(id: Long): Future<*>?
	{
		// Check if the ID if valid
		if (id < 0)
		{
			return null
		}

		return NacAlarmDatabase.executor.submit<NacAlarm> { alarmDao.findAlarm(id) }
	}

	/**
	 * @see .doFindAlarm
	 */
	fun doFindAlarm(alarm: NacAlarm?): Future<*>?
	{
		val id = alarm?.id ?: -1

		return this.doFindAlarm(id)
	}

	/**
	 * Get all active alarms in the database.
	 *
	 * @return A list of all active alarms.
	 */
	private fun doGetActiveAlarmsNow(): Future<*>
	{
		return NacAlarmDatabase.executor.submit<List<NacAlarm>> { alarmDao.activeAlarmsNow }
	}

	/**
	 * Get all alarms in the database.
	 *
	 * @return A list of all alarms.
	 */
	private fun doGetAllAlarmsNow(): Future<*>
	{
		return NacAlarmDatabase.executor.submit<List<NacAlarm>> { alarmDao.allAlarmsNow }
	}

	/**
	 * Insert an alarm, asynchronously, into the database.
	 */
	private fun doInsert(alarm: NacAlarm?): Future<*>?
	{
		// Check if the alarm is null
		if (alarm == null)
		{
			return null
		}

		return NacAlarmDatabase.executor.submit<Long> { alarmDao.insert(alarm) }
	}

	/**
	 * Update an alarm, asynchronously, in the database.
	 */
	fun doUpdate(alarm: NacAlarm?): Future<*>?
	{
		// Check if the alarm is null
		if (alarm == null)
		{
			return null
		}

		return NacAlarmDatabase.executor.submit<Int> { alarmDao.update(alarm) }
	}

	/**
	 * Get an alarm with the given ID.
	 *
	 * @return An alarm with the given ID.
	 */
	fun findAlarm(id: Long): NacAlarm?
	{
		// Check if the ID is valid
		if (id < 0)
		{
			return null
		}

		// Find the alarm future
		val future = this.doFindAlarm(id)

		// Get the alarm from the future
		return getAlarmFromFuture(future)
	}

	/**
	 * @see .findAlarm
	 */
	fun findAlarm(alarm: NacAlarm?): NacAlarm?
	{
		val id = alarm?.id ?: -1

		return this.findAlarm(id)
	}

	/**
	 * An active alarm.
	 */
	val activeAlarm: LiveData<NacAlarm>
		get() = alarmDao.activeAlarm

	/**
	 * The list of active alarms.
	 */
	val activeAlarms: LiveData<List<NacAlarm>>
		get() = alarmDao.activeAlarms

	/**
	 * The list of active alarms.
	 */
	val activeAlarmsNow: List<NacAlarm>
		get()
		{
			val future = doGetActiveAlarmsNow()

			return getAlarmListFromFuture(future)
		}

	/**
	 * All alarms in the database.
	 */
	val allAlarmsNow: List<NacAlarm>
		get()
		{
			val future = doGetAllAlarmsNow()

			return getAlarmListFromFuture(future)
		}

	/**
	 * Insert an alarm, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm to insert.
	 *
	 * @return The row ID of the inserted alarm.
	 */
	fun insert(alarm: NacAlarm?): Long
	{
		// Check if the alarm is null
		if (alarm == null)
		{
			return -1
		}

		val future = doInsert(alarm)

		return getLongFromFuture(future)
	}

	/**
	 * Update an alarm, asynchronously, in the database.
	 *
	 * @param  alarm  Alarm to update.
	 *
	 * @return The number of alarms updated.
	 */
	fun update(alarm: NacAlarm?): Int
	{
		// Check if the alarm is null
		if (alarm == null)
		{
			return -1
		}

		val future = doUpdate(alarm)

		return getIntegerFromFuture(future)
	}
}