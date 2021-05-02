package com.nfcalarmclock;

import android.app.Application;
import android.content.Context;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import java.util.concurrent.Future;
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
	 * Copy an alarm in the database using the repository.
	 */
	public Future<?> copy(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return null;
		}

		return this.getRepository().copy(alarm);
		// TODO: What should I do with the scheduler here? Do nothing?
	}

	/**
	 * Delete an alarm from the database using the repository.
	 */
	public Future<?> delete(Context context, NacAlarm alarm)
	{
		if (alarm == null)
		{
			return null;
		}

		NacScheduler.cancel(context, alarm);
		return this.getRepository().delete(alarm);
	}

	/**
	 * @return The live data list of all alarms.
	 */
	public LiveData<List<NacAlarm>> getAllAlarms()
	{
		return this.mAllAlarms;
	}

	/**
	 * @return Repository of the alarms.
	 */
	public NacAlarmRepository getRepository()
	{
		return this.mAlarmRepository;
	}

	/**
	 * Insert an alarm into the database using the repository.
	 */
	public Future<?> insert(Context context, NacAlarm alarm)
	{
		if (alarm == null)
		{
			return null;
		}

		NacScheduler.update(context, alarm);
		return this.getRepository().insert(alarm);
	}

	/**
	 * Update an alarm in the database using the repository.
	 */
	public Future<?> update(Context context, NacAlarm alarm)
	{
		if (alarm == null)
		{
			return null;
		}

		NacScheduler.update(context, alarm);
		return this.getRepository().update(alarm);
	}

}
