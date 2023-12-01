package com.nfcalarmclock.statistics

import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.statistics.db.NacAlarmCreatedStatistic
import com.nfcalarmclock.statistics.db.NacAlarmCreatedStatisticDao
import com.nfcalarmclock.statistics.db.NacAlarmDeletedStatistic
import com.nfcalarmclock.statistics.db.NacAlarmDeletedStatisticDao
import com.nfcalarmclock.statistics.db.NacAlarmDismissedStatistic
import com.nfcalarmclock.statistics.db.NacAlarmDismissedStatisticDao
import com.nfcalarmclock.statistics.db.NacAlarmMissedStatistic
import com.nfcalarmclock.statistics.db.NacAlarmMissedStatisticDao
import com.nfcalarmclock.statistics.db.NacAlarmSnoozedStatistic
import com.nfcalarmclock.statistics.db.NacAlarmSnoozedStatisticDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

/**
 * Alarm statistic repository.
 */
class NacAlarmStatisticRepository @Inject constructor(

	/**
	 * Data access object for a created alarm statistic.
	 */
	private val alarmCreatedStatisticDao: NacAlarmCreatedStatisticDao,

	/**
	 * Data access object for deleted alarm statistic.
	 */
	private val alarmDeletedStatisticDao: NacAlarmDeletedStatisticDao,

	/**
	 * Data access object for a dismissed alarm statistic.
	 */
	private val alarmDismissedStatisticDao: NacAlarmDismissedStatisticDao,

	/**
	 * Data access object for a missed alarm statistic.
	 */
	private val alarmMissedStatisticDao: NacAlarmMissedStatisticDao,

	/**
	 * Data access object for a snoozed alarm statistic.
	 */
	private val alarmSnoozedStatisticDao: NacAlarmSnoozedStatisticDao

)
{

	/**
	 * The number of created alarm statistics.
	 */
	suspend fun createdCount(): Long = alarmCreatedStatisticDao.count()

	/**
	 * The timestamp when the first alarm was created as a long type.
	 */
	suspend fun createdFirstTimestamp(): Long = alarmCreatedStatisticDao.firstCreatedTimestamp()

	/**
	 * The number of deleted alarm statistics.
	 */
	suspend fun deletedCount(): Long = alarmDeletedStatisticDao.count()

	/**
	 * The number of dismissed alarm statistics.
	 */
	suspend fun dismissedCount(): Long = alarmDismissedStatisticDao.count()

	/**
	 * The number of dismissed with NFC alarm statistics.
	 */
	suspend fun dismissedWithNfcCount(): Long = alarmDismissedStatisticDao.nfcCount()

	/**
	 * The number of missed alarm statistics.
	 */
	suspend fun missedCount(): Long = alarmMissedStatisticDao.count()

	/**
	 * The number of snoozed alarm statistics.
	 */
	suspend fun snoozedCount(): Long = alarmSnoozedStatisticDao.count()

	/**
	 * The total snooze duration.
	 */
	suspend fun snoozedTotalDuration(): Long = alarmSnoozedStatisticDao.totalDuration()

	/**
	 * Delete all rows from the created alarm statistics table.
	 *
	 * @return The number of rows in the created alarm statistics table.
	 */
	suspend fun deleteAllCreated(): Int = alarmCreatedStatisticDao.deleteAll()

	/**
	 * Delete all rows from the deleted alarm statistics table.
	 *
	 * @return The number of rows in the deleted alarm statistics table.
	 */
	suspend fun deleteAllDeleted(): Int = alarmDeletedStatisticDao.deleteAll()

	/**
	 * Delete all rows from the dismissed alarm statistics table.
	 *
	 * @return The number of rows in the dismissed alarm statistics table.
	 */
	suspend fun deleteAllDismissed(): Int = alarmDismissedStatisticDao.deleteAll()

	/**
	 * Delete all rows from the missed alarm statistics table.
	 *
	 * @return The number of rows in the missed alarm statistics table.
	 */
	suspend fun deleteAllMissed(): Int = alarmMissedStatisticDao.deleteAll()

	/**
	 * Delete all rows from the snoozed alarm statistics table.
	 *
	 * @return The number of rows in the snoozed alarm statistics table.
	 */
	suspend fun deleteAllSnoozed(): Int = alarmSnoozedStatisticDao.deleteAll()

	/**
	 * Get all created statistics.
	 *
	 * @return All created statistics.
	 */
	suspend fun getAllCreatedStatistics() = alarmCreatedStatisticDao.getAll()

	/**
	 * Get all deleted statistics.
	 *
	 * @return All deleted statistics.
	 */
	suspend fun getAllDeletedStatistics() = alarmDeletedStatisticDao.getAll()

	/**
	 * Get all dismissed statistics.
	 *
	 * @return All dismissed statistics.
	 */
	suspend fun getAllDismissedStatistics() = alarmDismissedStatisticDao.getAll()

	/**
	 * Get all missed statistics.
	 *
	 * @return All missed statistics.
	 */
	suspend fun getAllMissedStatistics() = alarmMissedStatisticDao.getAll()

	/**
	 * Get all snoozed statistics.
	 *
	 * @return All snoozed statistics.
	 */
	suspend fun getAllSnoozedStatistics() = alarmSnoozedStatisticDao.getAll()

	/**
	 * Insert a created alarm statistic, asynchronously, into the database.
	 *
	 * @return The row ID of the inserted statistic.
	 */
	suspend fun insertCreated(): Long
	{
		// Create the statistic
		val stat = NacAlarmCreatedStatistic()

		// Insert the statistic
		return alarmCreatedStatisticDao.insert(stat)
	}

	/**
	 * Insert a deleted alarm statistic, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm that was deleted.
	 *
	 * @return The row ID of the inserted statistic.
	 */
	suspend fun insertDeleted(alarm: NacAlarm?): Long
	{
		// Check if alarm is null
		if (alarm == null)
		{
			return -1
		}

		// Create the statistic
		val stat = NacAlarmDeletedStatistic(alarm)

		// Insert the statistic
		return alarmDeletedStatisticDao.insert(stat)
	}

	/**
	 * Insert a dismissed alarm statistic, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm that was dismissed.
	 * @param  usedNfc  Whether NFC was used to dismiss the alarm or not.
	 *
	 * @return The row ID of the inserted statistic.
	 */
	suspend fun insertDismissed(alarm: NacAlarm?, usedNfc: Boolean): Long
	{
		// Check if alarm is null
		if (alarm == null)
		{
			return -1
		}

		// Create the statistic
		val stat = NacAlarmDismissedStatistic(alarm, usedNfc)

		// Insert the statistic
		return alarmDismissedStatisticDao.insert(stat)
	}

	/**
	 * Insert a missed alarm statistic, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm that was missed.
	 *
	 * @return The row ID of the inserted statistic.
	 */
	suspend fun insertMissed(alarm: NacAlarm?): Long
	{
		// Check if alarm is null
		if (alarm == null)
		{
			return -1
		}

		// Create the statistic
		val stat = NacAlarmMissedStatistic(alarm)

		// Insert the statistic
		return alarmMissedStatisticDao.insert(stat)
	}

	/**
	 * Insert a snoozed alarm statistic, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm that was snoozed.
	 * @param  duration  The duration the alarm was snoozed for.
	 *
	 * @return The row ID of the inserted statistic.
	 */
	suspend fun insertSnoozed(alarm: NacAlarm?, duration: Long): Long
	{
		// Check if alarm is null
		if (alarm == null)
		{
			return -1
		}

		// Create the statistic
		val stat = NacAlarmSnoozedStatistic(alarm, duration)

		// Insert the statistic
		return alarmSnoozedStatisticDao.insert(stat)
	}

}

/**
 * Hilt module to provide an instance of the repository.
 */
@InstallIn(SingletonComponent::class)
@Module
class NacAlarmStatisticRepositoryModule
{

	/**
	 * Provide an instance of the repository.
	 */
	@Provides
	fun provideStatisticRepository(
		alarmCreatedStatisticDao: NacAlarmCreatedStatisticDao,
		alarmDeletedStatisticDao: NacAlarmDeletedStatisticDao,
		alarmDismissedStatisticDao: NacAlarmDismissedStatisticDao,
		alarmMissedStatisticDao: NacAlarmMissedStatisticDao,
		alarmSnoozedStatisticDao: NacAlarmSnoozedStatisticDao
	) : NacAlarmStatisticRepository
	{
		return NacAlarmStatisticRepository(
			alarmCreatedStatisticDao,
			alarmDeletedStatisticDao,
			alarmDismissedStatisticDao,
			alarmMissedStatisticDao,
			alarmSnoozedStatisticDao)
	}

}
