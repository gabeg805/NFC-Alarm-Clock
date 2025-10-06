package com.nfcalarmclock.timer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcalarmclock.timer.db.NacTimer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Timer view model.
 */
@HiltViewModel
class NacTimerViewModel @Inject constructor(

	/**
	 * Timer repository.
	 */
	val timerRepository: NacTimerRepository

) : ViewModel()
{

	/**
	 * Live data list of all timers.
	 */
	val allTimers: LiveData<List<NacTimer>> = timerRepository.allTimers

	/**
	 * Count the number of timers.
	 *
	 * @return The number of timers in the table.
	 */
	suspend fun count(): Int = timerRepository.count()

	/**
	 * Delete a timer from the database.
	 *
	 * @param timer Timer to delete.
	 *
	 * @return The number of rows deleted.
	 */
	fun delete(timer: NacTimer)
	{
		viewModelScope.launch {
			timerRepository.delete(timer)
		}
	}

	/**
	 * Get an active timer.
	 *
	 * @return An active timer.
	 */
	suspend fun getActiveTimer(): NacTimer? = timerRepository.getActiveTimer()

	/**
	 * Insert a timer into the database.
	 *
	 * @param timer The timer to insert.
	 *
	 * @return The row ID of the timer that was inserted.
	 */
	fun insert(timer: NacTimer, unit: () -> Unit = {})
	{
		viewModelScope.launch {

			// Get the row ID after inserting the timer
			val rowId = timerRepository.insert(timer)

			// Alarm was inserted successfully
			if (rowId > 0)
			{
				// Timer ID has not been set yet
				if (timer.id == 0L)
				{
					timer.id = rowId
				}
			}

			// Call unit
			unit()

		}
	}

	/**
	 * Update a timer in the database.
	 *
	 * @param timer The timer to update.
	 *
	 * @return The number of timers updated.
	 */
	fun update(timer: NacTimer, unit: () -> Unit = {})
	{
		viewModelScope.launch {

			// Update the timer
			timerRepository.update(timer)

			// Call the unit
			unit()

		}
	}

}