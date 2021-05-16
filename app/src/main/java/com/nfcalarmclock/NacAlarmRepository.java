package com.nfcalarmclock;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import java.lang.InterruptedException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.ArrayList;
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
	 */
	public NacAlarmRepository(Context context)
	{
		this((Application)context.getApplicationContext());
	}

	/**
	 * Copy an alarm, asynchronously, into the database.
	 *
	 * TODO: Be sure to test this when swiping.
	 */
	public long copy(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return -1;
		}

		Future<?> future = this.doCopy(alarm);

		try
		{
			return (Long) future.get();
		}
		catch (CancellationException | ExecutionException | InterruptedException e)
		{
			NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH COPY exception!");
			NacUtility.printf("String  : %s!", e.toString());
			NacUtility.printf("Message : %s!", e.getMessage());
			e.printStackTrace();
			return -1;
		}
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

		try
		{
			return (Integer) future.get();
		}
		catch (CancellationException | ExecutionException | InterruptedException e)
		{
			NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH DELETE exception!");
			NacUtility.printf("String  : %s!", e.toString());
			NacUtility.printf("Message : %s!", e.getMessage());
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Copy an alarm, asynchronously, into the database.
	 */
	public Future<?> doCopy(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return null;
		}

		NacAlarm copy = alarm.copy();
		return this.doInsert(copy);
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

		NacAlarmDao dao = this.getDao();
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

		NacAlarmDao dao = this.getDao();
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
	 * Insert an alarm, asynchronously, into the database.
	 */
	public Future<?> doInsert(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return null;
		}

		NacAlarmDao dao = this.getDao();
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

		NacAlarmDao dao = this.getDao();
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

		try
		{
			return (NacAlarm) future.get();
		}
		catch (CancellationException | ExecutionException | InterruptedException e)
		{
			NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH FINDALARM exception!");
			NacUtility.printf("String  : %s!", e.toString());
			NacUtility.printf("Message : %s!", e.getMessage());
			e.printStackTrace();
			return null;
		}
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
	 * @return An active alarm.
	 */
	public LiveData<NacAlarm> getActiveAlarm()
	{
		return this.getDao().getActiveAlarm();
	}

	/**
	 * @return The list of active alarms.
	 */
	public LiveData<List<NacAlarm>> getActiveAlarms()
	{
		return this.getDao().getActiveAlarms();
	}

	/**
	 * @return The list of active alarms.
	 */
	public List<NacAlarm> getActiveAlarmsList()
	{
		LiveData<List<NacAlarm>> alarms = this.getActiveAlarms();
		return alarms.getValue();
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

		try
		{
			return (Long) future.get();
		}
		catch (CancellationException | ExecutionException | InterruptedException e)
		{
			NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH INSERT exception!");
			NacUtility.printf("String  : %s!", e.toString());
			NacUtility.printf("Message : %s!", e.getMessage());
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Update an alarm, asynchronously, in the database.
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

		try
		{
			return (Integer) future.get();
		}
		catch (CancellationException | ExecutionException | InterruptedException e)
		{
			NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH UPDATE exception!");
			NacUtility.printf("String  : %s!", e.toString());
			NacUtility.printf("Message : %s!", e.getMessage());
			e.printStackTrace();
			return -1;
		}
	}

}
