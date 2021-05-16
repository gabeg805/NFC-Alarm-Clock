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
	 * Copy an alarm in the database using the repository.
	 */
	public long copy(NacAlarm alarm)
	{
		// TODO: What should I do with the scheduler here? Do nothing?
		return this.getRepository().copy(alarm);
	}

	/**
	 * Delete an alarm from the database using the repository.
	 */
	public int delete(Context context, NacAlarm alarm)
	{
		NacScheduler.cancel(context, alarm);
		return this.getRepository().delete(alarm);
	}

	/**
	 * @return The alarm with the given ID.
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
	 * @return An active alarm.
	 */
	public LiveData<NacAlarm> getActiveAlarm()
	{
		return this.getRepository().getActiveAlarm();
	}

	/**
	 * @return The active alarms.
	 */
	public LiveData<List<NacAlarm>> getActiveAlarms()
	{
		return this.getRepository().getActiveAlarms();
	}

	/**
	 * @return The list of active alarms.
	 */
	public List<NacAlarm> getActiveAlarmsList()
	{
		return this.getRepository().getActiveAlarmsList();
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
	 * Update an alarm in the database using the repository.
	 */
	public int update(NacAlarm alarm)
	{
		return this.getRepository().update(alarm);
	}

	/**
	 * Update an alarm in the database using the repository.
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
