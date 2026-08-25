package com.nfcalarmclock.alarm

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcalarmclock.alarm.db.NacAlarm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Alarm view model.
 *
 * @param alarmRepository Alarm repository.
 * @param savedState Saved state.
 */
@HiltViewModel
class NacAlarmViewModel @Inject constructor(
	val alarmRepository: NacAlarmRepository,
	private val savedState: SavedStateHandle
) : ViewModel()
{

	/**
	 * Live data list of all alarms.
	 */
	val allAlarms: LiveData<List<NacAlarm>> = alarmRepository.allAlarms

	/**
	 * List of alarm IDs corresponding to alarm cards that are expanded.
	 */
	var expandedAlarmIds: MutableList<Long>
		get()
		{
			// Saved state item is not present. Initialize it to an empty list
			if (savedState[SAVE_STATE_EXPANDED_ALARM_IDS] as Any? == null)
			{
				savedState[SAVE_STATE_EXPANDED_ALARM_IDS] = ArrayList<Long>()
			}

			// Return
			return (savedState[SAVE_STATE_EXPANDED_ALARM_IDS] as MutableList<Long>?)!!
		}
		set(value)
		{
			savedState[SAVE_STATE_EXPANDED_ALARM_IDS] = value
		}

	/**
	 * Recyclerview scroll state.
	 */
	var recyclerViewState: Parcelable?
		get()
		{
			return savedState[SAVE_STATE_REYCYCLERVIEW_SCROLL_STATE] as Parcelable?
		}
		set(value)
		{
			savedState[SAVE_STATE_REYCYCLERVIEW_SCROLL_STATE] = value
		}

	/**
	 * List of alarm IDs that are in sort order, how they are displayed to the user.
	 */
	var sortOrderedAlarmIds: List<Long>
		get()
		{
			// Saved state item is not present. Initialize it to an empty list
			if (savedState[SAVE_STATE_SORT_ORDERED_ALARM_IDS] as Any? == null)
			{
				savedState[SAVE_STATE_SORT_ORDERED_ALARM_IDS] = ArrayList<Long>()
			}

			// Return
			return (savedState[SAVE_STATE_SORT_ORDERED_ALARM_IDS] as List<Long>?)!!
		}
		set(value)
		{
			savedState[SAVE_STATE_SORT_ORDERED_ALARM_IDS] = value
		}

	/**
	 * Count the number of alarms.
	 *
	 * @return The number of alarms in the table.
	 */
	suspend fun count(): Int = alarmRepository.count()

	/**
	 * Delete an alarm from the database.
	 *
	 * @param alarm Alarm to delete.
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
	 * @param id The ID of the alarm to find.
	 *
	 * @return The alarm with the ID.
	 */
	@Suppress("unused")
	suspend fun findAlarm(id: Long): NacAlarm? = alarmRepository.findAlarm(id)

	/**
	 * Find the current list of NFC tags needed to dismiss an alarm.
	 *
	 * @param id The ID of the alarm to find.
	 *
	 * @return The current list of NFC tags needed to dismiss an alarm.
	 */
	suspend fun findCurrentNfcTagsNeededToDismiss(id: Long): String = alarmRepository.findCurrentNfcTagsNeededToDismiss(id)

	/**
	 * Get an active alarm.
	 *
	 * @return An active alarm.
	 */
	suspend fun getActiveAlarm(): NacAlarm? = alarmRepository.getActiveAlarm()

	/**
	 * All alarms in the database.
	 */
	suspend fun getAllAlarms(): List<NacAlarm> = alarmRepository.getAllAlarms()

	/**
	 * Insert an alarm into the database.
	 *
	 * @param alarm The alarm to insert.
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
	 * Update an alarm in the database.
	 *
	 * @param alarm The alarm to update.
	 *
	 * @return The number of alarms updated.
	 */
	fun update(alarm: NacAlarm, unit: () -> Unit = {})
	{
		viewModelScope.launch {

			// Update the alarm
			alarmRepository.update(alarm)

			// Call the unit
			unit()

		}
	}

	companion object
	{

		/**
		 * Key for the save instance state that will contain the list of alarm IDs that
		 * correspond to expanded alarm cards.
		 */
		const val SAVE_STATE_EXPANDED_ALARM_IDS = "com.nfcalarmclock.alarm.SAVE_STATE_EXPANDED_ALARM_IDS"

		/**
		 * Key for the reycyclerview scroll state.
		 */
		const val SAVE_STATE_REYCYCLERVIEW_SCROLL_STATE = "com.nfcalarmclock.alarm.SAVE_STATE_REYCYCLERVIEW_SCROLL_STATE"

		/**
		 * Key for the save instance state that will contain the list of alarm IDs that
		 * are in sort order, how they are displayed to the user.
		 */
		const val SAVE_STATE_SORT_ORDERED_ALARM_IDS = "com.nfcalarmclock.alarm.SAVE_STATE_SORT_ORDERED_ALARM_IDS"

	}

}