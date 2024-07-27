package com.nfcalarmclock.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcalarmclock.alarm.db.NacAlarm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Statistic view model.
 */
@HiltViewModel
class NacAlarmStatisticViewModel @Inject constructor(

	/**
	 * Statistic repository.
	 */
	val statisticRepository: NacAlarmStatisticRepository

	// Constructor
) : ViewModel()
{

	/**
	 * The number of created alarm statistics.
	 */
	suspend fun createdCount(): Long = statisticRepository.createdCount()

	/**
	 * The timestamp when the first alarm was created as a long type.
	 */
	suspend fun createdFirstTimestamp(): Long = statisticRepository.createdFirstTimestamp()

	/**
	 * The number of deleted alarm statistics.
	 */
	suspend fun deletedCount(): Long = statisticRepository.deletedCount()

	/**
	 * The number of dismissed alarm statistics.
	 */
	suspend fun dismissedCount(): Long = statisticRepository.dismissedCount()

	/**
	 * The number of dismissed with NFC alarm statistics.
	 */
	suspend fun dismissedWithNfcCount(): Long = statisticRepository.dismissedWithNfcCount()

	/**
	 * Get all created statistics.
	 *
	 * @return All created statistics.
	 */
	suspend fun getAllCreatedStatistics() = statisticRepository.getAllCreatedStatistics()

	/**
	 * Get all deleted statistics.
	 *
	 * @return All deleted statistics.
	 */
	suspend fun getAllDeletedStatistics() = statisticRepository.getAllDeletedStatistics()

	/**
	 * Get all dismissed statistics.
	 *
	 * @return All dismissed statistics.
	 */
	suspend fun getAllDismissedStatistics() = statisticRepository.getAllDismissedStatistics()

	/**
	 * Get all missed statistics.
	 *
	 * @return All missed statistics.
	 */
	suspend fun getAllMissedStatistics() = statisticRepository.getAllMissedStatistics()

	/**
	 * Get all snoozed statistics.
	 *
	 * @return All snoozed statistics.
	 */
	suspend fun getAllSnoozedStatistics() = statisticRepository.getAllSnoozedStatistics()

	/**
	 * The number of missed alarm statistics.
	 */
	suspend fun missedCount(): Long = statisticRepository.missedCount()

	/**
	 * The number of snoozed alarm statistics.
	 */
	suspend fun snoozedCount(): Long = statisticRepository.snoozedCount()

	/**
	 * The total snooze duration.
	 */
	suspend fun snoozedTotalDuration(): Long = statisticRepository.snoozedTotalDuration()

	/**
	 * Delete all rows from the created alarm statistics table.
	 */
	fun deleteAllCreated()
	{
		viewModelScope.launch {
			statisticRepository.deleteAllCreated()
		}
	}

	/**
	 * Delete all rows from the deleted alarm statistics table.
	 */
	fun deleteAllDeleted()
	{
		viewModelScope.launch {
			statisticRepository.deleteAllDeleted()
		}
	}

	/**
	 * Delete all rows from the dismissed alarm statistics table.
	 */
	fun deleteAllDismissed()
	{
		viewModelScope.launch {
			statisticRepository.deleteAllDismissed()
		}
	}

	/**
	 * Delete all rows from the missed alarm statistics table.
	 */
	fun deleteAllMissed()
	{
		viewModelScope.launch {
			statisticRepository.deleteAllMissed()
		}
	}

	/**
	 * Delete all rows from the snoozed alarm statistics table.
	 */
	fun deleteAllSnoozed()
	{
		viewModelScope.launch {
			statisticRepository.deleteAllSnoozed()
		}
	}

	/**
	 * Insert a created alarm statistic, asynchronously, into the database.
	 */
	fun insertCreated()
	{
		viewModelScope.launch {
			statisticRepository.insertCreated()
		}
	}

	/**
	 * Insert a deleted alarm statistic, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm that was deleted.
	 */
	fun insertDeleted(alarm: NacAlarm?)
	{
		viewModelScope.launch {
			statisticRepository.insertDeleted(alarm)
		}
	}

	/**
	 * Insert a dismissed alarm statistic, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm that was dismissed.
	 * @param  usedNfc  Whether NFC was used to dismiss the alarm or not.
	 */
	@Suppress("unused")
	fun insertDismissed(alarm: NacAlarm?, usedNfc: Boolean)
	{
		viewModelScope.launch {
			statisticRepository.insertDismissed(alarm, usedNfc)
		}
	}

	/**
	 * Insert a missed alarm statistic, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm that was missed.
	 */
	@Suppress("unused")
	fun insertMissed(alarm: NacAlarm?)
	{
		viewModelScope.launch {
			statisticRepository.insertMissed(alarm)
		}
	}

	/**
	 * Insert a snoozed alarm statistic, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm that was snoozed.
	 * @param  duration  The duration the alarm was snoozed for.
	 */
	@Suppress("unused")
	fun insertSnoozed(alarm: NacAlarm?, duration: Long)
	{
		viewModelScope.launch {
			statisticRepository.insertSnoozed(alarm, duration)
		}
	}

}