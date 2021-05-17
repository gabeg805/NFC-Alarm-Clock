package com.nfcalarmclock;

import android.app.Application;
import android.content.Context;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

/**
 * Alarm view model.
 */
public class NacAlarmViewModel
	extends AndroidViewModel
{

	/**
	 * Repository of the alarms.
	 */
	private NacAlarmRepository mAlarmRepository;

	/**
	 * Live data list of all alarms.
	 */
	private LiveData<List<NacAlarm>> mAllAlarms;

	/**
	 */
	public NacAlarmViewModel(Application app)
	{
		super(app);

		NacAlarmRepository repo = new NacAlarmRepository(app);

		this.mAlarmRepository = repo;
		this.mAllAlarms = repo.getAllAlarms();
	}

	/**
	 * Copy an alarm into the database.
	 *
	 * @param  copiedAlarm  Alarm that has been copied.
	 *
	 * @return The row ID of the inserted alarm.
	 */
	public long copy(NacAlarm copiedAlarm)
	{
		// TODO: What should I do with the scheduler here? Do nothing?
		return this.getRepository().copy(copiedAlarm);
	}

	/**
	 * Delete an alarm from the database, and cancel its scheduled run time.
	 *
	 * @param  context  Context.
	 * @param  alarm  Alarm to delete.
	 *
	 * @return The number of rows deleted.
	 */
	public int delete(Context context, NacAlarm alarm)
	{
		NacScheduler.cancel(context, alarm);
		return this.getRepository().delete(alarm);
	}

	/**
	 * Find an alarm.
	 *
	 * @param  id  The ID of the alarm to find.
	 *
	 * @return The alarm with the ID.
	 */
	public NacAlarm findAlarm(long id)
	{
		return this.getRepository().findAlarm(id);
	}

	/**
	 * @see #findAlarm(long)
	 */
	public NacAlarm findAlarm(NacAlarm alarm)
	{
		return this.getRepository().findAlarm(alarm);
	}

	/**
	 * Get an active alarm.
	 *
	 * @return An active alarm.
	 */
	public LiveData<NacAlarm> getActiveAlarm()
	{
		return this.getRepository().getActiveAlarm();
	}

	/**
	 * Get a list of all active alarms.
	 *
	 * @return List of all active alarms.
	 */
	public LiveData<List<NacAlarm>> getActiveAlarms()
	{
		return this.getRepository().getActiveAlarms();
	}

	/**
	 * Get all alarms.
	 *
	 * @return All alarms.
	 */
	public LiveData<List<NacAlarm>> getAllAlarms()
	{
		return this.mAllAlarms;
	}

	/**
	 * Get the database repository.
	 *
	 * @return The database repository.
	 */
	public NacAlarmRepository getRepository()
	{
		return this.mAlarmRepository;
	}

	/**
	 * Insert an alarm into the database, and schedule the alarm to run.
	 *
	 * @param  context  Context.
	 * @param  alarm  The alarm to insert.
	 *
	 * @return The row ID of the alarm that was inserted.
	 *
	 */
	public long insert(Context context, NacAlarm alarm)
	{
		if (alarm == null)
		{
			return -1;
		}

		NacScheduler.update(context, alarm);
		return this.getRepository().insert(alarm);
	}

	/**
	 * Update an alarm in the database, and schedule the alarm to run.
	 *
	 * @param  context  Context.
	 * @param  alarm  The alarm to update.
	 *
	 * @return The number of alarms updated.
	 */
	public int update(Context context, NacAlarm alarm)
	{
		if (alarm == null)
		{
			return 0;
		}

		NacScheduler.update(context, alarm);
		return this.getRepository().update(alarm);
	}

}
