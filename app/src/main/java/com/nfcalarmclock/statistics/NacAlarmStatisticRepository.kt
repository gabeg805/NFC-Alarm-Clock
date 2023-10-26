package com.nfcalarmclock.statistics

import android.app.Application
import android.content.Context
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.db.NacAlarmDatabase
import com.nfcalarmclock.db.NacRepository
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
import java.util.Date
import java.util.concurrent.Future

/**
 * Alarm statistic repository.
 */
class NacAlarmStatisticRepository(app: Application) : NacRepository()
{

	/**
	 * Data access object for a created alarm statistic.
	 */
	private val alarmCreatedStatisticDao: NacAlarmCreatedStatisticDao

	/**
	 * Data access object for deleted alarm statistic.
	 */
	private val alarmDeletedStatisticDao: NacAlarmDeletedStatisticDao

	/**
	 * Data access object for a dismissed alarm statistic.
	 */
	private val alarmDismissedStatisticDao: NacAlarmDismissedStatisticDao

	/**
	 * Data access object for a missed alarm statistic.
	 */
	private val alarmMissedStatisticDao: NacAlarmMissedStatisticDao

	/**
	 * Data access object for a snoozed alarm statistic.
	 */
	private val alarmSnoozedStatisticDao: NacAlarmSnoozedStatisticDao

	/**
	 * Constructor.
	 */
	init
	{
		val db = NacAlarmDatabase.getInstance(app)

		alarmCreatedStatisticDao = db.alarmCreatedStatisticDao()
		alarmDeletedStatisticDao = db.alarmDeletedStatisticDao()
		alarmDismissedStatisticDao = db.alarmDismissedStatisticDao()
		alarmMissedStatisticDao = db.alarmMissedStatisticDao()
		alarmSnoozedStatisticDao = db.alarmSnoozedStatisticDao()
	}

	/**
	 * Constructor.
	 */
	constructor(context: Context) : this(context.applicationContext as Application)

	/**
	 * The number of created alarm statistics.
	 */
	val createdCount: Long
		get()
		{
			val future: Future<*> = NacAlarmDatabase.executor.submit<Long> { alarmCreatedStatisticDao.count }

			return getLongFromFuture(future)
		}

	/**
	 * The date when the first alarm was created.
	 */
	val createdFirstDate: Date
		get()
		{
			val timestamp = createdFirstTimestamp

			return Date(timestamp)
		}

	/**
	 * The timestamp when the first alarm was created as a long type.
	 */
	val createdFirstTimestamp: Long
		get()
		{
			val future: Future<*> =
				NacAlarmDatabase.executor.submit<Long> { alarmCreatedStatisticDao.firstCreatedTimestamp }

			return getLongFromFuture(future)
		}

	/**
	 * The number of deleted alarm statistics.
	 */
	val deletedCount: Long
		get()
		{
			val future: Future<*> = NacAlarmDatabase.executor.submit<Long> { alarmDeletedStatisticDao.count }

			return getLongFromFuture(future)
		}

	/**
	 * The number of dismissed alarm statistics.
	 */
	val dismissedCount: Long
		get()
		{
			val future: Future<*> = NacAlarmDatabase.executor.submit<Long> { alarmDismissedStatisticDao.count }

			return getLongFromFuture(future)
		}

	/**
	 * The number of dismissed with NFC alarm statistics.
	 */
	val dismissedWithNfcCount: Long
		get()
		{
			val future: Future<*> = NacAlarmDatabase.executor.submit<Long> { alarmDismissedStatisticDao.nfcCount }

			return getLongFromFuture(future)
		}

	/**
	 * The number of missed alarm statistics.
	 */
	val missedCount: Long
		get()
		{
			val future: Future<*> = NacAlarmDatabase.executor.submit<Long> { alarmMissedStatisticDao.count }

			return getLongFromFuture(future)
		}

	/**
	 * The number of snoozed alarm statistics.
	 */
	val snoozedCount: Long
		get()
		{
			val future: Future<*> = NacAlarmDatabase.executor.submit<Long> { alarmSnoozedStatisticDao.count }

			return getLongFromFuture(future)
		}

	/**
	 * The total snooze duration.
	 */
	val snoozedTotalDuration: Long
		get()
		{
			val future: Future<*> =
				NacAlarmDatabase.executor.submit<Long> { alarmSnoozedStatisticDao.totalDuration }

			return getLongFromFuture(future)
		}

	/**
	 * Delete all rows from the created alarm statistics table.
	 *
	 * @return The number of rows in the created alarm statistics table.
	 */
	fun deleteAllCreated(): Int
	{
		val future = this.doDeleteAllCreated()

		return getIntegerFromFuture(future)
	}

	/**
	 * Delete all rows from the deleted alarm statistics table.
	 *
	 * @return The number of rows in the deleted alarm statistics table.
	 */
	fun deleteAllDeleted(): Int
	{
		val future = this.doDeleteAllDeleted()

		return getIntegerFromFuture(future)
	}

	/**
	 * Delete all rows from the dismissed alarm statistics table.
	 *
	 * @return The number of rows in the dismissed alarm statistics table.
	 */
	fun deleteAllDismissed(): Int
	{
		val future = this.doDeleteAllDismissed()

		return getIntegerFromFuture(future)
	}

	/**
	 * Delete all rows from the missed alarm statistics table.
	 *
	 * @return The number of rows in the missed alarm statistics table.
	 */
	fun deleteAllMissed(): Int
	{
		val future = this.doDeleteAllMissed()

		return getIntegerFromFuture(future)
	}

	/**
	 * Delete all rows from the snoozed alarm statistics table.
	 *
	 * @return The number of rows in the snoozed alarm statistics table.
	 */
	fun deleteAllSnoozed(): Int
	{
		val future = this.doDeleteAllSnoozed()

		return getIntegerFromFuture(future)
	}

	/**
	 * Delete all rows from the created alarm statistics table, asynchronously.
	 */
	fun doDeleteAllCreated(): Future<*>
	{
		return NacAlarmDatabase.executor.submit<Int> { alarmCreatedStatisticDao.deleteAll() }
	}

	/**
	 * Delete all rows from the deleted alarm statistics table, asynchronously.
	 */
	fun doDeleteAllDeleted(): Future<*>
	{
		return NacAlarmDatabase.executor.submit<Int> { alarmDeletedStatisticDao.deleteAll() }
	}

	/**
	 * Delete all rows from the dismissed alarm statistics table, asynchronously.
	 */
	fun doDeleteAllDismissed(): Future<*>
	{
		return NacAlarmDatabase.executor.submit<Int> { alarmDismissedStatisticDao.deleteAll() }
	}

	/**
	 * Delete all rows from the missed alarm statistics table, asynchronously.
	 */
	fun doDeleteAllMissed(): Future<*>
	{
		return NacAlarmDatabase.executor.submit<Int> { alarmMissedStatisticDao.deleteAll() }
	}

	/**
	 * Delete all rows from the snoozed alarm statistics table, asynchronously.
	 */
	fun doDeleteAllSnoozed(): Future<*>
	{
		return NacAlarmDatabase.executor.submit<Int> { alarmSnoozedStatisticDao.deleteAll() }
	}

	/**
	 * Insert a created alarm statistic, asynchronously, into the database.
	 *
	 * @return The row ID of the inserted statistic.
	 */
	fun insertCreated(): Long
	{
		val stat = NacAlarmCreatedStatistic()
		val future: Future<*> = NacAlarmDatabase.executor.submit<Long> { alarmCreatedStatisticDao.insert(stat) }

		return getLongFromFuture(future)
	}

	/**
	 * Insert a deleted alarm statistic, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm that was deleted.
	 *
	 * @return The row ID of the inserted statistic.
	 */
	fun insertDeleted(alarm: NacAlarm?): Long
	{
		// Check if alarm is null
		if (alarm == null)
		{
			return -1
		}

		val stat = NacAlarmDeletedStatistic(alarm)
		val future: Future<*> = NacAlarmDatabase.executor.submit<Long> { alarmDeletedStatisticDao.insert(stat) }

		return getLongFromFuture(future)
	}

	/**
	 * Insert a dismissed alarm statistic, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm that was dismissed.
	 * @param  usedNfc  Whether NFC was used to dismiss the alarm or not.
	 *
	 * @return The row ID of the inserted statistic.
	 */
	fun insertDismissed(alarm: NacAlarm?, usedNfc: Boolean): Long
	{
		// Check if alarm is null
		if (alarm == null)
		{
			return -1
		}

		val stat = NacAlarmDismissedStatistic(alarm, usedNfc)
		val future: Future<*> = NacAlarmDatabase.executor.submit<Long> { alarmDismissedStatisticDao.insert(stat) }

		return getLongFromFuture(future)
	}

	/**
	 * Insert a missed alarm statistic, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm that was missed.
	 *
	 * @return The row ID of the inserted statistic.
	 */
	fun insertMissed(alarm: NacAlarm?): Long
	{
		// Check if alarm is null
		if (alarm == null)
		{
			return -1
		}

		val stat = NacAlarmMissedStatistic(alarm)
		val future: Future<*> = NacAlarmDatabase.executor.submit<Long> { alarmMissedStatisticDao.insert(stat) }

		return getLongFromFuture(future)
	}

	/**
	 * Insert a snoozed alarm statistic, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm that was snoozed.
	 * @param  duration  The duration the alarm was snoozed for.
	 *
	 * @return The row ID of the inserted statistic.
	 */
	fun insertSnoozed(alarm: NacAlarm?, duration: Long): Long
	{
		// Check if alarm is null
		if (alarm == null)
		{
			return -1
		}

		val stat = NacAlarmSnoozedStatistic(alarm, duration)
		val future: Future<*> = NacAlarmDatabase.executor.submit<Long> { alarmSnoozedStatisticDao.insert(stat) }

		return getLongFromFuture(future)
	}

}