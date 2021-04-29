package com.nfcalarmclock;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import java.util.concurrent.Future;
import java.util.List;

/**
 * Alarm repository.
 */
public class NacAlarmRepository
{

	/**
	 * Data access object for an alarm.
	 */
	private NacAlarmDao mAlarmDao;

	/**
	 * Live data list of all alarms.
	 */
	private LiveData<List<NacAlarm>> mAllAlarms;

	/**
	 */
	public NacAlarmRepository(Application app)
	{
		NacAlarmDatabase db = NacAlarmDatabase.getInstance(app);
		NacAlarmDao dao = db.alarmDao();

		this.mAlarmDao = dao;
		this.mAllAlarms = dao.getAll();
	}

	/**
	 * Copy an alarm, asynchronously, into the database.
	 *
	 * TODO: Be sure to test this when swiping.
	 */
	public Future<?> copy(NacAlarm alarm)
	{
		alarm.setId(0);
		return this.insert(alarm);
	}

	/**
	 * Delete an alarm, asynchronously, from the database.
	 */
	public Future<?> delete(NacAlarm alarm)
	{
		NacAlarmDao dao = this.getDao();
		return NacAlarmDatabase.getExecutor().submit(() -> { return dao.delete(alarm); });
	}

	/**
	 * @return The live data list of all alarms.
	 */
	public LiveData<List<NacAlarm>> getAllAlarms()
	{
		return this.mAllAlarms;
	}

	/**
	 * @return The data access object for the alarm.
	 */
	public NacAlarmDao getDao()
	{
		return this.mAlarmDao;
	}

	/**
	 * Insert an alarm, asynchronously, into the database.
	 */
	public Future<?> insert(NacAlarm alarm)
	{
		NacAlarmDao dao = this.getDao();
		return NacAlarmDatabase.getExecutor().submit(() -> { return dao.insert(alarm); });
	}

	/**
	 * Update an alarm, asynchronously, in the database.
	 */
	public Future<?> update(NacAlarm alarm)
	{
		NacAlarmDao dao = this.getDao();
		return NacAlarmDatabase.getExecutor().submit(() -> { return dao.update(alarm); });
	}

}
