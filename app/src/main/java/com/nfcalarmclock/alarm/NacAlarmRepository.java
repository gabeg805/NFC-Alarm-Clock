package com.nfcalarmclock.alarm;

import android.app.Application;
import android.content.Context;
import androidx.lifecycle.LiveData;

import com.nfcalarmclock.db.NacAlarmDatabase;
import com.nfcalarmclock.db.NacRepository;

import java.util.concurrent.Future;
import java.util.List;

/**
 * Alarm repository.
 */
public class NacAlarmRepository
	extends NacRepository
{

	/**
	 * Data access object for an alarm.
	 */
	private final NacAlarmDao mAlarmDao;

	/**
	 * Live data list of all alarms.
	 */
	private final LiveData<List<NacAlarm>> mAllAlarms;

	/**
	 */
	public NacAlarmRepository(Application app)
	{
		NacAlarmDatabase db = NacAlarmDatabase.getInstance(app);
		NacAlarmDao dao = db.alarmDao();

		this.mAlarmDao = dao;
		this.mAllAlarms = dao.getAllAlarms();
	}

	/**
	 */
	public NacAlarmRepository(Context context)
	{
		this((Application)context.getApplicationContext());
	}

	/**
	 * Copy an alarm, asynchronously, into the database.
	 * <p>
	 * TODO: Be sure to test this when swiping.
	 *
	 * @param  copiedAlarm  Alarm that has been copied.
	 *
	 * @return The row ID of the inserted alarm.
	 */
	public long copy(NacAlarm copiedAlarm)
	{
		if (copiedAlarm == null)
		{
			return -1;
		}

		long id = this.insert(copiedAlarm);
		copiedAlarm.setId(id);

		return id;
	}

	/**
	 * Delete an alarm, asynchronously, from the database.
	 *
	 * @return The number of rows deleted.
	 */
	public int delete(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return -1;
		}

		Future<?> future = this.doDelete(alarm);

		return NacRepository.getIntegerFromFuture(future);
	}

	/**
	 * Delete an alarm, asynchronously, from the database.
	 */
	public Future<?> doDelete(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return null;
		}

		NacAlarmDao dao = this.getAlarmDao();
		return NacAlarmDatabase.getExecutor().submit(() -> dao.delete(alarm));
	}

	/**
	 * Find an alarm with the given ID.
	 */
	public Future<?> doFindAlarm(long id)
	{
		if (id < 0)
		{
			return null;
		}

		NacAlarmDao dao = this.getAlarmDao();
		return NacAlarmDatabase.getExecutor().submit(() -> dao.findAlarm(id));
	}

	/**
	 * @see #doFindAlarm(long)
	 */
	public Future<?> doFindAlarm(NacAlarm alarm)
	{
		long id = (alarm != null) ? alarm.getId() : -1;
		return this.doFindAlarm(id);
	}

	/**
	 * Get all active alarms in the database.
	 *
	 * @return A list of all active alarms.
	 */
	public Future<?> doGetActiveAlarmsNow()
	{
		NacAlarmDao dao = this.getAlarmDao();
		return NacAlarmDatabase.getExecutor().submit(dao::getActiveAlarmsNow);
	}

	/**
	 * Get all alarms in the database.
	 *
	 * @return A list of all alarms.
	 */
	public Future<?> doGetAllAlarmsNow()
	{
		NacAlarmDao dao = this.getAlarmDao();
		return NacAlarmDatabase.getExecutor().submit(dao::getAllAlarmsNow);
	}

	/**
	 * Insert an alarm, asynchronously, into the database.
	 */
	public Future<?> doInsert(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return null;
		}

		NacAlarmDao dao = this.getAlarmDao();
		return NacAlarmDatabase.getExecutor().submit(() -> dao.insert(alarm));
	}

	/**
	 * Update an alarm, asynchronously, in the database.
	 */
	public Future<?> doUpdate(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return null;
		}

		NacAlarmDao dao = this.getAlarmDao();
		return NacAlarmDatabase.getExecutor().submit(() -> dao.update(alarm));
	}

	/**
	 * @return An alarm with the given ID.
	 */
	public NacAlarm findAlarm(long id)
	{
		if (id < 0)
		{
			return null;
		}

		Future<?> future = this.doFindAlarm(id);

		return NacRepository.getAlarmFromFuture(future);
	}

	/**
	 * @see #findAlarm(long)
	 */
	public NacAlarm findAlarm(NacAlarm alarm)
	{
		long id = (alarm != null) ? alarm.getId() : -1;
		return this.findAlarm(id);
	}

	/**
	 * Get an active alarm.
	 *
	 * @return An active alarm.
	 */
	public LiveData<NacAlarm> getActiveAlarm()
	{
		return this.getAlarmDao().getActiveAlarm();
	}

	/**
	 * Get the list of active alarms.
	 *
	 * @return The list of active alarms.
	 */
	public LiveData<List<NacAlarm>> getActiveAlarms()
	{
		return this.getAlarmDao().getActiveAlarms();
	}

	/**
	 * Get the list of active alarms.
	 *
	 * @return The list of active alarms.
	 */
	public List<NacAlarm> getActiveAlarmsNow()
	{
		Future<?> future = this.doGetActiveAlarmsNow();

		return NacRepository.getAlarmListFromFuture(future);
	}

	/**
	 * Get the data access object for the alarm.
	 *
	 * @return The data access object for the alarm.
	 */
	public NacAlarmDao getAlarmDao()
	{
		return this.mAlarmDao;
	}

	/**
	 * Get all alarms in the database.
	 * <p>
	 * This is an asynchronous call. The LiveData object will be populated with
	 * alarms once all have been selected.
	 *
	 * @return A LiveData list of all alarms.
	 */
	public LiveData<List<NacAlarm>> getAllAlarms()
	{
		return this.mAllAlarms;
	}

	/**
	 * Get all alarms in the database.
	 * <p>
	 * This will wait until all alarms are selected.
	 *
	 * @return A list of all alarms.
	 */
	public List<NacAlarm> getAllAlarmsNow()
	{
		Future<?> future = this.doGetAllAlarmsNow();

		return NacRepository.getAlarmListFromFuture(future);
	}

	/**
	 * Insert an alarm, asynchronously, into the database.
	 *
	 * @param  alarm  Alarm to insert.
	 *
	 * @return The row ID of the inserted alarm.
	 */
	public long insert(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return -1;
		}

		Future<?> future = this.doInsert(alarm);

		return NacRepository.getLongFromFuture(future);
	}

	/**
	 * Update an alarm, asynchronously, in the database.
	 *
	 * @param  alarm  Alarm to update.
	 *
	 * @return The number of alarms updated.
	 */
	public int update(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return -1;
		}

		Future<?> future = this.doUpdate(alarm);

		return NacRepository.getIntegerFromFuture(future);
	}

}
