package com.nfcalarmclock;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import java.util.List;

/**
 * Alarm repository.
 */
public class NacAlarmRepository
	implements NacAlarmDatabase.OnDatabaseCreatedListener
{

	/**
	 * Data access object for an alarm.
	 */
	private NacAlarmDao mAlarmDao;

	/**
	 * Live data list of all alarms.
	 */
	private LiveData<List<NacAlarm>> mAllAlarms;

	private Application mApplication;

	/**
	 */
	@Override
	public void onDatabaseCreated(NacAlarmDatabase db)
	{
		Application app = this.mApplication;

		if (NacAlarmDatabase.wasCreated())
		{
			NacUtility.printf("Room database was created for the first time!");

			if (NacDatabase.exists(app))
			{
				NacUtility.printf("Old database file exists! Copying over data");
				this.migrateOldDatabase(app);
			}
			else
			{
				NacUtility.printf("Cannot find old database file! Adding new dummy entry to room");
				this.insertInitialAlarm();
			}
		}
		else
		{
			NacUtility.printf("Room database already exists! Dont need to do anything extra");
		}
	}

	/**
	 */
	public NacAlarmRepository(Application app)
	{
		NacAlarmDatabase db = NacAlarmDatabase.getInstance(app, this);
		NacAlarmDao dao = db.alarmDao();

		this.mApplication = app;
		this.mAlarmDao = dao;
		this.mAllAlarms = dao.getAll();
	}

	/**
	 * Copy an alarm, asynchronously, into the database.
	 *
	 * TODO: Be sure to test this when swiping.
	 */
	public void copy(NacAlarm alarm)
	{
		NacAlarmDao dao = this.getDao();

		alarm.setId(0);
		new InsertAsyncTask(dao).execute(alarm);
	}

	/**
	 * Delete an alarm, asynchronously, from the database.
	 */
	public void delete(NacAlarm alarm)
	{
		NacAlarmDao dao = this.getDao();
		new DeleteAsyncTask(dao).execute(alarm);
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
	public void insert(NacAlarm alarm)
	{
		NacAlarmDao dao = this.getDao();
		new InsertAsyncTask(dao).execute(alarm);
	}

	/**
	 * Insert the initial alarm into the database.
	 */
	protected void insertInitialAlarm()
	{
		NacAlarmDao dao = this.getDao();
		NacAlarm alarm = new NacAlarm.Builder()
			//.setId(1)
			.setIsActive(false)
			.setIsEnabled(true)
			.setHour(8)
			.setMinute(0)
			.setDays(NacCalendar.Days.valueToDays(62))
			.setRepeat(true)
			.setVibrate(true)
			.setUseNfc(false)
			.setNfcTagId("")
			.setMediaType(NacMedia.TYPE_NONE)
			.setMediaPath("")
			.setMediaTitle("")
			.setVolume(75)
			.setAudioSource("Media")
			.setName("Work")
			.build();

		new InsertAsyncTask(dao).execute(alarm);
	}

	/**
	 * Migrate data from the old database into the new database
	 */
	protected void migrateOldDatabase(Context context)
	{
		if (!NacDatabase.exists(context))
		{
			return;
		}

		NacAlarmDao dao = this.getDao();
		List<NacAlarm> alarms = NacDatabase.read(context);

		for (NacAlarm a : alarms)
		{
			NacUtility.printf("Inserting alarm : %d", a.getId());
			a.setId(0);
			new InsertAsyncTask(dao).execute(a);
		}
	}

	/**
	 * Update an alarm, asynchronously, in the database.
	 */
	public void update (NacAlarm alarm)
	{
		NacAlarmDao dao = this.getDao();
		new UpdateAsyncTask(dao).execute(alarm);
	}

	/**
	 * Asynchronously delete an alarm from the database.
	 */
	public static class DeleteAsyncTask
		extends RepositoryAsyncTask
	{

		/**
		 */
		public DeleteAsyncTask(NacAlarmDao dao)
		{
			super(dao);
		}

		/**
		 * Execute the delete in the background.
		 */
		@Override
		protected Void doInBackground(final NacAlarm... params)
		{
			this.getDao().delete(params[0]);
			return null;
		}

	}

	/**
	 * Asynchronously insert an alarm into the database.
	 */
	public static class InsertAsyncTask
		extends RepositoryAsyncTask
	{

		/**
		 */
		public InsertAsyncTask(NacAlarmDao dao)
		{
			super(dao);
		}

		/**
		 * Execute the insert in the background.
		 */
		@Override
		protected Void doInBackground(final NacAlarm... params)
		{
			this.getDao().insert(params[0]);
			return null;
		}

	}

	/**
	 * Execute a task asynchronously on the database.
	 */
	private static abstract class RepositoryAsyncTask
		extends AsyncTask<NacAlarm, Void, Void>
	{

		/**
		 * Asynchronous data access object for an alarm.
		 */
		private NacAlarmDao mAsyncAlarmDao;

		/**
		 */
		public RepositoryAsyncTask(NacAlarmDao dao)
		{
			this.mAsyncAlarmDao = dao;
		}

		/**
		 * @return The asynchronous data access object for an alarm.
		 */
		public NacAlarmDao getDao()
		{
			return this.mAsyncAlarmDao;
		}

	}

	/**
	 * Asynchronously update an alarm in the database.
	 */
	public static class UpdateAsyncTask
		extends RepositoryAsyncTask
	{

		/**
		 */
		public UpdateAsyncTask(NacAlarmDao dao)
		{
			super(dao);
		}

		/**
		 * Execute the insert in the background.
		 */
		@Override
		protected Void doInBackground(final NacAlarm... params)
		{
			this.getDao().update(params[0]);
			return null;
		}

	}

}
