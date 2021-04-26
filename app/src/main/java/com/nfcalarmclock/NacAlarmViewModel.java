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
	public void copy(NacAlarm alarm)
	{
		this.getRepository().copy(alarm);
		// TODO: What should I do with the scheduler here? Do nothing?
	}

	/**
	 * Delete an alarm from the database using the repository.
	 */
	public void delete(Context context, NacAlarm alarm)
	{
		NacSharedPreferences shared = new NacSharedPreferences(context);
		int id = alarm.getId();

		this.getRepository().delete(alarm);
		shared.editSnoozeCount(id, 0);
		NacScheduler.cancel(context, alarm);
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
	public void insert(Context context, NacAlarm alarm)
	{
		this.getRepository().insert(alarm);
		NacScheduler.update(context, alarm);
	}

	/**
	 * Update an alarm in the database using the repository.
	 */
	public void update(Context context, NacAlarm alarm)
	{
		this.getRepository().update(alarm);
		NacScheduler.update(context, alarm);
	}

}
