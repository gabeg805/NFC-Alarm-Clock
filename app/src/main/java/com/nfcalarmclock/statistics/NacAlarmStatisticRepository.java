package com.nfcalarmclock.statistics;

import android.app.Application;
import android.content.Context;
import com.nfcalarmclock.alarm.db.NacAlarm;
import com.nfcalarmclock.db.NacAlarmDatabase;
import com.nfcalarmclock.db.NacRepository;
import com.nfcalarmclock.statistics.db.NacAlarmCreatedStatistic;
import com.nfcalarmclock.statistics.db.NacAlarmCreatedStatisticDao;
import com.nfcalarmclock.statistics.db.NacAlarmDeletedStatistic;
import com.nfcalarmclock.statistics.db.NacAlarmDeletedStatisticDao;
import com.nfcalarmclock.statistics.db.NacAlarmDismissedStatistic;
import com.nfcalarmclock.statistics.db.NacAlarmDismissedStatisticDao;
import com.nfcalarmclock.statistics.db.NacAlarmMissedStatistic;
import com.nfcalarmclock.statistics.db.NacAlarmMissedStatisticDao;
import com.nfcalarmclock.statistics.db.NacAlarmSnoozedStatistic;
import com.nfcalarmclock.statistics.db.NacAlarmSnoozedStatisticDao;

import java.util.concurrent.Future;
import java.util.Date;

/**
 * Alarm statistic repository.
 */
@SuppressWarnings("UnusedReturnValue")
public class NacAlarmStatisticRepository
	extends NacRepository
{

	/**
	 * Data access object for a created alarm statistic.
	 */
	private final NacAlarmCreatedStatisticDao mAlarmCreatedStatisticDao;

	/**
	 * Data access object for deleted alarm statistic.
	 */
	private final NacAlarmDeletedStatisticDao mAlarmDeletedStatisticDao;

	/**
	 * Data access object for a dismissed alarm statistic.
	 */
	private final NacAlarmDismissedStatisticDao mAlarmDismissedStatisticDao;

	/**
	 * Data access object for a missed alarm statistic.
	 */
	private final NacAlarmMissedStatisticDao mAlarmMissedStatisticDao;

	/**
	 * Data access object for a snoozed alarm statistic.
	 */
	private final NacAlarmSnoozedStatisticDao mAlarmSnoozedStatisticDao;

	/**
	 */
	public NacAlarmStatisticRepository(Application app)
	{
		NacAlarmDatabase db = NacAlarmDatabase.getInstance(app);

		this.mAlarmCreatedStatisticDao = db.alarmCreatedStatisticDao();
		this.mAlarmDeletedStatisticDao = db.alarmDeletedStatisticDao();
		this.mAlarmDismissedStatisticDao = db.alarmDismissedStatisticDao();
		this.mAlarmMissedStatisticDao = db.alarmMissedStatisticDao();
		this.mAlarmSnoozedStatisticDao = db.alarmSnoozedStatisticDao();
	}

	/**
	 */
	public NacAlarmStatisticRepository(Context context)
	{
		this((Application)context.getApplicationContext());
	}

	/**
	 * Delete all rows from the created alarm statistics table.
	 *
	 * @return The number of rows in the created alarm statistics table.
	 */
	public int deleteAllCreated()
	{
		Future<?> future = this.doDeleteAllCreated();

		return NacRepository.getIntegerFromFuture(future);
	}

	/**
	 * Delete all rows from the deleted alarm statistics table.
	 *
	 * @return The number of rows in the deleted alarm statistics table.
	 */
	public int deleteAllDeleted()
	{
		Future<?> future = this.doDeleteAllDeleted();

		return NacRepository.getIntegerFromFuture(future);
	}

	/**
	 * Delete all rows from the dismissed alarm statistics table.
	 *
	 * @return The number of rows in the dismissed alarm statistics table.
	 */
	public int deleteAllDismissed()
	{
		Future<?> future = this.doDeleteAllDismissed();

		return NacRepository.getIntegerFromFuture(future);
	}

	/**
	 * Delete all rows from the missed alarm statistics table.
	 *
	 * @return The number of rows in the missed alarm statistics table.
	 */
	public int deleteAllMissed()
	{
		Future<?> future = this.doDeleteAllMissed();

		return NacRepository.getIntegerFromFuture(future);
	}

	/**
	 * Delete all rows from the snoozed alarm statistics table.
	 *
	 * @return The number of rows in the snoozed alarm statistics table.
	 */
	public int deleteAllSnoozed()
	{
		Future<?> future = this.doDeleteAllSnoozed();

		return NacRepository.getIntegerFromFuture(future);
	}

	/**
	 * Delete all rows from the created alarm statistics table, asynchronously.
	 */
	public Future<?> doDeleteAllCreated()
	{
		NacAlarmCreatedStatisticDao dao = this.getAlarmCreatedStatisticDao();

		return NacAlarmDatabase.getExecutor().submit(dao::deleteAll);
	}

	/**
	 * Delete all rows from the deleted alarm statistics table, asynchronously.
	 */
	public Future<?> doDeleteAllDeleted()
	{
		NacAlarmDeletedStatisticDao dao = this.getAlarmDeletedStatisticDao();

		return NacAlarmDatabase.getExecutor().submit(dao::deleteAll);
	}

	/**
	 * Delete all rows from the dismissed alarm statistics table, asynchronously.
	 */
	public Future<?> doDeleteAllDismissed()
	{
		NacAlarmDismissedStatisticDao dao = this.getAlarmDismissedStatisticDao();

		return NacAlarmDatabase.getExecutor().submit(dao::deleteAll);
	}

	/**
	 * Delete all rows from the missed alarm statistics table, asynchronously.
	 */
	public Future<?> doDeleteAllMissed()
	{
		NacAlarmMissedStatisticDao dao = this.getAlarmMissedStatisticDao();

		return NacAlarmDatabase.getExecutor().submit(dao::deleteAll);
	}

	/**
	 * Delete all rows from the snoozed alarm statistics table, asynchronously.
	 */
	public Future<?> doDeleteAllSnoozed()
	{
		NacAlarmSnoozedStatisticDao dao = this.getAlarmSnoozedStatisticDao();

		return NacAlarmDatabase.getExecutor().submit(dao::deleteAll);
	}

	/**
	 * Get the data access object for a created alarm statistic.
	 *
	 * @return The data access object for a created alarm statistic.
	 */
	public NacAlarmCreatedStatisticDao getAlarmCreatedStatisticDao()
	{
		return this.mAlarmCreatedStatisticDao;
	}

	/**
	 * Get the data access object for a deleted alarm statistic.
	 *
	 * @return The data access object for a deleted alarm statistic.
	 */
	public NacAlarmDeletedStatisticDao getAlarmDeletedStatisticDao()
	{
		return this.mAlarmDeletedStatisticDao;
	}

	/**
	 * Get the data access object for a dismissed alarm statistic.
	 *
	 * @return The data access object for a dismissed alarm statistic.
	 */
	public NacAlarmDismissedStatisticDao getAlarmDismissedStatisticDao()
	{
		return this.mAlarmDismissedStatisticDao;
	}

	/**
	 * Get the data access object for a missed alarm statistic.
	 *
	 * @return The data access object for a missed alarm statistic.
	 */
	public NacAlarmMissedStatisticDao getAlarmMissedStatisticDao()
	{
		return this.mAlarmMissedStatisticDao;
	}

	/**
	 * Get the data access object for a snoozed alarm statistic.
	 *
	 * @return The data access object for a snoozed alarm statistic.
	 */
	public NacAlarmSnoozedStatisticDao getAlarmSnoozedStatisticDao()
	{
		return this.mAlarmSnoozedStatisticDao;
	}

	/**
	 * Get the number of created alarm statistics.
	 *
	 * @return The number of created alarm statistics.
	 */
	public long getCreatedCount()
	{
		NacAlarmCreatedStatisticDao dao = this.getAlarmCreatedStatisticDao();
		Future<?> future = NacAlarmDatabase.getExecutor().submit(dao::getCount);

		return NacRepository.getLongFromFuture(future);
	}

	/**
	 * Get the date when the first alarm was created.
	 *
	 * @return The date when the first alarm was created.
	 */
	public Date getCreatedFirstDate()
	{
		long timestamp = this.getCreatedFirstTimestamp();

		return new Date(timestamp);
	}

	/**
	 * Get the timestamp when the first alarm was created as a long type.
	 *
	 * @return The timestamp when the first alarm was created as a long type.
	 */
	public long getCreatedFirstTimestamp()
	{
		NacAlarmCreatedStatisticDao dao = this.getAlarmCreatedStatisticDao();
		Future<?> future = NacAlarmDatabase.getExecutor().submit(dao::getFirstCreatedTimestamp);

		return NacRepository.getLongFromFuture(future);
	}

	/**
	 * Get the number of deleted alarm statistics.
	 *
	 * @return The number of deleted alarm statistics.
	 */
	public long getDeletedCount()
	{
		NacAlarmDeletedStatisticDao dao = this.getAlarmDeletedStatisticDao();
		Future<?> future = NacAlarmDatabase.getExecutor().submit(dao::getCount);

		return NacRepository.getLongFromFuture(future);
	}

	/**
	 * Get the number of dismissed alarm statistics.
	 *
	 * @return The number of dismissed alarm statistics.
	 */
	public long getDismissedCount()
	{
		NacAlarmDismissedStatisticDao dao = this.getAlarmDismissedStatisticDao();
		Future<?> future = NacAlarmDatabase.getExecutor().submit(dao::getCount);

		return NacRepository.getLongFromFuture(future);
	}

	/**
	 * Get the number of dismissed with NFC alarm statistics.
	 *
	 * @return The number of dismissed with NFC alarm statistics.
	 */
	public long getDismissedWithNfcCount()
	{
		NacAlarmDismissedStatisticDao dao = this.getAlarmDismissedStatisticDao();
		Future<?> future = NacAlarmDatabase.getExecutor().submit(dao::getNfcCount);

		return NacRepository.getLongFromFuture(future);
	}

	/**
	 * Get the number of missed alarm statistics.
	 *
	 * @return The number of missed alarm statistics.
	 */
	public long getMissedCount()
	{
		NacAlarmMissedStatisticDao dao = this.getAlarmMissedStatisticDao();
		Future<?> future = NacAlarmDatabase.getExecutor().submit(dao::getCount);

		return NacRepository.getLongFromFuture(future);
	}

	/**
	 * Get the number of snoozed alarm statistics.
	 *
	 * @return The number of snoozed alarm statistics.
	 */
	public long getSnoozedCount()
	{
		NacAlarmSnoozedStatisticDao dao = this.getAlarmSnoozedStatisticDao();
		Future<?> future = NacAlarmDatabase.getExecutor().submit(dao::getCount);

		return NacRepository.getLongFromFuture(future);
	}

	/**
	 * Get the total snooze duration.
	 *
	 * @return The total snooze duration.
	 */
	public long getSnoozedTotalDuration()
	{
		NacAlarmSnoozedStatisticDao dao = this.getAlarmSnoozedStatisticDao();
		Future<?> future = NacAlarmDatabase.getExecutor().submit(dao::getTotalDuration);

		return NacRepository.getLongFromFuture(future);
	}

	/**
	 * Insert a created alarm statistic, asynchronously, into the database.
	 *
	 * @return The row ID of the inserted statistic.
	 */
	public long insertCreated()
	{
		NacAlarmCreatedStatisticDao dao = this.getAlarmCreatedStatisticDao();
		NacAlarmCreatedStatistic stat = new NacAlarmCreatedStatistic();
		Future<?> future = NacAlarmDatabase.getExecutor().submit(() ->
			dao.insert(stat));

		return NacRepository.getLongFromFuture(future);
	}

	/**
	 * Insert a deleted alarm statistic, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm that was deleted.
	 *
	 * @return The row ID of the inserted statistic.
	 */
	public long insertDeleted(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return -1;
		}

		NacAlarmDeletedStatisticDao dao = this.getAlarmDeletedStatisticDao();
		NacAlarmDeletedStatistic stat = new NacAlarmDeletedStatistic(alarm);
		Future<?> future = NacAlarmDatabase.getExecutor().submit(() ->
			dao.insert(stat));

		return NacRepository.getLongFromFuture(future);
	}

	/**
	 * Insert a dismissed alarm statistic, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm that was dismissed.
	 * @param  usedNfc  Whether NFC was used to dismiss the alarm or not.
	 *
	 * @return The row ID of the inserted statistic.
	 */
	public long insertDismissed(NacAlarm alarm, boolean usedNfc)
	{
		if (alarm == null)
		{
			return -1;
		}

		NacAlarmDismissedStatisticDao dao = this.getAlarmDismissedStatisticDao();
		NacAlarmDismissedStatistic stat = new NacAlarmDismissedStatistic(alarm, usedNfc);
		Future<?> future = NacAlarmDatabase.getExecutor().submit(() ->
			dao.insert(stat));

		return NacRepository.getLongFromFuture(future);
	}

	/**
	 * Insert a missed alarm statistic, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm that was missed.
	 *
	 * @return The row ID of the inserted statistic.
	 */
	public long insertMissed(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return -1;
		}

		NacAlarmMissedStatisticDao dao = this.getAlarmMissedStatisticDao();
		NacAlarmMissedStatistic stat = new NacAlarmMissedStatistic(alarm);
		Future<?> future = NacAlarmDatabase.getExecutor().submit(() ->
			dao.insert(stat));

		return NacRepository.getLongFromFuture(future);
	}

	/**
	 * Insert a snoozed alarm statistic, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm that was snoozed.
	 * @param  duration  The duration the alarm was snoozed for.
	 *
	 * @return The row ID of the inserted statistic.
	 */
	public long insertSnoozed(NacAlarm alarm, long duration)
	{
		if (alarm == null)
		{
			return -1;
		}

		NacAlarmSnoozedStatisticDao dao = this.getAlarmSnoozedStatisticDao();
		NacAlarmSnoozedStatistic stat = new NacAlarmSnoozedStatistic(alarm, duration);
		Future<?> future = NacAlarmDatabase.getExecutor().submit(() ->
			dao.insert(stat));

		return NacRepository.getLongFromFuture(future);
	}

}
