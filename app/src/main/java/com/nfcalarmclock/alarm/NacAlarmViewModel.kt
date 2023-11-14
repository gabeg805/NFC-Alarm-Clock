package com.nfcalarmclock.alarm

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcalarmclock.alarm.db.NacAlarm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Alarm view model.
 */
@HiltViewModel
class NacAlarmViewModel @Inject constructor(

	/**
	 * Alarm repository.
	 */
	val alarmRepository: NacAlarmRepository

) : ViewModel()
{

	/**
	 * An active alarm.
	 */
	val activeAlarm: LiveData<NacAlarm>
		get() = alarmRepository.activeAlarm

	/**
	 * Live data list of all alarms.
	 */
	val allAlarms: LiveData<List<NacAlarm>> = alarmRepository.allAlarms

	/**
	 * Delete an alarm from the database, and cancel its scheduled run time.
	 *
	 * @param  alarm  Alarm to delete.
	 *
	 * @return The number of rows deleted.
	 */
	fun delete(alarm: NacAlarm)
	{
		viewModelScope.launch {
			alarmRepository.delete(alarm)
		}
	}

	/**
	 * Find an alarm.
	 *
	 * @param  id  The ID of the alarm to find.
	 *
	 * @return The alarm with the ID.
	 */
	suspend fun findAlarm(id: Long): NacAlarm? = alarmRepository.findAlarm(id)

	/**
	 * Insert an alarm into the database, and schedule the alarm to run.
	 *
	 * @param  alarm  The alarm to insert.
	 *
	 * @return The row ID of the alarm that was inserted.
	 */
	fun insert(alarm: NacAlarm, unit: () -> Unit = {})
	{
		viewModelScope.launch {

			// Get the row ID after inserting the alarm
			val rowId = alarmRepository.insert(alarm)

			// Alarm was inserted successfully
			if (rowId > 0)
			{
				// Alarm ID has not been set yet
				if (alarm.id == 0L)
				{
					alarm.id = rowId
				}
			}

			// Call unit
			unit()

		}
	}

	/**
	 * Update an alarm in the database, and schedule the alarm to run.
	 *
	 * @param  alarm  The alarm to update.
	 *
	 * @return The number of alarms updated.
	 */
	fun update(alarm: NacAlarm)
	{
		viewModelScope.launch {
			alarmRepository.update(alarm)
		}
	}

}