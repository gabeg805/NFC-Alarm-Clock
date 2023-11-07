package com.nfcalarmclock.alarm

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.scheduler.NacScheduler

/**
 * Alarm view model.
 */
class NacAlarmViewModel(app: Application) : AndroidViewModel(app)
{

	/**
	 * Repository of the alarms.
	 */
	val repository: NacAlarmRepository

	/**
	 * Live data list of all alarms.
	 */
	val allAlarms: LiveData<List<NacAlarm>>

	/**
	 * Constructor.
	 */
	init
	{
		val repo = NacAlarmRepository(app)
		repository = repo
		allAlarms = repo.allAlarms
	}

	/**
	 * Copy an alarm into the database.
	 *
	 * @param  copiedAlarm  Alarm that has been copied.
	 *
	 * @return The row ID of the inserted alarm.
	 */
	fun copy(copiedAlarm: NacAlarm?): Long
	{
		// TODO: What should I do with the scheduler here? Do nothing?
		return repository.copy(copiedAlarm)
	}

	/**
	 * Delete an alarm from the database, and cancel its scheduled run time.
	 *
	 * @param  context  Context.
	 * @param  alarm  Alarm to delete.
	 *
	 * @return The number of rows deleted.
	 */
	fun delete(context: Context, alarm: NacAlarm?): Int
	{
		NacScheduler.cancel(context, alarm)

		return repository.delete(alarm)
	}

	/**
	 * Find an alarm.
	 *
	 * @param  id  The ID of the alarm to find.
	 *
	 * @return The alarm with the ID.
	 */
	fun findAlarm(id: Long): NacAlarm?
	{
		return repository.findAlarm(id)
	}

	/**
	 * @see .findAlarm
	 */
	fun findAlarm(alarm: NacAlarm?): NacAlarm?
	{
		return repository.findAlarm(alarm)
	}

	/**
	 * An active alarm.
	 */
	val activeAlarm: LiveData<NacAlarm>
		get() = repository.activeAlarm

	/**
	 * A list of all active alarms.
	 */
	val activeAlarms: LiveData<List<NacAlarm>>
		get() = repository.activeAlarms

	/**
	 * Insert an alarm into the database, and schedule the alarm to run.
	 *
	 * @param  context  Context.
	 * @param  alarm  The alarm to insert.
	 *
	 * @return The row ID of the alarm that was inserted.
	 */
	fun insert(context: Context, alarm: NacAlarm?): Long
	{
		// Check if the alarm is null
		if (alarm == null)
		{
			return -1
		}

		val alarmId = alarm.id
		val rowId = repository.insert(alarm)

		// Alarm was inserted successfully
		if (rowId > 0)
		{
			// Alarm ID has not been set yet
			if (alarmId == 0L)
			{
				alarm.id = rowId
			}

			// Update the scheduler
			NacScheduler.update(context, alarm)
		}

		return rowId
	}

	/**
	 * Update an alarm in the database, and schedule the alarm to run.
	 *
	 * @param  context  Context.
	 * @param  alarm  The alarm to update.
	 *
	 * @return The number of alarms updated.
	 */
	fun update(context: Context, alarm: NacAlarm?): Int
	{
		// Check if alarm is null
		if (alarm == null)
		{
			return 0
		}

		// Update the scheduler
		NacScheduler.update(context, alarm)

		return repository.update(alarm)
	}

}