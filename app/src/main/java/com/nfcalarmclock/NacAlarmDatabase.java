package com.nfcalarmclock;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.List;

/**
 * Store alarms in a Room database.
 */
@Database(entities={NacAlarm.class}, version=1, exportSchema=true)
@TypeConverters({NacAlarmTypeConverters.class})
public abstract class NacAlarmDatabase
	extends RoomDatabase
{

	/**
	 * Store alarms in the datbase.
	 */
	public abstract NacAlarmDao alarmDao();

	/**
	 * Name of the database.
	 */
	public static final String DB_NAME = "NfcAlarmClock.db";

	/**
	 * Lock object for the single instance.
	 */
	private static final Object LOCK = new Object();

	/**
	 * Number of executor threads.
	 */
	//private static final int NUMBER_OF_THREADS = 4;

	/**
	 * Executor service.
	 */
	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
	//private static final ExecutorService EXECUTOR =
	//	Executors.newFixedThreadPool(NUMBER_OF_THREADS);

	/**
	 * Singleton instance of the database.
	 */
	private static NacAlarmDatabase sInstance;

	/**
	 * Application context.
	 *
	 * This will only be used when the Room database is created, in order to
	 * migrate from the old SQLite database to the Room database. Otherwise, this
	 * will be null.
	 */
	private static Context sContext;

	/**
	 * Cancel old alarms.
	 *
	 * @param  context  Application context.
	 */
	public static void cancelOldAlarms(Context context)
	{
		for (int i=1; i < 400; i++)
		{
			NacScheduler.cancel(context, i);
		}
	}

	/**
	 * Delete the old database.
	 */
	protected static void deleteOldDatabase(Context context)
	{
		if (!NacDatabase.exists(context))
		{
			return;
		}

		File file = context.getDatabasePath(NacDatabase.DATABASE_NAME);
		file.delete();
	}

	/**
	 * Check if the Room database exists or not.
	 *
	 * @param  context  Application context.
	 *
	 * @return True if the Room database file exists, and False otherwise.
	 */
	public static boolean exists(Context context)
	{
		File file = context.getDatabasePath(DB_NAME);
		return file.exists();
	}

	/**
	 * @return The application context.
	 */
	private static Context getContext()
	{
		return sContext;
	}

	/**
	 * @return The application context.
	 */
	public static ExecutorService getExecutor()
	{
		return EXECUTOR;
	}

	/**
	 * @return A static instance of the database.
	 */
	public static NacAlarmDatabase getInstance()
	{
		return sInstance;
	}

	/**
	 * Create a static instance of the database.
	 *
	 * @param  context  Application context.
	 *
	 * @return A static instance of the database.
	 */
	public static NacAlarmDatabase getInstance(Context context)
	{
		synchronized (LOCK)
		{
			if (sInstance == null)
			{
				Context appContext = context.getApplicationContext();
				sInstance = Room.databaseBuilder(appContext, NacAlarmDatabase.class,
					DB_NAME)
					//.allowMainThreadQueries()
					.addCallback(sDatabaseCallback)
					.build();

				sContext = appContext;
			}

			return sInstance;
		}
	}

	/**
	 * Insert the initial alarm into the database.
	 */
	protected static void insertInitialAlarm()
	{
		NacAlarmDatabase db = getInstance();
		NacAlarmDao dao = db.alarmDao();
		NacAlarm alarm = new NacAlarm.Builder()
			.setId(0)
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

		getExecutor().execute(() -> dao.insert(alarm));
	}

	/**
	 * Migrate data from the old database into the new database.
	 */
	protected static void migrateOldDatabase(Context context)
	{
		if (!NacDatabase.exists(context))
		{
			return;
		}

		NacAlarmDatabase db = getInstance();
		NacAlarmDao dao = db.alarmDao();
		List<NacAlarm> alarms = NacDatabase.read(context);

		for (NacAlarm a : alarms)
		{
			a.setId(0);
			getExecutor().execute(() -> { dao.insert(a); });
		}
	}

	/**
	 * Callback for populating the database, for testing purposes.
	 */
	private static RoomDatabase.Callback sDatabaseCallback =
		new RoomDatabase.Callback()
	{

		/**
		 */
		@Override
		public void onCreate(@NonNull SupportSQLiteDatabase db)
		{
			super.onCreate(db);

			Context context = getContext();

			if (NacDatabase.exists(context))
			{
				cancelOldAlarms(context);
				migrateOldDatabase(context);
				deleteOldDatabase(context);
			}
			else
			{
				insertInitialAlarm();
			}

			sContext = null;
		}

		/**
		 */
		@Override
		public void onOpen(@NonNull SupportSQLiteDatabase db)
		{
			super.onOpen(db);

			sContext = null;
		}

	};

}
